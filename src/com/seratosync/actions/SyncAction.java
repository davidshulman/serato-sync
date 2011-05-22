package com.seratosync.actions;

import com.seratosync.config.ActionExecutionException;
import com.seratosync.filesystem.FileDirectoryUtils;
import com.seratosync.filesystem.MediaLibrary;
import com.seratosync.log.Log;
import com.seratosync.db.SeratoLibrary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class SyncAction extends AbstractAction {

    /**
     * Scans media library on a filesystem and returns its contents (all music and video files)
     *
     *
     * @param path path where to find the content
     * @param excludeFilter Exclude patterns possibly containing wildcards
     * @return MediaLibrary
     */
    private MediaLibrary loadMediaLibrary(String path, List<String> excludeFilter) {
        List<Pattern> excludePatterns = FileDirectoryUtils.convertPatternsWildcardToRegex(excludeFilter);

        Log.info("  * scanning " + path + "...");
        MediaLibrary fsLibrary = MediaLibrary.readFrom(path, excludePatterns);
        if (fsLibrary.getTotalNumberOfTracks() <= 0) {
            Log.info("  * unable to find any matching media files");
        } else {
            Log.info("  * found " + fsLibrary.getTotalNumberOfTracks() + " matching media files in " + fsLibrary.getTotalNumberOfDirectories() + " directories");
        }
        return fsLibrary;
    }

    /**
     * Writes all information about music and video files to serato library
     *
     * @param mediaLibrary    Media library
     * @param relativeToCrate Crate as string
     */
    private void saveMediaLibraryToSerato(MediaLibrary mediaLibrary, String relativeToCrate) {
        SeratoLibrary seratoLibrary = SeratoLibrary.writeToCrates(mediaLibrary, getRuleFile().getSeratoBasePath(), relativeToCrate);
        Log.info("  * crate files left intact " + seratoLibrary.getCratesIntact() + ", modified " + seratoLibrary.getCratesModified() + ", created " + seratoLibrary.getCratesCreated());
    }

    private String getSyncPath() throws ActionExecutionException {
        String folder = getParameter("folder");
        if (folder == null || folder.isEmpty()) {
            folder = "/";
        }

        String syncPath = getRuleFile().getDriveBasePath() + folder;
        try {
            return new File(syncPath).getCanonicalPath();
        } catch (IOException e) {
            throw new ActionExecutionException("Invalid sync path: " + syncPath, e);
        }
    }

    private String getSyncCrate() {
        String result = getParameter("crate");
        if (result == null || result.isEmpty()) {
            result = "/";
        }
        return result;
    }

    private List<String> getExcludeFilter() {
        String value = getParameter("exclude");
        if (value == null) {
            value = "";
        }
        List<String> result = new ArrayList<String>();
        StringTokenizer items = new StringTokenizer(value, ",");
        while (items.hasMoreTokens()) {
            result.add(items.nextToken().trim());
        }
        return result;
    }


    public void run() throws ActionExecutionException {
        Log.info("* running sync");

        // scan media library using the specified mediaPath
        String mediaPath = getSyncPath();
        MediaLibrary mediaLibrary = loadMediaLibrary(mediaPath, getExcludeFilter());

        // sync media, starting from the specified crate
        String crate = getSyncCrate();
        saveMediaLibraryToSerato(mediaLibrary, crate);
    }

}
