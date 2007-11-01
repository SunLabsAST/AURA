
/*
 * Entry.java
 * 
 * Created on Oct 25, 2007, 5:22:51 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.store.item;

/**
 * Represents an Entry in an ATOM Feed
 * 
 * @author ja151348
 */
public interface Entry extends Item {
    
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
}
