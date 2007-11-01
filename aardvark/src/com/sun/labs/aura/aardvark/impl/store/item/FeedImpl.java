/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.store.item;

import com.sun.labs.aura.aardvark.store.item.Feed;

/**
 * An Item class that represents a feed
 * @author ja151348
 */
public class FeedImpl extends ItemImpl implements Feed {

    public FeedImpl(long itemID, String key) {
        super(itemID, key);
    }
    
    public static String getType() {
        return "AardvarkFeed";
    }
}
