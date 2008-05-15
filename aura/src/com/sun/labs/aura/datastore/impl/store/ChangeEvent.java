package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.datastore.ItemEvent;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;

/**
 * A container for an item change event that we can queue up.
 */
public class ChangeEvent {
    
    ItemImpl item;
    
    ItemEvent.ChangeType type;
    
    public ChangeEvent(ItemImpl item, ItemEvent.ChangeType type) {
        this.item = item;
        this.type = type;
    }

}
