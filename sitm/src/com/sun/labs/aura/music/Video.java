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

    public Video() {
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
            ds.defineField(FIELD_THUMBNAIL);
            ds.defineField(FIELD_URL);
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
