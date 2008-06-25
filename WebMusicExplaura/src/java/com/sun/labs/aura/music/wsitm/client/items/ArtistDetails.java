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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class ArtistDetails extends ArtistCompact implements IsSerializable, Details {
    private final static ArtistVideo[] EMPTY_ARTIST_VIDEO = new ArtistVideo[0];
    private final static ArtistEvent[] EMPTY_EVENT = new ArtistEvent[0];
    private final static ArtistCompact[] EMPTY_ARTIST_COMPACT = new ArtistCompact[0];
    
    /**
     *  @gwt.typeArgs <java.lang.String, java.lang.String>
     */
    private Map urls = new HashMap();
    private String musicURL;
    private ArtistCompact[] similarArtists = EMPTY_ARTIST_COMPACT;
    private ArtistCompact[] recommendedArtists = EMPTY_ARTIST_COMPACT;
    private ArtistCompact[] collaborations = EMPTY_ARTIST_COMPACT;
    private ItemInfo[] frequentTags = EMPTY_ITEM_INFO;
    private ArtistVideo[] videos = EMPTY_ARTIST_VIDEO;
    private ArtistEvent[] events = EMPTY_EVENT;
        
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

    public ArtistCompact[] getSimilarArtists() {
        return similarArtists;
    }

    public void setSimilarArtists(ArtistCompact[] similarArtists) {
        this.similarArtists = similarArtists;
    }
    
    public Map getUrls() {
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
 
    /**
     * Ensures proper conditiosn (ie no null arrays)
     */      
    public void fixup() {
        if (similarArtists == null) {
            similarArtists = EMPTY_ARTIST_COMPACT;
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
     * Create a new ArtistCompact object from this ArtistDetails, discarding the
     * additional information
     * @return new ArtistCompact object
     */
    public ArtistCompact toArtistCompact() {
        ArtistCompact aC = new ArtistCompact();

        aC.setAlbums(this.getAlbums());
        aC.setPhotos(this.getPhotos());
        aC.setBeginYear(this.getBeginYear());
        aC.setBiographySummary(this.getBiographySummary());
        aC.setDistinctiveTags(this.getDistinctiveTags());
        aC.setEncodedName(this.getEncodedName());
        aC.setEndYear(this.getEndYear());
        aC.setId(this.getId());
        aC.setImageURL(this.getImageURL());
        aC.setName(this.getName());
        aC.setNormPopularity(this.getNormPopularity());
        aC.setPopularity(this.getPopularity());
        aC.setStatus(this.getStatus());

        return aC;
    }

}
