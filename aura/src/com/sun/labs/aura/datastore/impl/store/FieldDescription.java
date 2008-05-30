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
        this.caps = EnumSet.copyOf(caps);
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
