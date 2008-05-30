/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
            ds.defineField(Item.ItemType.ALBUM, FIELD_ASIN);
            ds.defineField(Item.ItemType.ALBUM, FIELD_PHOTOS);
            ds.defineField(Item.ItemType.ALBUM, FIELD_TRACKS);
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
 