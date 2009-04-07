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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sun.labs.aura.TestUtilities;

/**
 * 
 */
public class ReadWriteTest {
    File f;

    public ReadWriteTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        f = TestSupport.getTempFile("tmp.keyed");
    }

    @After
    public void tearDown() {
//        f.delete();
    }
    
    private String gk(int i) {
        return String.format("This is record %05d", i);
    }

    @Test
    public void readWriteTest() throws Exception {
        KeyedOutputStream<String,Integer> kos = 
                new KeyedOutputStream<String, Integer>(f, false);
        for(int i = 0; i < 1000; i++) {
            kos.write(gk(i), i);
        }
        kos.close();
        KeyedInputStream<String,Integer> kis = new KeyedInputStream<String, Integer>(f);
        Record<String,Integer> rec;
        int r = 0;
        while((rec = kis.read()) != null) {
            assertEquals("Error with key for record " + r, rec.getKey(), gk(r));
            assertEquals("Error with value for record " + r, rec.getValue(), r);
            r++;
        }
        kis.close();
        assertTrue(r == 1000);
    }
    
    @Test
    public void writeBackwardsTest() throws Exception {
        KeyedOutputStream<String, Integer> kos =
                new KeyedOutputStream<String, Integer>(f, false);
        for(int i = 1000; i >= 0; i--) {
            kos.write(gk(i), i);
        }
        kos.close();
        KeyedInputStream<String, Integer> kis =
                new KeyedInputStream<String, Integer>(f);
        Record<String, Integer> rec;
        int r = 1000;
        while((rec = kis.read()) != null) {
            assertEquals("Error with key for record " + r, rec.getKey(),
                    gk(r));
            assertEquals("Error with value for record " + r, rec.getValue(), r);
            r--;
        }
        assertTrue("r: " + r, r == -1);
    }
}
