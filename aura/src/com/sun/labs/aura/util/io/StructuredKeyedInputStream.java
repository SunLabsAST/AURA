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
public class StructuredKeyedInputStream<K, V> extends KeyedStream
        implements KeyedInputStream {

    private Sorter.SortedRegion sr;
    
    private int nRead;

    public StructuredKeyedInputStream(String name)
            throws java.io.FileNotFoundException, IOException {
        this(name, null);
    }

    public StructuredKeyedInputStream(String name, boolean formatInFile)
            throws java.io.FileNotFoundException, IOException {
        this(new File(name), (Sorter.SortedRegion)null, formatInFile);
    }

    
    public StructuredKeyedInputStream(File f)
            throws java.io.FileNotFoundException, IOException {
        this(f, null);
    }

    public StructuredKeyedInputStream(String name, Sorter.SortedRegion sr)
            throws java.io.FileNotFoundException, IOException {
        this(new File(name), sr);
    }

    public StructuredKeyedInputStream(File f, Sorter.SortedRegion sr)
            throws FileNotFoundException, IOException {
        this(f, sr, true);
    }

    public StructuredKeyedInputStream(File f, Sorter.SortedRegion sr, boolean formatInFile)
            throws FileNotFoundException, IOException {
        this.f = f;
        this.sr = sr;
        raf = new RandomAccessFile(f, "rw");
        if(formatInFile) {
            sorted = raf.readBoolean();
            keyType = Record.Type.valueOf(raf.readUTF());
            valueType = Record.Type.valueOf(raf.readUTF());
        }
        
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
            K key = (K)read(keyType);
            V value = (V)read(valueType);
            nRead++;
            return new Record(key, value);
        } catch(EOFException eof) {
            return null;
        } catch(ClassCastException cce) {
            logger.severe("Class cast exception reading key value data: " + cce);
            return null;
        } catch(ClassNotFoundException cnfe) {
            logger.severe("Class not found reading key value data: " + cnfe);
            return null;
        }
    }
    
    private Object read(Record.Type t) throws IOException, ClassNotFoundException {
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
        for(Handler h : KeyedStream.logger.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }

        String inputFile = args[0];
        KeyedStream.logger.info("Processing: " + inputFile);
        
        StructuredKeyedInputStream<String,Integer> kis =
                new StructuredKeyedInputStream<String, Integer>(inputFile, false);
        kis.keyType = Record.Type.STRING;
        Record<String,Integer> rec;
        for(int lineCount = 0; (rec = kis.read()) != null; lineCount++) {
            KeyedStream.logger.info(lineCount + ": " + rec);
        }
        kis.close();
    }

    public boolean isSorted() {
        return getSorted();
    }
}
