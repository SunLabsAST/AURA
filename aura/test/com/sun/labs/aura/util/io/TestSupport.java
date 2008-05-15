
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

/**
 *
 */
public class TestSupport {

    protected Logger logger;

    protected int[] nrecs = {0, 51258, 21339, 3124, 4307, 3064, 1534, 480, 487,
        519
    };

    public TestSupport() {
        logger = Logger.getAnonymousLogger();
    }

    public static String getFN(int i) {
        return String.format("st-%04d", i);
    }

    public static File getTF(int i) {
        return new File(System.getProperty("java.io.tmpdir"), getFN(i));
    }

    public static File getTF(String fn) {
        return new File(System.getProperty("java.io.tmpdir"), fn);
    }
    
    protected static void copyData() throws Exception {

        //
        // Copy the data into the tmpdir.
        byte[] buff = new byte[8196];
        for(int i = 1; i <= 9; i++) {
            InputStream is =
                    new BufferedInputStream(SorterTest.class.getResourceAsStream(getFN(i)));
            OutputStream os =
                    new BufferedOutputStream(new FileOutputStream(getTF(i)));
            int nb;
            while((nb = is.read(buff)) >= 0) {
                os.write(buff, 0, nb);
            }
            is.close();
            os.close();
        }
    }

    protected static void cleanData() throws Exception {
        for(int i = 1; i <= 9; i++) {
            getTF(i).delete();
        }
    }

    public List<Record<String, Integer>> readRecs(int i) throws FileNotFoundException, IOException {
        return readRecs(getTF(i));
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
