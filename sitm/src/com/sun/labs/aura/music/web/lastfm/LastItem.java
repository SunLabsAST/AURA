/*
 * LastItem.java
 *
 * Created on Oct 22, 2007, 5:52:56 AM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.lastfm;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author plamere
 */
public class LastItem implements Serializable {
    private String name;
    private String mbid;
    private int frequency;
    
    public final static Comparator<LastItem> FREQ_ORDER = new Comparator<LastItem>() {
        public int compare(LastItem o1, LastItem o2) {
            return o1.getFreq() - o2.getFreq();
        }
    };

    public final static Comparator<LastItem> ALPHA_ORDER = new Comparator<LastItem>() {
        public int compare(LastItem o1, LastItem o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };

    public LastItem(String name, String mbid, int frequency) {
        this.name = name;
        this.frequency = frequency;
        this.mbid = mbid;
    }

    public LastItem(String name, int frequency) {
        this.name = name;
        this.frequency = frequency;
    }

    public int getFreq() {
        return frequency;
    }

    public String getName() {
        return name;
    }

    public String getMBID() {
        return mbid;
    }

    public String toString() {
        return name + " " + getFreq();
    }
}