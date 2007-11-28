
package com.sun.labs.aura.aardvark.store.item;

/**
 * Represents an ATOM Feed.  The key of the feed should be its URL.
 */
public interface Feed extends Item {
    /** A unique string that each Item must define to identify its own type */
    public final static String ITEM_TYPE = "AardvarkFeed";

    /**
     * Gets the time that this feed was last pulled.
     * 
     * @return the time in milliseconds since the Java epoch
     */
    public long getLastPullTime();

    /**
     * Sets the time that this feed was last pulled.
     * 
     * @param time the time that this feed was last pulled
     */
    public void setLastPullTime(long time);
    
    /**
     * Gets the time at which this feed should next be pulled.
     * 
     * @return the time in milliseconds since the Java epoch
     * @deprecated 
     */
    public long getNextPullTime();

    /**
     * Sets the time at which this feed should next be pulled.
     * 
     * @param time the time in milliseconds since the Java epoch
     * @deprecated
     */
    public void setNextPullTime(long time);
    
    /**
     * Gets the number of times this feed has been pulled.
     * 
     * @return the number of times this feed has been pulled
     */
    public long getNumPulls();
    
    /**
     * Sets the number of times this feed has been pulled.
     * 
     * @param num the number of times this feed has been pulled
     */
    public void setNumPulls(long num);
    
    /**
     * Gets the number of errors encountered while pulling this feed
     * 
     * @return the number of errors encountered while pulling this feed
     */
    public long getNumErrors();
    
    /**
     * Sets the number of errors encountered while pulling this feed
     * 
     * @param num the number of errors encountered while pulling this feed
     */
    public void setNumErrors(long num);
    
    /**
     * Gets the number of consecutive errors encountered while pulling
     * this feed.
     * 
     * @return the number of consecutive errors
     */
    public int getNumConsecutiveErrors();
    
    /**
     * Sets the number of consecutive errors encountered while pulling
     * this feed.
     * 
     * @param num the number of consecutive errors
     */
    public void setNumConsecutiveErrors(int num);
    
    /**
     * Gets the number of links to this feed that have been found
     * 
     * @return the number of links to this feed that have been found
     */
    public int getNumExternalLinks();
    
    /**
     * Sets the number of links to this feed that have been found
     * 
     * @param num the number of links to this feed that have been found
     */
    public void setNumExternalLinks(int num);
}
