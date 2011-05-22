package com.seratosync.db.files.entries;

import com.seratosync.db.SeratoLibraryException;
import com.seratosync.io.SeratoEofException;
import com.seratosync.io.SeratoInputStream;
import com.seratosync.io.SeratoOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SeratoFileEntry {

    private String name;
    private byte[] value;

    public SeratoFileEntry(String name, byte[] value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public byte[] getValue() {
        return value;
    }

    public boolean isTrack() {
        // crate files and database V2 file contain 'otrk' records for tracks, in slightly different formats though
        return "otrk".equals(getName());
    }

    public String getTrackName() throws SeratoLibraryException {
        SeratoInputStream in = new SeratoInputStream(new ByteArrayInputStream(value));
        for (; ;) {
            try {
                String name = in.readStringUTF8(4);
                int length = in.readIntegerValue();
                byte[] data = in.readBytes(length);

                // ptrk - for crates, pfil - for database V2
                if ("ptrk".equals(name) || "pfil".equals(name)) {
                    try {
                        return new String(data, "UTF-16");
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException(e);
                    }
                }
            } catch (SeratoEofException e) {
                throw new SeratoLibraryException("Can't extract file name from serato track record because 'ptrk' or 'phil' sections not found");
            }
        }
    }

    public static SeratoFileEntry readFrom(SeratoInputStream in) throws SeratoLibraryException, SeratoEofException {
        String name = in.readStringUTF8(4);
        try {
            int length = in.readIntegerValue();
            byte[] data = in.readBytes(length);
            return new SeratoFileEntry(name, data);
        } catch (SeratoEofException e) {
            throw new SeratoLibraryException(e);
        }
    }

    public void writeTo(SeratoOutputStream out) throws SeratoLibraryException {
        try {
            out.writeBytes(getName());
            out.writeInt(getValue().length);
            out.write(getValue());
        } catch (IOException e) {
            throw new SeratoLibraryException(e);
        }
    }

}
