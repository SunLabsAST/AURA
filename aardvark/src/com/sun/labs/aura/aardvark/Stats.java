/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark;

/**
 *
 * @author plamere
 */
public class Stats {
    private String version;
    private long numUsers;
    private long numItems;
    private long numAttentionData;
    private int numFeeds;
    private int feedPullCount;
    private int feedErrorCount;

    public Stats(String version, long numUsers, long numItems, long numAttentionData, int numFeeds, int feedPullCount, int feedErrorCount) {
        this.version = version;
        this.numUsers = numUsers;
        this.numItems = numItems;
        this.numAttentionData = numAttentionData;
        this.numFeeds = numFeeds;
        this.feedPullCount = feedPullCount;
        this.feedErrorCount = feedErrorCount;
    }

    public long getNumAttentionData() {
        return numAttentionData;
    }

    public long getNumItems() {
        return numItems;
    }

    public long getNumUsers() {
        return numUsers;
    }

    public String getVersion() {
        return version;
    }

    public int getFeedErrorCount() {
        return feedErrorCount;
    }

    public int getFeedPullCount() {
        return feedPullCount;
    }

    public int getNumFeeds() {
        return numFeeds;
    }

    @Override
    public String toString() {
        return "Users: " + getNumUsers() 
                + "  Items: " + getNumItems()  
                + "  Taste: " + getNumAttentionData() 
                + "  Feeds: " + getNumFeeds()
                + "  Pulls: " + getFeedPullCount()
                + "  Feed Errors: " + getFeedErrorCount();
    }
}
