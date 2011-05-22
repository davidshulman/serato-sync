package com.seratosync.db;

/**
 * @author Roman Alekseenkov
 */
public class SeratoLibraryException extends Exception {

    public SeratoLibraryException(String message) {
        super(message);
    }

    public SeratoLibraryException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeratoLibraryException(Throwable cause) {
        super(cause);
    }

}
