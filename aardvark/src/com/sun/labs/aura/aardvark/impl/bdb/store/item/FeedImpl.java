package com.sun.labs.aura.aardvark.impl.bdb.store.item;

import com.sleepycat.persist.model.Persistent;
import com.sun.labs.aura.aardvark.store.item.Feed;

/**
 * A persistent implementation of a Feed via the Berkeley DB Java Edition
 * direct persistent layer.
 */
@Persistent
public class FeedImpl extends ItemImpl implements Feed {

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
}
