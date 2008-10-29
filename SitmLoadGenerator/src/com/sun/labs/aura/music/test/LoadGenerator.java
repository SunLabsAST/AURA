/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.test;

import java.io.IOException;
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        int users = 1000;
        int threads = 100;
        int time = 60;
        String host = "http://www.tastekeeper.com/api/";
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

            if (args[i].equals("-help")) {
                System.out.println("Usage: LoadGenerator [-users n] [-threads threads] [-time time] [-url url-prefix]");
                System.exit(0);
            }
        }

        LoadGenerator loadGen = new LoadGenerator(users, threads, host);
        loadGen.go(time); 
    }


    public LoadGenerator(int users, int threads, String host) throws IOException {
        control = new Control(host);
        numThreads = threads;
        createUsers(users);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                control.dump();
            }
        });
    }


    public void createUsers(int users) {
        for (int i = 0; i < users; i++) {
            enqueueUser(new User("user-" + i, control));
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
        System.out.println("starting " + Thread.currentThread().getName());
        while (System.currentTimeMillis() < endTime ) {
            simulateNextUser();
        }
        System.out.println("finished " + Thread.currentThread().getName());
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
}
