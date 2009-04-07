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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import com.sun.labs.aura.TestUtilities;

/**
 * Collects functionality needed for multiple sorting tests.
 */
public class TestSupport {
    protected Logger logger;
    final static int NUM_RECS = 9;

    protected int[] nrecs = {0, 51258, 21339, 3124, 4307, 3064, 1534, 480, 487,
        519
    };

    public TestSupport() {
        logger = TestUtilities.getLogger(getClass());
    }

    public static String getTestFileName(int i) {
        return String.format("st-%04d", i);
    }

    public static File getTestFile(int i) {
        return getTempFile(getTestFileName(i));
    }

    public static File getTempFile(String filename) {
        return TestUtilities.createTempFile("sorttest", filename);
    }
    
    /**
     * Copy the data into the temp directory.
     * 
     * @throws java.lang.Exception
     */
    protected static void copyData() throws Exception {
        byte[] buff = new byte[8196];
        for(int i = 1; i <= NUM_RECS; i++) {
            InputStream is =
                    new BufferedInputStream(SorterTest.class.getResourceAsStream(getTestFileName(i)));
            OutputStream os =
                    new BufferedOutputStream(new FileOutputStream(getTestFile(i)));
            int nb;
            while((nb = is.read(buff)) >= 0) {
                os.write(buff, 0, nb);
            }
            is.close();
            os.close();
        }
    }

    protected static void cleanData() throws Exception {
    }

    public List<Record<String, Integer>> readRecs(int i) throws FileNotFoundException, IOException {
        return readRecs(getTestFile(i));
    }

    public List<Record<String, Integer>> readRecs(File f) throws FileNotFoundException, IOException {
        List<Record<String, Integer>> recs =
                new ArrayList<Record<String, Integer>>();
        KeyedInputStream<String, Integer> kis =
                new KeyedInputStream<String, Integer>(f);
        Record<String, Integer> rec;
        while((rec = kis.read()) != null) {
            recs.add(rec);
        }
        kis.close();
        return recs;
    }
}
