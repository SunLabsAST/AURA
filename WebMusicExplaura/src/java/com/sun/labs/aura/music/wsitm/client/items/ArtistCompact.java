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

package com.sun.labs.aura.music.wsitm.client.items;

import com.sun.labs.aura.music.wsitm.client.WebLib;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Image;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
public class ArtistCompact implements IsSerializable {

    protected final static ItemInfo[] EMPTY_ITEM_INFO = new ItemInfo[0];
    
    protected String status;
    protected String id;
    protected String name;
    protected String encodedName;
    protected String spotifyID;
    protected float popularity;
    protected float normPopularity;
    protected int beginYear;
    protected int endYear;
    protected String biographySummary = "None available";
    protected String imageURL;
    protected HashSet<String> audio;

    protected ItemInfo[] distinctiveTags = EMPTY_ITEM_INFO;

    public ArtistCompact() {
        setStatus("OK");
    }

    public boolean isOK() {
        return getStatus().equals("OK");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyID=spotifyId;
    }

    public String getSpotifyId() {
        return spotifyID;
    }

    public void setAudio(HashSet<String> audio) {
        this.audio = audio;
    }

    public HashSet<String> getAudio() {
        return audio;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEncodedName() {
        return encodedName;
    }

    public void setEncodedName(String encodedName) {
        this.encodedName = encodedName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return getName();
    }

    public int getBeginYear() {
        return beginYear;
    }

    public void setBeginYear(int beginYear) {
        this.beginYear = beginYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    public String getBiographySummary() {
        return biographySummary;
    }

    public void setBiographySummary(String biographySummary) {
        this.biographySummary = biographySummary;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public boolean isActive() {
        return getEndYear() == 0;
    }

    public float getPopularity() {
        return popularity;
    }

    public void setPopularity(float popularity) {
        this.popularity = popularity;
    }

    public float getNormPopularity() {
        return normPopularity;
    }

    public void setNormPopularity(float normPopularity) {
        this.normPopularity = normPopularity;
    }

    public ItemInfo[] getDistinctiveTags() {
        return distinctiveTags;
    }

    public void setDistinctiveTags(ItemInfo[] distinctiveTags) {
        this.distinctiveTags = distinctiveTags;
    }


    /**
     * Ensures proper conditiosn (ie no null arrays)
     */
    public void fixup() {
        if (distinctiveTags == null) {
            distinctiveTags = EMPTY_ITEM_INFO;
        }
    }

}
