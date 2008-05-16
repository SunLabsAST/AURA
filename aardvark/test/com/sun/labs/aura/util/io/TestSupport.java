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

    public static String getFileName(int i) {
        return String.format("st-%04d", i);
    }

    public static File getTestFile(int i) {
        return getTempFile(getFileName(i));
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
                    new BufferedInputStream(SorterTest.class.getResourceAsStream(getFileName(i)));
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