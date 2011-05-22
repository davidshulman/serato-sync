package com.seratosync.io;

import com.seratosync.db.SeratoLibraryException;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SeratoOutputStream extends DataOutputStream {

    public SeratoOutputStream(OutputStream out) {
        super(new BufferedOutputStream(out));
    }

    public void writeUTF16(String value) throws SeratoLibraryException {
        try {
            writeChars(value);
        } catch (IOException e) {
            throw new SeratoLibraryException(e);
        }
    }

    public void writeLong(long value, int bytes) throws SeratoLibraryException {
        byte[] data = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            data[bytes - 1 - i] = (byte) (value & 0xFF);
            value >>>= 8;
        }
        for (byte v : data) {
            try {
                write(v);
            } catch (IOException e) {
                throw new SeratoLibraryException(e);
            }
        }
    }

    public void writeLineUTF16(String value) throws SeratoLibraryException {
        writeUTF16(value);
        try {
            write(0);
            write(10);
        } catch (IOException e) {
            throw new SeratoLibraryException(e);
        }
    }

}
