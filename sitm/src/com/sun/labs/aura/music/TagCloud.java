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
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.aura.util.WordCloud;
import java.rmi.RemoteException;
import java.util.EnumSet;

/**
 *
 * @author plamere
 */
public class TagCloud extends ItemAdapter {

    public final static String FIELD_CREATOR = "creator";
    public final static String FIELD_DESCRIPTION = "description";
    public final static String FIELD_WORLD_CLOUD = "wordCloud";

    /**
     * Wraps an Item as an TagCloud
     * @param item the item to be turned into a TagCloud
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public TagCloud(Item item) {
        super(item, Item.ItemType.TAG_CLOUD);
    }

    public TagCloud() {
    }

    /**
     * Creates a new TagCloud
     * @param key the key for the artist
     * @param name the name of the artist
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public TagCloud(String key, String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.TAG_CLOUD, key, name));
    }

    public void defineFields(DataStore ds) throws AuraException {
        try {
            ds.defineField(FIELD_WORLD_CLOUD);
            ds.defineField(FIELD_DESCRIPTION, Item.FieldType.STRING, StoreFactory.INDEXED_TOKENIZED);
            ds.defineField(FIELD_CREATOR, Item.FieldType.STRING, StoreFactory.INDEXED);
        } catch (RemoteException ex) {
            throw new AuraException("Error defining fields for Album", ex);
        }
    }

    /**
     * Gets the wordcloudkm
     * @return tag map
     */
    public WordCloud getWordCloud() {
        return (WordCloud) getFieldAsObject(FIELD_WORLD_CLOUD);
    }

    /**
     * Sets the wordcloud
     * @return tag map
     */
    public void setWordCloud(WordCloud wordCloud) {
        setFieldAsObject(FIELD_WORLD_CLOUD, wordCloud);
    }

    /**
     * Gets the id of the user that created this TagCloud
     * @return id of the user
     */
    public String getCreator() {
        return getFieldAsString(FIELD_CREATOR);
    }

    /**
     * Sets the id of the user that created this TagCloud
     */
    public void setCreator(String creator) {
        setField(FIELD_CREATOR, creator);
    }

    /**
     * Gets a description of this TagCloud
     * @return the description
     */
    public String getDescription() {
        return getFieldAsString(FIELD_DESCRIPTION);
    }

    /**
     * Sets a description of this TagCloud
     * @param description the tag cloud
     */
    public void setDescription(String description) {
        setField(FIELD_DESCRIPTION, description);
    }
}
