/*
 * SimpleAttention.java
 * 
 * Created on Oct 26, 2007, 10:30:54 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.store;

import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.item.User;

/**
 * A basic Attention object that supports Starred and Viewed attention.
 * @author ja151348
 */
public class SimpleAttention implements Attention {

    /** The Aura ID of the User paying attention */
    protected long userID;
    
    /** The Aura ID of the Item the User paid attention to */
    protected long itemID;
    
    /** The type of attention */
    protected Type type;
    
    /** When the attention occurred */
    protected long timeStamp;
    
    /**
     * Creates a SimpleAttention object.  The timestamp is set to the current
     * system time.
     * 
     * @param u the related user
     * @param i the related item
     * @param type the type of this Attention
     */
    public SimpleAttention(User u, Item i, Type type) {
        this(u, i, type, System.currentTimeMillis());
    }
    
    /**
     * Creates a SimpleAttention object with a specified time.
     * 
     * @param u the related user
     * @param i the related item
     * @param type the type of this Attention
     * @param timeStamp the time at which the attention occurred
     */
    public SimpleAttention(User u, Item i,
                           Type type, long timeStamp) {
        this.userID = u.getID();
        this.itemID = i.getID();
        this.type = type;
        this.timeStamp = timeStamp;
    }
    
    public long getUserID() {
        return userID;
    }

    public long getItemID() {
        return itemID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public Type getType() {
        return type;
    }
    
    public String toString() {
        return String.format("Attn: %d/%d/%s", userID, itemID, type);
    }

}
