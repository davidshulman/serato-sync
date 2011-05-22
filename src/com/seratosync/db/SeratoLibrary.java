package com.seratosync.db;

import com.seratosync.filesystem.MediaLibrary;
import com.seratosync.log.Log;
import com.seratosync.db.files.SeratoCrateFile;

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Roman Alekseenkov
 */
@SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
public class SeratoLibrary {

    private int cratesIntact = 0;
    private int cratesCreated = 0;
    private int cratesModified = 0;

    public static String getBaseCrate(String relativeToCrate) {
        String result = relativeToCrate.replaceAll("^/", "");
        result = result.replaceAll("/$", "");
        result = result.replaceAll("//", "");
        result = result.replaceAll("/", "%%");
        return result;
    }

    public static SeratoLibrary writeToCrates(MediaLibrary fsLibrary, String seratoBasePath, String relativeToCrate) {
        // create serato library
        SeratoLibrary result = new SeratoLibrary();

        // populate serato library it with the tracks from real library
        //
        // note: behavior of serato is slightly different on windows and mac os platforms
        //       when includeSubcrateTracks is set to false, it allows serato on both platforms
        //       to control crates behavior using "include subcrate tracks" option from the "library" menu
        //       without forcing one way or another
        result.processLibrary(fsLibrary, seratoBasePath, getBaseCrate(relativeToCrate), false);

        return result;
    }

    private SortedSet<String> processLibrary(MediaLibrary fsLibrary, String seratoBasePath, String crateName, boolean includeSubcrateTracks) {
        // create the list of all tracks in this library
        SortedSet<String> all = new TreeSet<String>();

        // add tracks from the current directory
        all.addAll(fsLibrary.getTracks());

        // build everything for every sub-directory
        for (MediaLibrary child : fsLibrary.getChildren()) {
            String crateNameNext = crateName.length() > 0 ? crateName + "%%" + child.getDirectory() : child.getDirectory();
            SortedSet<String> children = processLibrary(child, seratoBasePath, crateNameNext, includeSubcrateTracks);

            // include subcrate tracks, but only if the option is specified
            if (includeSubcrateTracks) {
                all.addAll(children);
            }
        }

        if (crateName.isEmpty()) {
            // this is a root crate that corresponds to everything
            // we don't really need to update it, as serato automatically updates its database V2 file
        } else if (!all.isEmpty()) {
            // update subcrates
            {
                File crateFile = new File(seratoBasePath + "/Subcrates/" + crateName + ".crate");
                updateCrateFile(crateFile, all);
            }

            // update crates
            {
                File crateFile = new File(seratoBasePath + "/Crates/" + crateName + ".crate");
                updateCrateFile(crateFile, all);
            }
        }

        return all;
    }

    private void updateCrateFile(File crateFile, SortedSet<String> all) {
        SeratoCrateFile crate = new SeratoCrateFile();
        boolean loaded = false;
        if (crateFile.exists() && crateFile.isFile()) {
            try {
                crate.loadFrom(crateFile);
                loaded = true;
            } catch (SeratoLibraryException e) {
                Log.error("Can't load crate " + crateFile.getName() + ". " + e.getMessage());
                crate = new SeratoCrateFile();
            }
        }

        crateFile.getParentFile().mkdirs();
        boolean modified = crate.getEntries().addTracks(all);
        if (modified) {
            try {
                crate.saveTo(crateFile);
                if (loaded) {
                    cratesModified++;
                } else {
                    cratesCreated++;
                }
            } catch (SeratoLibraryException e) {
                Log.error("Can't write crate " + crateFile.getName() + ". " + e.getMessage());
            }
        } else {
            cratesIntact++;
        }
    }

    public int getCratesCreated() {
        return cratesCreated;
    }

    public int getCratesModified() {
        return cratesModified;
    }

    public int getCratesIntact() {
        return cratesIntact;
    }

}
