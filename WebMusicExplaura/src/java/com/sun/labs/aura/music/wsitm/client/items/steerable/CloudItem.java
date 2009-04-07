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
import com.sun.labs.aura.music.wsitm.client.items.ScoredTag;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
public interface CloudItem extends Comparable<CloudItem> {

    public enum CloudItemType {
        TAG,
        ARTIST
    }

    public String getId();
    public String getDisplayName();
    
    public double getWeight();
    public void setWeight(double weight);

    public HashSet<CloudItem> getContainedItems();
    public HashMap<String, ScoredTag> getTagMap();

    public void setSticky(boolean sticky);
    public boolean isSticky();

    public TagDisplayLib.TagColorType getTagColorType();
    public CloudItemType getCloudItemType();
    
    /**
     * If the item can be represented by an image, return its url
     * @return
     */
    public Image getImage();
    public Image getIcon();
}
