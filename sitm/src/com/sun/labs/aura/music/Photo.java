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

/**
 *
 * @author fm223201
 */
public class Photo extends ItemAdapter {

    public final static String FIELD_CREATOR_USER_NAME = "creatorUserName";
    public final static String FIELD_CREATOR_REAL_NAME = "creatorRealName";
    public final static String FIELD_IMG_URL = "imgUrl";
    public final static String FIELD_SMALL_IMG_URL = "smallImgUrl";
    public final static String FIELD_THUMBNAIL_URL = "thumbnailUrl";
    public final static String FIELD_PHOTO_PAGE_URL = "photoPageUrl";

    /**
     * Wraps an Item as a Photo
     * @param item the item to be turned into a photo
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Photo(Item item) {
        super(item, Item.ItemType.PHOTO);
    }

    public Photo() {
    }

    /**
     * Creates a new photo
     * @param key the key for the photo
     * @param name the name of the photo
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Photo(String key, String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.PHOTO, key, name));
    }
    
   public void defineFields(DataStore ds) throws AuraException {
        try {
            ds.defineField(FIELD_CREATOR_USER_NAME);
            ds.defineField(FIELD_CREATOR_REAL_NAME);
            ds.defineField(FIELD_IMG_URL);
            ds.defineField(FIELD_SMALL_IMG_URL);
            ds.defineField(FIELD_THUMBNAIL_URL);
            ds.defineField(FIELD_PHOTO_PAGE_URL);
        } catch(RemoteException rx) {
            throw new AuraException("Error defining fields for Photo", rx);
        }
    }
   
    /**
     * Gets the creator's username
     * @return the username
     */
    public String getCreatorUserName() {
        return getFieldAsString(FIELD_CREATOR_USER_NAME, "");
    }

    /**
     * Sets the creator's username 
     * @param userName the username
     */
    public void setCreatorUserName(String userName) {
        setField(FIELD_CREATOR_USER_NAME, userName);
    }
    
    /**
     * Gets the creator's realname
     * @return the realname
     */
    public String getCreatorRealName() {
        return getFieldAsString(FIELD_CREATOR_REAL_NAME, "");
    }

    /**
     * Sets the creator's realname 
     * @param realName the realname
     */
    public void setCreatorRealName(String realName) {
        setField(FIELD_CREATOR_REAL_NAME, realName);
    }
 
    /**
     * Gets the url of the image
     * @return the url
     */
    public String getImgUrl() {
        return getFieldAsString(FIELD_IMG_URL, "");
    }

    /**
     * Sets the url of the image
     * @param url the url
     */
    public void setImgUrl(String url) {
        setField(FIELD_IMG_URL, url);
    }
    
    /**
     * Gets the url of the small image
     * @return the url
     */
    public String getSmallImgUrl() {
        return getFieldAsString(FIELD_SMALL_IMG_URL, "");
    }

    /**
     * Sets the url of the small image
     * @param url the url
     */
    public void setSmallImgUrl(String url) {
        setField(FIELD_SMALL_IMG_URL, url);
    }   

    /**
     * Gets the url of the thumbnail image
     * @return the url
     */
    public String getThumbnailUrl() {
        return getFieldAsString(FIELD_THUMBNAIL_URL, "");
    }

    /**
     * Sets the url of the thumbnail image
     * @param url the url
     */
    public void setThumbnailUrl(String url) {
        setField(FIELD_THUMBNAIL_URL, url);
    }  
    
    /**
     * Set the photo page url
     * @param url url of the photo page
     */
    public void setPhotoPageUrl(String url) {
        setField(FIELD_PHOTO_PAGE_URL,url);
    }
    
    /**
     * Get the photo page url
     * @return the photo page url
     */
    public String getPhotoPageUrl() {
        return getFieldAsString(FIELD_PHOTO_PAGE_URL, "");
    }
    
    /**
     * Gets the title of the photo
     * @return the title
     */
    public String getTitle() {
        return getName();
    }

    /**
     * Sets the title of the photo
     * @param title the title
     */
    public void setTitle(String title) {
        setName(title);
    }

    public static String idToThumbnail(String id) {
        if (id.indexOf("_s.jpg") == -1) {
            return id.replace(".jpg", "_s.jpg");
        } else {
            return id;
        }
    }
    
}       
