package com.sun.labs.aura.aardvark.impl.bdb.store.item;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.util.FeedUtils;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * Implements a persistent Entry class via the Berkeley DB Java Edition
 * direct persistence layer.
 * 
 */
@Persistent
public class EntryImpl extends ItemImpl implements Entry {

    /**
     * The isEntry field exists only so that we can do a DB query to 
     * fetch only those elements that are entries.  This will be faster
     * than having to fetch all Items and check each for Entry.
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected boolean isEntry = true;

    /**
     * The parent feed from which this entry was derived.
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE,
                  /*relatedEntity=ItemImpl.class,*/
                  onRelatedEntityDelete=DeleteAction.CASCADE)
    private long parentFeedID;
    
    /**
     * The date & time at which this entry was posted
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private long postDate;
    
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private long entryAddedTime;
    
    /**
     * The content of this entry.  This is a persistent field.
     */
    protected String content;

    /**
     * The XML data from the synd entry.  This is a persistent field.
     */
    protected String syndEntryXML;
    
    /**
     * The URL of this entry.  This is a persistent field.
     */
    protected String entryURL;
    
    /**
     * A cached instantiated version of this syndicatin entry.
     */
    protected transient SyndEntry cachedEntry;
    
    /**
     * All persistent classes must have a default constructor.
     */
    protected EntryImpl() {
    }
    
    /**
     * Instantiates an Entry with a particular key
     *
     * @param key the key for this entry
     */
    public EntryImpl(String key) {
        super(key);
        entryAddedTime = System.currentTimeMillis();
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets the syndication entry for this entry, also transforming it to
     * XML.
     * 
     * @param syndEntry the feed entry
     */
    public void setSyndEntry(SyndEntry syndEntry) throws AuraException {
        this.cachedEntry = syndEntry;
        this.syndEntryXML = FeedUtils.toString(syndEntry);
    }

    public SyndEntry getSyndEntry() throws AuraException {
        if (cachedEntry != null) {
            return cachedEntry;
        }
        cachedEntry = FeedUtils.toSyndEntry(syndEntryXML);
        return cachedEntry;
    }
    
    public long getParentFeedID() {
        return parentFeedID;
    }
    
    public void setParentFeedID(long id) {
        this.parentFeedID = id;
    }

    
    public String getEntryURL() {
        return entryURL;
    }

    public void setEntryURL(String url) {
        this.entryURL = url;
    }

    
    @Override
    public String getTypeString() {
        return Entry.ITEM_TYPE;
    }
    
    public long getPostDate() {
        return postDate;
    }

    public void setPostDate(long date) {
        this.postDate = date;
    }
    
    public long getTimeStamp() {
        return entryAddedTime;
    }

    public void setTimeStamp(long timeStamp) {
        this.entryAddedTime = timeStamp;
    }

}
