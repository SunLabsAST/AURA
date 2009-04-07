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
