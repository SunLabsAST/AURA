/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.test;

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
    private Map<String, Stats> statsMap = Collections.synchronizedMap(new HashMap<String, Stats>());

    public Monitor(boolean trace) {
        this.trace = trace;
    }

    public void reset() {
        statsMap.clear();
    }

    public long opStart() {
        return System.currentTimeMillis();
    }

    public void opFinish(String who, String name, long startTime, boolean ok) {

        Stats stats = getStats(name);
        Stats gstats = getStats("AllStats");
        stats.count++;
        gstats.count++;

        if (ok) {
            long delta = System.currentTimeMillis() - startTime;
            if (delta < stats.minTime) {
                stats.minTime = delta;
            }

            if (delta < gstats.minTime) {
                gstats.minTime = delta;
            }

            if (delta > stats.maxTime) {
                stats.maxTime = delta;
            }

            if (delta > gstats.maxTime) {
                gstats.maxTime = delta;
            }

            stats.sumTime += delta;
            gstats.sumTime += delta;
            if (trace) {
                System.out.printf("%10s %8d ms %s\n", who, delta, name);
            }
        } else {
            stats.errors++;
            gstats.errors++;
            if (trace) {
                System.out.printf("%10s ERROR %s\n", who, name);
            }
        }


    }

    private Stats getStats(String name) {
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
            System.out.printf("%8d %8d %8d %8d %8d %s\n",
                    stats.count,
                    stats.sumTime / stats.count,
                    stats.minTime, stats.maxTime,
                    stats.errors, key);
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