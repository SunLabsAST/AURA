package com.sun.labs.aura.aardvark.impl.store.bdb;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.item.SimpleItem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The implementation of a generic item that is stored in the berkeley
 * database.  The item should be just a data holder class with no links
 * into the rest of the system.
 */
@Entity(version = 1)
public class ItemImpl implements SimpleItem {
    /** The primary key will be the Item's id */
    @PrimaryKey(sequence="Items")
    private long id;

    /** Items also have String keys that for now we'll say are unique */
    @SecondaryKey(relate=Relationship.ONE_TO_ONE)
    private String key;
    
    /** The type of this item, used for building sub indexes */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private int itemType;
    
    /** The type combined with the time added, for faster querying */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private IntAndTimeKey typeAndTimeAdded;
    
    /** The name of this item.  This is a persistent field. */
    private String name;
    
    /**
     * Attention must be stored as a set of IDs since entity types cannot
     * be stored as references in fields (in other words, the system won't
     * do a JOIN automatically).  We'll have to manually pull in the attention
     * data when somebody asks for it.
     */
    private HashSet<Long> attentionIDs;

    private transient List<Attention> attention;

    /** Persistent data for the HashMap of values */
    private byte[] mapBytes;
    
    private transient HashMap<String,Serializable> map;
    
    protected static Logger logger = Logger.getLogger("");
    
    /**
     * We need to provide a default constructor for BDB.
     */
    protected ItemImpl() {
    }

    /**
     * Sets up the data for an Item
     * 
     * @param key the key to use for this item
     */
    public ItemImpl(SimpleItem.ItemType itemType, String key, String name) {
        this.itemType = itemType.ordinal();
        this.key = key;
        this.name = name;
        this.attentionIDs = new HashSet<Long>();
        this.typeAndTimeAdded = new IntAndTimeKey(this.itemType,
                                                  System.currentTimeMillis());
    }

    
    public long getID() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public ItemType getType() {
        for (SimpleItem.ItemType t : SimpleItem.ItemType.values()) {
            if (t.ordinal() == itemType) {
                return t;
            }
        }
        return null;

    }

    public String getName() {
        return name;
    }

    /**
     * Internal method.  Adds the ID of an attention to this Item for storage
     * in the database.
     * 
     * @param id the primary key of the attention object
     */
    public void addAttentionID(long id) {
        attentionIDs.add(id);
    }
    
    public Set<Long> getAttentionIDs() {
        return attentionIDs;
    }

    /**
     * Gets the internal copy of the map used by this item.
     * @return the item's map
     */
    public HashMap<String,Serializable> getMap() {
        if (map == null && mapBytes.length > 0) {
            // deserialize mapBytes into map object
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(mapBytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                map = (HashMap<String,Serializable>)ois.readObject();
            } catch (IOException e) {
                logger.log(Level.WARNING,
                        "Map serialization failed for item " + id, e);
            } catch (ClassNotFoundException e) {
                logger.log(Level.WARNING,
                        "Map serialization failed for item " + id, e);
            }
        }
        return map;
    }
    
    /**
     * Replaces the map that this item had.
     */
    public void setMap(HashMap<String,Serializable> map) {
        this.map = map;
    }
    
    /**
     * This method should be called before the HashMap is stored in the BDB.
     * This will cause the HashMap to get serialized into byte[] form.
     */
    protected void storeMap() {
        if (map != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(map);
                mapBytes = baos.toByteArray();
            } catch (IOException e) {
                logger.log(Level.WARNING,
                        "Map serialization failed for item " + id, e);
            }
        } else {
            mapBytes = new byte[0];
        }
    }

}
