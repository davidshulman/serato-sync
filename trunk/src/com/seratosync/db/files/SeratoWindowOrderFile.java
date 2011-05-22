package com.seratosync.db.files;

import com.seratosync.db.SeratoLibraryException;
import com.seratosync.actions.PruneSeratoDatabaseAction;
import com.seratosync.io.SeratoEofException;
import com.seratosync.io.SeratoInputStream;
import com.seratosync.io.SeratoOutputStream;

import java.util.*;

public class SeratoWindowOrderFile extends AbstractSeratoFile {

    private static final String HEADER_START = "[begin record]";
    private static final String HEADER_END = "[end record]";
    private static final String ENTRY_START = "[crate]";

    private List<String> entries = new ArrayList<String>();

    public SeratoWindowOrderFile() {
    }

    @Override
    public void readFrom(SeratoInputStream in) throws SeratoLibraryException {
        {
            // start
            String header;
            try {
                header = in.readLineUTF16();
            } catch (SeratoEofException e) {
                throw new SeratoLibraryException(e);
            }
            if (!HEADER_START.equals(header)) {
                throw new SeratoLibraryException("Window order file doesn't start with " + HEADER_START);
            }
        }

        for (; ; ) {
            // is it the end
            String line;
            try {
                line = in.readLineUTF16();
            } catch (SeratoEofException e) {
                throw new SeratoLibraryException(e);
            }
            if (HEADER_END.equals(line)) {
                break;
            }

            // process line
            if (line.startsWith(ENTRY_START)) {
                String entry = line.substring(ENTRY_START.length());
                entries.add(entry);
            }
        }
    }

    @Override
    public void writeTo(SeratoOutputStream out) throws SeratoLibraryException {
        out.writeLineUTF16(HEADER_START);
        for (String entry : entries) {
            out.writeLineUTF16(ENTRY_START + entry);
        }
        out.writeLineUTF16(HEADER_END);
    }

    public void add(String crateName) {
        if (!entries.contains(crateName)) {
            entries.add(crateName);
        }
    }

    public void reorder(final PruneSeratoDatabaseAction.CrateFileNameAdvancedComparator crateFileNameAdvancedComparator) {
        Collections.sort(entries, crateFileNameAdvancedComparator);
    }

}
