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

package com.sun.labs.aura;

import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.logging.Handler;
import com.sun.labs.util.SimpleLabsLogFormatter;

/**
 *
 * @author wh224365
 */
public class TestUtilities {
    static File testBaseDirectory;
    static Logger log;
    
    // Controls if temporary test files are removed post testing
    final static boolean removeFiles = true;
    
    static {
        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
        String baseDirName = System.getProperty("java.io.tmpdir");
        if(!baseDirName.endsWith(File.separator)) {
            baseDirName += File.separator;
        }
        baseDirName += "auratest." + timestampFormat.format(new Date());
        testBaseDirectory = new File(baseDirName);
        testBaseDirectory.deleteOnExit();
        
        log = Logger.getAnonymousLogger();
    }
    
    /**
     * Creates a directory named dirname within a directory timestamped to this
     * particular set of tests.
     * 
     * @param dirname
     * @return
     */
    public static File createTempDir(String dirname) {
        File tempDir = new File(testBaseDirectory, dirname);
        tempDir.mkdirs();
        if(removeFiles) {
            tempDir.deleteOnExit();
        }
        return tempDir;
    }
    
    public static File createTempFile(String dirname, String filename) {
        File tempFile = new File(createTempDir(dirname), filename);
        if(removeFiles) {
            tempFile.deleteOnExit();
        }
        return tempFile;
    }
    
    public static void delTree(String dirname) {
        delTree(new File(dirname));
    }

    /**
     * Removes a path. If the path is a directory, it is recursively removed.
     * 
     * @param path
     */
    public static void delTree(File path) {
        if(removeFiles) {
            if(path.exists()) {
                for(File file : path.listFiles()) {
                    if(file.isDirectory()) {
                        delTree(file);
                    } else {
                        file.delete();
                    }
                }
            }
            path.delete();
        }
    }
    
    public static Logger getLogger(Class loggedClass) {
        Logger logger = Logger.getLogger(loggedClass.getName());
        for(Handler handler : logger.getHandlers()) {
            handler.setFormatter(new SimpleLabsLogFormatter());
        }
        return logger;
    }
}
