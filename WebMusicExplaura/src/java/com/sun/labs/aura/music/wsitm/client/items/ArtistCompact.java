/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client.items;

import com.sun.labs.aura.music.wsitm.client.WebLib;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Image;

/**
 *
 * @author mailletf
 */
public class ArtistCompact implements IsSerializable {

    protected final static ItemInfo[] EMPTY_ITEM_INFO = new ItemInfo[0];
    protected final static ArtistPhoto[] EMPTY_ARTIST_PHOTO = new ArtistPhoto[0];
    protected final static AlbumDetails[] EMPTY_ALBUM = new AlbumDetails[0];
    
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

    protected ItemInfo[] distinctiveTags = EMPTY_ITEM_INFO;
    protected ArtistPhoto[] photos  = EMPTY_ARTIST_PHOTO;
    protected AlbumDetails[] albums = EMPTY_ALBUM;

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

    public ArtistPhoto[] getPhotos() {
        return photos;
    }

    public void setPhotos(ArtistPhoto[] photos) {
        this.photos = photos;
    }

    public AlbumDetails[] getAlbums() {
        return albums;
    }

    public void setAlbums(AlbumDetails[] albums) {
        this.albums = albums;
    }

    /**
     * Ensures proper conditiosn (ie no null arrays)
     */
    public void fixup() {
        if (distinctiveTags == null) {
            distinctiveTags = EMPTY_ITEM_INFO;
        }
        if (photos == null) {
            photos = EMPTY_ARTIST_PHOTO;
        }
        if (albums == null) {
            albums = EMPTY_ALBUM;
        }
    }

    /**
     * Get the artist's image and use an album cover as a fallback
     * @param thumbnail return a thumbnail sized image
     * @return
     */
    public Image getBestArtistImage(boolean thumbnail) {
        Image img = null;
        if (photos.length > 0) {
            if (thumbnail) {
                img = new Image(photos[0].getThumbNailImageUrl());
            } else {
                img = new Image(photos[0].getSmallImageUrl());
            }
        } else if (albums.length > 0) {
            img = new Image(albums[0].getAlbumArt());
            if (thumbnail) {
                img.setVisibleRect(0, 0, 75, 75);
            }
        }
        return img;
    }

    /**
     * Get the artist's image and use an album cover as a fallback, wrapped in HTML
     * @param thumbnail return a thumbnail sized image
     * @return
     */
    public String getBestArtistImageAsHTML() {
        String imgHtml = "";
        if (photos.length > 0) {
            imgHtml = photos[0].getHtmlWrapper();
        } else if (albums.length > 0) {
            AlbumDetails album = albums[0];
            imgHtml = WebLib.createAnchoredImage(album.getAlbumArt(), album.getAmazonLink(), "margin-right: 10px; margin-bottom: 10px");
        }
        return imgHtml;
    }
}
