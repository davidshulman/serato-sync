package com.seratosync;

import com.seratosync.db.SeratoLibraryException;
import com.seratosync.filesystem.FileDirectoryUtils;
import junit.framework.TestCase;

/**
 * @author Roman Alekseenkov
 */
public class UniformFileNameTests extends TestCase {

    private void test(String expected, String fileName) {
        String actual = FileDirectoryUtils.convertFromFileToSeratoFile(fileName);
        assertEquals(expected, actual);
    }

    public void testMacNormalization() throws SeratoLibraryException {
        test(
                "Users/johndoe/Music/Very good album/track.mp3",
                "/Users/johndoe/Music/Very good album/track.mp3"
        );
        test(
                "Music/Very good album/track.mp3",
                "/Volumes/Johndoe External Drive/Music/Very good album/track.mp3"
        );
        test(
                "Music/Very good album/track.mp3",
                "/voLUmes/Johndoe External Drive/Music/Very good album/track.mp3"
        );
    }

    public void testWinNormalization() throws SeratoLibraryException {
        test(
                "Music/Very good album/track.mp3",
                "C:\\Music\\Very good album\\track.mp3"
        );
        test(
                "Trance/very good.mp3",
                "D:\\Trance\\very good.mp3"
        );
        test(
                "root.mp3",
                "D:\\root.mp3"
        );
    }

}
