/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
