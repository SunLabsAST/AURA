/*
 * ArtistDetails.java
 *
 * Created on March 30, 2007, 11:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Image;
import com.sun.labs.aura.music.wsitm.client.WebLib;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class ArtistDetails extends ArtistCompact implements IsSerializable, Details {
    private final static ArtistPhoto[] EMPTY_ARTIST_PHOTO = new ArtistPhoto[0];
    private final static AlbumDetails[] EMPTY_ALBUM = new AlbumDetails[0];
    private final static ArtistVideo[] EMPTY_ARTIST_VIDEO = new ArtistVideo[0];
    private final static ArtistEvent[] EMPTY_EVENT = new ArtistEvent[0];
    private final static ArtistCompact[] EMPTY_ARTIST_COMPACT = new ArtistCompact[0];
    private final static ArrayList<ScoredC<ArtistCompact>> EMPTY_ARTISTCOPACT_MAP = new ArrayList<ScoredC<ArtistCompact>>();
    
    private Map<String, String> urls = new HashMap<String, String>();
    private String musicURL;
    private ArrayList<ScoredC<ArtistCompact>> similarArtists = EMPTY_ARTISTCOPACT_MAP;
    private ArtistCompact[] recommendedArtists = EMPTY_ARTIST_COMPACT;
    private ArtistCompact[] collaborations = EMPTY_ARTIST_COMPACT;
    private ItemInfo[] frequentTags = EMPTY_ITEM_INFO;
    private ArtistVideo[] videos = EMPTY_ARTIST_VIDEO;
    private ArtistEvent[] events = EMPTY_EVENT;
    protected ArtistPhoto[] photos  = EMPTY_ARTIST_PHOTO;
    protected AlbumDetails[] albums = EMPTY_ALBUM;
        
    /**
     * Creates a new instance of ArtistDetails
     */
    public ArtistDetails() {
        super();
    }

    public ArtistVideo[] getVideos() {
        return videos;
    }

    public void setVideos(ArtistVideo[] videos) {
        this.videos = videos;
    }

    public ArrayList<ScoredC<ArtistCompact>> getSimilarArtists() {
        return similarArtists;
    }

    /**
     * Converts an HashMap reprensenting a list of artists and their similarity value
     * to an artist compact array
     * @param aCMap
     * @return
     */
    public static ArtistCompact[] getSimilarArtistsAsArray(ArrayList<ScoredC<ArtistCompact>> aCList) {
        ArtistCompact[] aCArray = new ArtistCompact[aCList.size()];
        int index = 0;
        for (ScoredC<ArtistCompact> saC : aCList) {
            aCArray[index++] = saC.getItem();
        }
        return aCArray;
    }

    public ArtistCompact[] getSimilarArtistsAsArray() {
        return getSimilarArtistsAsArray(similarArtists);
    }
    
    public void setSimilarArtists(ArrayList<ScoredC<ArtistCompact>> similarArtists) {
        this.similarArtists = similarArtists;
    }
    
    public Map<String,String> getUrls() {
        return urls;
    }

    public void setUrls(Map urls) {
        this.urls = urls;
    }

    public String getMusicURL() {
        fixupMusicURL();
        return musicURL;
    }

    // one crawl tacked on the '?play' command, we don't
    // want it so, lets strip it off.
    private void fixupMusicURL() {
        if (musicURL != null) {
            if (musicURL.endsWith("?play")) {
                int last = musicURL.lastIndexOf("?play");
                musicURL = musicURL.substring(0, last);
            }
        }
    }

    public void setMusicURL(String musicURL) {
        this.musicURL = musicURL;
    }

    public ArtistEvent[] getEvents() {
        return events;
    }

    public void setEvents(ArtistEvent[] events) {
        this.events = events;
    }
    
    public ArtistCompact[] getRecommendedArtists() {
        return recommendedArtists;
    }

    public void setRecommendedArtists(ArtistCompact[] recommendedArtists) {
        this.recommendedArtists = recommendedArtists;
    }

    public ItemInfo[] getFrequentTags() {
        return frequentTags;
    }

    public void setFrequentTags(ItemInfo[] frequentTags) {
        this.frequentTags = frequentTags;
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
        super.fixup();
        if (similarArtists == null) {
            similarArtists = EMPTY_ARTISTCOPACT_MAP;
        }
        if (recommendedArtists == null) {
            recommendedArtists = EMPTY_ARTIST_COMPACT;
        }
        
        if (frequentTags == null) {
            frequentTags = EMPTY_ITEM_INFO;
        }
        
        if (distinctiveTags == null) {
            distinctiveTags = EMPTY_ITEM_INFO;
        }
        if (videos == null) {
            videos = EMPTY_ARTIST_VIDEO;
        }
        if (photos == null) {
            photos = EMPTY_ARTIST_PHOTO;
        }
        if (albums == null) {
            albums = EMPTY_ALBUM;
        }
        if (events == null) {
            events = EMPTY_EVENT;
        }
    }

    public ArtistCompact[] getCollaborations() {
        return collaborations;
    }

    public void setCollaborations(ArtistCompact[] collaborations) {
        this.collaborations = collaborations;
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
    
    private String getBestThumbnailImageURL() {
        if (photos.length > 0) {
            return photos[0].getThumbNailImageUrl();
        } else if (albums.length > 0) {
            return albums[0].getAlbumArt();
        } else {
            return "nopic.gif";
        }
    }

    /**
     * Create a new ArtistCompact object from this ArtistDetails, discarding the
     * additional information
     * @return new ArtistCompact object
     */
    public ArtistCompact toArtistCompact() {
        ArtistCompact aC = new ArtistCompact();

        aC.setImageURL(getBestThumbnailImageURL());
        aC.setBeginYear(this.getBeginYear());
        aC.setBiographySummary(this.getBiographySummary());
        aC.setDistinctiveTags(this.getDistinctiveTags());
        aC.setEncodedName(this.getEncodedName());
        aC.setEndYear(this.getEndYear());
        aC.setId(this.getId());
        aC.setName(this.getName());
        aC.setNormPopularity(this.getNormPopularity());
        aC.setPopularity(this.getPopularity());
        aC.setStatus(this.getStatus());
        aC.setAudio(this.getAudio());
        aC.setSpotifyId(this.getSpotifyId());

        return aC;
    }

}
