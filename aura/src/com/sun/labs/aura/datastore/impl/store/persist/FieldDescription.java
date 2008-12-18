package com.sun.labs.aura.datastore.impl.store.persist;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sun.labs.aura.datastore.Item;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashSet;

/**
 * An encapsulation of the description of a particular field.
 */
@Entity(version = 1)
public class FieldDescription implements Serializable {
    private static final long serialVersionUID = 1;

    @PrimaryKey
    private String name;
    
    @SuppressWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED",
                      justification="Used as an in-memory cache of perCaps")
    private transient EnumSet<Item.FieldCapability> caps;

    private HashSet<Integer> perCaps;

    private boolean indexed;

    private Item.FieldType type;
    
    public FieldDescription() {
        
    }

    public FieldDescription(String name, boolean indexed, Item.FieldType type) {
        this.name = name;
        this.indexed = indexed;
        this.type = type;
    }

    public FieldDescription(String name, EnumSet<Item.FieldCapability> caps,
            Item.FieldType type) {
        this.name = name;
        if(caps == null) {
            this.caps = EnumSet.noneOf(Item.FieldCapability.class);
        } else {
            this.caps = EnumSet.copyOf(caps);
        }
        perCaps = new HashSet<Integer>();
        for(Item.FieldCapability fc : this.caps) {
            perCaps.add(fc.ordinal());
        }
        this.type = type;
    }

    public String getName() {
        return name;
    }

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
    public boolean mustIndex() {
        return indexed;
    }

    public boolean getIndexed() {
        return indexed;
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
                perCaps.equals(fd.perCaps) &&
                type == fd.type;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
