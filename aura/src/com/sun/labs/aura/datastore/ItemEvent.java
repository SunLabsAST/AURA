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

package com.sun.labs.aura.datastore;

import com.sun.labs.aura.util.AuraException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents an event as related to an Item.
 */
public class ItemEvent implements Serializable {
    /** Describes the type of change if this is event is sent for a change */
    public enum ChangeType {
        /** The Aura changed */
        AURA
    }
    
    protected Item[] items;
    
    protected ChangeType ct;
    
    /**
     * Contructs an ItemEvent that does not involve a change of data.
     * 
     * @param items the items that this event relates to
     */
    public ItemEvent(Item[] items) {
        if (items != null) {
            this.items = Arrays.copyOf(items, items.length);
        }
        ct = null;
    }
    
    /**
     * Constructs an ItemEvent for items that all had a change
     * 
     * @param items the items that this event relates to
     * @param ct what type of change occurred
     */
    public ItemEvent(Item[] items, ChangeType ct) {
        this(items);
        this.ct = ct;
    }
    
    /**
     * Gets the items that were involved in this change
     * 
     * @return the array of items
     */
    public Item[] getItems() {
        if (items != null) {
            return Arrays.copyOf(items, items.length);
        } else {
            return null;
        }
    }
    
    /**
     * Gets the type of change that occurred with these items
     * 
     * @return the change type
     * @throws AuraException if this was not a change event
     */
    public ChangeType getChangeType()
        throws AuraException {
        
        if (ct == null) {
            throw new AuraException("No change found in ItemEvent");
        }
        return ct;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ct);
        sb.append(" [");
        for(int i = 0; i < items.length; i++) {
            if(i > 0) {
                sb.append(", ");
            }
            sb.append(items[i].toString());
        }
        sb.append("]");
        return sb.toString();
        
    }
}
