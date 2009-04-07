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

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;

/**
 *
 * @author plamere
 */
public class TagDetails implements IsSerializable, Details {
    private final static ItemInfo[] EMPTY_ITEM_INFO = new ItemInfo[0];
    private final static ArrayList<ScoredC<ArtistCompact>> EMPTY_ARTIST_COMPACT =
            new ArrayList<ScoredC<ArtistCompact>>();
    private final static ArtistVideo[] EMPTY_ARTIST_VIDEO = new ArtistVideo[0];
    private final static ArtistPhoto[] EMPTY_ARTIST_PHOTO = new ArtistPhoto[0];
    
    private String status;
    private String id;
    private String name;
    private String encodedName;
    private float popularity;
    private String description = "None available";
    private String imageURL;
    private ArrayList<ScoredC<ArtistCompact>> representativeArtists = EMPTY_ARTIST_COMPACT;
    private ItemInfo[] similarTags = EMPTY_ITEM_INFO;
    private ArtistVideo[] videos = EMPTY_ARTIST_VIDEO;
    private ArtistPhoto[] photos  = EMPTY_ARTIST_PHOTO;
    
    
    /** Creates a new instance of TagDetails */
    public TagDetails() {
        setStatus("OK");
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public float getPopularity() {
        return popularity;
    }
    
    public void setPopularity(float popularity) {
        this.popularity = popularity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageURL() {
        return imageURL;
    }
    
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    
    public ArtistCompact[] getRepresentativeArtists() {
        ArtistCompact[] aCA = new ArtistCompact[representativeArtists.size()];
        int i=0;
        for (ScoredC<ArtistCompact> sac : representativeArtists) {
            aCA[i++] = sac.getItem();
        }
        return aCA;
    }

    public ArrayList<ScoredC<ArtistCompact>> getScoredRepArtists() {
        return representativeArtists;
    }
    
    public void setRepresentativeArtists(ArrayList<ScoredC<ArtistCompact>> representativeArtists) {
        this.representativeArtists = representativeArtists;
    }
    
    public ItemInfo[] getSimilarTags() {
        return similarTags;
    }
    
    public void setSimilarTags(ItemInfo[] similarTags) {
        this.similarTags = similarTags;
    }
    
    public void fixup() {
    }
    
    public boolean isOK() {
        return getStatus().equals("OK");
    }

    public ArtistVideo[] getVideos() {
        return videos;
    }

    public void setVideos(ArtistVideo[] videos) {
        this.videos = videos;
    }

    public ArtistPhoto[] getPhotos() {
        return photos;
    }

    public void setPhotos(ArtistPhoto[] photos) {
        this.photos = photos;
    }
}
