package com.seratosync.filesystem;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class DriveResolutionTestsMac extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        List<Drive> drives = new ArrayList<Drive>();
        String[] prefixes = new String[] {"/", "/Volumes/External Hard Drive/", "/Volumes/Yet Another Hard Drive/"};
        for (String prefix : prefixes) {
            drives.add(new Drive(prefix));
        }
        Drive.setDrives(drives);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Drive.unsetDrives();
    }

    public void testDriveChecksInternal() {
        assertTrue(Drive.getDrive("/Users/ralekseenkov").equals(new Drive("/")));
        assertTrue(Drive.onSameDrive("/Users/ralekseenkov", "/Users/serato/"));
        assertTrue(Drive.onSameDrive("/Users/ralekseenkov/Music/Techno/", "/Users/ralekseenkov/_Serato_"));
    }

    public void testDriveChecksExternal() {
        assertTrue(Drive.getDrive("/Volumes/External Hard Drive/Music/Trance").equals(new Drive("/Volumes/External Hard Drive")));
        assertTrue(Drive.onSameDrive("/Volumes/External Hard Drive", "/Volumes/External Hard Drive/Music/"));
        assertTrue(Drive.onSameDrive("/Volumes/External Hard Drive/Music/Trance/", "/Volumes/External Hard Drive/Music/Techno"));
    }

    public void testNegativeDriveChecks() {
        assertFalse(Drive.onSameDrive("/Users/ralekseenkov/Music/", "/Volumes/External Hard Drive/Music/"));
    }

}
