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
    private int numUsers;
    private int numItems;
    private int numAttentionData;

    public Stats(String version, int numUsers, int numItems, int numAttentionData) {
        this.version = version;
        this.numUsers = numUsers;
        this.numItems = numItems;
        this.numAttentionData = numAttentionData;
    }

    public int getNumAttentionData() {
        return numAttentionData;
    }

    public int getNumItems() {
        return numItems;
    }

    public int getNumUsers() {
        return numUsers;
    }

    public String getVersion() {
        return version;
    }
}
