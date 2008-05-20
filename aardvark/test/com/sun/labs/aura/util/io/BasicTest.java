/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
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
public class BasicTest extends TestSupport {

    public BasicTest() {
    }

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
    public void testNumberOfRecords() throws FileNotFoundException, IOException {
        for(int i = 1; i <= NUM_RECS; i++) {
            List<Record<String,Integer>> recs = readRecs(i);
            assertTrue("Non equal record sizes for " + i, recs.size() ==
                    nrecs[i]);
        }
    }
}