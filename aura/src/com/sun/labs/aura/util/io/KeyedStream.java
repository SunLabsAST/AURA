package com.sun.labs.aura.util.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * A base class for keyed streams
 */
public abstract class KeyedStream {

    protected enum Type {
        STRING, INTEGER, LONG, FLOAT, DOUBLE, OBJECT
    };

    protected File f;

    protected boolean sorted;

    protected  Type keyType;

    protected  Type valueType;

    protected  RandomAccessFile raf ; 

    public boolean getSorted() {
        return sorted;
    }
    
    public FileChannel getChannel() {
        return raf.getChannel();
    }

    public long position() throws IOException {
        return raf.getFilePointer();
    }
    
    public void position(long pos) throws IOException {
        raf.seek(pos);
    }

    public void close() throws IOException {
        raf.close();
    }

    protected Type getType(Object o) {
        if(o instanceof String) {
            return Type.STRING;
        }
        
        if(o instanceof Integer) {
            return Type.INTEGER;
        }
        
        if(o instanceof Long) {
            return Type.LONG;
        }
        
        if(o instanceof Float) {
            return Type.FLOAT;
        }
        
        if(o instanceof Double) {
            return Type.DOUBLE;
        }
        
        return Type.OBJECT;
    }
    
}
