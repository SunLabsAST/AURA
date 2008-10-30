/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class Monitor {

    private boolean trace;
    private boolean periodicDump;
    private Map<String, Stats> statsMap = Collections.synchronizedMap(new HashMap<String, Stats>());
    private final static long DUMP_DELTA_MS = 20000L;
    private long nextDump = 0;

    public Monitor(boolean trace, boolean periodicDump) {
        this.trace = trace;
        this.periodicDump = periodicDump;
    }

    public long opStart() {
        return System.currentTimeMillis();
    }

    public void opFinish(String name, long startTime, long servletTime, boolean ok) {
        long delta = System.currentTimeMillis() - startTime;
        updateStats(name, delta, ok);
        updateStats(name + "-svlt", servletTime, ok);

        updateStats("AllStats", delta, ok);
        updateStats("AllStats-svlt", servletTime, ok);

        if (trace) {
            if (ok) {
                System.out.printf("%8d %8d ms %s\n", delta, servletTime, name);
            } else {
                System.out.printf("ERROR %s\n", name);
            }
        }

        checkForPeriodicDump();
    }

    private synchronized void checkForPeriodicDump() {
        if (periodicDump) {
            long now = System.currentTimeMillis();
            if (now >= nextDump) {
                nextDump = now + DUMP_DELTA_MS;
                dumpAllStats();
            }
        }
    }

    public void opFinish(String name, long startTime, long servletTime) {
        opFinish(name, startTime, servletTime, true);
    }

    public void opError(String name) {
        opFinish(name, 0L, 0L, false);
    }

    private void updateStats(String name, long delta, boolean ok) {
        Stats stats = getStats(name);
        synchronized (stats) {
            stats.count++;

            if (ok) {
                if (delta < stats.minTime) {
                    stats.minTime = delta;
                }

                if (delta > stats.maxTime) {
                    stats.maxTime = delta;
                }

                stats.sumTime += delta;
            } else {
                stats.errors++;
            }
        }
    }

    private synchronized Stats getStats(String name) {
        Stats stats = statsMap.get(name);
        if (stats == null) {
            stats = new Stats();
            statsMap.put(name, stats);
        }
        return stats;
    }

    public void dumpAllStats() {
        List<String> keys = new ArrayList<String>(statsMap.keySet());
        Collections.sort(keys);
        System.out.printf("%8s %8s %8s %8s %8s %s\n",
                "Count", "AvgTime", "Min", "Max", "Errs", "Operation");
        for (String key : keys) {
            Stats stats = getStats(key);
            if (stats.count > 0) {
                System.out.printf("%8d %8d %8d %8d %8d %s\n",
                        stats.count,
                        stats.sumTime / stats.count,
                        stats.minTime, stats.maxTime,
                        stats.errors, key);
            }
        }
    }
}

class Stats {

    int count;
    int errors;
    long minTime;
    long maxTime;
    long sumTime;

    Stats() {
        count = 0;
        errors = 0;
        minTime = Long.MAX_VALUE;
        maxTime = -Long.MAX_VALUE;
        sumTime = 0;
    }
}