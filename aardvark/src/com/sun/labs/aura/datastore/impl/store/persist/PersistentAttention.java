
package com.sun.labs.aura.datastore.impl.store.persist;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;

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
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String sourceKey;
    
    /**
     * The item ID for this Attention.
     * Many attentions will have the same item ID
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String targetKey;
        
    /**
     * The type of this Attention.
     * Many attentions will have the same type.
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private int type;
        
    /**
     * The timestamp of when this attention was applied.
     * Many attentions might conceivably have the same timestamp.
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private long timeStamp;

    /**
     * The user and ID and the time stamp in a composite key
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private StringAndTimeKey sourceAndTime;
    
    /*
     * Maybe add an item type to speed up some queries?
     */
    
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
        this.sourceKey = attn.getSourceKey();
        this.targetKey = attn.getTargetKey();
        this.timeStamp = attn.getTimeStamp();
        this.type = attn.getType().ordinal();
        this.sourceAndTime = new StringAndTimeKey(sourceKey, timeStamp);
    }
    
    public PersistentAttention(String sourceKey, String targetKey,
                               Type type) {
        this(sourceKey, targetKey, type, System.currentTimeMillis());
    }

    public PersistentAttention(String sourceKey, String targetKey,
                               Type type, long timeStamp) {
        this.sourceKey = sourceKey;
        this.targetKey = targetKey;
        this.type = type.ordinal();
        this.timeStamp = timeStamp;
        this.sourceAndTime = new StringAndTimeKey(sourceKey, timeStamp);
    }

    public long getID() {
        return id;
    }
    
    public String getSourceKey() {
        return sourceKey;
    }

    public String getTargetKey() {
        return targetKey;
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
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Attention) {
            Attention other = (Attention) o;
            if (other.getSourceKey().equals(getSourceKey()) &&
                    other.getTargetKey().equals(getTargetKey()) &&
                    other.getTimeStamp() == getTimeStamp() &&
                    other.getType().equals(getType())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return (sourceKey + targetKey).hashCode();
    }

    @Override
    public String toString() {
        return String.format("Attn %d (Src[%s] Tar[%s] Type[%s] Hash[%s]",
                             id, sourceKey, targetKey, type,
                             Integer.toBinaryString(hashCode()));
    }

}
