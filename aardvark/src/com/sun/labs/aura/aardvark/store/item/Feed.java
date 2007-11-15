/*
 * Feed.java
 * 
 * Created on Oct 25, 2007, 5:27:43 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.store.item;

/**
 * Represents an ATOM Feed.  The key of the feed should be its URL.
 * 
 * @author ja151348
 */
public interface Feed extends Item {
    /** A unique string that each Item must define to identify its own type */
    public final static String ITEM_TYPE = "AardvarkFeed";

}
