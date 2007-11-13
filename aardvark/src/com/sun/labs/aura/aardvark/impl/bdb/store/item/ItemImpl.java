
package com.sun.labs.aura.aardvark.impl.bdb.store.item;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.item.Item;
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

    public List<Attention> getAttentionData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void addAttention(long id) {
        attentionIDs.add(id);
    }
    
    public String getTypeString() {
        return this.ITEM_TYPE;
    }
}
