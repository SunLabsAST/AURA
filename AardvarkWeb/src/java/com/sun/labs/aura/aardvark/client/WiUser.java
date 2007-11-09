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
public class WiUser implements IsSerializable {
    private String name;
    private long id;
    private String recommendedFeed;
    private int numItems;

    public WiUser(String name, long id, String recommendedFeed, int numItems) {
        this.name = name;
        this.id = id;
        this.recommendedFeed = recommendedFeed;
        this.numItems = numItems;
    }

    public WiUser() {
    }

    public long getId() {
        return id;
    }

    public int getNumItems() {
        return numItems;
    }


    public String getName() {
        return name;
    }

    public String getRecommendedFeed() {
        return recommendedFeed;
    }
}
