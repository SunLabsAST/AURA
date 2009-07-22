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
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemAdapter;
import java.rmi.RemoteException;

/**
 *
 * @author mailletf
 */
public abstract class CrawlableItem extends ItemAdapter {

    public final static String FIELD_LAST_CRAWL = "lastCrawl";
    public final static String FIELD_UPDATE_COUNT = "updateCount";


    public CrawlableItem(Item item, Item.ItemType type) {
        super(item, type);
    }

    public CrawlableItem() {
    }

    
    @Override
    public void defineFields(DataStore ds) throws AuraException {
        try {
            ds.defineField(FIELD_LAST_CRAWL);
            ds.defineField(FIELD_UPDATE_COUNT);
            
        } catch (RemoteException ex) {
            throw new AuraException("Error defining fields for " + item.getType().toString(), ex);
        }
    }

    
    /**
     * Gets the number of times this item has been updated
     * @return the number of times this item has been updated
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

    /**
     * Gets the time that this item was last crawled
     * @return the time this item was last crawled in ms since the epoch
     */
    public long getLastCrawl() {
        return getFieldAsLong(FIELD_LAST_CRAWL);
    }

    /**
     * Sets the time when this item was last crawled to now.
     */
    public void setLastCrawl() {
        setField(FIELD_LAST_CRAWL, System.currentTimeMillis());
    }
}
