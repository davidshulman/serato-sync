package com.seratosync.db.files;

import com.seratosync.db.SeratoLibraryException;
import com.seratosync.db.files.entries.SeratoFileEntries;
import com.seratosync.db.files.entries.SeratoFileHeader;
import com.seratosync.io.SeratoInputStream;
import com.seratosync.io.SeratoOutputStream;

/**
 * @author Roman Alekseenkov
 */
public abstract class AbstractSeratoFileWithHeaderAndEntries extends AbstractSeratoFile {

    private SeratoFileHeader header;
    private SeratoFileEntries tracks;

    public AbstractSeratoFileWithHeaderAndEntries(SeratoFileHeader header) {
        this.header = header;
        this.tracks = new SeratoFileEntries();
    }

    public SeratoFileHeader getHeader() {
        return header;
    }

    public SeratoFileEntries getEntries() {
        return tracks;
    }

    @Override
    public void readFrom(SeratoInputStream in) throws SeratoLibraryException {
        // Read header
        getHeader().readFrom(in);

        // Read entries
        getEntries().readFrom(in);
    }

    @Override
    public void writeTo(SeratoOutputStream out) throws SeratoLibraryException {
        // Write header first
        getHeader().writeTo(out);

        // Write all entries
        getEntries().writeTo(out);
    }

}
