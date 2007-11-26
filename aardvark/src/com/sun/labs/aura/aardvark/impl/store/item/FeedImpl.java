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

    public long getLastPullTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getNextPullTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getNumPulls() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getNumErrors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getNumConsecutiveErrors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getNumExternalLinks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLastPullTime(long time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNextPullTime(long time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNumPulls(long num) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNumErrors(long num) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNumConsecutiveErrors(int num) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNumExternalLinks(int num) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
