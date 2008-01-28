
package com.sun.labs.aura.aardvark.store;

import com.sun.labs.aura.aardvark.impl.store.bdb.ItemImpl;
import com.sun.labs.aura.aardvark.impl.store.bdb.UserImpl;
import com.sun.labs.aura.aardvark.store.item.SimpleItem;
import com.sun.labs.aura.aardvark.store.item.SimpleItem.ItemType;
import com.sun.labs.aura.aardvark.store.item.SimpleUser;

/**
 * A simple factory class for instantiating items
 */
public class SimpleItemFactory {
    /**
     * Constructs an item with the given attributes.
     * 
     * @param type the type of the item
     * @param key the key to use for this item
     * @param name the item's user readable name
     * 
     * @return the item
     */
    public static SimpleItem newItem(ItemType type, String key, String name) {
        return new ItemImpl(type, key, name);
    }
    
    /**
     * Constructs a user with the given attributes
     * 
     * @param key the key to use for this user
     * @param name the user's readable name
     * 
     * @return the user
     */
    public static SimpleUser newUser(String key, String name) {
        return new UserImpl(key, name);
    }
}
