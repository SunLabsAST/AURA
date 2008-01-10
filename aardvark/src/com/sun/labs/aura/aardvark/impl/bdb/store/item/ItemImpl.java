
package com.sun.labs.aura.aardvark.impl.bdb.store.item;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.impl.bdb.store.BerkeleyDataWrapper;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.item.Item;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    
    private transient List<Attention> attention;
    
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
        this.attentionIDs = new HashSet<Long>();
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
    public Set<Long> getAttentionIDs() {
        return attentionIDs;
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
    
    public String getTypeString() {
        return this.ITEM_TYPE;
    }

}
