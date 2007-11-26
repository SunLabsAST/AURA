/*
 * User.java
 * 
 * Created on Oct 25, 2007, 4:53:12 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.store.item;

import java.net.URL;

/**
 * A User is a type of item that has some specific data associated with it.
 * 
 * @author ja151348
 */
public interface User extends Item {
    /** A unique string that each Item must define to identify its own type */
    public final static String ITEM_TYPE = "AardvarkUser";


    /**
     * Gets the unique hard-to-discover key that was generated to be used as
     * part of the URL for any of this User's content
     * 
     * @return the RFeedKey
     */
    public String getRecommenderFeedKey();
    
    /**
     * Sets the unique hard-to-discover key used as part of the URL for any
     * of this User's content
     * 
     * @param newKey the key that should be in the URL
     */
    public void setRecommenderFeedKey(String newKey);

    /**@deprecated*/
    public void setStarredItemFeedURL(URL newURL);

    /**@deprecated*/
    public URL getStarredItemFeedURL();
}
