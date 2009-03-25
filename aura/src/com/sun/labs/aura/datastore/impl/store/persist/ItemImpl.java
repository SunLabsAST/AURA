package com.sun.labs.aura.datastore.impl.store.persist;

import com.sleepycat.persist.evolve.Conversion;
import com.sleepycat.persist.evolve.Converter;
import com.sleepycat.persist.evolve.Mutations;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.model.NotPersistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sleepycat.persist.raw.RawObject;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The implementation of a generic item that is stored in the berkeley
 * database.  The item should be just a data holder class with no links
 * into the rest of the system.
 */
@Entity(version = 2)
public class ItemImpl implements Item {
    private static final long serialVersionUID = 2;
    
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
     * A flag indicating that a field that the search engine needs to index
     * has been set by someone.  This member is marked as transient because we
     * don't want to persist it to the database, but, unfortunately, we do want
     * to serialize it when doing RMI, so we do the serialization of this member
     * by hand.
     */
    @NotPersistent
    private Set<String> modifiedFields;

    protected static final Logger logger = Logger.getLogger("");
    
    /**
     * We need to provide a default constructor for BDB.
     */
    protected ItemImpl() {
        modifiedFields = new HashSet<String>();
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
        modifiedFields = new HashSet<String>();
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
    
    public void setField(String field, Serializable value) {
        if (value!=null && field!=null) {
            getMap();
            map.put(field, value);
            if (modifiedFields==null) {
                modifiedFields = new HashSet<String>();                
            }
            modifiedFields.add(field);
        }
    }

    public Set<String> getModifiedFields() {
        return modifiedFields;
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
                ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(bais));
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
                GZIPOutputStream gzip = new GZIPOutputStream(baos);
                ObjectOutputStream oos = new ObjectOutputStream(gzip);
                oos.writeObject(map);
                oos.flush();
                oos.close();
                gzip.flush();
                gzip.close();
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

    public static void addMutations(Mutations mutations) {
        Converter gzipItemMap = new Converter(ItemImpl.class.getName(), 1,
                                              "mapBytes", new GZIPConversion());
        Converter gzipItemMapForUser = new Converter(UserImpl.class.getName(), 3,
                                              "mapBytes", new GZIPConversion());
        mutations.addConverter(gzipItemMap);
        mutations.addConverter(gzipItemMapForUser);
    }

    /**
     * A conversion for version 1 to version 2 (introduced GZIPed mapBytes)
     */
    public static class GZIPConversion implements Conversion {
        protected static Logger logger = Logger.getLogger("");
        
        @Override
        public void initialize(EntityModel model) {
            // nothing to do here
        }

        /**
         * Takes an array of bytes and gzips the data, returning another array
         * of bytes.
         *
         * @param fromValue a RawObject representing a byte[] of uncompressed data
         * @return a byte[] of compressed data
         */
        @Override
        public Object convert(Object fromValue) {
            Object[] fromBytes = ((RawObject)fromValue).getElements();
            if (fromBytes.length > 0) {
                byte[] frombytes = new byte[fromBytes.length];
                for (int i=0; i < fromBytes.length; i++) {
                    frombytes[i] = ((Byte)fromBytes[i]).byteValue();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    GZIPOutputStream out = new GZIPOutputStream(baos);
                    out.write(frombytes);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "GZIP failed during upgrade!", e);
                    throw new RuntimeException(e);
                }
                byte[] tobytes = baos.toByteArray();
                Object[] toBytes = new Byte[tobytes.length];
                for (int i=0; i < tobytes.length; i++) {
                    toBytes[i] = new Byte(tobytes[i]);
                }
                return new RawObject(((RawObject)fromValue).getType(),
                                     toBytes);
            } else {
                return fromValue;
            }
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GZIPConversion;
        }
    }
}
