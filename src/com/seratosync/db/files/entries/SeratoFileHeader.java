package com.seratosync.db.files.entries;

import com.seratosync.db.SeratoLibraryException;
import com.seratosync.io.SeratoEofException;
import com.seratosync.io.SeratoInputStream;
import com.seratosync.io.SeratoOutputStream;

import java.io.IOException;

public class SeratoFileHeader {

    private String version;
    private String type;

    /**
    /**
     * Creates an empty serato crate header
     * @param version internal version string in serato binary file
     * @param type internal version string in serato binary file
     */
    public SeratoFileHeader(String version, String type) {
        this.version = version;
        this.type = type;
    }

    /**
     * Reads itself from the input stream
     *
     * @param in Input stream
     * @throws com.seratosync.db.SeratoLibraryException
     *          In case of I/O exception
     */
    public void readFrom(SeratoInputStream in) throws SeratoLibraryException {

        try {
            // version
            in.skipExactString("vrsn");
            in.skipByte(); // should be 0 byte, but we are not enforcing any strict checks here
            in.skipByte(); // should be 0 byte, but we are not enforcing any strict checks here
            in.skipExactStringUTF16(version);
            in.skipExactStringUTF16(type);
        } catch (SeratoEofException e) {
            throw new SeratoLibraryException(e);
        }

    }

    /**
     * Write the header to output stream
     *
     * @param out Serato output stream
     * @throws com.seratosync.db.SeratoLibraryException
     *          In case if I/O exception
     */
    public void writeTo(SeratoOutputStream out) throws SeratoLibraryException {
        try {
            // version
            out.writeBytes("vrsn");
            out.write((byte) 0);
            out.write((byte) 0);
            out.writeUTF16(version);
            out.writeUTF16(type);
        } catch (IOException e) {
            throw new SeratoLibraryException("Can't write crate header to file", e);
        }

    }


}
