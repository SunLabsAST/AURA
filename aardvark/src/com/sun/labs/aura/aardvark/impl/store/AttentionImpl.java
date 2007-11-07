/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.store;

import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.item.User;

/**
 * An attention models a user paying some sort of attention to an Item.
 * 
 * @author ja151348
 */
public class AttentionImpl implements Attention {

    protected long userID;
    
    protected long itemID;
    
    protected long timeStamp;
    
    protected Type type;
    
    public AttentionImpl(User user, Item item, Type type) {
        this(user.getID(), item.getID(), type);
    }
    
    public AttentionImpl(long userID, long itemID, Type type) {
        this.userID = userID;
        this.itemID = itemID;
        this.type = type;
        this.timeStamp = System.currentTimeMillis();
    }
    
    public AttentionImpl(AttentionImpl orig) {
        this.userID = orig.userID;
        this.itemID = orig.itemID;
        this.timeStamp = orig.timeStamp;
        this.type = orig.type;
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
        return String.format("%d/%d/%s", userID, itemID, type);
    }

}
