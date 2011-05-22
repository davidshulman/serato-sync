package com.seratosync.db.files;

import com.seratosync.db.files.entries.SeratoFileHeader;

/**
 * @author Roman Alekseenkov
 */
public class SeratoDatabaseV2File extends AbstractSeratoFileWithHeaderAndEntries {

    public SeratoDatabaseV2File() {
        super(new SeratoFileHeader("@2.0", "/Serato Scratch LIVE Database"));
    }

}
