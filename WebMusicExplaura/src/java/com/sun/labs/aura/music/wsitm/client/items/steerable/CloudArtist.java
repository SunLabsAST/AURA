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

package com.sun.labs.aura.music.wsitm.client.items.steerable;

import com.google.gwt.user.client.ui.Image;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib.TagColorType;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
public class CloudArtist extends CloudComposite {

    private Image artistImage;
        
    public CloudArtist(String artistId, String displayName, double weight, HashSet<CloudItem> items, Image artistImage) {
        super(artistId, displayName, weight, items);
        this.artistImage = artistImage;
    }

    public CloudArtist(ArtistCompact aC, double weight) {
        super(aC.getId(), aC.getName(), weight, new HashSet<CloudItem>());
        for (ItemInfo iI : aC.getDistinctiveTags()) {
            addItem(new CloudTag(iI));
        }
        this.artistImage = null; /// TBD PBL broke this
        this.weight = weight;
    }
    
    @Override
    public Image getImage() {
        return artistImage;
    }

    @Override
    public Image getIcon() {
        return new Image("icon-a.jpg");
    }

    @Override
    public TagColorType getTagColorType() {
        if (this.isSticky()) {
            return TagColorType.STICKY_ARTIST;
        } else {
            return TagColorType.ARTIST;
        }
    }

    @Override
    public CloudItemType getCloudItemType() {
        return CloudItemType.ARTIST;
    }
}
