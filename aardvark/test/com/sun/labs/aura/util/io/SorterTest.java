/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stgreen
 */
public class SorterTest extends TestSupport {

    @BeforeClass
    public static void setUpClass() throws Exception {
        copyData();
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        cleanData();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void testSort() throws FileNotFoundException, IOException {
        List<Record<String, Integer>> recs = readRecs(1);
        Collections.sort(recs);
        Sorter s = new Sorter(getTF(1), getTF("blocked"), getTF("sorted"));
        s.sort();
        List<Record<String,Integer>> srecs = readRecs(getTF("sorted"));
        assertTrue("original and sorted sizes don't match: " + 
                recs.size() + " vs " + srecs.size(),
                recs.size() == srecs.size());
    }
}
