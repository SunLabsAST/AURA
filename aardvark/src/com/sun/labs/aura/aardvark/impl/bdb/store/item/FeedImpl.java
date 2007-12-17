package com.sun.labs.aura.aardvark.impl.bdb.store.item;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Feed;
import java.util.SortedSet;

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
     * The last time this feed has been pulled
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected long lastPullTime;
    
    /**
     * The next time this feed will be pulled
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected long nextPullTime;

    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected long feedAddedTime;
    
    /**
     * Number of times this feed has been pulled. 
     * This is a persistent field.
     */
    protected long numPulls;
    
    /**
     * Number of errors encountered while pulling this feed.
     * This is a persistent field.
     */
    protected long numErrors;
    
    /**
     * Number of consecutive errors encountered while pulling this feed.
     * This is a persistent field.
     */
    protected int numConsecutiveErrors;
    
    /**
     * The number of links to this feed encountered while crawling
     */
    protected int numExternalLinks;
    
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
        feedAddedTime = System.currentTimeMillis();
    }
    
    public SortedSet<Entry> getEntries() {
        return bdb.getAllEntriesForFeed(getID());
    }
    
    @Override
    public String getTypeString() {
        return Feed.ITEM_TYPE;
    }

    public long getLastPullTime() {
        return lastPullTime;
    }

    public void setLastPullTime(long time) {
        lastPullTime = time;
    }

    @Deprecated
    public long getNextPullTime() {
        return nextPullTime;
    }

    @Deprecated
    public void setNextPullTime(long time) {
        nextPullTime = time;
    }

    public long getNumPulls() {
        return numPulls;
    }

    public void setNumPulls(long num) {
        numPulls = num;
    }

    public long getNumErrors() {
        return numErrors;
    }

    public void setNumErrors(long num) {
        numErrors = num;
    }

    public int getNumConsecutiveErrors() {
        return numConsecutiveErrors;
    }

    public void setNumConsecutiveErrors(int num) {
        numConsecutiveErrors = num;
    }

    public int getNumExternalLinks() {
        return numExternalLinks;
    }

    public void setNumExternalLinks(int num) {
        numExternalLinks = num;
    }

    public long getTimeStamp() {
        return feedAddedTime;
    }

    public void setTimeStamp(long timeStamp) {
        this.feedAddedTime = timeStamp;
    }

}
