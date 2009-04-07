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

import java.io.Serializable;

/**
 * Represents some form of attention that a User can attribute to an Item.
 */
public interface Attention extends Serializable, Comparable<Attention> {
    /**
     * The type of attention data that this Attention represents.
     */
    public static enum Type {
        /** Starred in Google Reader by the user */
        STARRED,

        /** Subscribed by the user */
        SUBSCRIBED,
        
        /** Viewed by the user */
        VIEWED,
        
        /** Disliked by the user */
        DISLIKED,
        
        /** Subscribed to a feed */
        SUBSCRIBED_FEED,
        
        /** Starred for an entire feed */
        STARRED_FEED,
        
        /** Dislike for an entire feed */
        DISLIKED_FEED,

        /** an item such as a feed or an entry can link to another */
        LINKS_TO,
        
        /** Played by a user */
        PLAYED,
        
        /** Loved by a user */
        LOVED,
        
        /** a rating - should have a number arg */
        RATING,
        
        /** a tag - should have a string arg*/
        TAG,
        
        /** a search was performed - should have a string arg*/
        SEARCH,
    };
    
    /**
     * Gets the Aura ID of the source that applied this attention
     * 
     * @return the Aura ID of the source
     */
    public String getSourceKey();
    
    /**
     * Gets the Aura ID of the target that this attention was applied to
     * 
     * @return the Aura ID of the target
     */
    public String getTargetKey();
    
    /**
     * Gets the timestamp indicating when this Attention was applied.
     * 
     * @return the time the attention was applied, in milliseconds since the
     *         Java epoch (Jan 1, 1970)
     */
    public long getTimeStamp();
    
    /**
     * Gets the will-defined specific type of this attention.
     * 
     * @return the type of attention
     */
    public Type getType();
    
    /**
     * Gets the meta-data string value associated with this attention if one
     * exists.
     * 
     * @return the meta-data or null
     */
    public String getString();
    
    /**
     * Gets the meta-data number value associated with this attention if one
     * exists.
     * 
     * @return the meta-data or null
     */
    public Long getNumber();
}
