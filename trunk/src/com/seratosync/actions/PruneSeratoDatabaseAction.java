package com.seratosync.actions;

import com.seratosync.config.ActionExecutionException;
import com.seratosync.db.SeratoLibraryException;
import com.seratosync.filesystem.Drive;
import com.seratosync.log.Log;
import com.seratosync.db.SeratoLibrary;
import com.seratosync.db.files.SeratoCrateFile;
import com.seratosync.db.files.SeratoDatabaseV2File;
import com.seratosync.db.files.SeratoWindowOrderFile;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

public class PruneSeratoDatabaseAction extends AbstractAction {

    private int deletedCrates = 0;
    private int modifiedCrates = 0;
    private int intactCrates = 0;
    private int removedFiles = 0;
    private boolean reorderedCrates = false;

    public static class CrateFileNameAdvancedComparator implements Comparator<String> {
        private final Comparator<String> ASCENDING_COMPARATOR = new Comparator<String>() {
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        };
        private final Comparator<String> DESCENDING_COMPARATOR = new Comparator<String>() {
            public int compare(String a, String b) {
                return b.compareTo(a);
            }
        };

        private Comparator<String> defaultComparator = ASCENDING_COMPARATOR;
        private Map<Integer, Comparator<String>> comparators = new HashMap<Integer, Comparator<String>>();

        public CrateFileNameAdvancedComparator(String input) {
            StringTokenizer items = new StringTokenizer(input, ",");
            while (items.hasMoreTokens()) {
                StringTokenizer t = new StringTokenizer(items.nextToken().trim());

                if (t.hasMoreTokens()) {
                    String where = t.nextToken();
                    if ("default".equals(where)) {
                        if (t.hasMoreTokens()) {
                            defaultComparator = getComparatorByMode(t.nextToken());
                        }
                    } else if ("level".equals(where)) {
                        if (t.hasMoreTokens()) {
                            try {
                                int level = Integer.parseInt(t.nextToken());
                                if (t.hasMoreTokens()) {
                                    comparators.put(level, getComparatorByMode(t.nextToken()));
                                }
                            } catch (NumberFormatException e) {
                                // do nothing
                            }
                        }
                    }
                }
            }
        }

        private Comparator<String> getComparatorByMode(String mode) {
            return "ascending".equals(mode) ? ASCENDING_COMPARATOR : DESCENDING_COMPARATOR;
        }

        public Comparator<String> getComparator(int level) {
            Comparator<String> result = comparators.get(level);
            if (result == null) {
                result = defaultComparator;
            }
            return result;
        }

        public int compare(String a, String b) {
            String[] aa = a.split("%%");
            String[] bb = b.split("%%");

            int common = Math.min(aa.length, bb.length);
            for (int k = 0; k < common; k++) {
                int result = getComparator(k).compare(aa[k], bb[k]);
                if (result != 0) {
                    return result;
                }
            }
            return aa.length - bb.length;
        }

    }

    private boolean removeMissingTracks() {
        return !"false".equals(getParameter("remove-missing-tracks"));
    }

    private boolean removeEmptyCrates() {
        return !"false".equals(getParameter("remove-empty-crates"));
    }

