package com.seratosync.db.files.entries;

import com.seratosync.db.SeratoLibraryException;
import com.seratosync.filesystem.Drive;
import com.seratosync.filesystem.FileDirectoryUtils;
import com.seratosync.log.Log;
import com.seratosync.io.SeratoEofException;
import com.seratosync.io.SeratoInputStream;
import com.seratosync.io.SeratoOutputStream;
import com.seratosync.io.SeratoOutputStreamDataCollector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SeratoFileEntries {

    // raw file entries
    private LinkedList<SeratoFileEntry> rawData = new LinkedList<SeratoFileEntry>();

    /**
     * Creates an empty serato tracks section
     */
    public SeratoFileEntries() {
    }

    /**
     * Reads itself from the input stream
     *
     * @param in Input stream
     * @throws com.seratosync.db.SeratoLibraryException
     *          In case of I/O exception
     */
    public void readFrom(SeratoInputStream in) throws SeratoLibraryException {
        for (; ;) {
            try {
                SeratoFileEntry entry = SeratoFileEntry.readFrom(in);
                rawData.add(entry);
            } catch (SeratoEofException e) {
                break;
            }
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
        for (SeratoFileEntry entry : rawData) {
            entry.writeTo(out);
        }
    }

    public int removeMissingTracks(Drive drive) {
        int removed = 0;

        Iterator<SeratoFileEntry> it = rawData.iterator();
        for (; it.hasNext();) {
            SeratoFileEntry entry = it.next();

            // skip all non-track files
            if (!entry.isTrack()) {
                continue;
            }

            // retrieve track name
            String track;
            try {
                track = entry.getTrackName();
            } catch (SeratoLibraryException e) {
                Log.error(e.getMessage());
                continue;
            }

            // construct the entire path, including a drive
            String path = drive.getPrefix() + FileDirectoryUtils.convertFromFileToSeratoFile(track);

            // check for existence
            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                Log.debug("Removing: " + path + " (" + track + ")");
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    public boolean hasTracks() {
        for (SeratoFileEntry entry : rawData)
            if (entry.isTrack()) {
                return true;
            }
        return false;
    }

    /**
     * xxx...
     *
     * @param tracks
     * @return whether the crate was modified, or not
     */
    public boolean addTracks(Collection<String> tracks) {
        // hash set of track names for optimization
        Set<String> existingTrackNames = new HashSet<String>();
        for (SeratoFileEntry entry : rawData)
            if (entry.isTrack()) {
                try {
                    existingTrackNames.add(entry.getTrackName());
                } catch (SeratoLibraryException e) {
                    // do nothing
                }
            }

        // find a place to insert tracks (well, may be we'll start adding them to the end)
        ListIterator<SeratoFileEntry> it = rawData.listIterator();
        while (it.hasNext()) {
            SeratoFileEntry entry = it.next();

            // if the next element is track, let's stop here (that's the right place, we'll be inserting to the beginning)
            if (entry.isTrack()) {
                break;
            }
        }

        boolean result = false;
        for (String trackRaw : tracks) {
            // convert track name to serato path
            String trackSeratoPath = FileDirectoryUtils.convertFromFileToSeratoFile(trackRaw);

            // don't add track if it's already there
            if (existingTrackNames.contains(trackSeratoPath)) {
                continue;
            }

            // create track entry
            SeratoFileEntry entry = createTrackEntry(trackSeratoPath);

            // insert track entry into the appropriate place
            it.add(entry);

            // ok, we have made a modification by adding a track
            result = true;
        }

        return result;
    }

    /**
     * Creates default raw data for track entry in the crate file
     *
     * @param trackSeratoPath track path
     * @return serato file entry to write into the crate file
     */
    private SeratoFileEntry createTrackEntry(final String trackSeratoPath) {
        // create track entry
        byte[] data;
        try {
            data = new SeratoOutputStreamDataCollector() {
                @Override
                public void write() throws SeratoLibraryException {
                    try {
                        // ptrk as string
                        getStream().writeBytes("ptrk");

                        // likely all these 4 bytes is a length of the track name in UTF 16
                        getStream().writeInt(trackSeratoPath.length() * 2);

                        // track name
                        getStream().writeUTF16(trackSeratoPath);
                    } catch (IOException e) {
                        throw new SeratoLibraryException(e);
                    }
                }
            }.collect();
        } catch (SeratoLibraryException e) {
            Log.error("Failed to serialize track name: " + trackSeratoPath);
            return null;
        }

        // add track entry
        return new SeratoFileEntry("otrk", data);
    }

}
