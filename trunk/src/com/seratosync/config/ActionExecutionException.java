package com.seratosync.config;

/**
 * @author Roman Alekseenkov
 */
public class ActionExecutionException extends Exception {

    public ActionExecutionException(String message) {
        super(message);
    }

    public ActionExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionExecutionException(Throwable cause) {
        super(cause);
    }

}
