package com.seratosync.config;

import com.seratosync.actions.AbstractAction;
import com.seratosync.actions.BackupSeratoDatabaseAction;
import com.seratosync.actions.PruneSeratoDatabaseAction;
import com.seratosync.actions.SyncAction;
import com.seratosync.filesystem.Drive;
import com.seratosync.log.Log;

import java.io.*;
import java.util.*;

/**
 * @author Roman Alekseenkov
 */
public class RuleFile {

    public static final int STATE_WAITING = 0;
    public static final int STATE_IN_PROGRESS = 1;
    public static final int STATE_COMPLETED = 2;
    public static final int STATE_FAILED = 3;

    private Map<String, String> globalSettings = new HashMap<String, String>();
    private LinkedList<AbstractAction> actions = new LinkedList<AbstractAction>();

    /**
     * Creates a rule file by loading it from the file
     *
     * @param file Input file
     * @throws RuleFileLoadingException In case of I/O exception or file format exception
     */
    public RuleFile(File file) throws RuleFileLoadingException {
        loadFromFile(file);
    }

    public String getDriveBasePath() {
        return globalSettings.get("drive-base-path");
    }

    public String getSeratoBasePath() {
        return globalSettings.get("serato-base-path");
    }

    private String setSeratoBasePath(String value) {
        return globalSettings.put("serato-base-path", value);
    }

    /**
     * Executes rule file
     *
     * @throws RuleFileLoadingException In case of rule execution error
     */
    public void execute() throws RuleFileLoadingException {
        for (AbstractAction action : actions) {
            try {
                action.run();
            } catch (ActionExecutionException e) {
                throw new RuleFileLoadingException(e);
            }
        }
    }

    /**
     * Validates rule file and completes evaluation of auto-detect parameters
     *
     * @throws RuleFileLoadingException In case of rule validation error
     */
    public void validate() throws RuleFileLoadingException {

        {
            // check that drive base path is not empty
            if (getDriveBasePath() == null) {
                throw new RuleFileLoadingException("Drive base path is not specified: " + getDriveBasePath());
            }

            // recognise user home directory
            if (getSeratoBasePath().contains("~")) {
                setSeratoBasePath(getSeratoBasePath().replaceAll("~", System.getProperty("user.home")));
            }

            // check that drive base path exists, and it's a directory
            File driveBase = new File(getDriveBasePath());
            if (!driveBase.exists()) {
                throw new RuleFileLoadingException("Drive base path doesn't exist: " + getDriveBasePath());
            }
            if (!driveBase.isDirectory()) {
                throw new RuleFileLoadingException("Drive base path is not a valid directory: " + getDriveBasePath());
            }
        }

        {
            // check that serato base path is valid
            boolean detected = false;
            if ("autodetect".equals(getSeratoBasePath())) {
                autodetectSeratoBasePath();
                detected = true;
            }
            checkSeratoBasePath();
            Log.info("Using serato base path (" + (detected ? "auto-detected" : "specified") + "): " + getSeratoBasePath());
        }

    }

    /**
     * Checks serato base path for validity, and make sure it indeed points to the serato library
     *
     * @throws RuleFileLoadingException If serato base path is not valid
     */
    private void checkSeratoBasePath() throws RuleFileLoadingException {
        if (!isSeratoDirectory(getSeratoBasePath())) {
            throw new RuleFileLoadingException("Invalid serato base path: " + getSeratoBasePath());
        }
    }

    /**
     * Auto detects and sets serato base path. It's all dependent on the drive:
     * - for internal drive, serato db is stored in the user's home directory
     * - for external drive, serato db is stored in the root of the drive
     *
     * @throws RuleFileLoadingException When serato path can not be auto-detected
     */
    private void autodetectSeratoBasePath() throws RuleFileLoadingException {
        // first of all, we look in the drive root
        {
            String candidate = Drive.getDrive(getDriveBasePath()).getPrefix();
            if (autodetectEvaluateCandidate(candidate)) return;
        }

        // second, we look in the current user home directory, but only if it's located on the same drive
        {
            String candidate = System.getProperty("user.home");
            if (Drive.onSameDrive(candidate, getDriveBasePath()) && autodetectEvaluateCandidate(candidate)) return;
        }

        // third, we look in another user home directory potentially, if it's located
        {
            File candidate = new File(getDriveBasePath());
            while (candidate != null) {
                if (autodetectEvaluateCandidate(candidate.getAbsolutePath())) return;
                candidate = candidate.getParentFile();
            }
        }

        throw new RuleFileLoadingException("Can't autodetect serato base path");
    }

