package com.sun.labs.aura.datastore;

import com.sun.labs.aura.util.AuraException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents an event as related to an Item.
 */
public class ItemEvent implements Serializable {
    /** Describes the type of change if this is event is sent for a change */
    public enum ChangeType {
        /** The Aura changed */
        AURA
    }
    
    protected Item[] items;
    
    protected ChangeType ct;
    
    /**
     * Contructs an ItemEvent that does not involve a change of data.
     * 
     * @param items the items that this event relates to
     */
    public ItemEvent(Item[] items) {
        this.items = Arrays.copyOf(items, items.length);
        ct = null;
    }
    
    /**
     * Constructs an ItemEvent for items that all had a change
     * 
     * @param items the items that this event relates to
     * @param ct what type of change occurred
     */
    public ItemEvent(Item[] items, ChangeType ct) {
        this(items);
        this.ct = ct;
    }
    
    /**
     * Gets the items that were involved in this change
     * 
     * @return the array of items
     */
    public Item[] getItems() {
        return Arrays.copyOf(items, items.length);
    }
    
    /**
     * Gets the type of change that occurred with these items
     * 
     * @return the change type
     * @throws AuraException if this was not a change event
     */
    public ChangeType getChangeType()
        throws AuraException {
        
        if (ct == null) {
            throw new AuraException("No change found in ItemEvent");
        }
        return ct;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ct);
        sb.append(" [");
        for(int i = 0; i < items.length; i++) {
            if(i > 0) {
                sb.append(", ");
            }
            sb.append(items[i].toString());
        }
        sb.append("]");
        return sb.toString();
        
    }
}
