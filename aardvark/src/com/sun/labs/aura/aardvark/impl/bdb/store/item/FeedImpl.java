package com.sun.labs.aura.aardvark.impl.bdb.store.item;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.store.item.Feed;

/**
 * A persistent implementation of a Feed via the Berkeley DB Java Edition
 * direct persistent layer.
 */
@Persistent
public class FeedImpl extends ItemImpl implements Feed {

    /**
     * The isFeed field exists only so that we can do a DB query to 
     * fetch only those elements that are feeds.  This will be faster
     * than having to fetch all Items and check each for Feed.
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected boolean isFeed = true;

    
    /**
     * All persistent objects must have a default constructor.
     */
    public FeedImpl() {
    }

    /**
     * Constructs a Feed with a particular key (probably a URL)
     * 
     * @param key the key for this feed
     */
    public FeedImpl(String key) {
        super(key);
    }
    
    public String getTypeString() {
        return Feed.ITEM_TYPE;
    }
}
