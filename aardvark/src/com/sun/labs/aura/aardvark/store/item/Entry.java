
/*
 * Entry.java
 * 
 * Created on Oct 25, 2007, 5:22:51 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.store.item;

import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * Represents an Entry in an ATOM Feed
 * 
 * @author ja151348
 */
public interface Entry extends Item {
    /** A unique string that each Item must define to identify its own type */
    public final static String ITEM_TYPE = "AardvarkEntry";

    /**
     * Gets the content of this Entry
     * 
     * @return the content of the entry
     */
    public String getContent();
    
    /**
     * Set the content of this Entry
     * 
     * @param content the content of the entry
     */
    public void setContent(String content);

    /**
     * Sets the RSS entry contents
     * 
     * @param syndEntry the RSS entry
     */
    public void setSyndEntry(SyndEntry syndEntry) throws AuraException;

    /**
     * Gets the RSS entry contents
     *
     * @return the RSS entry
     */
    public SyndEntry getSyndEntry() throws AuraException;

    /**
     * Gets the time stamp associated with this entry (when it was posted).
     * 
     * @return the time stamp in milliseconds since the Java epoch
     */
    public long getTimeStamp();
    
    /**
     * Sets the time associated with this entry (when it was posted).
     * 
     * @param timeStamp the time in milliseconds since the Java epoch
     */
    public void setTimeStamp(long timeStamp);
}
