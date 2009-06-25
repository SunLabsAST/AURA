/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.aura.util.Tag;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author plamere
 */
public class Listener extends ItemAdapter {
    public final static String FIELD_YOB =                      "LISTENER_YEAR_OF_BIRTH";
    public final static String FIELD_STATE =                    "LISTENER_STATE";
    public final static String FIELD_GENDER =                   "LISTENER_GENDER";
    public final static String FIELD_LAST_FM_NAME =             "LISTENER_LAST_FM_NAME";
    public final static String FIELD_PANDORA_NAME =             "LISTENER_PANDORA_NAME";
    public final static String FIELD_LOCALE_COUNTRY =           "LISTENER_LOCALE_COUNTRY";
    public final static String FIELD_CRAWLED_PLAY_HISTORY =     "CRAWLED_PLAY_HISTORY";
    /**
     * @deprecated
     */
    public final static String FIELD_ARTIST =                   "LISTENER_ARTIST";
    public final static String FIELD_LAST_CRAWL =               "lastCrawl";
    public final static String FIELD_LAST_NEIGHBOURS_CRAWL =    "LAST_NEIGHBOURS_CRAWL";
    public final static String FIELD_AGG_PLAY_HIST_HASH =       "AGG_PLAY_HIST_HASH";
    public final static String FIELD_AGGREGATED_PLAY_HISTORY =  "AGGREGATED_PLAY_HISTORY";
    public final static String FIELD_SOCIAL_TAGS =              Artist.FIELD_SOCIAL_TAGS;
    public final static String FIELD_UPDATE_COUNT =             "updateCount";


    public enum Gender { Male, Female, Unknown };

    @Override
    public void defineFields(DataStore ds) throws AuraException {
        try {
            ds.defineField(FIELD_GENDER);
            ds.defineField(FIELD_STATE);
            ds.defineField(FIELD_LAST_FM_NAME, Item.FieldType.STRING, StoreFactory.INDEXED);
            ds.defineField(FIELD_PANDORA_NAME);
            ds.defineField(FIELD_LOCALE_COUNTRY);
            ds.defineField(FIELD_CRAWLED_PLAY_HISTORY);
            ds.defineField(FIELD_AGG_PLAY_HIST_HASH);
            ds.defineField(FIELD_AGGREGATED_PLAY_HISTORY, Item.FieldType.STRING, StoreFactory.INDEXED);
            ds.defineField(FIELD_SOCIAL_TAGS, Item.FieldType.STRING, StoreFactory.INDEXED);
            ds.defineField(FIELD_ARTIST, Item.FieldType.STRING, StoreFactory.INDEXED);
            ds.defineField(FIELD_YOB, Item.FieldType.INTEGER, StoreFactory.INDEXED);
            ds.defineField(FIELD_LAST_CRAWL);
            ds.defineField(FIELD_LAST_NEIGHBOURS_CRAWL, Item.FieldType.DATE, StoreFactory.INDEXED);
            ds.defineField(FIELD_UPDATE_COUNT);
        } catch(RemoteException ex) {
            throw new AuraException("Error defining fields for Album", ex);
        }
    }

    public Listener(Item user) {
        super(user, Item.ItemType.USER);
    }

    public Listener() {
    }

    public int getAge() {
        int yob = getYearOfBirth();
        if (yob > 0) {
            return getCurrentYear() - yob;
        }
        return 0;
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

    public long getLastNeighboursCrawl() {
        return getFieldAsLong(FIELD_LAST_NEIGHBOURS_CRAWL);
    }

    public void setLastNeighboursCrawl() {
        setField(FIELD_LAST_NEIGHBOURS_CRAWL, System.currentTimeMillis());
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
        return java.util.Calendar.YEAR;
    }

    /**
     * Adds a new week to the list of weekly last.fm charts that we have crawled.
     * @param timestamp timestamp representing the first second of the week
     */
    public void addCrawledPlayHistoryDate(long timestamp) {
        appendToField(FIELD_CRAWLED_PLAY_HISTORY, String.valueOf(timestamp));
    }

    /**
     * Verifies if we already crawled the last.fm play history for a certain week
     * @param timestamp
     * @return
     */
    public boolean crawledPlayHistory(long timestamp) {
        return getFieldAsStringSet(FIELD_CRAWLED_PLAY_HISTORY).contains(String.valueOf(timestamp));
    }

    /**
     * Updates the play history aggregation hash. This needs to be called whenever
     * a new aggregation is performed
     */
    public void updatePlayHistoryAggregationHash() {
        int newHash = getFieldAsStringSet(FIELD_CRAWLED_PLAY_HISTORY).hashCode();
        setField(FIELD_AGG_PLAY_HIST_HASH, newHash);
    }

    /**
     * Verifies if the current play history aggregation needs to be updated
     * @return update needed
     */
    public boolean playHistoryAggregationNeedsUpdate() {
        return getFieldAsInt(FIELD_AGG_PLAY_HIST_HASH) !=
                getFieldAsStringSet(FIELD_CRAWLED_PLAY_HISTORY).hashCode();
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
     * Clears the map of aggregated play counts
     */
    public void clearAggregatedPlayCounts() {
        // This is to clear all objects that have this field set. Should be remove
        // along with the field
        clearTagMap(FIELD_ARTIST);
        
        clearTagMap(FIELD_AGGREGATED_PLAY_HISTORY);
    }

    /**
     * Sets aggregated play count for an artist
     * @param artistMbid artist for which to set the aggregated play count
     * @param count play count
     */
    public void setAggregatedPlayCount(String artistMbid, int count) {
        setTag(FIELD_AGGREGATED_PLAY_HISTORY, artistMbid, count);
    }

    /**
     * Gets the listener's aggregated play counts for all artists
     * @return list of artist ids with their associated counts
     */
    public List<Tag> getAggregatedPlayCount() {
        return getTagsAsList(FIELD_AGGREGATED_PLAY_HISTORY);
    }

    /**
     * clears all social tags for this listener
     */
    public void clearSocialTags() {
        clearTagMap(FIELD_SOCIAL_TAGS);
    }

    /**
     * Gets the number of times this artist has been updated
     * @return the number of times this artist has been updated
     */
    public int getUpdateCount() {
        return getFieldAsInt(FIELD_UPDATE_COUNT);
    }

    /**
     * Sets the time when this item was last crawled to now.
     */
    public void incrementUpdateCount() {
        setField(FIELD_UPDATE_COUNT, getUpdateCount() + 1);
    }
}
