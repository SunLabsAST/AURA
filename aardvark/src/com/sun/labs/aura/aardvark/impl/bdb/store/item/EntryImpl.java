package com.sun.labs.aura.aardvark.impl.bdb.store.item;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * Implements a persistent Entry class via the Berkeley DB Java Edition
 * direct persistence layer.
 * 
 */
@Persistent
public class EntryImpl extends ItemImpl implements Entry {

    /**
     * The content of this entry.  This is a persistent field.
     */
    protected String content;
    
    /**
     * The isEntry field exists only so that we can do a DB query to 
     * fetch only those elements that are entries.  This will be faster
     * than having to fetch all Items and check each for Entry.
     */
    @SecondaryKey(relate=Relationship.ONE_TO_ONE)
    protected boolean isEntry = true;
    
    /**
     * All persistent classes must have a default constructor.
     */
    protected EntryImpl() {
    }
    
    /**
     * Instantiates an Entry with a particular key (probaby the URL)
     *
     * @param key the key for this entry
     */
    public EntryImpl(String key) {
        super(key);
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSyndEntry(SyndEntry syndEntry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SyndEntry getSyndEntry() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getTypeString() {
        return Entry.ITEM_TYPE;
    }
}
