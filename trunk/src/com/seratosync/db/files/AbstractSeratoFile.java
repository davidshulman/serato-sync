package com.seratosync.db.files;

import com.seratosync.db.SeratoLibraryException;
import com.seratosync.io.SeratoInputStream;
import com.seratosync.io.SeratoOutputStream;

import java.io.*;

/**
 * @author Roman Alekseenkov
 */
public abstract class AbstractSeratoFile {

    public abstract void readFrom(SeratoInputStream in) throws SeratoLibraryException;
    public abstract void writeTo(SeratoOutputStream out) throws SeratoLibraryException;


    /**
     * Loads itself from file
     *
     * @param inFile file to read from
     * @throws com.seratosync.db.SeratoLibraryException
     *          if something went wrong during reading
     */
    public void loadFrom(File inFile) throws SeratoLibraryException {

        // Create input stream to read serato crate file
        SeratoInputStream in;
        try {
            in = new SeratoInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            throw new SeratoLibraryException(e);
        }

        // Read header
        readFrom(in);

        try {
            in.close();
        } catch (IOException e) {
            // do nothing
        }

    }

    /**
     * Writes itself into a stream
     *
     * @param outStream print stream to write the result to
     * @throws com.seratosync.db.SeratoLibraryException
     *          if something went wrong during writing
     */
    private void saveTo(OutputStream outStream) throws SeratoLibraryException {
        SeratoOutputStream out = new SeratoOutputStream(outStream);

        writeTo(out);

        try {
            out.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Writes itself into a file
     *
     * @param outFile output file
     * @throws com.seratosync.db.SeratoLibraryException
     *          if something went wrong during writing
     */
    public void saveTo(File outFile) throws SeratoLibraryException {
        try {
            saveTo(new FileOutputStream(outFile));
        } catch (FileNotFoundException e) {
            throw new SeratoLibraryException(e);
        }
    }

}
