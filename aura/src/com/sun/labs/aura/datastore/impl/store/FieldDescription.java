package com.sun.labs.aura.datastore.impl.store;

import com.sleepycat.persist.model.PrimaryKey;
import com.sun.labs.aura.datastore.Item;
import java.io.Serializable;
import java.util.EnumSet;

/**
 * An encapsulation of the description of a particular field.
 */
public class FieldDescription implements Serializable {

    @PrimaryKey
    private String name;

    private EnumSet<Item.FieldCapability> caps;

    private Item.FieldType type;

    public FieldDescription(String name, EnumSet<Item.FieldCapability> caps,
            Item.FieldType type) {
        this.name = name;
        if(caps == null) {
            this.caps = EnumSet.noneOf(Item.FieldCapability.class);
        } else {
            this.caps = EnumSet.copyOf(caps);
        }
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public EnumSet<Item.FieldCapability> getCapabilities() {
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
        return caps.size() > 0;
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
                caps.equals(fd.caps) &&
                type == fd.type;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
