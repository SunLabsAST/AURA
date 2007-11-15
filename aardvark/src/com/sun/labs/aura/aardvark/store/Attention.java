/*
 * Attention.java
 * 
 * Created on Oct 25, 2007, 4:09:56 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.store;

/**
 * Represents some form of attention that a User can attribute to an Item.
 * 
 * @author ja151348
 */
public interface Attention {
    /**
     * The type of attention data that this Attention represents.
     */
    public enum Type {
        /** Starred in Google Reader by the user */
        STARRED,

        /** Subscribed by the user */
        SUBSCRIBED,
        
        /** Viewed by the user */
        VIEWED
    };
    
    /**
     * Gets the Aura ID of the user that applied this attention
     * 
     * @return the Aura ID of the user
     */
    public long getUserID();
    
    /**
     * Gets the Aura ID of the item that this attention was applied to
     * 
     * @return the Aura ID of the item
     */
    public long getItemID();
    
    /**
     * Gets the timestamp indicating when this Attention was applied.
     * 
     * @return the time the attention was applied, in milliseconds since the
     *         Java epoch (Jan 1, 1970)
     */
    public long getTimeStamp();
    
    /**
     * Gets the will-defined specific type of this attention.
     * 
     * @return the type of attention
     */
    public Type getType();
}
