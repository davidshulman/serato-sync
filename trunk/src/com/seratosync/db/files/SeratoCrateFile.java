package com.seratosync.db.files;

import com.seratosync.db.files.entries.SeratoFileHeader;

/**
 * @author Roman Alekseenkov
 */
public class SeratoCrateFile extends AbstractSeratoFileWithHeaderAndEntries {

    public SeratoCrateFile() {
        super(new SeratoFileHeader("81.0", "/Serato ScratchLive Crate"));
    }

}
