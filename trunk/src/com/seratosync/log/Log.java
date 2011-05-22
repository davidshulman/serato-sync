package com.seratosync.log;

/**
 * @author Roman Alekseenkov
 */
public class Log {

    private static boolean GUI_MODE = true;
    private static WindowHandler WINDOW_HANDLER;

    public static void debug(String message) {
        // TODO: disable for production
        // System.out.println(message);
        // System.out.flush();
    }

    public static void info(String message) {
        if (GUI_MODE) {
            WINDOW_HANDLER.publish(message);
        } else {
            System.out.println(message);
            System.out.flush();
        }
    }

    public static void error(String message) {
        if (GUI_MODE) {
            WINDOW_HANDLER.publish(message);
        } else {
            System.err.println(message);
            System.err.flush();
        }
    }

    public static void processRuleFileEvent(String fileName, int ruleFileState) {
        if (GUI_MODE) {
            WINDOW_HANDLER.processRuleFileEvent(fileName, ruleFileState);
        }
    }

    public static void fatal(String message) {
        error(message);
        fatalError();
    }

    private static void fatalError() {
        if (GUI_MODE) {
            WINDOW_HANDLER.fatalError();
        }
        System.exit(-1);
    }

    public static void success() {
        initGui();
        if (GUI_MODE) {
            WINDOW_HANDLER.success();
        }
        System.exit(0);
    }

    public static void initGui() {
        try {
            WINDOW_HANDLER = WindowHandler.getInstance();
        } catch (Exception e) {
            // fallback to command-line mode
            GUI_MODE = false;
        }
    }

}
