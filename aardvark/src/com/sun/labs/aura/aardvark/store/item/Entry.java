
/*
 * Entry.java
 * 
 * Created on Oct 25, 2007, 5:22:51 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.store.item;

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
    public void setSyndEntry(SyndEntry syndEntry);

    /**
     * Gets the RSS entry contents
     *
     * @return the RSS entry
     */
    public SyndEntry getSyndEntry();
}
