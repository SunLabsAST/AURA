/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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

import com.sun.labs.aura.music.test.rmi.SitmAPIDirectImpl;
import com.sun.labs.aura.music.webservices.api.SitmAPI;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.DelayQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class LoadGenerator {
    private int numThreads = 100;
    private DelayQueue<User> queue = new DelayQueue<User>();
    private long endTime;
    private Control control;
    private boolean readOnly = true;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        int users = 1000;
        int threads = 100;
        int time = 60;
        boolean readOnly = true;
        boolean summary = false;
        String host = "http://www.tastekeeper.com/api/";
        String commType = "ws"; // ws or rmi
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-users")) {
                if (i < args.length - 1) {
                    String susers = args[++i];
                    users =  Integer.parseInt(susers);
                }
            }
            if (args[i].equals("-threads")) {
                if (i < args.length - 1) {
                    String sthreads = args[++i];
                    threads =  Integer.parseInt(sthreads);
                }
            }

            if (args[i].equals("-time")) {
                if (i < args.length - 1) {
                    String stime = args[++i];
                    time =  Integer.parseInt(stime);
                }
            }

            if (args[i].equals("-url")) {
                if (i < args.length - 1) {
                    host = args[++i];
                }
            }

            if (args[i].equals("-writeOK")) {
                readOnly = false;
            }

            if (args[i].equals("-summary")) {
                summary = true;
            }

            if (args[i].equals("-comm")) {
                if (i < args.length - 1) {
                    commType = args[++i];
                    if (!(commType.equals("ws") || commType.equals("rmi"))) {
                        usage();
                        System.exit(0);
                    }
                }
            }

            if (args[i].equals("-help")) {
                usage();
                System.exit(0);
            }
        }

        LoadGenerator loadGen= new LoadGenerator(users, threads, host, readOnly, summary, commType);
        loadGen.go(time); 
    }

    public static void usage() {
        System.out.println("Usage: LoadGenerator [-users n] [-threads threads] [-time time] [-url url-prefix] [-summary] [-writeOK] [-comm rmi|ws]");
    }

    public LoadGenerator(final int users, final int threads, final String url, boolean readOnly, boolean summary, String commType) throws IOException {
        System.out.println("URL " + url + " users " + users + " threads " + threads + " commType " + commType);
        if (commType.equals("ws")) {
            control = new Control(SitmAPI.getSitmAPI(url, false, true, !summary));
        } else if (commType.equals("rmi")) {
            URL config = LoadGenerator.class.getResource("/com/sun/labs/aura/music/test/rmi/loadGenConfig.xml");
            if (config == null) {
                System.out.println("Failed to find config for data store use");
                System.exit(0);
            }
            control = new Control(new SitmAPIDirectImpl(config, !summary));
        }
        numThreads = threads;
        this.readOnly = readOnly;
        createUsers(users);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                control.dump();
                System.out.println("URL " + url + " users " + users + " threads " + threads);
            }
        });
    }



    public void createUsers(int users) {
        for (int i = 0; i < users; i++) {
            enqueueUser(new User("user-" + i, control, readOnly));
        }
    }

    public void go(long seconds) {
        endTime = System.currentTimeMillis() + seconds * 1000L;

        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread() {
                public void run() {
                    simulate();
                }
            };
            t.setName("LoadGen-thread-" + i);
            t.start();
        }
    }
    
    public void simulate() {
        trace("starting " + Thread.currentThread().getName());
        while (System.currentTimeMillis() < endTime ) {
            simulateNextUser();
        }
        trace("finished " + Thread.currentThread().getName());
    }

    public void simulateNextUser() {
        User user = getNextUser();
        if (user != null) {
            user.simulate();
            enqueueUser(user);
        }
    }

    public User getNextUser() {
        try {
            return queue.take();
        } catch (InterruptedException ex) {
            Logger.getLogger(LoadGenerator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void enqueueUser(User user) {
        queue.put(user);
    }

    private void trace(String msg) {
        if (false) {
            System.out.println(msg);
        }
    }
}
