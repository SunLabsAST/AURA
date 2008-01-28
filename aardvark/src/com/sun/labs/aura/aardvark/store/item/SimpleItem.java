
package com.sun.labs.aura.aardvark.store.item;

import java.io.Serializable;
import java.util.HashMap;

/**
 * A simple item for storage in an item store.  Items have keys, types, and
 * names.  Items also store a map from string names to object values for
 * storage of arbitrary data.
 */
public interface SimpleItem extends Serializable {
    public enum ItemType {
        FEED,
        BLOGENTRY,
        ALBUM,
        TRACK
    }

    /**
     * Gets the Aura ID assigned to this Item
     * 
     * @return the ID
     */
    public long getID();
    
    /**
     * Gets the globally unique key that was assigned to this item when it was
     * entered into the ItemStore
     * 
     * @return the Item's key
     */
    public String getKey();
        
    /**
     * Gets the type of this item.  The type helps define which fields are
     * likely to be accessible in this item's property map.
     * 
     * @return the type of this item
     */
    public ItemType getType();
    
    /**
     * Gets the name of this item.  The name should be an end-user readable
     * name for this item.
     * 
     * @return the name of this item
     */
    public String getName();
    
    /**
     * Sets the name of this item.  The name should be an end-user readable
     * name for this item.
     * 
     * @param name the new name of this item
     */
    public void setName(String name);
    
    /**
     * Gets the internal copy of the data storage map used by this item.
     * 
     * @return the item's map
     */
    public HashMap<String,Serializable> getMap();
    
    /**
     * Replaces the internal copy of the data storage map with the provided map
     */
    public void setMap(HashMap<String,Serializable> map);

}
