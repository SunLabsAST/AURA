package com.sun.labs.aura.datastore.impl.store.persist;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.FieldCapability;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashSet;

/**
 * An encapsulation of the description of a particular field.
 */
@Entity(version = 2)
public class FieldDescription implements Serializable {

    private static final long serialVersionUID = 2;

    @PrimaryKey
    private String name;

    @SuppressWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
    justification = "Used as an in-memory cache of perCaps")
    private transient EnumSet<FieldCapability> caps;

    private HashSet<Integer> perCaps;

    private HashSet<String> perCapNames;

    private Item.FieldType type;

    public FieldDescription() {
    }

    public FieldDescription(String name) {
        this(name, null, null);
    }

    public FieldDescription(String name,
            Item.FieldType type,
            EnumSet<FieldCapability> caps) {
        this.name = name;

        //
        // Make the in-memory set of capabilities.
        this.caps = EnumSet.noneOf(FieldCapability.class);
        if(caps != null) {
            for(FieldCapability cap : caps) {
                //
                // Upgrade old caps to new.
                this.caps.add(FieldCapability.coerce(cap));
            }
        }

        //
        // Make the sets that will persist.
        perCaps = new HashSet<Integer>();
        perCapNames = new HashSet<String>();
        for(FieldCapability fc : this.caps) {
            perCapNames.add(fc.name());
            perCaps.add(fc.ordinal());
        }
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public EnumSet<FieldCapability> getCapabilities() {
        if(caps == null) {
            caps = EnumSet.noneOf(Item.FieldCapability.class);
            //
            // Upgrade code we'll remove this once we go to the strings set.
            // We'll coerce all of the old values into the INDEXED attribute.
            if(perCaps != null && perCaps.size() > 0) {
                Item.FieldCapability[] vals = Item.FieldCapability.values();
                for(Integer fc : perCaps) {
                    caps.add(FieldCapability.coerce(vals[fc]));
                }
            } else {
                for(String name : perCapNames) {
                    caps.add(FieldCapability.coerce(FieldCapability.valueOf(name)));
                }
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
        return caps != null && caps.contains(FieldCapability.INDEXED);
    }

    public boolean isTokenized() {
        return caps != null && caps.contains(FieldCapability.TOKENIZED);
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