    private CrateFileNameAdvancedComparator getCrateSortRules() {
        String value = getParameter("sort-crates");
        if (value == null || value.isEmpty()) {
            value = "default ascending";
        }
        return !"false".equals(value) ? new CrateFileNameAdvancedComparator(value) : null;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void run() throws ActionExecutionException {
        Log.info("* running: serato db pruning");

        // understand what is the drive that corresponds to serato base path
        Drive drive = Drive.getDrive(getRuleFile().getSeratoBasePath());

        // retrieve the list of crates files from the filesystem
        List<File> crateFiles = findCrateFiles();

        // cleanup each and every crate (delete missing files, delete empty crates)
        cleanupCrates(drive, crateFiles);

        // cleanup database V2 file
        cleanupDbV2(drive);

        // modify crate order file
        modifyCrateOrder(crateFiles);

        Log.info("  * crate files left intact " + intactCrates + ", modified " + modifiedCrates + ", deleted " + deletedCrates);
        Log.info("  * removed missing file entries " + removedFiles);
        if (reorderedCrates) {
            Log.info("  * reordered crates");
        }
    }

    private void modifyCrateOrder(List<File> crateFiles) throws ActionExecutionException {
        CrateFileNameAdvancedComparator crateFileNameAdvancedComparator = getCrateSortRules();

        // well, should we be reordering crates? if not - just return
        if (crateFileNameAdvancedComparator == null) {
            return;
        }

        // reorder crates
        File crateOrderFile = new File(getRuleFile().getSeratoBasePath() + "/neworder.pref");
        SeratoWindowOrderFile crateOrder = new SeratoWindowOrderFile();
        if (crateOrderFile.exists()) {
            try {
                crateOrder.loadFrom(crateOrderFile);
            } catch (SeratoLibraryException e) {
                throw new ActionExecutionException("Failed to load crate order file", e);
            }
        }

        for (File crateFile : crateFiles) {
            String crateName = SeratoLibrary.getBaseCrate(crateFile.getName()).replaceAll("\\.crate$", "");
            crateOrder.add(crateName);
        }

        crateOrder.reorder(crateFileNameAdvancedComparator);
        reorderedCrates = true;

        try {
            crateOrder.saveTo(crateOrderFile);
        } catch (SeratoLibraryException e) {
            throw new ActionExecutionException("Failed to write modified crate order file", e);
        }
    }

    private void cleanupDbV2(Drive drive) throws ActionExecutionException {

        // well, should we be removing empty tracks? if not - just return
        if (!removeMissingTracks()) {
            return;
        }

        // remove missing tracks from 'database V2' file
        File dbV2File;
        SeratoDatabaseV2File dbV2;
        try {
            dbV2File = new File(getRuleFile().getSeratoBasePath() + "/database V2");
            dbV2 = new SeratoDatabaseV2File();
            dbV2.loadFrom(dbV2File);
        } catch (SeratoLibraryException e) {
            throw new ActionExecutionException("Failed to read serato database V2 file", e);
        }

        int removed = dbV2.getEntries().removeMissingTracks(drive);
        if (removed > 0) {
            try {
                dbV2.saveTo(dbV2File);
                modifiedCrates++;
                removedFiles += removed;
            } catch (SeratoLibraryException e) {
                throw new ActionExecutionException("Failed to write modified database V2 file", e);
            }
        } else {
            intactCrates++;
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private void cleanupCrates(Drive drive, List<File> crateFiles) throws ActionExecutionException {
        for (File crateFile : crateFiles) {
            SeratoCrateFile crate = new SeratoCrateFile();
            try {
                crate.loadFrom(crateFile);
            } catch (SeratoLibraryException e) {
                throw new ActionExecutionException("Failed to read crate '" + crateFile.getName() + "'", e);
            }


            // should we remove missing tracks? if so - let's go ahead and remove them
            int removed = 0;
            if (removeMissingTracks()) {
                removed = crate.getEntries().removeMissingTracks(drive);
            }

            // delete crates only if we need to do so
            if (removeEmptyCrates() && !crate.getEntries().hasTracks()) {
                crateFile.delete();
                deletedCrates++;
            } else if (removed > 0) {
                try {
                    crate.saveTo(crateFile);
                    modifiedCrates++;
                    removedFiles += removed;
                } catch (SeratoLibraryException e) {
                    throw new ActionExecutionException("Failed to write modified crate '" + crateFile.getName() + "'", e);
                }
            } else {
                intactCrates++;
            }
        }
    }

    private List<File> findCrateFiles() {
        List<File> crateFiles = new ArrayList<File>();

        {
            String cratesPath = getRuleFile().getSeratoBasePath() + "/Subcrates";
            File[] foundCrates = new File(cratesPath).listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.exists() && file.isFile() && file.getName().toLowerCase().endsWith(".crate");
                }
            });
            if (foundCrates != null) {
                crateFiles.addAll(Arrays.asList(foundCrates));
            }
        }

        {
            String cratesPath = getRuleFile().getSeratoBasePath() + "/Crates";
            File[] foundCrates = new File(cratesPath).listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.exists() && file.isFile() && file.getName().toLowerCase().endsWith(".crate");
                }
            });
            if (foundCrates != null) {
                crateFiles.addAll(Arrays.asList(foundCrates));
            }
        }
        return crateFiles;
    }

}
