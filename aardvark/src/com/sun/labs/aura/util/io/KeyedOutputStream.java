package com.sun.labs.aura.util.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
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
public class KeyedOutputStream<K, V> extends KeyedStream {
    
    ByteArrayOutputStream bos;
    
    ObjectOutputStream oos;

    public KeyedOutputStream(String name, boolean sorted) throws java.io.FileNotFoundException, IOException {
        this(new File(name), sorted);
    }

    public KeyedOutputStream(File f, boolean sorted) throws java.io.FileNotFoundException, IOException {
        this.f = f;
        this.sorted = sorted;
        raf = new RandomAccessFile(f, "rw");
        raf.writeBoolean(sorted);
    }

    public void write(K key, V value) throws IOException {
        //
        // Figure out what to do with the data.
        if(keyType == null) {
            keyType = getType(key);
            valueType = getType(value);
            raf.writeUTF(keyType.toString());
            raf.writeUTF(valueType.toString());
        }
        write(keyType, key);
        write(valueType, value);
    }

    public void write(Record<K, V> rec) throws IOException {
        write(rec.getKey(), rec.getValue());
    }

    private void write(Type t, Object o) throws IOException {
        switch(t) {
            case STRING:
                raf.writeUTF((String) o);
                break;
            case INTEGER:
                raf.writeInt((Integer) o);
                break;
            case LONG:
                raf.writeLong((Long) o);
                break;
            case FLOAT:
                raf.writeFloat((Float) o);
                break;
            case DOUBLE:
                raf.writeDouble((Double) o);
                break;
            case OBJECT:
                bos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(bos);
                oos.writeObject(o);
                raf.write(bos.size());
                raf.write(bos.toByteArray());
                break;
        }
    }
}
