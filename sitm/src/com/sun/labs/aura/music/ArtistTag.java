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

package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Counted;
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.aura.util.Tag;
import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 *
 * Represents a social tag that has been applied to an artist
 */
public class ArtistTag extends CrawlableItem {

    public final static String FIELD_POPULARITY = "popularity";
    public final static String FIELD_DESCRIPTION = "description";
    public final static String FIELD_TAGGED_ARTISTS = "taggedArtists";
    public final static String FIELD_TAGGED_TRACKS = "taggedTracks";
    public final static String FIELD_VIDEOS = "videos";
    public final static String FIELD_PHOTOS = "photos";

    public final static Comparator<ArtistTag> POPULARITY = new Comparator<ArtistTag>() {
        public int compare(ArtistTag o1, ArtistTag o2) {
            float delta = o1.getPopularity() - o2.getPopularity();
            if (delta > 0) {
                return 1;
            } else if (delta < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    /**
     * Wraps an Item as an ArtistTag
     * @param item the item to be turned into an ArtistTag
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public ArtistTag(Item item) {
        super(item, Item.ItemType.ARTIST_TAG);
    }

    /**
     * Constructor used by ArtistTagRaw
     */
    protected ArtistTag(Item item, Item.ItemType it) {
        super(item, it);
    }

    public ArtistTag() {
    }

    /**
     * Creates a new ArtistTag
     * @param name the name of the ArtistTag
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public ArtistTag(String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.ARTIST_TAG, nameToKey(name), name));
    }

    /**
     * Converts an item type enum value to the corresponding datastore field
     * @param it item type to tag
     * @return corresponding datastore field
     */
    protected static final String taggedItemToField(ItemType it) {
        switch (it) {
            case ARTIST:        return FIELD_TAGGED_ARTISTS;
            case TRACK:         return FIELD_TAGGED_TRACKS;
            default:            throw new RuntimeException("Invalid parameter '"+it.toString()+"'");
        }
    }

    @Override
    public void defineFields(DataStore ds) throws AuraException {
        try {
            super.defineFields(ds);
            ds.defineField(FIELD_DESCRIPTION, Item.FieldType.STRING, StoreFactory.INDEXED_TOKENIZED);
            ds.defineField(FIELD_PHOTOS);
            ds.defineField(FIELD_POPULARITY, Item.FieldType.FLOAT, StoreFactory.INDEXED);
            ds.defineField(FIELD_TAGGED_ARTISTS, Item.FieldType.STRING, StoreFactory.INDEXED);
            ds.defineField(FIELD_TAGGED_TRACKS, Item.FieldType.STRING, StoreFactory.INDEXED);
            ds.defineField(FIELD_VIDEOS);
        } catch(RemoteException rx) {
            throw new AuraException("Error defining fields for ArtistTag", rx);
        }
    }
    
    /**
     * Gets the popularity of the ArtistTag
     * @return the popularity
     */
    public float getPopularity() {
        return getFieldAsFloat(FIELD_POPULARITY);
    }

    /**
     * Sets the popularity of the ArtistTag
     * @param popularity the ArtistTag
     */
    public void setPopularity(float popularity) {
        setField(FIELD_POPULARITY, popularity);
    }

    /**
     * Gets the description of the tag
     * @return the description of the tag
     */
    public String getDescription() {
        return getFieldAsString(FIELD_DESCRIPTION, "");
    }

    /**
     * Sets the biography summary of the ArtistTag
     * @param biography summary the ArtistTag
     */
    public void setDescription(String description) {
        setField(FIELD_DESCRIPTION, description);
    }

    /**
     * Gets the artists that have been tagged with the social tag
     * @return tag map
     * @deprecated 
     */
    public List<Tag> getTaggedArtist() {
        return getTaggedItems(ItemType.ARTIST);
    }

    /**
     * Adds a an artist to the artisttag
     * @param mbaid the musicbrainzid of the artist
     * @param count tag count
     * @deprecated
     */
    public void addTaggedArtist(String mbaid, int count) {
        addTaggedItem(ItemType.ARTIST, mbaid, count);
    }

    /**
     * @deprecated
     */
    public void clearTaggedArtists() {
        clearTaggedItems(ItemType.ARTIST);
    }

    /**
     * Adds an item to the tag
     * @param it item type
     * @param mbaid item's musicbrainzid
     * @param count tag count
     */
    public void addTaggedItem(ItemType it, String mbaid, int count) {
        addTag(taggedItemToField(it), mbaid, count);
    }

    public void addTaggedItems(ItemType it, List<Counted<String>> lcs) {
        for (Counted<String> cS : lcs) {
            addTaggedItem(it, cS.getItem(), cS.getCount());
        }
    }

    public void clearTaggedItems(ItemType it) {
        clearTagMap(taggedItemToField(it));
    }

    /**
     * Gets the items that have been tagged with the tag
     * @param it item type
     * @return tag map
     */
    public List<Tag> getTaggedItems(ItemType it) {
        return getTagsAsList(taggedItemToField(it));
    }

    /**
     * Adds a video to an ArtistTag
     * @param videoID id of the video
     */
    public void addVideo(String videoId) {
        appendToField(FIELD_VIDEOS, videoId);
    }

    /**
     * Get the videos associated with an ArtistTag
     * @return videos id set
     */
    public Set<String> getVideos() {
        return getFieldAsStringSet(FIELD_VIDEOS);
    }

    /**
     * Get the photos associated with an ArtistTag
     * @return photos map
     */
    public Set<String> getPhotos() {
        return getFieldAsStringSet(FIELD_PHOTOS);
    }

    /**
     * Adds a photo to an ArtistTag
     * @param photoID id of the photo
     */
    public void addPhoto(String photoId) {
        appendToField(FIELD_PHOTOS, photoId);
    }

    public static String nameToKey(String name) {
        return "artist-tag:" + normalizeName(name);
    }

    public static String normalizeName(String name) {
        name = name.replaceAll("\\W", "").toLowerCase();
        return name;
    }
}
