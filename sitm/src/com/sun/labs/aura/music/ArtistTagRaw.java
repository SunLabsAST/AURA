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

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.util.AuraException;


public class ArtistTagRaw extends ArtistTag {

    public ArtistTagRaw(Item item) {
        super(item, Item.ItemType.ARTIST_TAG_RAW);
    }

    public ArtistTagRaw() {
    }

    /**
     * Creates a new ArtistTagRaw
     * @param name the name of the ArtistTagRaw
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public ArtistTagRaw(String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.ARTIST_TAG_RAW, nameToKey(name), name));
    }


    
    public static String nameToKey(String name) {
        return "artist-tag-raw:" + normalizeName(name);
    }

}
