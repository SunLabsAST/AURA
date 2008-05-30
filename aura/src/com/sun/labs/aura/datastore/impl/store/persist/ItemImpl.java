package com.sun.labs.aura.datastore.impl.store.persist;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The implementation of a generic item that is stored in the berkeley
 * database.  The item should be just a data holder class with no links
 * into the rest of the system.
 */
@Entity(version = 1)
public class ItemImpl implements Item {
    private static final long serialVersionUID = 1;
    
    /** Items also have String keys that for now we'll say are unique */
    @PrimaryKey
    protected String key;
    
    /** The type of this item, used for building sub indexes */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected int itemType;
    
    /** The type combined with the time added, for faster querying */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected IntAndTimeKey typeAndTimeAdded;
    
    /** The name of this item.  This is a persistent field. */
    protected String name;
    
    /** Persistent data for the HashMap of values */
    protected byte[] mapBytes;
    
    /** Instantiated hashmap from the mapBytes */
    private transient HashMap<String,Serializable> map = null;
    
    /**
     * A map from names to field descriptions that we can use to test 
     * field assignments.
     */
    private Map<String,FieldDescription> fieldDescr;
    
    /**
     * A flag indicating that a field that the search engine needs to index
     * has been set by someone.
     */
    private transient boolean mustIndex;
    
    protected static final Logger logger = Logger.getLogger("");
    
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
    public ItemImpl(Item.ItemType itemType, String key, String name) {
        this.itemType = itemType.ordinal();
        this.key = key;
        this.name = name;
        this.typeAndTimeAdded = new IntAndTimeKey(this.itemType,
                                                  System.currentTimeMillis());
    }

    public String getKey() {
        return key;
    }
    
    public long getTimeAdded() {
        return typeAndTimeAdded.getTimeStamp();
    }
    
    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ItemImpl other = (ItemImpl) obj;
        if (this.key == null || !this.key.equals(other.key)) {
            return false;
        }
        return true;
    }

    public ItemType getType() {
        for (Item.ItemType t : Item.ItemType.values()) {
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
    
    public void setFields(Map<String,FieldDescription> m) {
        this.fieldDescr = new HashMap<String,FieldDescription>(m);
    }

    public void setField(String field, Serializable value) {
        getMap();
        FieldDescription d = fieldDescr.get(field);
        if(d == null) {
            throw new IllegalArgumentException("Attempting to set undefined field " + field);
        }
        map.put(field, value);
        
        //
        // If this is a field that the search engine will care about, then 
        // we need to index it.
        if(d.mustIndex()) {
            mustIndex = true;
        }
    }
    
    /**
     * Indicates whether this item needs to be indexed by the search engine.
     * @return <code>true</code> if the item was changed in such a way that it 
     * must be indexed.
     */
    public boolean mustIndex() {
        return mustIndex;
    }
    
    public Serializable getField(String field) {
        getMap();
        return map.get(field);
    }
    
    /**
     * Gets the internal copy of the map used by this item.
     * @return the item's map
     */
    private void getMap() {
        if (map == null && mapBytes != null && mapBytes.length > 0) {
            // deserialize mapBytes into map object
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(mapBytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                map = (HashMap<String,Serializable>)ois.readObject();
            } catch (IOException e) {
                logger.log(Level.WARNING,
                        "Map serialization failed for item " + this, e);
            } catch (ClassNotFoundException e) {
                logger.log(Level.WARNING,
                        "Map serialization failed for item " + this, e);
            }
        } else if (map == null) {
            map = new HashMap<String,Serializable>();
        }
    }
    
    /**
     * Returns an iterator for the entries in this item's map.
     */
    public Iterator<Map.Entry<String,Serializable>> iterator() {
        getMap();
        return map.entrySet().iterator();
    }
    
    /**
     * This method should be called before the HashMap is stored in the
     * database.  This will cause the HashMap to get serialized into byte[]
     * form.
     */
    public void storeMap() {
        if ((map != null) && (!map.isEmpty())) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(map);
                mapBytes = baos.toByteArray();
            } catch (IOException e) {
                logger.log(Level.WARNING,
                        "Map serialization failed for item " + this, e);
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
        // Try to store the map (storeMap checks to see if it needs storing)
        storeMap();
        oos.defaultWriteObject();
    }
    
    @Override
    public String toString() {
        return key + " [" + Integer.toBinaryString(key.hashCode()) + "]";
    }
}
