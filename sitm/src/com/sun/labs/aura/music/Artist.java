/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music;

import com.sun.labs.aura.util.Tag;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemAdapter;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author fm223201
 */
public class Artist extends ItemAdapter {

    public final static String FIELD_POPULARITY = "popularity";
    public final static String FIELD_BEGIN_YEAR = "beginYear";
    public final static String FIELD_END_YEAR = "endYear";
    public final static String FIELD_URLS = "urls";
    public final static String FIELD_BIOGRAPHY_SUMMARY = "biographySummary";
    public final static String FIELD_SOCIAL_TAGS = "socialTags";
    public final static String FIELD_AUTO_TAGS = "autoTags";
    public final static String FIELD_COLLABORATIONS = "collaborations";
    public final static String FIELD_VIDEOS = "videos";
    public final static String FIELD_PHOTOS = "photos";
    public final static String FIELD_ALBUM = "album";
    public final static String FIELD_EVENTS = "events";
    

    /**
     * Wraps an Item as an artist
     * @param item the item to be turned into an artist
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Artist(Item item) {
        super(item, Item.ItemType.ARTIST);
    }

    /**
     * Creates a new artist
     * @param key the key for the artist
     * @param name the name of the artist
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Artist(String key, String name) throws AuraException {
        this(StoreFactory.newItem(Item.ItemType.ARTIST, key, name));
    }
    
    /**
     * Gets the popularity of the artist
     * @return the popularity
     */
    public float getPopularity() {
        return getFieldAsFloat(FIELD_POPULARITY);
    }

    /**
     * Sets the popularity of the artist
     * @param popularity the artist
     */
    public void setPopularity(float popularity) {
        setField(FIELD_POPULARITY, popularity);
    }
    
    /**
     * Gets the begin year of the artist
     * @return the begin year
     */
    public int getBeginYear() {
        return getFieldAsInt(FIELD_BEGIN_YEAR);
    }

    /**
     * Sets the begin year of the artist
     * @param begin year of the artist
     */
    public void setBeginYear(int beginYear) {
        setField(FIELD_BEGIN_YEAR, beginYear);
    }
    
    /**
     * Gets the end year of the artist
     * @return the end year
     */
    public int getEndYear() {
        return getFieldAsInt(FIELD_END_YEAR);
    }

    /**
     * Sets the end year of the artist
     * @param end year of the artist
     */
    public void setEndYear(int endYear) {
        setField(FIELD_END_YEAR, endYear);
    }
    /**
     * Gets the biography summary of the artist
     * @return the biography summary
     */
    public String getBioSummary() {
        return getFieldAsString(FIELD_BIOGRAPHY_SUMMARY, "");
    }

    /**
     * Sets the biography summary of the artist
     * @param biography summary the artist
     */
    public void setBioSummary(String bio) {
        setField(FIELD_BIOGRAPHY_SUMMARY, bio);
    }
    
    /**
     * Adds a frequent tag
     * @param tag name of the tag
     * @param count tag count
     */
    public void addAutoTag(String tag, int count) {
       addTag(FIELD_AUTO_TAGS,tag,count);   
    }
   
    /**
     * Gets the artist's auto tags 
     * @return tag map
     */
    public Map<String,Tag> getAutoTags() {
        return getTagMap(FIELD_AUTO_TAGS);
    }
    
    /**
     * Gets the artist's social tags 
     * @return tag map
     */
    public Map<String,Tag> getSocialTags() {
        return getTagMap(FIELD_SOCIAL_TAGS);
    }
    
    /**
     * Adds a social tag to the artist
     * @param tag name of the tag
     * @param count tag count
     */
    public void addSocialTag(String tag, int count) {
       addTag(FIELD_SOCIAL_TAGS,tag,count);   
    }
    
    /**
     * Gets the artist's associated URLs
     * @return associated urls
     */
    public Map<String,String> getUrls() {
        return (Map<String,String>) getFieldAsObject(FIELD_URLS);
    }
    
    /**
     * Adds an associated URL to artist
     * @param siteName name of the site
     * @param newURL URL of the artist's page
     */
    public void addUrl(String siteName, String newURL) {
        addObjectToMap(FIELD_URLS,newURL,siteName);
    }
    
    /**
     * Adds a video to an artist
     * @param videoID id of the video
     */
    public void addVideo(String videoId) {
        appendToField(FIELD_VIDEOS, videoId);
    }
    
    /**
     * Get the videos associated with an artist
     * @return videos id set
     */
    public Set<String> getVideos() {
        return getFieldAsStringSet(FIELD_VIDEOS);
    }
    
    /**
     * Get the photos associated with an artist
     * @return photos map
     */
    public Set<String> getPhotos() {
        return getFieldAsStringSet(FIELD_PHOTOS);
    }
    
    /**
     * Adds a photo to an artist
     * @param photoID id of the photo
     */
    public void addPhoto(String photoId) {
        appendToField(FIELD_PHOTOS, photoId);
    }
    
    /**
     * Get the events associated with an artist
     * @return events id map
     */
    public Set<String> getEvents() {
        return getFieldAsStringSet(FIELD_EVENTS);
    }
    
    /**
     * Adds an event to an artist
     * @param eventID id of the event
     */
    public void addEvent(String eventId) {
        appendToField(FIELD_PHOTOS, eventId);
    }

    /**
     * Get the collaborations associated with an artist
     * @return collaborations map
     */
    public Set<String> getCollaborations() {
        return getFieldAsStringSet(FIELD_EVENTS);
    }
    
    /**
     * Adds a collaboration to an artist
     * @param artistId id of the artist the current artist has collaborated with
     */
    public void addCollaboration(String artistId) {
        appendToField(FIELD_COLLABORATIONS, artistId);
    }

    /**
     * Get the albums associated with an artist
     * @return album id set
     */
    public Set<String> getAlbums() {
        return getFieldAsStringSet(FIELD_ALBUM);
    }
    
    /**
     * Adds an album to an artist
     * @param albumId the album's id
     */
    public void addAlbum(String albumId) {
        appendToField(FIELD_ALBUM,albumId);
    }
}
