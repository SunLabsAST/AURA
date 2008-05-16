/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES..
 */


package com.sun.labs.aura.util;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The ItemAdaptor provides a set of method that can be used by specific item
 * types to add and retrieve data from the item map in a type-friendly fashion.
 */
public class ItemAdapter implements Serializable {
    protected Item item;
    private boolean modified;


    /**
     * Creates the ItemAdapter
     * @param item the Item that is being wrapped by this Item
     * @param type the type of the item
     */
    public ItemAdapter(Item item, Item.ItemType type) {
        this.item = item;
        if (item.getType() != type) {
            throw new IllegalArgumentException("bad item type expected " + type + " found " + item.getType());
        }
        modified = true;
    }

    /**
     * Creates the ItemAdapter
     * @param item the Item that is being wrapped by this Item
     */
    public ItemAdapter(Item item) {
        this.item = item;
        modified = false;
    }

    /**
     * Gets the name of the item
     * @return the item name
     */
    public String getName() {
        return item.getName();
    }
    

    /**
     * Gets the key for the item
     * @return the item key
     */
    public String getKey() {
        return item.getKey();
    }

    /**
     * Gets the time when this item was added
     * @return milliseconds from the epoch
     */
    public long getTimeAdded() {
        return item.getTimeAdded();
    }
    
    /**
     * Push the item to the datastore if necessary
     * @param dataStore
     */
    public void flush(DataStore dataStore) throws AuraException, RemoteException {
        if (isModified()) {
            item = dataStore.putItem(getItem());
            clearModified();
        }
    }


    /**
     * Determines if this item has been modified
     * @return true if the item has been modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Clears the modiied flag
     */
    public void clearModified() {
        modified = false;
    }
    
    /**
     * Gets the wrapped Item
     * @return the wrapped item
     */
    public Item getItem() {
        return item;
    }

    /**
     * Sets the name of the item
     * @param name the name
     */
    public void setName(String name) {
        if (name != null && !name.equals(getName())) {
            item.setName(name);
            modified = true;
        }
    }


    /**
     * Gets a field value as a float
     * @param name the name of the field
     * @return the value as a float
     */
    protected float getFieldAsFloat(String name) {
        Float f = (Float) item.getMap().get(name);
        return f == null ? 0 : f;
    }

    /**
     * Gets a field as an object
     * @param name the field anme
     * @return the object
     */
    protected Object getFieldAsObject(String name) {
        return item.getMap().get(name);
    }

    /**
     * Gets a field value as a int
     * @param name the name of the field
     * @return the value as a int
     */
    protected int getFieldAsInt(String name) {
        Integer i = (Integer) item.getMap().get(name);
        return i == null ? 0 : i;
    }

    /**
     * Gets a field value as a long
     * @param name the name of the field
     * @return the value as a long
     */
    protected long getFieldAsLong(String name) {
        Long l = (Long) item.getMap().get(name);
        return l == null ? 0 : l;
    }

    /**
     * Gets a field value as a String
     * @param name the name of the field
     * @return the value as a String
     */
    protected String getFieldAsString(String name) {
        return (String) item.getMap().get(name);
    }

    /**
     * Gets a field value as a String or the default value if the string is null
     * @param name the name of the field
     * @param defaultVal the default value
     * @return the value as a String
     */
    protected String getFieldAsString(String name, String defaultVal) {
        String ret =  (String) item.getMap().get(name);
        if (ret == null) {
            ret = defaultVal;
        }
        return ret;
    }

    /**
     * Gets a field value as a set of Strings
     * @param name the name of the field
     * @return the value as a set of strings 
     */
    protected Set<String> getFieldAsStringSet(String fieldName) {
        HashSet<String> set = (HashSet<String>) item.getMap().get(fieldName);
        if (set == null) {
            set = new HashSet<String>();
            item.getMap().put(fieldName, set);
        }
        return set;
    }

    /**
     * Gets a field value as a tag map
     * @param field the name of the field
     * @return the value of the field as a tagmap
     */
    protected Map<String, Tag> getTagMap(String field) {
        HashMap<String, Tag> tagMap = (HashMap<String, Tag>) item.getMap().get(field);
        if (tagMap == null) {
            tagMap = new HashMap<String, Tag>();
            item.getMap().put(field, tagMap);
        }
        return tagMap;
    }

