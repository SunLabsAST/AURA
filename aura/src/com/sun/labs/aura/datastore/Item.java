/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.datastore;

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
        ARTIST_TAG,
	ARTIST_TAG_RAW,
        TAG_CLOUD,
        ARTICLE
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
         * The field will be used for exact match or range queries.
         */
        MATCH,
        
        /**
         * The field will be used to search for particular words.
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
        SORT,

        /**
         * Whether the values of the field should be indexed by the search engine.
         */
        INDEXED,
        
        /**
         * Whether the values of the field should be tokenized, if they are
         * being indexed by the search engine.  If the field does not have the
         * <code>INDEXED</code> capability set, then it doesn't make much sense
         * to set this capability, but it will be allowed.
         */
        TOKENIZED;

        /**
         * Coerces one of the old values to one of the new ones.  This will
         * go away eventually.
         * @param fc the capability to coerce.
         * @return the coerced capability
         */
        public static FieldCapability coerce(FieldCapability fc) {
            if(fc != null) {
                if(fc != TOKENIZED) {
                    return INDEXED;
                } else {
                    return TOKENIZED;
                }
            }
            return null;
        }
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
