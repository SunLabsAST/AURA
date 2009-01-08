package com.sun.labs.aura.grid.util;

import com.sun.labs.minion.util.NanoWatch;
import java.io.InputStream;
import java.net.URL;

/**
 * Fetches a URL and tells you how long it took to do so.
 */
public class Fetch {

    public static int fetch(String url) throws Exception {
        URL u = new URL(url);
        byte[] b = new byte[8192];
        InputStream us = u.openStream();
        int n = us.read(b);
        int read = n;
        while(n > 0) {
            n = us.read(b);
            read += n;
        }
        return read;
    }

    public static void main(String[] args) throws Exception {
        for(String fetch : args) {
            NanoWatch nw = new NanoWatch();
            int n = 0;
            for(int i = 0; i < 10; i++) {
                nw.start();
                n = fetch(fetch);
                nw.stop();

            }
            System.out.println(String.format("Read %d bytes in an avg %.3fms from %s", n,
                    nw.getAvgTimeMillis(), fetch));
        }
    }

}
