
package com.sun.labs.aura.aardvark.impl.bdb.store;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.ItemImpl;
import com.sun.labs.aura.aardvark.store.Attention;

/**
 * An attention implementation that is persisted in the Berkeley DB Java
 * Edition.
 */
@Entity(version=1)
public class PersistentAttention implements Attention {

    /** The unique ID */
    @PrimaryKey(sequence="Attentions")
    private long id;
    
    /**
     * The user ID for this Attention.
     * Many attentions will have the same user ID
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE,
                  relatedEntity=ItemImpl.class,
                  onRelatedEntityDelete=DeleteAction.CASCADE)
    private long userID;
    
    /**
     * The item ID for this Attention.
     * Many attentions will have the same item ID
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE,
                  relatedEntity=ItemImpl.class,
                  onRelatedEntityDelete=DeleteAction.CASCADE)
    private long itemID;
    
    /**
     * The type of this Attention.
     * Many attentions will have the same type.
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private int type;
        
    /**
     * The timestamp of when this attention was applied.
     * Many attentions might conceivably have the same timestamp.  It should
     * be possible to query ranges based on the timestamp.
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private long timeStamp;
    
    
    /**
     * Default constructor for BDB to instantiate this object
     */
    protected PersistentAttention() {
    }
    
    /**
     * Construct a persistent attention from any other kind of attention
     * 
     * @param attn the attention value
     */
    public PersistentAttention(Attention attn) {
        this.userID = attn.getUserID();
        this.itemID = attn.getItemID();
        this.timeStamp = attn.getTimeStamp();
        this.type = attn.getType().ordinal();
    }

    public long getID() {
        return id;
    }
    
    public long getUserID() {
        return userID;
    }

    public long getItemID() {
        return itemID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public Type getType() {
        for (Type t : Type.values()) {
            if (t.ordinal() == type) {
                return t;
            }
        }
        return null;
    }
    
    public boolean equals(Object o) {
        if (o instanceof Attention) {
            Attention other = (Attention) o;
            if (other.getUserID() == getUserID() &&
                    other.getItemID() == getItemID() &&
                    other.getTimeStamp() == getTimeStamp() &&
                    other.getType().equals(getType())) {
                return true;
            }
        }
        return false;
    }
}