    private boolean autodetectEvaluateCandidate(String drivePath) {
        String[] suffixes = new String[]{"/_Serato_", "/Music/_Serato_", "_ScratchLIVE_", "/Music/_ScratchLIVE_"};
        for (String suffix : suffixes) {
            String candidatePath;
            try {
                candidatePath = new File(drivePath + suffix).getCanonicalPath();
            } catch (IOException e) {
                // just skip
                continue;
            }
            if (isSeratoDirectory(candidatePath)) {
                setSeratoBasePath(candidatePath);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether it's a serato directory or not
     *
     * @param path candidate path to check
     * @return true of false
     */
    private boolean isSeratoDirectory(String path) {
        File basePath = new File(path);
        File dbPath = new File(path + "/" + "database V2");
        return basePath.exists() && basePath.isDirectory() && dbPath.exists() && dbPath.isFile();
    }

    /**
     * Loads a rule file, transforming it into the list of properties and actions
     *
     * @param file Input file
     * @throws RuleFileLoadingException In case of I/O exception or file format exception
     */
    private void loadFromFile(File file) throws RuleFileLoadingException {
        String fileName = file.getName();

        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new RuleFileLoadingException("Can't read rule file " + fileName, e);
        }

        try {
            int level = 0;

            String line;
            while ((line = in.readLine()) != null) {

                // remove leading and trailing whitespaces
                line = line.trim();

                // skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // we are expecting either global setting here, or an action
                if (level == 0) {

                    // parse name/value pair
                    if (line.startsWith("run ")) {

                        // cut out the beginning
                        line = line.substring("run ".length());

                        // action spans across multiple lines?
                        if (line.endsWith("{")) {
                            // cut out the action name from the middle
                            line = line.substring(0, line.length() - 1);

                            // remove leading and trailing whitespaces
                            line = line.trim();

                            // create an action and add it to the list
                            actions.add(instantiateAction(line));

                            level = 1;
                        } else {
                            // single-lined action, remove leading and trailing whitespaces
                            line = line.trim();

                            // create an action and add it to the list
                            actions.add(instantiateAction(line));
                        }

                    } else if (line.contains(":")) {
                        // global settings
                        int index = line.indexOf(":");

                        String name = line.substring(0, index).trim();
                        String value = line.substring(index + 1).trim();

                        globalSettings.put(name, value);
                    } else {
                        throw new RuleFileLoadingException("Unsupported line in the rule file " + fileName + ": " + line);
                    }

                } else {

                    if (line.contains(":")) {
                        // parameters for the multi-line action
                        int index = line.indexOf(":");

                        String name = line.substring(0, index).trim();
                        String value = line.substring(index + 1).trim();

                        actions.getLast().setParameter(name, value);
                    } else if ("}".equals(line)) {
                        level = 0;
                    } else {
                        throw new RuleFileLoadingException("Unsupported line in the rule file " + fileName + ": " + line);
                    }

                }
            }

            if (level != 0) {
                throw new RuleFileLoadingException("Malformed rule file  " + fileName + ": some of the tags are not closed properly");
            }
        } catch (IOException e) {
            throw new RuleFileLoadingException("Can't read a line of rule file " + fileName, e);
        }

        try {
            in.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Creates an action based on the input string, which was read from the rule file
     *
     * @param action Action as string
     * @return Runnable action
     * @throws RuleFileLoadingException When action is not supported
     */
    private AbstractAction instantiateAction(String action) throws RuleFileLoadingException {
        AbstractAction result;
        if ("sync".equals(action)) {
            result = new SyncAction();
        } else if ("backup-serato-db".equals(action)) {
            result = new BackupSeratoDatabaseAction();
        } else if ("prune-serato-db".equals(action)) {
            result = new PruneSeratoDatabaseAction();
        } else {
            throw new RuleFileLoadingException("Unsupported action: " + action);
        }

        result.setRuleFile(this);
        return result;
    }

    /**
     * Searches for the files in current directory
     *
     * @return list of rule files
     */
    public static File[] listRuleFiles() {
        File[] result = new File(".").listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && (file.getName().endsWith(".rules") || file.getName().endsWith(".rules.txt"));
            }
        });

        if (result == null) {
            result = new File[]{};
        }

        Arrays.sort(result, new Comparator<File>() {
            public int compare(File a, File b) {
                return a.getName().compareTo(b.getName());
            }
        });

        return result;
    }

}
