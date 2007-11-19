
package com.sun.labs.aura.aardvark.impl.bdb.store.item;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.impl.bdb.store.BerkeleyDataWrapper;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.item.Item;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Implementation of an Item that can be stored in a Berkeley DB Java
 * Edition database using the Direct Persistence Layer.
 */

@Entity(version=1)
public abstract class ItemImpl implements Item {
    /** The primary key will be the Item's id */
    @PrimaryKey(sequence="Items")
    private long id;

    /** Items also have String keys that for now we'll say are unique */
    @SecondaryKey(relate=Relationship.ONE_TO_ONE)
    private String key;

    /**
     * Attention must be stored as a set of IDs since entity types cannot
     * be stored as references in fields (in other words, the system won't
     * do a JOIN automatically).  We'll have to manually pull in the attention
     * data when somebody asks for it.
     */
    private HashSet<Long> attentionIDs;
    
    private List<Attention> attention;
    
    protected transient BerkeleyDataWrapper bdb;
    
    /**
     * All persistent objects must have a default constructor.
     */
    public ItemImpl() {
    }
    
    /**
     * Sets up the data for an Item
     * 
     * @param key the key to use for this item
     */
    public ItemImpl(String key) {
        this.key = key;
        attentionIDs = new HashSet<Long>();
    }
    
    public long getID() {
        return id;
    }

    public String getKey() {
        return key;
    }

    /**
     * Get the attention data for this item.  Since attention is stored as a
     * list of attention IDs, the IDs must be looked up in the database and
     * have attention objects instantiated for them.
     * 
     * @return the attention for this item
     */
    public List<Attention> getAttentionData() {
        if (attention == null) {
            attention = new ArrayList();
            for (Long aid : attentionIDs) {
                attention.add(bdb.getAttention(aid));
            }
        }
        return attention;
    }
    
    /**
     * Internal method.  Adds the ID of an attention to this Item for storage
     * in the database.
     * 
     * @param id the primary key of the attention object
     */
    public void addAttention(long id) {
        attentionIDs.add(id);
    }
    
    /**
     * Provides an instances of a berkeley data wrapper to this item.  The
     * database access is intended to be used read-only for fetching the
     * attention data using lazy evaluation.
     * 
     * @param b the database wrapper
     */
    public void setBerkeleyDataWrapper(BerkeleyDataWrapper b) {
        this.bdb = b;
    }
    
    public String getTypeString() {
        return this.ITEM_TYPE;
    }
}
