/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.User;
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
public class Listener extends ItemAdapter {
    public final static String FIELD_YOB =              "LISTENER_YEAR_OF_BIRTH";
    public final static String FIELD_STATE =            "LISTENER_STATE";
    public final static String FIELD_GENDER =           "LISTENER_GENDER";
    public final static String FIELD_LAST_FM_NAME =     "LISTENER_LAST_FM_NAME";
    public final static String FIELD_PANDORA_NAME =     "LISTENER_PANDORA_NAME";
    public final static String FIELD_LOCALE_COUNTRY =   "LISTENER_LOCALE_COUNTRY";
    public final static String FIELD_ARTIST =           "LISTENER_ARTIST";
    public final static String FIELD_LAST_CRAWL =       "lastCrawl";
    public final static String FIELD_SOCIAL_TAGS =      Artist.FIELD_SOCIAL_TAGS;

    public enum Gender { Male, Female, Unknown };

    public  final static int STATE_INITIAL_LASTFM_CRAWL = 1;
    public  final static int STATE_INITIAL_PANDORA_CRAWL = 2;

    @Override
    public void defineFields(DataStore ds) throws AuraException {
        try {
            ds.defineField(Item.ItemType.USER, FIELD_GENDER);
            ds.defineField(Item.ItemType.USER, FIELD_STATE);
            ds.defineField(Item.ItemType.USER, FIELD_LAST_FM_NAME);
            ds.defineField(Item.ItemType.USER, FIELD_PANDORA_NAME);
            ds.defineField(Item.ItemType.USER, FIELD_LOCALE_COUNTRY);
            ds.defineField(Item.ItemType.USER, FIELD_SOCIAL_TAGS, EnumSet.of(
                    Item.FieldCapability.SEARCH,
                    Item.FieldCapability.SIMILARITY), Item.FieldType.STRING);
            ds.defineField(Item.ItemType.USER, FIELD_ARTIST, 
                    EnumSet.of(Item.FieldCapability.MATCH, Item.FieldCapability.SIMILARITY),
                    Item.FieldType.STRING);

            ds.defineField(Item.ItemType.USER, FIELD_YOB,
                    EnumSet.of(Item.FieldCapability.MATCH,
                    Item.FieldCapability.SORT),
                    Item.FieldType.INTEGER);
            ds.defineField(Item.ItemType.USER, FIELD_LAST_CRAWL);
        } catch(RemoteException ex) {
            throw new AuraException("Error defining fields for Album", ex);
        }
    }

    public Listener(Item user) {
        super(user, Item.ItemType.USER);
        setYearOfBirth(1959);
    }

    public Listener() {
    }

    public int getAge() {
        return getCurrentYear() - getYearOfBirth();
    }

    public User getUser() {
        return (User) getItem();
    }

    public int getYearOfBirth() {
        return getFieldAsInt(FIELD_YOB);
    }

    public long getLastCrawl() {
        return getFieldAsLong(FIELD_LAST_CRAWL);
    }

    public void setLastCrawl() {
        setField(FIELD_LAST_CRAWL, System.currentTimeMillis());
    }

    public Gender getGender() {
        String sgender = getFieldAsString(FIELD_GENDER);
        if (sgender == null) {
            return Gender.Unknown;
        } else {
            return Gender.valueOf(sgender);
        }
    }

    public void setGender(Gender g) {
        setField(FIELD_GENDER, g.toString());
    }

    public String getLastFmName() {
        return getFieldAsString(FIELD_LAST_FM_NAME);
    }

    public void setLastFmName(String name) {
        setField(FIELD_LAST_FM_NAME, name);
    }

    public int getState() {
        return getFieldAsInt(FIELD_STATE);
    }

    public void setState(int flags) {
        setField(FIELD_STATE, flags);
    }

    public String getPandoraName() {
        return getFieldAsString(FIELD_PANDORA_NAME);
    }

    public void setPandoraName(String name) {
        setField(FIELD_PANDORA_NAME, name);
    }

    public void setYearOfBirth(int yob) {
        setField(FIELD_YOB, yob);
    }

    public String getLocaleCountry() {
        return getFieldAsString(FIELD_LOCALE_COUNTRY);
    }
    
    public void setLocaleCountry(String country) {
        setField(FIELD_LOCALE_COUNTRY, country);
    }
    
    public int getCurrentYear() {
        return 2008;        // FIX ME
    }

    /**
     * Gets the artists that have been listened to  
     * @return tag map
     */
    public List<Tag> getFavoriteArtist() {
        return getTagsAsList(FIELD_ARTIST);
    }

    /**
     * Adds a an artist to the artisttag
     * @param mbaid the musicbrainzid of the artist
     * @param count tag count
     */
    public void addFavoriteArtist(String mbaid, int count) {
        addTag(FIELD_ARTIST, mbaid, count);
    }

    public void clearFavoriteArtists() {
        clearTagMap(FIELD_ARTIST);
    }

    /**
     * Gets the listener's social tags 
     * @return tag map
     */
    public List<Tag> getSocialTags() {
        return getTagsAsList(FIELD_SOCIAL_TAGS);
    }

    /**
     * Adds a social tag to the listener
     * @param tag name of the tag
     * @param count tag count
     */
    public void addSocialTag(String tag, int count) {
        addTag(FIELD_SOCIAL_TAGS, tag, count);
    }

    /**
     * Sets a social tag to the listener
     * @param tag name of the tag
     * @param count tag count
     */
    public void setSocialTag(String tag, int count) {
        setTag(FIELD_SOCIAL_TAGS, tag, count);
    }

    /**
     * clears all social tags for this listener
     */
    public void clearSocialTags() {
        clearTagMap(FIELD_SOCIAL_TAGS);
    }
}
