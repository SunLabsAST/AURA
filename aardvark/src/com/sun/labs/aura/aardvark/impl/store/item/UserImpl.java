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
    
    protected final static String STARRED_URL = "starredURL";
    
    protected final static String LAST_FETCH = "lastFetch";
    
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
    
    public URL getStarredItemFeedURL() {
        try {
            return new URL(getField(STARRED_URL));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public void setStarredItemFeedURL(URL newURL) {
        setField(STARRED_URL, newURL.toString());
    }

    public long getLastFetchTime() {
        String lf = getField(LAST_FETCH);
        return Long.valueOf(lf);
    }

    public void setLastFetchTime(long lastFetchTime) {
        setField(LAST_FETCH, Long.toString(lastFetchTime));
    }

}
