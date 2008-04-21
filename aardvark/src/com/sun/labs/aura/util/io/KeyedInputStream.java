package com.sun.labs.aura.util.io;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

/**
 * An input stream of keyed records.
 * @param K the type of the key for the records.  Must extend Serializable and Comparable
 * @param V the type of the value for the records.  Must extend Serializable.
 */
public class KeyedInputStream<K, V> {

    private File f;

    private ObjectInputStream ois;

    private FileInputStream fis;

    private Sorter.SortedRegion sr;

    private boolean sorted;

    private int nRead;

    public KeyedInputStream(String name) throws java.io.FileNotFoundException, IOException {
        this(name, null);
    }

    public KeyedInputStream(File f) throws java.io.FileNotFoundException, IOException {
        this(f, null);
    }

    public KeyedInputStream(String name, Sorter.SortedRegion sr) throws java.io.FileNotFoundException, IOException {
        this(new File(name), sr);
    }

    public KeyedInputStream(File f, Sorter.SortedRegion sr) throws FileNotFoundException, IOException {
        this.f = f;
        this.sr = sr;
        fis = new FileInputStream(f);
        ois = new ObjectInputStream(fis);
        sorted = ois.readBoolean();

        //
        // Position the stream, if necessary.
        if(sr != null) {
            fis.getChannel().position(sr.start);
        }
    }

    public void reset() throws IOException {
        if(sr == null) {
            fis.getChannel().position();
            ois.readBoolean();
        } else {
            fis.getChannel().position(sr.start);
        }
    }

    public boolean getSorted() {
        return sorted;
    }

    public FileChannel getChannel() {
        return fis.getChannel();
    }

    public void close() throws IOException {
        ois.close();
    }

    public Record<K, V> read() throws IOException {

        //
        // If we've read all of the records in our region, then we're done.
        if(sr != null && nRead == sr.size) {
            return null;
        }
        try {
            Record<K, V> rec = new Record((K) ois.readObject(),
                    (V) ois.readObject());
            nRead++;
            return rec;
        } catch(EOFException eof) {
            return null;
        } catch(ClassNotFoundException cnfe) {
            Logger.getLogger("com.sun.labs.aura.util.io").severe("Class not found reading key value data: " +
                    cnfe);
            return null;
        }
    }
}
