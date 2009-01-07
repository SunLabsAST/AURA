package com.sun.labs.aura.datastore.impl.store.persist;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sun.labs.aura.datastore.Item;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * An encapsulation of the description of a particular field.
 */
@Entity(version = 2)
public class FieldDescription implements Serializable {
    private static final long serialVersionUID = 2;

    @PrimaryKey
    private String name;
    
    @SuppressWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED",
                      justification="Used as an in-memory cache of perCaps")
    private transient EnumSet<Item.FieldCapability> caps;

    /**
     * A set of capabilities.  This will go away shortly, to be replaced by the
     * indexed and tokenized flags.
     */
    private HashSet<Integer> perCaps;

    /**
     * Whether this field should be stored in the search index.  By default,
     * fields will not be stored in the search engine.
     */
    private boolean indexed;

    /**
     * Whether the values of this field should be tokenized, if it is to be put
     * into the search index.  By default, values will be not tokenized if they are
     * to be stored in the search index.
     */
    private boolean tokenized;

    private Item.FieldType type;
    
    public FieldDescription() {
        
    }

    public FieldDescription(String name, Item.FieldType type) {
        this(name, type, false, false);
    }

    public FieldDescription(String name, Item.FieldType type, boolean indexed) {
        this(name, type, indexed, false);
    }

    public FieldDescription(String name, Item.FieldType type, boolean indexed, boolean tokenized) {
        this.name = name;
        this.type = type;
        this.indexed = indexed;
        this.tokenized = tokenized;
        if(tokenized && !indexed) {
            Logger.getLogger("").warning(String.format("Field %s will not be indexed by the search engine, but tokenized is true."));
        }
    }

    public String getName() {
        return name;
    }

    @Deprecated
    public EnumSet<Item.FieldCapability> getCapabilities() {
        if(caps == null) {
            caps = EnumSet.noneOf(Item.FieldCapability.class);
            Item.FieldCapability[] vals = Item.FieldCapability.values();
            for(Integer fc : perCaps) {
                caps.add(vals[fc]);
            }
        }
        return EnumSet.copyOf(caps);
    }

    public Item.FieldType getType() {
        return type;
    }
    
    /**
     * Indicates whether this field is one that must be indexed by a search 
     * engine.
     * @return <code>true</code> if this field must be indexed.
     */
    public boolean isIndexed() {
        return indexed;
    }

    /**
     * Sets whether this field is one that must be indexed by the search engine.
     * A field should be indexed if you need to search for it using any of the
     * query methods of the data store.
     *
     * @param indexed whether this field should be indexed.
     */
    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    /**
     * Indicates whether this field is one that must be tokenized when it is
     * indexed by a search engine.  Typically, fields that contain running text
     * (like a review or song lyrics) should be tokenized.
     * <p>
     * The tokenizer will break running text into words at spaces and punctuation.
     * @return whether this field will be tokenized.
     */
    public boolean isTokenized() {
        return tokenized;
    }

    /**
     * Sets whether this field should be tokenized when it is indexed by a search
     * engine.
     * @param tokenized if <code>true</code>, then this field will be tokenized
     * when it is indexed.
     */
    public void setTokenized(boolean tokenized) {
        this.tokenized = tokenized;
    }
    
    public boolean equals(Object o) {
        if(!(o instanceof FieldDescription)) {
            return false;
        }

        FieldDescription fd = (FieldDescription) o;
        if(this == fd) {
            return true;
        }
        return name.equals(fd.name) &&
                type == fd.type;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
