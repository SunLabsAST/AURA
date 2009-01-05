package com.sun.labs.aura.grid.util;

import com.sun.labs.minion.util.NanoWatch;
import java.io.InputStream;
import java.net.URL;

/**
 * Fetches a URL and tells you how long it took to do so.
 */
public class Fetch {

    public static void main(String[] args) throws Exception {
        URL u = new URL(args[0]);
        byte[] b = new byte[8192];
        NanoWatch nw = new NanoWatch();
        nw.start();
        InputStream us = u.openStream();
        int n = us.read(b);
        int read = n;
        while(n > 0) {
            n = us.read(b);
            read += n;
        }
        nw.stop();
        System.out.println(String.format("Read %d bytes in %.3fms", read, nw.getTimeMillis()));
    }

}
