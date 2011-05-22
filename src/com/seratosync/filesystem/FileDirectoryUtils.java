package com.seratosync.filesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Roman Alekseenkov
 */
public class FileDirectoryUtils {

    @SuppressWarnings({"ResultOfMethodCallIgnored", "UnusedDeclaration"})
    public static void deleteAllFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            File[] all = directory.listFiles();
            for (File file : all)
                if (file.isFile()) {
                    file.delete();
                }
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "UnusedDeclaration"})
    public static void deleteFile(String filePath) {
        new File(filePath).delete();
    }

    public static void zipDirectory(String directoryPath, File zipFileName, List<String> includeWildcardPatterns) throws IOException {
        List<Pattern> includePatterns = convertPatternsWildcardToRegex(includeWildcardPatterns);

        File directory = new File(directoryPath);
        ZipOutputStream out;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipFileName));
        } catch (FileNotFoundException e) {
            throw new IOException(e);
        }
        try {
            addDirectoryToZipOutputStream(directory, directory, out, includePatterns);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    private static void addDirectoryToZipOutputStream(File baseDirectory, File directory, ZipOutputStream out, List<Pattern> includePatterns) throws IOException {
        File[] files = directory.listFiles();
        byte[] buf = new byte[1 << 16];
        for (File file : files) {

            // determine the relative path
            String relativePath = baseDirectory.toURI().relativize(file.toURI()).getPath();

            // does it match one of the paths?
            boolean add = false;
            for (Pattern p : includePatterns)
                if (p.matcher(relativePath).matches()) {
                    add = true;
                    break;
                }

            if (!add) {
                continue;
            }

            // handle directories
            if (file.isDirectory()) {
                addDirectoryToZipOutputStream(baseDirectory, file, out, includePatterns);
                continue;
            }

            // handle files, read it using FileInputStream and wrap into ZipEntry
            FileInputStream in = new FileInputStream(file.getAbsolutePath());
            out.putNextEntry(new ZipEntry(relativePath));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
        }

    }

    /**
     * No matter whether it's windows or mac os, in the serato file there should be always:
     * - forward slashes, no backslashes
     * - no leading slashes at all
     * - no drive name on Windows (C:\, etc)
     * - no drive name on Mac OS (/Volumes/VolumeName/, etc)
     *
     * @param name Track name with absolute path
     * @return Track name with all slashes replaced with forward slashes
     */
    public static String convertFromFileToSeratoFile(String name) {
        // make all forward slashes on windows
        name = name.replaceAll("\\\\", "/");

        // remove drive on windows
        name = name.replaceAll("^[a-zA-Z]\\:/", "");

        // remove drive on mac os
        name = name.replaceAll("^/[vV][oO][lL][uU][mM][eE][sS]/[^/]+/", "");

        // remove all leading slashes
        name = name.replaceAll("^/", "");

        return name;
    }

    /**
     * Converts wildcard patterns to regex patterns
     * @param wildcardPatterns The list of patterns, possibly containing '*' wildcards
     * @return The list of regex patterns
     */
    public static List<Pattern> convertPatternsWildcardToRegex(List<String> wildcardPatterns) {
        List<Pattern> result = new ArrayList<Pattern>();
        for (String wcPattern : wildcardPatterns) {

            // delete leading forward slash if it exists
            if (wcPattern.startsWith("/")) {
                wcPattern = wcPattern.substring(1);
            }

            // start with an optional leading forward slash
            StringBuilder pattern = new StringBuilder();
            pattern.append(Pattern.quote("/"));
            pattern.append("?");

            // quote between stars
            StringBuilder buf = new StringBuilder();
            for (char c : wcPattern.toCharArray())
                    if (c == '*') {
                        if (buf.length() > 0) {
                            pattern.append(Pattern.quote(buf.toString()));
                            buf.setLength(0);
                        }
                        pattern.append("(.*)");
                    } else {
                        buf.append(c);
                    }
            if (buf.length() > 0) {
                pattern.append(Pattern.quote(buf.toString()));
                buf.setLength(0);
            }

            // compile the pattern
            result.add(Pattern.compile("^" + pattern + "$"));
        }
        return result;
    }

}
