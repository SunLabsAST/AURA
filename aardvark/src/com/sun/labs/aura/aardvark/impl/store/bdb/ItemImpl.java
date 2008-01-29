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
    protected long id;

    /** Items also have String keys that for now we'll say are unique */
    @SecondaryKey(relate=Relationship.ONE_TO_ONE)
    protected String key;
    
    /** The type of this item, used for building sub indexes */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected int itemType;
    
    /** The type combined with the time added, for faster querying */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected IntAndTimeKey typeAndTimeAdded;
    
    /** The name of this item.  This is a persistent field. */
    protected String name;
    
    /**
     * Attention must be stored as a set of IDs since entity types cannot
     * be stored as references in fields (in other words, the system won't
     * do a JOIN automatically).  We'll have to manually pull in the attention
     * data when somebody asks for it.
     */
    protected HashSet<Long> attentionIDs;

    /** Persistent data for the HashMap of values */
    protected byte[] mapBytes;
    
    /** Instantiated hashmap from the mapBytes */
    private transient HashMap<String,Serializable> map = null;
    
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

    public void setName(String name) {
        this.name = name;
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
        if (map == null && mapBytes != null && mapBytes.length > 0) {
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
        if (map == null) {
            mapBytes = new byte[0];
        }
    }
    
    /**
     * This method should be called before the HashMap is stored in the BDB.
     * This will cause the HashMap to get serialized into byte[] form.
     */
    public void storeMap() {
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
        }
    }

    /**
     * When this item is serialized, see if we have an in-memory (transient)
     * version of our data hash map.  If so, serialize it to the byte array
     * before any further serialization happens.
     * 
     * @param oos
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        //
        // If the map has been instantiated, we'll assume that it is dirty.
        if (map != null) {
            storeMap();
        }
        oos.defaultWriteObject();
    }
}
