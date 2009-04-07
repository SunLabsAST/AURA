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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

/**
 * A base class for keyed streams
 */
public abstract class KeyedStream {

    protected static final Logger logger;
    
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
