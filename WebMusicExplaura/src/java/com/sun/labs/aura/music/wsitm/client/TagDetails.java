/*
 * TagDetails.java
 *
 * Created on April 7, 2007, 8:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class TagDetails implements IsSerializable, Details {
    private final static ItemInfo[] EMPTY_ITEM_INFO = new ItemInfo[0];
    private final static ArtistVideo[] EMPTY_ARTIST_VIDEO = new ArtistVideo[0];
    private final static ArtistPhoto[] EMPTY_ARTIST_PHOTO = new ArtistPhoto[0];
    
    private String status;
    private String id;
    private String name;
    private String encodedName;
    private float popularity;
    private String description = "None available";
    private String imageURL;
    private ItemInfo[] representativeArtists = getEMPTY_ITEM_INFO();
    private ItemInfo[] similarTags = getEMPTY_ITEM_INFO();
    private ArtistVideo[] videos = getEMPTY_ARTIST_VIDEO();
    private ArtistPhoto[] photos  = getEMPTY_ARTIST_PHOTO();
    
    
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
    
    public ItemInfo[] getRepresentativeArtists() {
        return representativeArtists;
    }
    
    public void setRepresentativeArtists(ItemInfo[] representativeArtists) {
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

    public static ItemInfo[] getEMPTY_ITEM_INFO() {
        return EMPTY_ITEM_INFO;
    }

    public static ArtistVideo[] getEMPTY_ARTIST_VIDEO() {
        return EMPTY_ARTIST_VIDEO;
    }

    public static ArtistPhoto[] getEMPTY_ARTIST_PHOTO() {
        return EMPTY_ARTIST_PHOTO;
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
