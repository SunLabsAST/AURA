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

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class PerformanceTimer {

    static boolean enabled = false;
    static Map<String, TimeStat> timeMap = new LinkedHashMap<String, TimeStat>();

    public static boolean isEnabled() {
        return enabled;
    }

    public static void start(String name) {
        if (enabled) {
            TimeStat ts = get(name);
            if (ts.startTime != 0) {
                ts.errorCount++;
            } else {
                ts.startTime = System.currentTimeMillis();
            }
        }
    }

    public static void stop(String name) {
        if (enabled) {
            TimeStat ts = get(name);
            if (ts.startTime == 0) {
                ts.errorCount++;
            } else {
                ts.lastTime = System.currentTimeMillis() - ts.startTime;
                ts.totalTime += ts.lastTime;
                ts.startTime = 0L;
                ts.count++;
            }
        }
    }

    public static void reset() {
        if (enabled) {
            timeMap.clear();
        }
    }

    public static void showPopup() {
        if (enabled) {
            PopupPanel pp = new StatsPopup(getReportAsHTML());
            pp.center();
        }
    }

    private static TimeStat get(String name) {
        TimeStat ts = timeMap.get(name);
        if (ts == null) {
            ts = new TimeStat(name);
            timeMap.put(name, ts);
        }
        return ts;
    }

    private static Collection<TimeStat> getAllStats() {
        return timeMap.values();
    }

    public static String getReportAsHTML() {
        String rep = "<table><tr><tn>Name<th>Count<th>Total Time<th>Last Time<th>Error Count<th>Avg Time";

        for (TimeStat ts : getAllStats()) {
            rep += ts.toHTML();
        }
        rep += "</table>";
        return rep;
    }

    public static void dumpReport() {
        for (TimeStat ts : getAllStats()) {
            System.out.println(ts);
        }
    }
}

class TimeStat {

    String name;
    int count;
    long totalTime;
    long startTime;
    long lastTime;
    int errorCount;

    TimeStat(String name) {
        this.name = name;
    }

    public String toString() {
        long avgTime = count == 0 ? 0 : totalTime / count;
        return name + " " + count + " " + lastTime + " " + totalTime + " " + errorCount + " " + avgTime;
    }

    public String toHTML() {
        long avgTime = count == 0 ? 0 : totalTime / count;
        return "<tr><th>" + name + "<td> " + count + "<td> " + totalTime + "<td> " + lastTime + "<td>"+ errorCount + "<td>" + avgTime;
    }
}

class StatsPopup extends PopupPanel {

    StatsPopup(String html) {
        super(true);
        FlowPanel container = new FlowPanel();
        container.setStyleName("performancePopup");
        Panel p = new HTMLPanel(html);
        p.setStyleName("performancePopup");
        container.add(p);
        Button reset = new Button("reset stats");
        reset.addClickListener(new ClickListener() {

            public void onClick(Widget arg0) {
                PerformanceTimer.reset();
                StatsPopup.this.hide();
            }
        });
        container.add(reset);
        setWidget(container);
    }
}
