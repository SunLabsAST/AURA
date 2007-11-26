/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.store.item;

import com.sun.labs.aura.aardvark.store.item.User;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implements a user item.  Users are regular items that have a few
 * extra specific bits of info in them.
 * 
 * @author ja151348
 */
public class UserImpl extends ItemImpl implements User {

    protected final static String R_FEED_KEY = "rFeedKey";
    
    public UserImpl(long itemID, String key) {
        super(itemID, key);
    }
    
    public static String getType() {
        return "AardvarkUser";
    }
    
    public String getRecommenderFeedKey() {
        return getField(R_FEED_KEY);
    }

    public void setRecommenderFeedKey(String newKey) {
        setField(R_FEED_KEY, newKey);
    }

    public void setStarredItemFeedURL(URL newURL) {
        throw new UnsupportedOperationException("no longer supported");
    }

    public URL getStarredItemFeedURL() {
        throw new UnsupportedOperationException("no longer supported");
    }
}
