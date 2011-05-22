package com.seratosync.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Roman Alekseenkov
 */
public class Drive {

    // list of drives in the system
    private static List<Drive> drives;

    // drive identifier which ends with a '/'
    private String prefix;

    public Drive(String prefix) {
        this.prefix = withTrailingSlash(prefix);
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Drive drive = (Drive) o;
        return !(prefix != null ? !prefix.equals(drive.prefix) : drive.prefix != null);
    }

    @Override
    public int hashCode() {
        return prefix != null ? prefix.hashCode() : 0;
    }

    private boolean isMoreSpecificThan(Drive that) {
        return this.getPrefix().length() > that.getPrefix().length();
    }

    private static String withTrailingSlash(String prefix) {
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    public static synchronized List<Drive> getAllDrives() {
        if (drives == null) {
            Set<Drive> result = new HashSet<Drive>();

            // first of all, add all root reported by the system
            {
                File[] roots = File.listRoots();
                for (File dir : roots) {
                    try {
                        result.add(new Drive(dir.getCanonicalPath()));
                    } catch (IOException e) {
                        // skip the drive
                    }
                }
            }


            // second, add mounted points on Mac OS
            {
                File[] mounted = new File("/Volumes").listFiles();
                for (File dir : mounted) {
                    try {
                        result.add(new Drive(dir.getCanonicalPath()));
                    } catch (IOException e) {
                        // skip the drive
                    }
                }
            }

            drives = new ArrayList<Drive>(result);
        }
        return drives;
    }

    static synchronized void setDrives(List<Drive> drives) {
        Drive.drives = drives;
    }

    static synchronized void unsetDrives() {
        Drive.drives = null;
    }

    public static Drive getDrive(String name) {
        String absolutePath = withTrailingSlash(name);

        Drive result = null;
        for (Drive d : getAllDrives())
            if (absolutePath.startsWith(d.getPrefix()) && (result == null || d.isMoreSpecificThan(result))) {
                result = d;
            }

        if (result == null) {
            throw new IllegalStateException("Failed to retrieve drive for path: " + name);
        }

        return result;
    }

    public static boolean onSameDrive(String path1, String path2) {
        return getDrive(path1).equals(getDrive(path2));
    }

    public static String getDetectedDrivesDescription() {
        StringBuilder buf = new StringBuilder();
        for (Drive d : getAllDrives()) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("'");
            buf.append(d);
            buf.append("'");
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        return getPrefix();
    }

}
