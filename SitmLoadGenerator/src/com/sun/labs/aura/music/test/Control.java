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
    private Monitor monitor = new Monitor(true);
    private Random rng = new Random();

    public Control(String host) throws IOException {
        sitm = new SitmAPI(host);
        fetchArtistKeys();
        fetchArtistTagKeys();
    }
    
    public String getRandomArtistKey() {
       int index = rng.nextInt(artistKeys.size());
       return artistKeys.get(index);
    }

    public String getRandomArtistTagKey() {
       int index = rng.nextInt(artistTagKeys.size());
       return artistTagKeys.get(index);
    }
    
    public SitmAPI getSitm() {
        return sitm;
    }

    public Monitor getMonitor() {
        return monitor;
    }
    
    public void dump() {
        getMonitor().dumpAllStats();
        sitm.showTimeSummary();
    }

    private void fetchArtistKeys() throws IOException {
        List<Item> artists = sitm.getArtists(5000);
        artistKeys = new ArrayList<String>();
        for (Item item : artists) {
            artistKeys.add(item.getKey());
        }
        System.out.printf("Using %d artists\n", artistKeys.size());
    }

    private void fetchArtistTagKeys() throws IOException {
        List<Item> artistTags = sitm.getArtistTags(1000);
        artistTagKeys = new ArrayList<String>();
        for (Item item : artistTags) {
            artistTagKeys.add(item.getKey());
        }
        System.out.printf("Using %d tags\n", artistTagKeys.size());
    }
}
