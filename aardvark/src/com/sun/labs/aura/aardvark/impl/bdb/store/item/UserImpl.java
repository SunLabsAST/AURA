package com.sun.labs.aura.aardvark.impl.bdb.store.item;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.store.item.User;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

/**
 * Implementation of a persistent User through the Berkeley DB Java Edition.
 */
@Persistent
public class UserImpl extends ItemImpl implements User {

    /**
     * The isUser field exists only so that we can do a DB query to 
     * fetch only those elements that are users.  This will be faster
     * than having to fetch all Items and check each for User.
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected boolean isUser = true;

    
    /**
     * The recommendation feed key unqiue to this user.
     * This field is persistent.
     */
    protected String rFeedKey;
    
    /**
     * The String representation of the URL to the User's starred items
     * feed.  This field is persistent.
     */
    protected String starredItemFeed;

    /**
     * Feeds that this user is interested in (not including starred items).
     * This field is persistent.
     */
    protected HashSet<String> interestedFeeds;
    
    /**
     * The last time data was fetched for this user.  This should probably
     * be moved to another table.
     */
    protected Long lastFetchTime;
    
    /**
     * All persisted objects must have a default constructor
     */
    public UserImpl() {
    }
    
    /**
     * Construct a user with a particular key (probably an Open ID username)
     * 
     * @param key the unqiue name of this user
     */
    public UserImpl(String key) {
        super(key);
    }
    
    public String getRecommenderFeedKey() {
        return rFeedKey;
    }

    public void setRecommenderFeedKey(String newKey) {
        this.rFeedKey = newKey;
    }

    public URL getStarredItemFeedURL() {
        URL feed;
        try {
            feed = new URL(starredItemFeed);
        } catch (MalformedURLException e) {
            feed = null;
        }
        return feed;
    }

    public void setStarredItemFeedURL(URL newURL) {
        if (newURL != null) {
            starredItemFeed = newURL.toString();
        } else {
            starredItemFeed = null;
        }
    }

    public long getLastFetchTime() {
        return lastFetchTime;
    }

    public void setLastFetchTime(long lastFetchTime) {
        this.lastFetchTime = lastFetchTime;
    }

    public String getTypeString() {
        return User.ITEM_TYPE;
    }
}
