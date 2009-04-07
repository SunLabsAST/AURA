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
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.StoreFactory;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fm223201
 */
public class Album extends ItemAdapter {

    public final static String FIELD_ASIN = "asin";
    public final static String FIELD_PHOTOS = "photos";
    public final static String FIELD_TRACKS = "tracks";

    
    /**
     * Wraps an Item as an album
     * @param item the item to be turned into an album
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Album(Item item) {
        super(item, Item.ItemType.ALBUM);
    }
    
    public Album() {
       
    }

    /**
     * Creates a new album
     * @param key the key for the album
     * @param name the name of the album
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Album(String key, String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.ALBUM, key, name));
    }
    
    public void defineFields(DataStore ds) throws AuraException {
        try {
            ds.defineField(FIELD_ASIN);
            ds.defineField(FIELD_PHOTOS);
            ds.defineField(FIELD_TRACKS);
        } catch(RemoteException ex) {
            throw new AuraException("Error defining fields for Album", ex);
        }
    }
    
    /**
     * Gets the title of the album
     * @return the title
     */
    public String getTitle() {
        return getName();
    }

    /**
     * Sets the title of the album
     * @param title the title
     */
    public void setTitle(String title) {
        setName(title);
    }      
    
    /**
     * Gets the asin of the album
     * @return the asin
     */
    public String getAsin() {
        return getFieldAsString(FIELD_ASIN, "");
    }

    /**
     * Sets the asin of the album
     * @param asin the asin
     */
    public void setAsin(String asin) {
        setField(FIELD_ASIN, asin);
    }
    
    /**
     * Get the photos associated with an artist
     * @return photos map
     */
    public Set<String> getPhotos() {
        return (Set<String>) getFieldAsStringSet(FIELD_PHOTOS);
    }
    
    /**
     * Adds a photo to an artist
     * @param photoID id of the photo
     */
    public void addPhoto(String photoId) {
        appendToField(FIELD_PHOTOS, photoId);
    }

    /**
     * Gets the album art for the ablum
     * @return a url to the album art
     */
    public String getAlbumArt() {
         return "http://images.amazon.com/images/P/" + getAsin() + ".01.MZZZZZZZ.jpg";
    }
    
    /**
     * Gets the  link to the album
     * @return a url to the album 
     */
    public String getAmazonLink() {
        return "http://www.amazon.com/gp/product/" + getAsin();
    }
    
   /**
     * Gets the album's tracks
     * @return associated urls
     */
    public Map<Integer,String> getUrls() {
        return (Map<Integer,String>) getFieldAsObject(FIELD_TRACKS);
    }
    
    /**
     * Adds a track to the album
     * @param trackNumber number of the track
     * @param trackId the id of the track
     */
    public void addTrack(int trackNumber, String trackId) {
        addObjectToMap(FIELD_TRACKS, Integer.valueOf(trackNumber), trackId);
    }
}
 
