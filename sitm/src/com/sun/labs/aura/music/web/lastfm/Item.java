/*
 * Item.java
 *
 * Created on Oct 22, 2007, 5:52:56 AM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.lastfm;

import java.util.Comparator;

/**
 *
 * @author plamere
 */
public class Item /*implements Comparable<Item>*/ {

    private String name;
    private int frequency;
    
    public final static Comparator<Item> FREQ_ORDER = new Comparator<Item>() {
        public int compare(Item o1, Item o2) {
            return o1.getFreq() - o2.getFreq();
        }
    };

    public Item(String name, int frequency) {
        this.name = name;
        this.frequency = frequency;
    }

    public int getFreq() {
        return frequency;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name + " " + getFreq();
    }
}