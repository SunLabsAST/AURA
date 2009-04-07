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
import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author fm223201
 */
public class Track extends ItemAdapter {

    public final static String FIELD_ARTISTS_ID = "artistId";
    public final static String FIELD_SECS = "secs";
    public final static String FIELD_MD5 = "md5";
    public final static String FIELD_LYRICS = "lyrics";
    
    
    /**
     * Wraps an Item as a track
     * @param item the item to be turned into a track
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Track(Item item) {
        super(item, Item.ItemType.TRACK);
    }
    
    public Track() {

    }

    /**
     * Creates a new track
     * @param key the key for the track
     * @param name the name of the track
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Track(String key, String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.TRACK, key, name));
    }
   
  public void defineFields(DataStore ds) throws AuraException {
        try {
            ds.defineField(FIELD_ARTISTS_ID, Item.FieldType.STRING, StoreFactory.INDEXED);
            ds.defineField(FIELD_LYRICS, Item.FieldType.STRING, StoreFactory.INDEXED_TOKENIZED);
            ds.defineField(FIELD_MD5, Item.FieldType.STRING, StoreFactory.INDEXED);
            ds.defineField(FIELD_SECS, Item.FieldType.INTEGER, StoreFactory.INDEXED);
        } catch(RemoteException rx) {
            throw new AuraException("Error defining fields for Track", rx);
        }
    }

  /**
     * Get the videos associated with an artist
     * @return videos id set
     */
    public Set<String> getArtistId() {
        return (Set<String>) getFieldAsStringSet(FIELD_ARTISTS_ID);
    }
    
   /**
     * Adds an artist to this track
     * @param artistId id of the artist
     */
    public void addArtistId(String artistId) {
        appendToField(FIELD_ARTISTS_ID, artistId);
    }
    
     /**
     * Gets the md5 of the track
     * @return the md5
     */
    public String getMD5() {
        return getFieldAsString(FIELD_MD5, "");
    }

    /**
     * Sets the MD5 of the track
     * @param md5 the md5
     */
    public void setMD5(String md5) {
        setField(FIELD_MD5, md5);
    }
    
    /**
     * Gets the lyrics of the track
     * @return the lyrics
     */
    public String getLyrics() {
        return getFieldAsString(FIELD_LYRICS, "");
    }

    /**
     * Sets the lyrics of the track
     * @param lyrics the lyrics
     */
    public void setImgUrl(String lyrics) {
        setField(FIELD_LYRICS, lyrics);
    }
    
     /**
     * Gets the length of the track in secs
     * @return the number of secs
     */
    public int getSecs() {
        return this.getFieldAsInt(FIELD_SECS);
    }

    /**
     * Sets the length of the track in secs
     * @param secs length in secs
     */
    public void setSecs(int secs) {
        setField(FIELD_SECS, secs);
    }
    
}
