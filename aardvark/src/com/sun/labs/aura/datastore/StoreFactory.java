
package com.sun.labs.aura.datastore;

import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.impl.store.persist.UserImpl;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.aura.util.AuraException;
import java.util.Random;


/**
 * A simple factory class for instantiating items
 */
public class StoreFactory {
    
    protected static Random random = new Random();
    
    /**
     * Constructs an item with the given attributes.
     * 
     * @param type the type of the item
     * @param key the key to use for this item
     * @param name the item's user readable name
     * 
     * @return the item
     */
    public static Item newItem(ItemType type, String key, String name)
            throws AuraException {
        if (type == ItemType.USER) {
            throw new AuraException("Invalid item type: USER. " +
                                    "Use newUser(...) instead.");
        }
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
    public static User newUser(String key, String name) {
        UserImpl ui = new UserImpl(key, name);
        long rand = random.nextLong();
        String keyHex = Integer.toHexString(key.hashCode());
        keyHex = keyHex.format("%8s", keyHex).replace(' ','0');
        String randHex = Long.toHexString(rand);
        randHex = randHex.format("%16s", randHex).replace(' ','0');
        ui.setUserRandString(keyHex + randHex);
        return ui;
    }
    
    /**
     * Constructs a new attention object with the given attributes
     * 
     * @param source the item paying attention
     * @param target the item attended to
     * @param type the type of attention paid
     * 
     * @return the attention object
     */
    public static Attention newAttention(Item source, Item target,
                                         Attention.Type type) {
        return new PersistentAttention(source.getKey(), target.getKey(), type);
    }

    /**
     * Constructs a new attention object with the given attributes
     * 
     * @param sourceKey the key of the item paying attention
     * @param targetKey the key of the item attended to
     * @param type the type of attention paid
     * 
     * @return the attention object
     */
    public static Attention newAttention(String sourceKey, String targetKey,
                                         Attention.Type type) {
        return new PersistentAttention(sourceKey, targetKey, type);
    }

}
