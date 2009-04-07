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
        Stats gstats = getStats("AllSummaryStats");
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
