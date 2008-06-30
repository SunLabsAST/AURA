/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.aura.util.Tag;
import java.rmi.RemoteException;
import java.util.EnumSet;
import java.util.List;

/**
 *
 * @author plamere
 */
public class TagCloud extends ItemAdapter {

    public final static String FIELD_CREATOR = "creator";
    public final static String FIELD_DESCRIPTION = "description";
    public final static String FIELD_SOCIAL_TAGS = "socialTags";

    /**
     * Wraps an Item as an TagCloud
     * @param item the item to be turned into a TagCloud
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public TagCloud(Item item) {
        super(item, Item.ItemType.TAG_CLOUD);
    }

    public TagCloud() {
    }

    /**
     * Creates a new TagCloud
     * @param key the key for the artist
     * @param name the name of the artist
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public TagCloud(String key, String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.TAG_CLOUD, key, name));
    }

    public void defineFields(DataStore ds) throws AuraException {
        try {
            ds.defineField(Item.ItemType.TAG_CLOUD, FIELD_SOCIAL_TAGS, EnumSet.of(
                    Item.FieldCapability.SEARCH,
                    Item.FieldCapability.SIMILARITY), Item.FieldType.STRING);

            ds.defineField(Item.ItemType.TAG_CLOUD, FIELD_DESCRIPTION,
                    EnumSet.of(Item.FieldCapability.SIMILARITY,
                    Item.FieldCapability.SEARCH), Item.FieldType.STRING);

            ds.defineField(Item.ItemType.TAG_CLOUD, FIELD_CREATOR,
                    EnumSet.of(Item.FieldCapability.MATCH), Item.FieldType.STRING);

        } catch (RemoteException ex) {
            throw new AuraException("Error defining fields for Album", ex);
        }
    }

    /**
     * Gets the artist's social tags 
     * @return tag map
     */
    public List<Tag> getSocialTags() {
        return getTagsAsList(FIELD_SOCIAL_TAGS);
    }

    /**
     * Adds a social tag to the TagCloud
     * @param tag name of the tag
     * @param count tag count
     */
    public void addSocialTag(String tag, int count) {
        addTag(FIELD_SOCIAL_TAGS, tag, count);
    }

    /**
     * Sets a social tag to the TagCloud
     * @param tag name of the tag
     * @param count tag count
     */
    public void setSocialTag(String tag, int count) {
        if (count <= 0) {
            removeSocialTag(tag);
        } else {
            setTag(FIELD_SOCIAL_TAGS, tag, count);
        }
    }

    public void removeSocialTag(String tag) {
        removeTag(FIELD_SOCIAL_TAGS, tag);
    }

    /**
     * Gets the id of the user that created this TagCloud
     * @return id of the user
     */
    public String getCreator() {
        return getFieldAsString(FIELD_CREATOR);
    }

    /**
     * Sets the id of the user that created this TagCloud
     */
    public void setCreator(String creator) {
        setField(FIELD_CREATOR, creator);
    }

    /**
     * Gets a description of this TagCloud
     * @return the description
     */
    public String getDescription() {
        return getFieldAsString(FIELD_DESCRIPTION);
    }

    /**
     * Sets a description of this TagCloud
     * @param description the tag cloud
     */
    public void setDescription(String description) {
        setField(FIELD_DESCRIPTION, description);
    }
}
