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

package com.sun.labs.aura.aardvark.dashboard.story;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class StatusManager {

    private long period;
    private String baseUrl;
    private URL url;
    private Thread t;
    private Stats curStats;

    public StatusManager(String baseURL, long period) {
        try {
            url = new URL(baseURL + "/GetStatus");
            this.period = period;
        } catch (MalformedURLException ex) {
            Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void start() {
        t = new Thread() {

            public void run() {
                collector();
            }
        };

        t.start();
    }

    public void stop() {
        t = null;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public void collector() {
        while (t != null) {
            try {
                try {
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(30000);
                    connection.setReadTimeout(30000);

                    InputStream is = connection.getInputStream();
                    Stats stats = Util.loadStats(is);
                    is.close();
                    curStats = stats;
                } catch (IOException ex) {
                    Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
                    curStats = null;
                }
                Thread.sleep(period);
            } catch (InterruptedException ex) {
                Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Stats getStats() {
        return curStats;
    }

    public static void main(String[] args) throws Exception {
        StatusManager sm = new StatusManager(
                "http://localhost:8080/DashboardWebServices/GetStatus", 1000l);
        sm.start();
        Thread.sleep(10000L);
    }
}
