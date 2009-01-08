
package com.sun.labs.aura.datastore;

import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.impl.Util;
import com.sun.labs.aura.datastore.impl.store.persist.UserImpl;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.aura.util.AuraException;
import java.util.EnumSet;
import java.util.Random;


/**
 * A simple factory class for instantiating items
 */
public class StoreFactory {
    
    protected static final Random random = new Random();

    public static EnumSet<Item.FieldCapability> INDEXED_TOKENIZED =
            EnumSet.of(Item.FieldCapability.INDEXED, Item.FieldCapability.TOKENIZED);

    public static EnumSet<Item.FieldCapability> INDEXED =
            EnumSet.of(Item.FieldCapability.INDEXED);
    
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
        String keyHex = Util.toHexString(key.hashCode());
        keyHex = keyHex.format("%9s", keyHex).replace(' ','0');
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
        return newAttention(source.getKey(), target.getKey(), type);
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
                                         Attention.Type type, String data) {
        return newAttention(source.getKey(), target.getKey(), type, data);
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
                                         Attention.Type type, Long num) {
        return newAttention(source.getKey(), target.getKey(), type, num);
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
                                         Attention.Type type, String data) {
        PersistentAttention attn =
                new PersistentAttention(sourceKey, targetKey, type);
        attn.setString(data);
        return attn;
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
                                         Attention.Type type, Long num) {
        PersistentAttention attn =
                new PersistentAttention(sourceKey, targetKey, type);
        attn.setNumber(num);
        return attn;
    }

}
