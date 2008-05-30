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

/**
 *
 * @author fm223201
 */
public class Video extends ItemAdapter {

    public final static String FIELD_URL = "url";
    public final static String FIELD_THUMBNAIL = "thumbnail";
    
            
     /**
     * Wraps an Item as a Video
     * @param item the item to be turned into a video
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Video(Item item) {
        super(item, Item.ItemType.VIDEO);
    }

    /**
     * Creates a new video
     * @param key the key for the video
     * @param name the name of the video
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Video(String key, String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.VIDEO, key, name));
    }
    
   public void defineFields(DataStore ds) throws AuraException {
        try {
            ds.defineField(Item.ItemType.VIDEO, FIELD_THUMBNAIL);
            ds.defineField(Item.ItemType.VIDEO, FIELD_URL);
        } catch(RemoteException rx) {
            throw new AuraException("Error defining fields for Video", rx);
        }
    }
    
    /**
     * Gets the thumbnail url of the video
     * @return the thumbnail url
     */
    public String getThumbnailUrl() {
        return getFieldAsString(FIELD_THUMBNAIL, "");
    }

    /**
     * Sets the thumbnail url of the video
     * @param thumbUrl the thumbnail url of the video
     */
    public void setThumbnailUrl(String thumbUrl) {
        setField(FIELD_THUMBNAIL, thumbUrl);
    }
    
    /**
     * Gets the url of the video
     * @return the url
     */
    public String getUrl() {
        return getKey();
    }
}