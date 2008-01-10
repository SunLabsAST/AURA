package com.sun.labs.aura.aardvark.store.item;

import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.util.AuraException;
import java.util.Set;
import java.util.SortedSet;

/**
 * A User is a type of item that has some specific data associated with it.
 */
public interface User extends Item {
    /** A unique string that each Item must define to identify its own type */
    public final static String ITEM_TYPE = "AardvarkUser";


    /**
     * Gets the unique hard-to-discover key that was generated to be used as
     * part of the URL for any of this User's content
     * 
     * @return the RFeedKey
     */
    public String getRecommenderFeedKey();
    
    /**
     * Sets the unique hard-to-discover key used as part of the URL for any
     * of this User's content
     * 
     * @param newKey the key that should be in the URL
     */
    public void setRecommenderFeedKey(String newKey);

    /**
     * Adds a feed for this user.  This is a convenience method that is
     * equivalent to creating an Attention object and passing it to the
     * Item Store's attend method.
     * 
     * @param f the feed to be added
     * @param type the type of attention to be associated with the feed
     */
    public void addFeed(Feed f, Attention.Type type) throws AuraException;

    
    /**
     *  Gets the last time that the feeds for this user was fetched.
     * @return the time the attention was applied, in milliseconds since the
     *         Java epoch (Jan 1, 1970 
     */
    public long getLastFetchTime();

    /**
     * Sets the last time that the feeds for this user was fetched.
     * @param lastFetchTime  the time the attention was applied, in milliseconds since the
     *         Java epoch (Jan 1, 1970 
     */
    public void setLastFetchTime(long lastFetchTime);

}
