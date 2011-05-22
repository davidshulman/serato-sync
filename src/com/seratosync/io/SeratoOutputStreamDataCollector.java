package com.seratosync.io;

import com.seratosync.db.SeratoLibraryException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class SeratoOutputStreamDataCollector {

    private ByteArrayOutputStream bytes;
    private SeratoOutputStream out;

    protected SeratoOutputStreamDataCollector() throws SeratoLibraryException {
        this.bytes = new ByteArrayOutputStream();
        this.out = new SeratoOutputStream(bytes);
        write();
    }

    public abstract void write() throws SeratoLibraryException;

    public final SeratoOutputStream getStream() {
        return out;
    }

    public final byte[] collect() {
        try {
            out.close();
        } catch (IOException e) {
            // do nothing
        }
        return bytes.toByteArray();
    }

}
