
package com.sun.labs.aura.datastore;

import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import com.sun.labs.aura.datastore.impl.store.ItemStore;
import java.io.Serializable;
import java.util.Map;

/**
 * A simple item for storage in an item store.  Items have keys, types, and
 * names.  Items also store a map from string names to object values for
 * storage of arbitrary data.
 */
public interface Item extends Serializable, Iterable<Map.Entry<String,Serializable>> {
    public enum ItemType {
        USER,
        FEED,
        BLOGENTRY,
        ALBUM,
        TRACK,
        ARTIST,
        PHOTO,
        VIDEO,
        EVENT,
        VENUE,
        ARTIST_TAG
    }
    
    /**
     * An enumeration of the capabilities that we want the fields inside an
     * item's map to have.  These describe how the application will want to use
     * the particular field.
     * 
     * @see ItemStore#defineField
     */
    public enum FieldCapability {
        /**
         * The field will be used for textual similarity operations.
         */
        SIMILARITY,
        
        /**
         * The field will be used to search for particular words or as part
         * of a relational query.
         */
        SEARCH,
        
        /**
         * The field will be used to filter results from queries in the data
         * store.
         */
        FILTER,
        
        /**
         * The field will be used to sort results from queries to the data store.
         */
        SORT
    }
    
    /**
     * An enumeration of the data types for field values in an item.
     */
    public enum FieldType {
        STRING,
        INTEGER,
        FLOAT,
        DATE
    }

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
     * Gets the time that this item was added.
     * 
     * @return the added time in milliseconds since the Java epoch
     */
    public long getTimeAdded();
    
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
     * Sets the value of a field.
     * @param field the name of the field whose value we want to set
     * @param value the value that we want to set
     * @throws IllegalArgumentException if the named field is not defined
     * @see DataStore#defineField
     */
    public void setField(String field, Serializable value);
    
    /**
     * Gets the value of a field.
     * @param field the field whose value we want
     * @return the value for the field, or <code>null</code> if there is no such
     * field in this item.
     */
    public Serializable getField(String field);
    
}
