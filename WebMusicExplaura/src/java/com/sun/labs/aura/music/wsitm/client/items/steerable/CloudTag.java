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
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ScoredTag;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib.TagColorType;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
public class CloudTag implements CloudItem {

    private String tagId;
    private String tagName;
    private double tagWeight;
    private boolean sticky;
   
    public CloudTag(String tagId, String tagName, double tagWeight) {
        this(tagId, tagName, tagWeight, false);
    }

    public CloudTag(String tagId, String tagName, double tagWeight, boolean sticky) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.tagWeight = tagWeight;
        this.sticky = sticky;
    }

    public CloudTag(ItemInfo tag) {
        this(tag, false);
    }

    public CloudTag(ItemInfo tag, boolean useDefaultSize) {
        this.tagId = tag.getId();
        this.tagName = tag.getItemName();
        if (useDefaultSize) {
            this.tagWeight = 0;
        } else {
            this.tagWeight = tag.getScore();
        }
    }

    @Override
    public String getDisplayName() {
        return tagName;
    }

    @Override
    public double getWeight() {
        return tagWeight;
    }

    @Override
    public void setWeight(double tagWeight) {
        this.tagWeight = tagWeight;
    }

    @Override
    public HashSet<CloudItem> getContainedItems() {
        HashSet<CloudItem> ciL = new HashSet<CloudItem>();
        ciL.add(this);
        return ciL;
    }

    @Override
    public HashMap<String, ScoredTag> getTagMap() {
        HashMap<String, ScoredTag> tagMap = new HashMap<String, ScoredTag>();
        // Take the absolute of tagWeight because this weight will be used multiplied
        // with itself by TagWidget which will make it always positive
        tagMap.put(tagName, new ScoredTag(tagName, Math.abs(tagWeight), sticky));
        return tagMap;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public Image getIcon() {
        return new Image("icon-t.jpg");
    }

    @Override
    public String getId() {
        return tagId;
    }

    @Override
    public int compareTo(CloudItem o) {
        return new Double(getWeight()).compareTo(o.getWeight());
    }

    @Override
    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    @Override
    public boolean isSticky() {
        return this.sticky;
    }

    @Override
    public TagColorType getTagColorType() {
        if (this.isSticky()) {
            return TagColorType.STICKY_TAG;
        } else {
            return TagColorType.TAG;
        }
    }

    @Override
    public CloudItemType getCloudItemType() {
        return CloudItemType.TAG;
    }
}
