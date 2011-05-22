package com.seratosync.io;

/**
 * @author Roman Alekseenkov
 */
public class SeratoEofException extends Exception {

    public SeratoEofException() {
        this("End of file");
    }

    private SeratoEofException(String s) {
        super(s);
    }

}
