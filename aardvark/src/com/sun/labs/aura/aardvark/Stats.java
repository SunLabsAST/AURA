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

    public Stats(String version, long numUsers, long numItems, long numAttentionData) {
        this.version = version;
        this.numUsers = numUsers;
        this.numItems = numItems;
        this.numAttentionData = numAttentionData;
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
}
