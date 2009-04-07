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

/**
 *
 * @author fm223201
 */
public class Event extends ItemAdapter {

    public final static String FIELD_VENUE_NAME = "venueName";
    public final static String FIELD_DATE = "date";
    
    /**
     * Wraps an Item as an Event
     * @param item the item to be turned into an event
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Event(Item item) {
        super(item, Item.ItemType.EVENT);
    }

    /**
     * Creates a new photo
     * @param key the key for the photo
     * @param name the name of the photo
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Event(String key, String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.EVENT, key, name));
    }
    
    public Event() {
    }

   public void defineFields(DataStore ds) throws AuraException {
        try {
            ds.defineField(FIELD_DATE, Item.FieldType.DATE, StoreFactory.INDEXED);
            ds.defineField(FIELD_VENUE_NAME);
        } catch(RemoteException rx) {
            throw new AuraException("Error defining fields for ArtistTag", rx);
        }
    }
   
   /**
     * Gets the id of the venue
     * @return the id
     */
    public String getVenueName() {
        return getFieldAsString(FIELD_VENUE_NAME, "");
    }

    /**
     * Sets the id of the venue
     * @param id the venue
     */
    public void setVenueName(String name) {
        setField(FIELD_VENUE_NAME, name);
    }
    
    /**
     * Sets the event date
     * @param date the date of the event
     */
    public void setDate(String date) {
        setField(FIELD_DATE, date);
    }

    /**
     * Gets the date of the event
     * @return the date of the event
     */
    public String getDate() {
        return getFieldAsString((FIELD_DATE));
    }
}        
