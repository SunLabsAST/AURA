package com.sun.labs.aura.util.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

/**
 * A base class for keyed streams
 */
public abstract class KeyedStream {

    protected static Logger logger;
    
    protected File f;
    protected boolean sorted = false;
    protected Record.Type keyType = Record.Type.OBJECT;
    protected Record.Type valueType = Record.Type.OBJECT;
    protected RandomAccessFile raf;

    static {
        logger = Logger.getLogger("com.sun.labs.aura.util.io");
    }
    
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
}
