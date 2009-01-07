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
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * An aardvark feed item
 * @author plamere
 */
public class BlogFeed extends ItemAdapter implements Serializable {

    // field names
    private final static String FIELD_DESCRIPTION = "description";
    private final static String FIELD_IMAGE = "image";
    private final static String FIELD_AUTHOR = "author";
    private final static String FIELD_TAG = "tag";
    private final static String FIELD_LINK = "link";
    private final static String FIELD_LAST_PULL_TIME = "lastPullTime";
    private final static String FIELD_NUM_PULLS = "numPulls";
    private final static String FIELD_NUM_ERRORS = "numErrors";
    private final static String FIELD_NUM_INCOMING_LINKS = "numIncomingLinks";
    private final static String FIELD_NUM_CONSECUTIVE_ERRORS = "numConsecutiveErrors";
    private final static String FIELD_NUM_STARRED_ENTRIES = "numStarredEntries";
    
    /**
     * Wraps a Item as a feed
     * @param item the item to be turned into a feed
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public BlogFeed(Item item) {
        super(item, Item.ItemType.FEED);
    }

    /**
     * Creates a new feed item
     * @param key the key for the feed
     * @param name the name of the feed
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public BlogFeed(String key, String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.FEED, key, name));
    }

    public void defineFields(DataStore store) throws AuraException {
        try {
            store.defineField(FIELD_DESCRIPTION, Item.FieldType.STRING, StoreFactory.INDEXED_TOKENIZED);
            store.defineField(FIELD_IMAGE);
            store.defineField(FIELD_AUTHOR, Item.FieldType.STRING, StoreFactory.INDEXED_TOKENIZED);
            store.defineField(FIELD_TAG, Item.FieldType.STRING, StoreFactory.INDEXED);
            store.defineField(FIELD_LINK);
            store.defineField(FIELD_LAST_PULL_TIME);
            store.defineField(FIELD_NUM_PULLS);
            store.defineField(FIELD_NUM_ERRORS);
            store.defineField(FIELD_NUM_INCOMING_LINKS, 
                    Item.FieldType.INTEGER, StoreFactory.INDEXED);
            store.defineField(FIELD_NUM_CONSECUTIVE_ERRORS);
            store.defineField(FIELD_NUM_STARRED_ENTRIES, 
                    Item.FieldType.INTEGER, StoreFactory.INDEXED);
        } catch(RemoteException rx) {
            throw new AuraException("Error defining fields for BlogFeed", rx);
        }
    }
    
    /**
     * Sets the description of the feed
     * @param description the desription
     */
    public void setDescription(String description) {
        setField(FIELD_DESCRIPTION, description);
    }

    /**
     * Sets the image link for a feed. Feeds can have images associated
     * with them.
     * @param imageLink the link ot the image
     */
    public void setImage(String imageLink) {
        setField(FIELD_IMAGE, imageLink);
    }

    /**
     * Sets the link for a feed. 
     * @param link the link ot the image
     */
    public void setPullLink(String link) {
        setField(FIELD_LINK, link);
    }

    /**
     * Adds an author to the feed
     * @param author the name of the author
     */
    public void addAuthor(String author) {
        appendToField(FIELD_AUTHOR, author);
    }

    /**
     * Gets the URL for this feed
     * @return the URL for the feed
     */
    public String getCannonicalURL() {
        return getKey();
    }

    /**
     * Adds a tag to the feed
     * @param tag the string representation of the tag
     * @param count the number of times the tag has been applied
     */
    public void addTag(String tag, int count) {
        addTag(FIELD_TAG, tag, count);
    }
    
    

    /**
     * Used to indicates that the feed has been pulled
     * @param pullOK if true, the pull was successful
     */
    public void pulled(boolean pullOK) {
        setField(FIELD_LAST_PULL_TIME, System.currentTimeMillis());
        setField(FIELD_NUM_PULLS, getNumPulls() + 1);
        if (pullOK) {
            setField(FIELD_NUM_CONSECUTIVE_ERRORS, 0);
        } else {
            setField(FIELD_NUM_CONSECUTIVE_ERRORS, getNumConsecutiveErrors() + 1);
            setField(FIELD_NUM_ERRORS, getNumErrors() + 1);
        } 
    }

    /**
     * Gets the number of times the feed has been pulled
     * @return the number of pulls
     */
    public int getNumPulls() {
        return getFieldAsInt(FIELD_NUM_PULLS);
    }

    /**
     * Gets the number of incoming links for this feed
     * @return the number of incoming links
     */
    public int getNumIncomingLinks() {
        return getFieldAsInt(FIELD_NUM_INCOMING_LINKS);
    }

    /**
     * Sets the number of incoming links for this feed
     * @param links the number of incoming links
     */
    public void setNumIncomingLinks(int links) {
        setField(FIELD_NUM_INCOMING_LINKS, links);
    }

    public long getLastPullTime() {
        return getFieldAsLong(FIELD_LAST_PULL_TIME);
    }

    /**
     * Gets the number of consecutive pull errors
     * @return the number of consecutive pull errors
     */
    public int getNumConsecutiveErrors() {
        return getFieldAsInt(FIELD_NUM_CONSECUTIVE_ERRORS);
    }

    /**
     * Get the number of errors
     * 
     * @return the number of errors
     */
    public float getAuthority() {
        return getNumIncomingLinks() + getNumStarredEntries() + 1;
    }

    /**
     * Get the number of errors
     * 
     * @return the number of errors
     */
    public int getNumErrors() {
        return getFieldAsInt(FIELD_NUM_ERRORS);
    }

    /**
     * Get the number of starred entries on this feed
     * 
     * @return the number of starred entries
     */
    public int getNumStarredEntries() {
        return getFieldAsInt(FIELD_NUM_STARRED_ENTRIES);
    }

    /**
     * Sets the number of starred entries on this feed
     * 
     * @return the number of starred entries
     */
    public void setNumStarredEntries(int num) {
        setField(FIELD_NUM_STARRED_ENTRIES, num);
    }

    /**
     * Gets the description of the feed
     * @return the description of the feed
     */
    public String getDescription() {
        return getFieldAsString(FIELD_DESCRIPTION);
    }
    
    /**
     * Gets a link to the image for the feed
     * @return the link to the image
     */
    public String getImage() {
        return getFieldAsString(FIELD_IMAGE);
    }

    /**
     * Gets a link to the feed
     * @return the link to the image
     */
    public String getPullLink() {
        return getFieldAsString(FIELD_LINK);
    }

    /**
     * Gets the set of authors for the feed
     * @return the authors
     */
    public Set<String> getAuthors() {
        return getFieldAsStringSet(FIELD_AUTHOR);
    }

    /**
     * Gets the ordered list of tags. Tags are ordered from most frequent to least frequent.
     * @return the list of tags
     */
    public List<Tag> getTags() {
        return getTagsAsList(FIELD_TAG);
    }
}
