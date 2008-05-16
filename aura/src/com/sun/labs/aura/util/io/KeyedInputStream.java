package com.sun.labs.aura.util.io;

import com.sun.labs.util.SimpleLabsLogFormatter;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * An input stream of keyed records.
 * @param K the type of the key for the records.  Must extend Serializable and Comparable
 * @param V the type of the value for the records.  Must extend Serializable.
 */
public class KeyedInputStream<K, V> extends KeyedStream {

    private Sorter.SortedRegion sr;

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
        raf = new RandomAccessFile(f, "rw");
        sorted = raf.readBoolean();
        keyType = Type.valueOf(raf.readUTF());
        valueType = Type.valueOf(raf.readUTF());
        
        //
        // Position the stream, if necessary.
        if(sr != null) {
            position(sr.start);
        }
    }

    public void reset() throws IOException {
        if(sr == null) {
            position(0);
            raf.readBoolean();
        } else {
            position(sr.start);
        }
    }
    
    public int getNRead() {
        return nRead;
    }

    public Record<K, V> read() throws IOException {

        //
        // If we've read all of the records in our region, then we're done.
        if(sr != null && nRead == sr.size) {
            return null;
        }
        try {
            K key = (K) read(keyType);
            V value = (V) read(valueType);
            nRead++;
            return new Record(key, value);
        } catch(EOFException eof) {
            return null;
        } catch(ClassCastException cce) {
            Logger.getLogger("com.sun.labs.aura.util.io").severe("Class cast exception reading key value data: " +
                    cce);
            return null;
        } catch(ClassNotFoundException cnfe) {
            Logger.getLogger("com.sun.labs.aura.util.io").severe("Class not found reading key value data: " +
                    cnfe);
            return null;
        }
    }
    
    private Object read(Type t) throws IOException, ClassNotFoundException {
        switch(t) {
            case STRING:
                return raf.readUTF();
            case INTEGER:
                return raf.readInt();
            case LONG:
                return raf.readLong();
            case FLOAT:
                return raf.readFloat();
            case DOUBLE:
                return raf.readDouble();
            default:
                int size = raf.readInt();
                byte[] b = new byte[size];
                raf.readFully(b);
                ByteArrayInputStream bis = new ByteArrayInputStream(b);
                ObjectInputStream ois = new ObjectInputStream(bis);
                try {
                    return ois.readObject();
                } catch (ClassNotFoundException cnfe) {
                    Logger.getLogger("com.sun.labs.aura.util.io").severe("Class not found reading key value data: " +
                            cnfe);
                    return null;
                }
        }
    }
    
    public static void main(String[] args) throws Exception {
        //
        // Use the labs format logging.
        Logger rl = Logger.getLogger("");
        for(Handler h : rl.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }

        KeyedInputStream<String,Integer> kis = new KeyedInputStream<String, Integer>(args[0]);
        Record<String,Integer> rec;
        while((rec = kis.read()) != null) {
            System.out.println(rec);
        }
        kis.close();
    }
}
