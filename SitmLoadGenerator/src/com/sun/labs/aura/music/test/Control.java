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
    private List<String> keys; 
    private Monitor monitor = new Monitor(true);
    private Random rng = new Random();

    public Control(String host) throws IOException {
        sitm = new SitmAPI(host);
        fetchArtistKeys();
    }
    
    public String getRandomArtistKey() {
       int index = rng.nextInt(keys.size());
       return keys.get(index);
    }
    
    public SitmAPI getSitm() {
        return sitm;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    private void fetchArtistKeys() throws IOException {
        List<Item> artists = sitm.getArtists(5000);
        keys = new ArrayList<String>();
        for (Item item : artists) {
            keys.add(item.getKey());
        }
        System.out.printf("Using %d artists\n", keys.size());
    }
}
