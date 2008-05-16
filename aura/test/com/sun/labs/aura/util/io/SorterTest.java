/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.util.io;

import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.SimpleLabsLogFormatter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
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
        //
        // Use the labs format logging.
        Logger rl = Logger.getLogger("");
        for(Handler h : rl.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }

        copyData();
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        cleanData();
    }

    private String gk(int i) {
        return String.format("This is record %05d", i);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void testSimpleSort() throws Exception {
        File f = getTF("input");
        KeyedOutputStream<String, Integer> kos =
                new KeyedOutputStream<String, Integer>(f, false);
        for(int i = 5000; i >= 0; i--) {
            kos.write(gk(i), i);
        }
        kos.close();

        Sorter s = new Sorter(f, getTF("blocked"), getTF("sorted"));
        s.sort();
        
        KeyedInputStream<String,Integer> kis = new KeyedInputStream<String,Integer>(getTF("sorted"));
        Record<String,Integer> rec;
        int r = 0;
        while((rec = kis.read()) != null) {
            assertEquals("Error with key for record " + r, gk(r), rec.getKey());
            assertEquals("Error with value for record " + r, r, rec.getValue());
            r++;
        }
        kis.close();
    }
    
    private void sort(int p) throws FileNotFoundException, IOException {
        List<Record<String, Integer>> recs = readRecs(p);
        Collections.sort(recs);
        Sorter s = new Sorter(getTF(p), getTF("blocked"), getTF("sorted"));
        NanoWatch nw = new NanoWatch();
        nw.start();
        s.sort();
        nw.stop();
        Logger.getLogger("").info(String.format("sorting %d took: %.3f",
                p, nw.getTimeMillis()));
        List<Record<String, Integer>> srecs = readRecs(getTF("sorted"));

        //
        // Make sure the lists are the same size.
        assertTrue("original and sorted sizes don't match: " +
                recs.size() + " vs " + srecs.size(),
                recs.size() == srecs.size());

        //
        // Make sure that the sorted file is the same as the list.
        int pos = 0;
        for(Iterator<Record<String, Integer>> i = recs.iterator(),  j =
                srecs.iterator(); i.hasNext() && j.hasNext();) {
            Record<String, Integer> r1 = i.next();
            Record<String, Integer> r2 = j.next();
            assertEquals(p + " error at key " + pos, r1.getKey(), r2.getKey());
            pos++;
        }
    }
    
    @Test
    public void testDataSort() throws FileNotFoundException, IOException {
        for(int i = 1; i <=9; i++) {
            sort(i);
        }
    }
}
