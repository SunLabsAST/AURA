/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES..
 */

package com.sun.labs.aura.aardvark;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.syndication.feed.synd.SyndEntry;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * An BlogEntry representation of a Item
 * @author plamere
 */
public class BlogEntry extends ItemAdapter {
    public final static String FIELD_FEED_KEY = "feedKey";
    public final static String FIELD_CONTENT = "content";
    public final static String FIELD_SYND_ENTRY = "syndEntry";
    public final static String FIELD_TAG = "tag";
    public final static String FIELD_AUTHOR = "author";
    public final static String FIELD_AUTHORITY = "authority";
    public final static String FIELD_PUBLISH_DATE = "publish-date";
    public final static String FIELD_AUTOTAG = "autotag";

    /**
     * Wraps a Item as blog entry
     * @param item the item to be turned into a blog entry
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public BlogEntry(Item item) {
        super(item, Item.ItemType.BLOGENTRY);
    }

    /**
     * Creates a new blog entry
     * @param key the key for the blog entry
     * @param name the name of the blog entry
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public BlogEntry(String key, String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.BLOGENTRY, key, name));
    }
    
    public void defineFields(DataStore store) throws AuraException {
        EnumSet<Item.FieldCapability> ss = EnumSet.of(Item.FieldCapability.SIMILARITY,
                Item.FieldCapability.SEARCH);
        try {
            store.defineField(Item.ItemType.BLOGENTRY, FIELD_FEED_KEY, 
                    EnumSet.of(Item.FieldCapability.SEARCH), 
                    Item.FieldType.STRING);
            store.defineField(Item.ItemType.BLOGENTRY, FIELD_TAG,
                    ss, Item.FieldType.STRING);
            store.defineField(Item.ItemType.BLOGENTRY, FIELD_CONTENT,
                    ss, Item.FieldType.STRING);
            store.defineField(Item.ItemType.BLOGENTRY, FIELD_SYND_ENTRY);
            store.defineField(Item.ItemType.BLOGENTRY, FIELD_TAG,
                    ss, Item.FieldType.STRING);
            store.defineField(Item.ItemType.BLOGENTRY, FIELD_AUTHOR,
                EnumSet.of(Item.FieldCapability.SEARCH), 
                Item.FieldType.STRING);
            store.defineField(Item.ItemType.BLOGENTRY, FIELD_AUTHORITY,
                    EnumSet.of(Item.FieldCapability.SORT, 
                    Item.FieldCapability.FILTER),
                    Item.FieldType.INTEGER);
            store.defineField(Item.ItemType.BLOGENTRY, FIELD_PUBLISH_DATE,
                    EnumSet.of(Item.FieldCapability.SORT, 
                    Item.FieldCapability.FILTER), Item.FieldType.DATE);
            store.defineField(Item.ItemType.BLOGENTRY, FIELD_AUTOTAG,
                    EnumSet.of(Item.FieldCapability.SIMILARITY), null);
        } catch(RemoteException rx) {
            throw new AuraException("Error defining fields for BlogEntry", rx);
        }
    }

    /**
     * Gets the author of the entry
     * @return the author
     */
    public String getAuthor() {
        return getFieldAsString(FIELD_AUTHOR, "");
    }

    /**
     * Sets the author of the entry
     * @param author the author
     */
    public void setAuthor(String author) {
        setField(FIELD_AUTHOR, author);
    }

    /**
     * Gets the authority of the entry
     * @return the authority of the entry
     */
    public float getAuthority() {
        return getFieldAsFloat(FIELD_AUTHORITY);
    }

    /**
     * Sets the published date of the entry
     * @param date the published date of the entry
     */
    public void setPublishDate(Date date) {
        setFieldAsObject(FIELD_PUBLISH_DATE, date);
    }

    /**
     * Gets the publish date of the entry
     * @return the publish date of the entry
     */
    public Date getPublishDate() {
        return (Date) getFieldAsObject(FIELD_PUBLISH_DATE);
    }


    /**
     * Sets the authority of the entry
     * @param authority the authority (1.0 is most, 0.0 is least)
     */
    public void setAuthority(float authority) {
        setField(FIELD_AUTHORITY, authority);
    }

    /**
     * Gets the content of the entry
     * @return the entry content
     */
    public String getContent() {
        return getFieldAsString(FIELD_CONTENT, "");
    }

    /**
     * Sets the content of the entry
     * @param content the entry content
     */
    public void setContent(String content) {
        setField(FIELD_CONTENT, content);
    }

    /**
     * Gets the ID of the owning feed
     * @return the id of the owning feed
     */
    public String getFeedKey() {
        return getFieldAsString(FIELD_FEED_KEY);
    }

    /**
     * Sets the ID of the owning feed
     * @param feedID the id of the owning feed
     */
    public void setFeedKey(String key) {
        setField(FIELD_FEED_KEY, key);
    }

    /**
     * Gets the ordered list of tags applied to this entry
     * @return the ordered list of tags
     */
    public List<Tag> getTags() {
        return getTagsAsList(FIELD_TAG);
    }

    /**
     * Gets the ordered list of tags applied to this entry
     * @return the ordered list of tags
     */
    public List<Scored<String>> getAutoTags() {
        List<Scored<String>> list =  (List<Scored<String>>) getFieldAsObject(FIELD_AUTOTAG);
        if (list == null) {
            list = new ArrayList<Scored<String>>(0);
        }
        return list;
    }

    /**
     * Adds a tag to the entry
     * @param tag the tag name
     * @param count the frequency of the tag
     */
    public void addTag(String tag, int count) {
        addTag(FIELD_TAG, tag, count);
    }

    /**
     * Gets the title of the entry
     * @return the title of the entry
     */
    public String getTitle() {
        return getName();
    }



    /**
     * Gets the SyndEntry associated with this entry
     * @return the synd entry
     */
    public SyndEntry getSyndEntry() {
        return (SyndEntry) getFieldAsObject(FIELD_SYND_ENTRY);
    }

    /**
     * Sets the syndentry associated with this entry
     * @param entry the syndentry
     */
    public void setSyndEntry(SyndEntry entry) {
        setFieldAsObject(FIELD_SYND_ENTRY, entry);
    }
}
