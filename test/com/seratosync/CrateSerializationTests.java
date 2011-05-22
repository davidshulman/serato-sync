package com.seratosync;

import com.seratosync.db.SeratoLibraryException;
import com.seratosync.db.files.SeratoCrateFile;
import com.seratosync.io.SeratoOutputStream;
import junit.framework.TestCase;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Roman Alekseenkov
 */
public class CrateSerializationTests extends TestCase {

    public static String md5(File inFile) {
        try {
            return md5(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String md5(InputStream in) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        try {
            in = new DigestInputStream(in, md);
            byte[] buf = new byte[1 << 16];
            while (in.read(buf) >= 0) {
                // do nothing
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // do nothing
            }
        }
        return new String(md.digest());
    }

    private static void testRW(String fileName) throws SeratoLibraryException {
        File file = new File(fileName);
        SeratoCrateFile crate = new SeratoCrateFile();
        crate.loadFrom(file);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        SeratoOutputStream out = new SeratoOutputStream(buf);
        crate.writeTo(out);
        try {
            out.close();
        } catch (IOException e) {
            // close the stream
        }

        String md5Original = md5(file);
        String md5Reassembled = md5(new ByteArrayInputStream(buf.toByteArray()));
        if (!md5Original.equals(md5Reassembled)) {
            throw new SeratoLibraryException("md5 mismatch");
        }
    }

    public void testMacCrates() throws SeratoLibraryException {
        // Crates from Mac OS
        testRW("test/resources/mac/small.crate");
        testRW("test/resources/mac/large.crate");

        testRW("test/resources/mac/main%%sub1.crate");
        testRW("test/resources/mac/main%%sub2.crate");
        testRW("test/resources/mac/main%%sub1%%subsub1.crate");
    }

    public void testWinCrates() throws SeratoLibraryException {
        // Crates from Windows XP
        testRW("test/resources/win/disk-c.crate");
        testRW("test/resources/win/disk-z.crate");
    }

}
