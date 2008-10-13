/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.crawler;

/**
 *
 * @author plamere
 */
public class FixedPeriod {
    private long period;
    private long start;

    FixedPeriod(long period) {
        this.period = period;
    }

    void start() {
        start = System.currentTimeMillis();
    }

    void end() throws InterruptedException {
        long delta = System.currentTimeMillis() - start;
        long delay = period - delta;
        if (delay > 0) {
            Thread.sleep(delay);
        }
        start = 0;
    }
}
