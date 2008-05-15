/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
        f = new File(new File(System.getProperty("java.io.tmpdir")),
                "tmp.keyed");
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