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

import com.sun.labs.aura.music.webservices.api.Item;
import com.sun.labs.aura.music.webservices.api.SitmAPI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author plamere
 */
public class Control {
    private SitmAPI sitm;
    private List<String> artistKeys; 
    private List<String> artistTagKeys; 
    private List<String> artistNames; 
    private List<String> artistTagNames; 
    private Monitor monitor = new Monitor(false);
    private Random rng = new Random();
    private long lateSum;
    private long lateCount;
    private long lastLateCount;

    public Control(SitmAPI sitm) throws IOException {
        this.sitm = sitm;
        fetchArtists();
        fetchArtistTags();
        reset();
    }


    public void reset() {
        sitm.resetStats();
        monitor.reset();
    }
    
    public String getRandomArtistKey() {
       int index = rng.nextInt(artistKeys.size());
       return artistKeys.get(index);
    }

    public String getRandomArtistName() {
       int index = rng.nextInt(artistNames.size());
       return artistNames.get(index);
    }

    public String getRandomArtistTagKey() {
       int index = rng.nextInt(artistTagKeys.size());
       return artistTagKeys.get(index);
    }

    public String getRandomArtistTagName() {
       int index = rng.nextInt(artistTagNames.size());
       return artistTagNames.get(index);
    }

    public String getRandomWordCloud() {
        int num = rng.nextInt(10) + 5;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < num; i++) {
            String tag = getRandomArtistTagName();
            float weight = rng.nextFloat();
            boolean banned = rng.nextFloat() > .8;
            boolean sticky = rng.nextFloat() > .8;

            // about %20 of the time, tags will be banned
            // about %20 of the time when they are not banned, they will be sticky
            //
            String prefix = "";
            if (banned) {
                prefix = "-";
                weight = -weight;
            } else if (sticky) {
                prefix = "+";
            }
            //prefix = "";
            sb.append(String.format("(%s%s,%.3f)", prefix, tag, weight));
        }
        return sb.toString();
    }
    
    public SitmAPI getSitm() {
        return sitm;
    }

    public Monitor getMonitor() {
        return monitor;
    }
    
    public void dump() {
        sitm.showStats();
        getMonitor().dumpAllStats();
        System.out.printf("Late calls: %d  Total Late delay: %d\n", lateCount, lateSum);
        if (lateCount > lastLateCount) {
            System.out.printf("WARNING - simulator is overloaded\n");
            lastLateCount = lateCount;
        }
    }

    public synchronized void late(long late) {
        lateSum += late;
        lateCount++;
    }

    private void fetchArtists() throws IOException {
        List<Item> artists = sitm.getArtists(5000);
        artistKeys = new ArrayList<String>();
        artistNames = new ArrayList<String>();
        for (Item item : artists) {
            artistKeys.add(item.getKey());
            artistNames.add(item.getName());
        }
        //System.out.printf("Using %d artists\n", artistKeys.size());
    }

    private void fetchArtistTags() throws IOException {
        List<Item> artistTags = sitm.getArtistTags(1000);
        artistTagKeys = new ArrayList<String>();
        artistTagNames = new ArrayList<String>();
        for (Item item : artistTags) {
            artistTagKeys.add(item.getKey());
            artistTagNames.add(item.getName());
        }
        //System.out.printf("Using %d tags\n", artistTagKeys.size());
    }
}
