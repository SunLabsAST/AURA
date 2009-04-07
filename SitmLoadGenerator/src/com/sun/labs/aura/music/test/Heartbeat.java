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

package com.sun.labs.aura.music.test;

import com.sun.labs.aura.music.webservices.api.Item;
import com.sun.labs.aura.music.webservices.api.Scored;
import com.sun.labs.aura.music.webservices.api.SitmAPI;
import com.sun.labs.aura.music.webservices.api.SitmAPIImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 *
 * @author plamere
 */
public class Heartbeat {
    private final static String DEFAULT_EMAIL_ADDRESS = "aura-admin@sun.com";
    private final static String DEFAULT_URL = "http://www.tastekeeper.com/SitmWebServices/";
    private final static int DEFAULT_TIME = 60;
    private final static String SEP = "\t";

    private int loopTimeInSeconds;
    private String url;
    private String email;

    private boolean currentState = true;
    private int pings = 0;
    private int failures = 0;
    private long totalPingTime;
    private long lastPingTime;
    private long startTime;
    private long lastChangeTime ;

    private SitmAPI sitm;
    private List<Item> artists;
    private Random rng = new Random();

    private MailManager mailManager = new MailManager();

    public Heartbeat(int loopTimeInSeconds, String url, String email) {
        this.loopTimeInSeconds = loopTimeInSeconds;
        this.url = url;
        this.email = email;

        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                shutdown();
            }
        });
    }

    public void go() {
        if (startup()) {
            try {
                long lastRunTime = 0L;
                while (true) {
                    long delta = (lastRunTime + loopTimeInSeconds * 1000L) - System.currentTimeMillis();
                    delay(delta);
                    lastRunTime = System.currentTimeMillis();
                    Results results = ping();
                    logResults(results);
                }
            } finally {
                shutdown();
            }
        }
    }

    private void delay(long ms) {
        try {
            if (ms > 0) {
                Thread.sleep(ms);
            }
        } catch (InterruptedException ex) {
        }
    }

    private Results ping() {
        long pingStartTime = System.currentTimeMillis();
        try {
            sitm.getStats();
            Item artist = selectArtistAtRandom();
            List<Scored<Item>> simArtists = sitm.findSimilarArtistsByKey(artist.getKey(), 10);

            if (false && pings % 10 == 0) {
                forceError();
            }

            List<Item> items = sitm.getItems(getKeys(simArtists), currentState);
        } catch (IOException ioe) {
            return new Results(false, System.currentTimeMillis() - pingStartTime, ioe.getMessage());
        }
        return new Results(true, System.currentTimeMillis() - pingStartTime, "");
    }


    private void forceError() throws IOException {
        sitm.artistSearch("\\&&");
    }

    private List<String> getKeys(List<Scored<Item>> items) {
        List<String> keys = new ArrayList<String>();
        for (Scored<Item> item : items) {
            keys.add(item.getItem().getKey());
        }
        return keys;
    }

    private Item selectArtistAtRandom() {
        return artists.get(rng.nextInt(artists.size()));
    }

    private void logResults(Results results) {
        pings++;
        if (!results.isOk()) {
            failures++;
        }

        totalPingTime += results.getPingTime();
        lastPingTime = results.getPingTime();

        if (results.isOk() != currentState) {
            currentState = results.isOk();
            sendMailMessage(results.getDetails());
            lastChangeTime = System.currentTimeMillis();
        }
        System.out.println(getReport(false));
        if (results.getDetails().length() > 0) {
            System.out.println("=============== Start Details ============");
            System.out.println(results.getDetails());
            System.out.println("=============== end Details ============");
        }
    }

    private void sendMailMessage(String details) {
        String status = currentState ? "GOOD" : "BAD";
        String msg = getReport(true);

        if (details.length() > 0) {
            msg += "\n\n" + details;

        }
        mailManager.sendMessage(email, "tastekeeper status is " + status, msg);
    }

    private boolean startup() {
        if (sitm == null) {
            try {
                sitm = SitmAPI.getSitmAPI(url, false, true, false);
                artists = sitm.getArtists(100);
                startTime = System.currentTimeMillis();
                lastChangeTime = startTime;
            } catch (IOException ioe) {
                System.out.println("Aborted. Can't connect to " + url);
                return false;
            }

        }
        return true;
    }

    private void shutdown() {
        System.out.println(getReport(false));
        System.out.println(new Date() + SEP + "SHUTDOWN");
        sitm.showStats();
        mailManager.sendMessage(email, "tastekeeper heartbeat monitor is SHUTDOWN ", getReport(true));
    }


    private String getReport(boolean full) {
        Date date = new Date();
        String runtime = formatTime(System.currentTimeMillis() - startTime);
        String status = currentState ? "OK" : "ERR";

        long averagePingTime = 0;
        if (pings > 0) {
            averagePingTime = totalPingTime / pings;
        }

        if (full) {
            StringBuilder sb = new StringBuilder();
            sb.append(" Current Status: " + status + "\n");
            sb.append(" Pings: " + pings + "\n");
            sb.append(" Fails: " + failures + "\n");
            sb.append(" Average Ping Time: " + averagePingTime + "\n");
            sb.append(" Last Ping Time: " + lastPingTime + "\n");
            sb.append(" Last Status Change : " + formatTime(System.currentTimeMillis() - lastChangeTime) + "\n");
            sb.append(" Runtime: " + runtime + "\n");
            sb.append(" Date: " + date + "\n");
            return sb.toString();
        } else {
            return date + SEP + status + SEP + pings + SEP + failures + SEP + lastPingTime + SEP + averagePingTime + SEP + runtime;
        }
    }

    private String formatTime(long milli) {
        long seconds = milli / 1000L;

        long days = seconds / (60 * 60 * 24);
        seconds -= days * 60 * 60 * 24;

        long hours = seconds / (60 * 60);
        seconds -= hours * (60 * 60);

        long minutes = seconds / 60;
        seconds -= minutes * 60;

        return String.format("%dd%dh%dm%ds", days, hours, minutes, seconds);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int time = DEFAULT_TIME;
        String url = DEFAULT_URL;
        String email = DEFAULT_EMAIL_ADDRESS;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-time")) {
                if (i < args.length - 1) {
                    String stime = args[++i];
                    time = Integer.parseInt(stime);
                } else {
                    usageAndQuit();
                }
            } else if (args[i].equals("-url")) {
                if (i < args.length - 1) {
                    url = args[++i];
                } else {
                    usageAndQuit();
                }
            } else if (args[i].equals("-email")) {
                if (i < args.length - 1) {
                    email = args[++i];
                } else {
                    usageAndQuit();
                }
            } else if (args[i].equals("-help")) {
                usageAndQuit();
            } else {
                usageAndQuit();
            }
        }

        Heartbeat heartBeat = new Heartbeat(time, url, email);
        heartBeat.go();
    }

    private final static void usageAndQuit() {
        System.out.println("Usage: HeartBeat [-time time] [-url url-prefix] [-email notification@example.com]");
        System.exit(0);
    }
}

class Results {

    private boolean ok;
    private long pingTime;
    private String details;

    public Results(boolean ok, long pingTime, String details) {
        this.ok = ok;
        this.pingTime = pingTime;
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public long getPingTime() {
        return pingTime;
    }

    public boolean isOk() {
        return ok;
    }
}
