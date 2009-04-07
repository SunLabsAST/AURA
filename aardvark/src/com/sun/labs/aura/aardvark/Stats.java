/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
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
    private double entriesPerMin;

    public Stats(String version, long numUsers, long numEntries, long numAttentionData, 
                long numFeeds, long feedPullCount, long feedErrorCount,
                double entriesPerMin) {
        this.version = version;
        this.numUsers = numUsers;
        this.numEntries = numEntries;
        this.numAttentionData = numAttentionData;
        this.numFeeds = numFeeds;
        this.feedPullCount = feedPullCount;
        this.feedErrorCount = feedErrorCount;
        this.entriesPerMin = entriesPerMin;
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

    public double getEntriesPerMin() {
        return entriesPerMin;
    }

    public void setEntriesPerMin(double entriesPerMin) {
        this.entriesPerMin = entriesPerMin;
    }
}
