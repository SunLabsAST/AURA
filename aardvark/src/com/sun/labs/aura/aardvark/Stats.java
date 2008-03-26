/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark;

import java.io.Serializable;

/**
 * Aardvark stats
 */
public class Stats implements Serializable {
    private String version;
    private long numUsers;
    private long numEntries;
    private long numAttentionData;
    private long numFeeds;
    private long feedPullCount;
    private long feedErrorCount;

    public Stats(String version, long numUsers, long numEntries, long numAttentionData, 
                long numFeeds, long feedPullCount, long feedErrorCount) {
        this.version = version;
        this.numUsers = numUsers;
        this.numEntries = numEntries;
        this.numAttentionData = numAttentionData;
        this.numFeeds = numFeeds;
        this.feedPullCount = feedPullCount;
        this.feedErrorCount = feedErrorCount;
    }

    public long getNumAttentionData() {
        return numAttentionData;
    }

    public long getNumEntries() {
        return numEntries;
    }

    public long getNumUsers() {
        return numUsers;
    }

    public String getVersion() {
        return version;
    }

    public long getFeedErrorCount() {
        return feedErrorCount;
    }

    public long getFeedPullCount() {
        return feedPullCount;
    }

    public long getNumFeeds() {
        return numFeeds;
    }

    @Override
    public String toString() {
        return "Users: " + getNumUsers() 
                + "  Entries: " + getNumEntries()  
                + "  Taste: " + getNumAttentionData() 
                + "  Feeds: " + getNumFeeds()
                + "  Pulls: " + getFeedPullCount()
                + "  Feed Errors: " + getFeedErrorCount();
    }
}
