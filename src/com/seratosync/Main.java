package com.seratosync;

import com.seratosync.config.RuleFile;
import com.seratosync.config.RuleFileLoadingException;
import com.seratosync.filesystem.Drive;
import com.seratosync.log.Log;

import java.io.File;

/**
 * @author Roman Alekseenkov
 */
public class Main {

    public static final String VERSION = "0.2";

    public static void main(String[] args) {
        Log.initGui();

        // process all rule files
        File[] ruleFiles = RuleFile.listRuleFiles();
        int ruleFilesCount = ruleFiles.length;
        if (ruleFilesCount <= 0) {
            Log.fatal("No rule files found");
        }

        for (File ruleFile : ruleFiles) {
            Log.processRuleFileEvent(ruleFile.getName(), RuleFile.STATE_WAITING);
        }
        Log.info("Found " + ruleFilesCount + " rule file(s). Drive roots detected: " + Drive.getDetectedDrivesDescription() + "\n");

        int current = 0;
        int successfullyProcessed = 0;
        for (File file : ruleFiles) {
            // print progress information
            Log.info("Loading rule file '" + file.getName() + "' (" + (++current) + " out of " + ruleFilesCount + ")");
            Log.processRuleFileEvent(file.getName(), RuleFile.STATE_IN_PROGRESS);

            // load rule file
            RuleFile ruleFile;
            try {
                ruleFile = new RuleFile(file);
            } catch (RuleFileLoadingException e) {
                Log.error("Failed to process rule file. " + e.getMessage());
                Log.error("Skipping this rule file.\n");
                Log.processRuleFileEvent(file.getName(), RuleFile.STATE_FAILED);
                continue;
            }

            // validate rule file
            try {
                ruleFile.validate();
            } catch (RuleFileLoadingException e) {
                Log.error("Failed to process rule file. " + e.getMessage());
                Log.error("Skipping this rule file.\n");
                Log.processRuleFileEvent(file.getName(), RuleFile.STATE_FAILED);
                continue;
            }

            // execute rule file
            try {
                ruleFile.execute();
            } catch (RuleFileLoadingException e) {
                Log.error("Failed to process rule file. " + e.getMessage());
                Log.error("Skipping this rule file.\n");
                Log.processRuleFileEvent(file.getName(), RuleFile.STATE_FAILED);
                continue;
            }

            // successfully processed
            successfullyProcessed++;
            Log.info("Done with rule file '" + file.getName() + "'\n");
            Log.processRuleFileEvent(file.getName(), RuleFile.STATE_COMPLETED);
        }

        Log.info("Successfully processed " + successfullyProcessed + " rule fule(s) out of " + ruleFiles.length + " total");

        // TODO:
        // Log.success();
    }

}
