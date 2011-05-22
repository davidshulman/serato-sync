package com.seratosync.actions;

import com.seratosync.config.ActionExecutionException;
import com.seratosync.filesystem.FileDirectoryUtils;
import com.seratosync.log.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class BackupSeratoDatabaseAction extends AbstractAction {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    private String getBackupFolder() {
        String value = getParameter("folder");
        if (value == null || value.isEmpty()) {
            value = "Backup";
        }
        return value;
    }

    private List<String> getBackupIncludeFilter() {
        String value = getParameter("include");
        if (value == null || value.isEmpty()) {
            value = "/Crates/*, /Subcrates/*, /database V2, /*.pref";
        }
        List<String> result = new ArrayList<String>();
        StringTokenizer items = new StringTokenizer(value, ",");
        while (items.hasMoreTokens()) {
            result.add(items.nextToken().trim());
        }
        return result;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void run() throws ActionExecutionException {
        Log.info("* running serato db backup");

        // create directory structure to store the backup file in
        File backupZipFile = new File(getRuleFile().getSeratoBasePath() + "/" + getBackupFolder() + "/" + "backup_" + getDateAsString() + ".zip");
        backupZipFile.getParentFile().mkdirs();

        // create backup
        try {
            FileDirectoryUtils.zipDirectory(getRuleFile().getSeratoBasePath(), backupZipFile, getBackupIncludeFilter());
        } catch (IOException e) {
            throw new ActionExecutionException("Backup creation failed", e);
        }

        Log.info("  * successfully created " + backupZipFile.getName());
    }

    public String getDateAsString() {
        return DATE_FORMAT.format(new Date());
    }

}