    /**
     * Gets a field value as a list of tags
     * @param field the name of the field
     * @return the value of the field as a list of tags
     */
    protected List<Tag> getTagsAsList(String fieldName) {
        Map<String,Tag> tagMap = getTagMap(fieldName);
        List<Tag> tagList = new ArrayList<Tag>(tagMap.values());
        Collections.sort(tagList);
        Collections.reverse(tagList);
        return tagList;
    }

    /**
     * Sets the named field to the given value. If the new value is different
     * than the previous value, then this item is considered to be modified.
     * @param name the name of the field
     * @param value the new value.
     */
    protected void setField(String name, String value) {
        String curValue = getFieldAsString(name);
        if (curValue == null || !curValue.equals(value)) {
            item.getMap().put(name, value);
            modified = true;
        }
    }

    /**
     * Sets the named field to the given value. 
     * @param name the name of the field
     * @param value the new value.
     */
    protected void setFieldAsObject(String name, Object value) {
        Object curValue = getFieldAsString(name);
        if (curValue == null || !curValue.equals(value)) {
            item.getMap().put(name, (Serializable) value);
            modified = true;
        }
    }

    /**
     * Sets the named field to the given value. If the new value is different
     * than the previous value, then this item is considered to be modified.
     * @param name the name of the field
     * @param value the new value.
     */
    protected void setField(String name, float value) {
        float curValue = getFieldAsFloat(name);
        if (value != curValue) {
            item.getMap().put(name, new Float(value));
            modified = true;
        }
    }

    /**
     * Sets the named field to the given value. If the new value is different
     * than the previous value, then this item is considered to be modified.
     * @param name the name of the field
     * @param value the new value.
     */
    protected void setField(String fieldName, int value) {
        int curValue = getFieldAsInt(fieldName);
        if (value != curValue) {
            item.getMap().put(fieldName, new Integer(value));
            modified = true;
        }
    }

    /**
     * Sets the named field to the given value. If the new value is different
     * than the previous value, then this item is considered to be modified.
     * @param name the name of the field
     * @param value the new value.
     */
    protected void setField(String fieldName, long value) {
        long curValue = getFieldAsLong(fieldName);
        if (value != curValue) {
            item.getMap().put(fieldName, new Long(value));
            modified = true;
        }
    }

    /**
     * Appends a new value to a field
     * @param name the name of the field
     * @param value the value to append
     */
    protected void appendToField(String name, String value) {
        Set<String> set = getFieldAsStringSet(name);
        if (!set.contains(value)) {
            set.add(value);
            modified = true;
        }
    }

    /**
     * Appends a tag to a field
     * @param field the name of the field
     * @param tagName the name of the tag.
     * @param count the tag count
     */
    protected void addTag(String field, String tagName, int count) {
        Map<String,Tag> tagMap = getTagMap(field);
        if (tagMap == null) {
            HashMap<String, Tag> newTagMap = new HashMap<String, Tag>();
            item.getMap().put(field, newTagMap);
            tagMap = newTagMap;
        }

        Tag tag = tagMap.get(tagName);
        if (tag == null) {
            tag = new Tag(tagName, count);
            tagMap.put(tagName, tag);
        } else {
            tag.accum(count);
        }
        modified = true;
    }
    
    /**
     * Adds an object to an artist
     * @param <T>
     * @param field the name of the field
     * @param key the key to use (probably the ID of the object being added)
     * @param o the object to add
     */
    protected <K,V> void addObjectToMap(String field, K key, V o) {
        // If no map exists, create one
        HashMap<K,V> objMap = (HashMap<K,V>) getFieldAsObject(field);
        if (objMap == null) {
            objMap = new HashMap<K,V>();
            item.getMap().put(field, objMap);
        }
        
        // If the key already exists, replace it with version being passed
        if (objMap.containsKey(key)) {
            objMap.remove(key);
        }
        objMap.put(key, o);
        
        modified=true;
    }

    @Override
    public String toString() {
        return toString(getItem());
    }
    
    public static String toString(Item item) {
        StringBuilder sb = new StringBuilder();
        sb.append("key : " + item.getKey() + "\n");
        sb.append("name: " + item.getName() + "\n");
        sb.append("type: " + item.getType() + "\n");

        List<String> keys = new ArrayList<String>(item.getMap().keySet());
        Collections.sort(keys);

        for (String name : keys) {
            Object o = item.getMap().get(name);
            sb.append("    " + name + ": ");
            if (o instanceof Collection) {
                for (Object element : (Collection) o) {
                    sb.append(element + ",");
                }
            } else {
                sb.append(o);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
