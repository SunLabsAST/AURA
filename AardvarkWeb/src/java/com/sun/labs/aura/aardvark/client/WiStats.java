/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class WiStats implements IsSerializable {
    private String version ;
    private long numUsers;
    private long numItems;
    private long numAttention;
    private long numFeeds;
    private int feedPulls;
    private int feedErrors;
    private long memUsed;

    public WiStats(String version, long numUsers, long numItems, long numAttention,
            long numFeeds, int feedPulls, int feedErrors, long memUsed) {
        this.version = version;
        this.numUsers = numUsers;
        this.numItems = numItems;
        this.numAttention = numAttention;
        this.numFeeds = numFeeds;
        this.feedPulls = feedPulls;
        this.feedErrors = feedErrors;
        this.memUsed = memUsed;
    }

    public WiStats() {
        version = "Error";
    }

    public long getNumAttention() {
        return numAttention;
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

    public int getFeedErrors() {
        return feedErrors;
    }

    public int getFeedPulls() {
        return feedPulls;
    }

    public long getNumFeeds() {
        return numFeeds;
    }

    public long getMemUsed() {
        return memUsed;
    }
    
}
