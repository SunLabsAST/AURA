/*
 * Item.java
 *
 * Created on Oct 22, 2007, 5:52:56 AM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.lastfm;

/**
 *
 * @author plamere
 */
public class Item implements Comparable<Item> {

    private String name;
    private int frequency;

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

    public int compareTo(Item o) {
        return getFreq() - o.getFreq();
    }

    public String toString() {
        return name + " " + getFreq();
    }
}