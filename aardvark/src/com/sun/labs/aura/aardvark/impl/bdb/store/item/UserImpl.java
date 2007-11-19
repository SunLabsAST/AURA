package com.sun.labs.aura.aardvark.impl.bdb.store.item;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.impl.bdb.store.BerkeleyItemStore;
import com.sun.labs.aura.aardvark.impl.bdb.store.PersistentAttention;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.SimpleAttention;
import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

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
     * Feeds that this user is interested in.  If storing feeds via attn
     * proves to be too slow or too messy, we'll add a direct list here.
     * This field is persistent.
     */
    //protected HashSet<String> interestedFeeds;
    
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

    /**
     * Get all the feeds associated with this user that have a particular
     * attention type in the association.
     * 
     * @param type the type of attention to get feeds for
     * @return the feeds
     */
    public Set<Feed> getFeeds(Attention.Type type) {
        Set<Feed> feeds = new HashSet<Feed>();
        
        Set<PersistentAttention> attns = bdb.getAttentionForUser(getID(), type);
        for (PersistentAttention attn : attns) {
            FeedImpl feed = (FeedImpl)bdb.getItem(attn.getItemID());
            feeds.add(feed);
        }
        return feeds;
    }
    
    /**
     * Adds a feed for this user.  This is a convenience method that is
     * equivalent to creating an Attention object and passing it to the
     * Item Store's attend method.
     * 
     * @param f the feed to be added
     * @param type the type of attention to be associated with the feed
     */
    public void addFeed(Feed f, Attention.Type type) throws AuraException {
        if (f.getID() <= 0) {
            throw new AuraException("Feed must be made persistent before it "+
                    "can be added to a user");
        }
        SimpleAttention attn = new SimpleAttention(this, f, type);
        BerkeleyItemStore store = BerkeleyItemStore.getItemStore();
        store.attend(attn);
    }
    
    @Deprecated
    public URL getStarredItemFeedURL() {
        URL feed;
        try {
            if (starredItemFeed != null) {
                feed = new URL(starredItemFeed);
            } else {
                feed = null;
            }
        } catch (MalformedURLException e) {
            feed = null;
        }
        return feed;
    }

    @Deprecated
    public void setStarredItemFeedURL(URL newURL) {
        if (newURL != null) {
            starredItemFeed = newURL.toString();
        } else {
            starredItemFeed = null;
        }
    }

    public long getLastFetchTime() {
        if (lastFetchTime != null) {
            return lastFetchTime;
        }
        return 0;
    }

    public void setLastFetchTime(long lastFetchTime) {
        this.lastFetchTime = lastFetchTime;
    }

    public String getTypeString() {
        return User.ITEM_TYPE;
    }
}
