/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.util.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

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
public class StructuredKeyedOutputStream<K, V> extends KeyedStream
        implements KeyedOutputStream<K, V> {
    
    ByteArrayOutputStream bos;
    
    ObjectOutputStream oos;

    public StructuredKeyedOutputStream(String name, boolean sorted) throws java.io.FileNotFoundException, IOException {
        this(new File(name), sorted);
    }

    public StructuredKeyedOutputStream(File f, boolean sorted) throws java.io.FileNotFoundException, IOException {
        this.f = f;
        this.sorted = sorted;
        raf = new RandomAccessFile(f, "rw");
        raf.writeBoolean(sorted);
    }

    public void write(K key, V value) throws IOException {
        //
        // Figure out what to do with the data.
        if(keyType == null) {
            keyType = Record.getType(key);
            valueType = Record.getType(value);
            raf.writeUTF(keyType.toString());
            raf.writeUTF(valueType.toString());
        }
        write(keyType, key);
        write(valueType, value);
    }

    public void write(Record<K, V> rec) throws IOException {
        write(rec.getKey(), rec.getValue());
    }

    private void write(Record.Type t, Object o) throws IOException {
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
    
    public void close() throws java.io.IOException {
        raf.setLength(raf.getFilePointer());
        super.close();
    }
}
