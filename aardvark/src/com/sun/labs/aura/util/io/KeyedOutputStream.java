package com.sun.labs.aura.util.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;

/**
 * An output file format that includes an initial key that can be used for
 * sorting and merging.  The data in the file will have the following format:
 * 
 * <pre>
 * <UTF-8 encoded String><null byte><n><n bytes of data> 
 * </pre>
 * 
 * The data will be written to the output file using a DataOutput.  <code>n</code>
 * will be written as an integer.
 * @param K the type of the key to use for the record.  Must extend Serializable and Comparable.
 * @param V the type of the value to use for the record.  Must extend Serialiable
 */
public class KeyedOutputStream<K,V> {
    private File f;
    
    private ObjectOutputStream oos;
    
    private FileOutputStream fos;
    
    private boolean sorted;
    
    public KeyedOutputStream(String name, boolean sorted) throws java.io.FileNotFoundException, IOException {
        this(new File(name), sorted);
    }
    
    public KeyedOutputStream(File f, boolean sorted) throws java.io.FileNotFoundException, IOException {
        this.f = f;
        this.sorted = sorted;
        fos = new FileOutputStream(f);
        oos = new ObjectOutputStream(new BufferedOutputStream(fos));
        
        //
        // Write the sorted attribute.
        oos.writeBoolean(sorted);
    }
    
    public FileChannel getChannel() {
        return fos.getChannel();
    }
    
    public void close() throws IOException {
        oos.close();
    }
    
    public void write(K key, V value) throws IOException {
        oos.writeObject(key);
        oos.writeObject(value);
    }
    
    public void write(Record<K,V> rec) throws IOException {
        write(rec.getKey(), rec.getValue());
    }
}
