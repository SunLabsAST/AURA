/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.dashboard.story;

/**
 *
 * 
 */
public class Stats {
    private long entries;
    private long feeds;
    private long users;
    private long taste;
    private float entriesPerMinute;

    public Stats(long entries, long feeds, long users, long taste, float entriesPerMinute) {
        this.entries = entries;
        this.feeds = feeds;
        this.users = users;
        this.taste = taste;
        this.entriesPerMinute = entriesPerMinute;
    }

    public long getEntries() {
        return entries;
    }

    public float getEntriesPerMinute() {
        return entriesPerMinute;
    }

    public long getFeeds() {
        return feeds;
    }

    public long getTaste() {
        return taste;
    }

    public long getUsers() {
        return users;
    }

    public String toString() {
        return 
            "entries: " + entries + " " +
            "feeds: " + feeds + " " +
            "users: " + users + " " +
            "taste: " + taste + " " +
            "entriesPerMinute: " + entriesPerMinute;
    }
}
