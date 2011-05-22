package com.seratosync.io;

import com.seratosync.db.SeratoLibraryException;

import java.io.*;
import java.util.Arrays;

public class SeratoInputStream extends DataInputStream {

    public SeratoInputStream(InputStream in) {
        super(in);
    }

    /**
     * A wrapper around read() data input stream function
     *
     * @param data byte array
     * @return the number of bytes read
     * @throws com.seratosync.db.SeratoLibraryException
     *                            In case if I/O exception
     * @throws SeratoEofException When end of file is reached
     */
    private int readBytesInternal(byte[] data) throws SeratoLibraryException, SeratoEofException {
        try {
            int read = read(data);
            if (read < 0) {
                throw new SeratoEofException();
            }
            return read;
        } catch (EOFException e) {
            throw new SeratoEofException();
        } catch (IOException e) {
            throw new SeratoLibraryException(e);
        }
    }

    /**
     * A wrapper around readUnsignedByte() data input stream function
     *
     * @return the read byte
     * @throws com.seratosync.db.SeratoLibraryException
     *          In case if I/O exception
     * @throws SeratoEofException In case of EOF
     */
    private int readUnsignedByteInternal() throws SeratoLibraryException, SeratoEofException {
        try {
            return readUnsignedByte();
        } catch (EOFException e) {
            throw new SeratoEofException();
        } catch (IOException e) {
            throw new SeratoLibraryException(e);
        }
    }

    /**
     * A wrapper around readByte() data input stream function
     *
     * @return the read byte
     * @throws com.seratosync.db.SeratoLibraryException
     *          In case if I/O exception
     * @throws SeratoEofException In case of EOF
     */
    private byte readByteInternal() throws SeratoLibraryException, SeratoEofException {
        try {
            return readByte();
        } catch (EOFException e) {
            throw new SeratoEofException();
        } catch (IOException e) {
            throw new SeratoLibraryException(e);
        }
    }


    /**
     * Utility method for reading 4 bytes
     *
     * @return 32-bit integer value
     * @throws com.seratosync.db.SeratoLibraryException
     *          In case of I/O exception
     * @throws SeratoEofException In case of EOF
     */
    public int readIntegerValue() throws SeratoLibraryException, SeratoEofException {
        return (int) readLongValue(4);
    }

    /**
     * Utility method for reading variable amount of bytes
     *
     * @param bytes the number of bytes to read
     * @return 32-bit integer value
     * @throws com.seratosync.db.SeratoLibraryException
     *          In case of I/O exception
     * @throws SeratoEofException In case of EOF
     */
    public long readLongValue(int bytes) throws SeratoLibraryException, SeratoEofException {
        long nameLength = 0;
        for (int i = 0; i < bytes; i++) {
            nameLength <<= 8;
            nameLength |= readUnsignedByteInternal();
        }
        return nameLength;
    }

    /**
     * Utility method for reading a string and making sure it's the right one
     *
     * @param expected Expected string
     * @throws com.seratosync.db.SeratoLibraryException
     *          In case of I/O exception or string mismatch
     * @throws SeratoEofException In case of end of file is reached
     */
    public void skipExactString(String expected) throws SeratoLibraryException, SeratoEofException {
        byte[] data = new byte[expected.length()];
        int read = readBytesInternal(data);
        if (read != data.length) {
            throw new SeratoLibraryException("Expected '" + expected + "', but read only " + read + " bytes");
        }
        String dataAsString = new String(data);
        if (!expected.equals(dataAsString)) {
            throw new SeratoLibraryException("Expected '" + expected + "' but found '" + dataAsString + "'");
        }
    }

    /**
     * Utility method for reading a UTF-16 string and making sure it's the right one
     *
     * @param expected Expected string
     * @throws com.seratosync.db.SeratoLibraryException
     *          In case of I/O exception, string mismatch, or unsupported encoding
     * @throws SeratoEofException In case of end of file is reached
     */
    public void skipExactStringUTF16(String expected) throws SeratoLibraryException, SeratoEofException {
        byte[] data = new byte[expected.length() << 1];
        int read = readBytesInternal(data);
        if (read != data.length) {
            throw new SeratoLibraryException("Expected '" + expected + "', but read only " + read + " bytes");
        }
        String dataAsString;
        try {
            dataAsString = new String(data, "UTF-16");
        } catch (UnsupportedEncodingException e) {
            throw new SeratoLibraryException(e);
        }
        if (!expected.equals(dataAsString)) {
            throw new SeratoLibraryException("Expected '" + expected + "' but found '" + dataAsString + "'");
        }
    }

    /**
     * Utility method for reading a byte
     *
     * @return true if EOF reached, false otherwise
     * @throws com.seratosync.db.SeratoLibraryException
     *          In case of I/O exception or string mismatch
     * @throws SeratoEofException In case of end of file is reached
     */
    public boolean skipByte() throws SeratoLibraryException, SeratoEofException {
        byte[] data = new byte[1];
        int read = readBytesInternal(data);
        if (read != data.length) {
            throw new SeratoLibraryException("Expected a single byte, but was unable to read anything");
        }
        return false;
    }

    /**
     * Utility method for reading UTF-8 string
     *
     * @param length Number of bytes to read
     * @return string that was read
     * @throws com.seratosync.db.SeratoLibraryException
     *                            In case of I/O exception or unsupported encoding
     * @throws SeratoEofException In case of end of file is reached
     */
    public String readStringUTF8(int length) throws SeratoLibraryException, SeratoEofException {
        byte[] data = new byte[length];
        int read = readBytesInternal(data);
        if (read != length) {
            throw new SeratoLibraryException("Expected to read " + length + " bytes, but read only " + read);
        }
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new SeratoLibraryException(e);
        }
    }

    /**
     * Utility method for reading raw byte array
     *
     * @param length Number of bytes to read
     * @return byte array that was read
     * @throws com.seratosync.db.SeratoLibraryException
     *                            In case of I/O exception
     * @throws SeratoEofException In case of end of file is reached
     */
    public byte[] readBytes(int length) throws SeratoLibraryException, SeratoEofException {
        byte[] data = new byte[length];
        int read = readBytesInternal(data);
        if (read != length) {
            throw new SeratoLibraryException("Expected to read " + length + " bytes, but read only " + read);
        }
        return data;
    }

    /**
     * Utility method for reading UTF-8 string
     *
     * @return string that was read
     * @throws com.seratosync.db.SeratoLibraryException
     *                            In case of I/O exception or unsupported encoding
     * @throws SeratoEofException In case of EOF
     */
    public String readLineUTF16() throws SeratoLibraryException, SeratoEofException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte lastA;
        byte lastB = -1;
        for (;;) {
            lastA = lastB;
            try {
                lastB = readByteInternal();
            } catch (SeratoEofException e) {
                break;
            }
            out.write(lastB);
            if (lastA == 0 && lastB == 10) {
                try {
                    byte[] data = out.toByteArray();
                    data = Arrays.copyOfRange(data, 0, data.length - 2);
                    return new String(data, "UTF-16");
                } catch (UnsupportedEncodingException e) {
                    throw new SeratoLibraryException(e);
                }
            }
        }

        if (out.size() <= 0) {
            throw new SeratoEofException();
        }

        byte[] data = out.toByteArray();
        try {
            return new String(data, "UTF-16");
        } catch (UnsupportedEncodingException e) {
            throw new SeratoLibraryException(e);
        }
    }

}
