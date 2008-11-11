/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
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

    public Control(String host, boolean periodicDump) throws IOException {
        sitm = new SitmAPI(host, false, true, periodicDump);
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
            sb.append(String.format("(%s,%.3f)", tag, weight));
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
