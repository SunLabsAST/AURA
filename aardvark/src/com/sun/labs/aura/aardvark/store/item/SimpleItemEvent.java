package com.sun.labs.aura.aardvark.store.item;

import com.sun.labs.aura.aardvark.util.AuraException;

/**
 * Represents an event as related to an Item.
 */
public class SimpleItemEvent {
    /** Describes the type of change if this is event is sent for a change */
    public enum ChangeType {
        /** Only the attention was changed, not the Aura */
        ATTENTION,
        
        /** The Aura changed */
        AURA
    }
    
    protected SimpleItem[] items;
    
    protected ChangeType ct;
    
    /**
     * Contructs an ItemEvent that does not involve a change of data.
     * 
     * @param items the items that this event relates to
     */
    public SimpleItemEvent(SimpleItem[] items) {
        this.items = items;
        ct = null;
    }
    
    /**
     * Constructs an ItemEvent for items that all had a change
     * 
     * @param items the items that this event relates to
     * @param ct what type of change occurred
     */
    public SimpleItemEvent(SimpleItem[] items, ChangeType ct) {
        this(items);
        this.ct = ct;
    }
    
    /**
     * Gets the items that were involved in this change
     * 
     * @return the array of items
     */
    public SimpleItem[] getItems() {
        return items;
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
}
