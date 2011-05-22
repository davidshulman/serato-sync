package com.seratosync.config;

/**
 * @author Roman Alekseenkov
 */
public class RuleFileLoadingException extends Exception {

    public RuleFileLoadingException(String message) {
        super(message);
    }

    public RuleFileLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuleFileLoadingException(Throwable cause) {
        super(cause);
    }

}
