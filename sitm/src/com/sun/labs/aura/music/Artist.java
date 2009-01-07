/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.aura.util.Tag;
import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fm223201
 */
public class Artist extends ItemAdapter {

    public final static String FIELD_ALBUM = "album";
    public final static String FIELD_AUTO_TAGS = "autoTags";
    public final static String FIELD_BEGIN_YEAR = "beginYear";
    public final static String FIELD_BIOGRAPHY_SUMMARY = "biographySummary";
    public final static String FIELD_COLLABORATIONS = "collaborations";
    public final static String FIELD_END_YEAR = "endYear";
    public final static String FIELD_EVENTS = "events";
    public final static String FIELD_FOLLOWERS = "followers";
    public final static String FIELD_INFLUENCERS = "influencers";
    public final static String FIELD_PHOTOS = "photos";
    public final static String FIELD_SPOTIFY = "spotifyID";
    public final static String FIELD_POPULARITY = "popularity";
    public final static String FIELD_RELATED_ARTISTS = "relatedArtists";
    public final static String FIELD_SOCIAL_TAGS = "socialTags";
    public final static String FIELD_BIO_TAGS = "bioTags";
    public final static String FIELD_BLURB_TAGS = "blurbTags";
    public final static String FIELD_URLS = "urls";
    public final static String FIELD_VIDEOS = "videos";
    public final static String FIELD_LAST_CRAWL = "lastCrawl";
    public final static String FIELD_UPDATE_COUNT = "updateCount";
    public final static String FIELD_AUDIO = "audio";

    public final static Comparator<Artist> POPULARITY = new Comparator<Artist>() {
        public int compare(Artist o1, Artist o2) {
            float delta = o1.getPopularity() - o2.getPopularity();
            if (delta > 0) {
                return 1;
            } else if (delta < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    /**
     * Wraps an Item as an artist
     * @param item the item to be turned into an artist
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Artist(Item item) {
        super(item, Item.ItemType.ARTIST);
    }

    public Artist() {
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

    public void defineFields(DataStore ds) throws AuraException {
        try {
            ds.defineField(FIELD_ALBUM, Item.FieldType.STRING, true, false);
            ds.defineField(FIELD_AUTO_TAGS, Item.FieldType.STRING, true, false);
            ds.defineField(FIELD_BEGIN_YEAR, Item.FieldType.INTEGER, true, false);
            ds.defineField(FIELD_BIOGRAPHY_SUMMARY, Item.FieldType.STRING, true,
                    true);
            ds.defineField(FIELD_COLLABORATIONS, Item.FieldType.STRING, true,
                    false);
            ds.defineField(FIELD_END_YEAR, Item.FieldType.INTEGER, true, false);
            ds.defineField(FIELD_EVENTS, Item.FieldType.STRING, true, false);
            ds.defineField(FIELD_FOLLOWERS, Item.FieldType.STRING, true, false);
            ds.defineField(FIELD_INFLUENCERS, Item.FieldType.STRING, true, false);
            ds.defineField(FIELD_PHOTOS);
            ds.defineField(FIELD_POPULARITY, Item.FieldType.FLOAT, true, false);
            ds.defineField(FIELD_RELATED_ARTISTS, Item.FieldType.STRING, true,
                    false);
            ds.defineField(FIELD_SOCIAL_TAGS, Item.FieldType.STRING, true, false);
            ds.defineField(FIELD_BIO_TAGS, Item.FieldType.STRING, true, false);
            ds.defineField(FIELD_BLURB_TAGS, Item.FieldType.STRING, true, false);
            ds.defineField(FIELD_URLS);
            ds.defineField(FIELD_VIDEOS);
            ds.defineField(FIELD_SPOTIFY);
            ds.defineField(FIELD_LAST_CRAWL);
            ds.defineField(FIELD_UPDATE_COUNT);
            ds.defineField(FIELD_AUDIO);
        } catch(RemoteException ex) {
            throw new AuraException("Error defining fields for Album", ex);
        }
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
     * Gets the time that this item was last crawled
     * @return the time this item was last crawled in ms since the epoch
     */
    public long getLastCrawl() {
        return getFieldAsLong(FIELD_LAST_CRAWL);
    }

    /**
     * Sets the time when this item was last crawled to now.
     */
    public void setLastCrawl() {
        setField(FIELD_LAST_CRAWL, System.currentTimeMillis());
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
     * Gets the number of times this artist has been updated
     * @return the number of times this artist has been updated
     */
    public int getUpdateCount() {
        return getFieldAsInt(FIELD_UPDATE_COUNT);
    }

    /**
     * Sets the time when this item was last crawled to now.
     */
    public void incrementUpdateCount() {
        setField(FIELD_UPDATE_COUNT, getUpdateCount() + 1);
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
     * Gets the spotify id for the artist
     * @return the spotifiy id
     */
    public String getSpotifyID() {
        return getFieldAsString(FIELD_SPOTIFY, "");
    }

    /**
     * Sets the spotify id for  the artist
     * @param spotify id for  the artist
     */
    public void setSpotifyID(String id) {
        setField(FIELD_SPOTIFY, id);
    }

    /**
     * Adds a frequent tag
     * @param tag name of the tag
     * @param count tag count
     */
    public void addAutoTag(String tag, int count) {
        addTag(FIELD_AUTO_TAGS, tag, count);
    }

    /**
     * Gets the artist's auto tags 
     * @return tag map
     */
    public List<Tag> getAutoTags() {
        return getTagsAsList(FIELD_AUTO_TAGS);
    }

    /**
     * Sets an auto tag to the artist
     * @param tag name of the tag
     * @param count tag count
     */
    public void setAutoTag(String tag, int count) {
        setTag(FIELD_AUTO_TAGS, tag, count);
    }

    /**
     * Clears all of the autotags
     */
    public void clearAutoTags() {
        clearTags(FIELD_AUTO_TAGS);
    }

    /**
     * Gets the artist's social tags 
     * @return tag map
     */
    public List<Tag> getSocialTags() {
        return getTagsAsList(FIELD_SOCIAL_TAGS);
    }

    /**
     * Adds a social tag to the artist
     * @param tag name of the tag
     * @param count tag count
     */
    public void addSocialTag(String tag, int count) {
        addTag(FIELD_SOCIAL_TAGS, tag, count);
    }

    /**
     * Sets a social tag to the artist
     * @param tag name of the tag
     * @param count tag count
     */
    public void setSocialTag(String tag, int count) {
        setTag(FIELD_SOCIAL_TAGS, tag, count);
    }

    /**
     * Clears the social tags
     */
    public void clearSocialTags() {
        clearTags(FIELD_SOCIAL_TAGS);
    }

    /**
     * Gets the artist's bio tags 
     * @return tag map
     */
    public List<Tag> getBioTags() {
        return getTagsAsList(FIELD_BIO_TAGS);
    }

    /**
     * Adds a bio tag to the artist
     * @param tag name of the tag
     * @param count tag count
     */
    public void addBioTag(String tag, int count) {
        addTag(FIELD_BIO_TAGS, tag, count);
    }

    /**
     * Sets a bio tag to the artist
     * @param tag name of the tag
     * @param count tag count
     */
    public void setBioTag(String tag, int count) {
        setTag(FIELD_BIO_TAGS, tag, count);
    }
    
    /**
     * Clears the bio tags
     */
    public void clearBioTags() {
        clearTags(FIELD_BIO_TAGS);
    }

    /**
     * Gets the artist's blurb tags 
     * @return tag map
     */
    public List<Tag> getBlurbTags() {
        return getTagsAsList(FIELD_BLURB_TAGS);
    }

    /**
     * Adds a blurb tag to the artist
     * @param tag name of the tag
     * @param count tag count
     */
    public void addBlurbTag(String tag, int count) {
        addTag(FIELD_BLURB_TAGS, tag, count);
    }

    /**
     * Sets a blurb tag to the artist
     * @param tag name of the tag
     * @param count tag count
     */
    public void setBlurbTag(String tag, int count) {
        setTag(FIELD_BLURB_TAGS, tag, count);
    }

    /**
     * Clears the blurb tags
     */
    public void clearBlurbTags() {
        clearTags(FIELD_BLURB_TAGS);
    }

    /**
     * Gets the artist's associated URLs
     * @return associated urls
     */
    public Map<String, String> getUrls() {
        Map<String,String> map = (Map<String, String>) getFieldAsObject(FIELD_URLS);
        if (map == null) {
            map = new HashMap<String, String>();
        }
        return map;
    }

    /**
     * Adds an associated URL to artist
     * @param siteName name of the site
     * @param newURL URL of the artist's page
     */
    public void addUrl(String siteName, String newURL) {
        addObjectToMap(FIELD_URLS, siteName, newURL);
    }

    /**
     * Clears the URLS
     */
    public void clearUrls() {
        Map<String,String> map = (Map<String, String>) getFieldAsObject(FIELD_URLS);
        if (map != null) {
            map.clear();
        }
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
     * Clears the videos
     */
    public void clearVideos() {
        Set<String> videos = getFieldAsStringSet(FIELD_VIDEOS);
        if (videos != null) {
            videos.clear();
        }
    }

    /**
     * Adds a related artist
     * @param artist id of a related artist
     */
    public void addRelatedArtist(String artistID) {
        appendToField(FIELD_RELATED_ARTISTS, artistID);
    }

    /**
     * Get the videos associated with an artist
     * @return videos id set
     */
    public Set<String> getRelatedArtists() {
        return getFieldAsStringSet(FIELD_RELATED_ARTISTS);
    }

    /**
     * Clears the related artists
     */
    public void clearRelatedArtists() {
        Set<String> set = getFieldAsStringSet(FIELD_RELATED_ARTISTS);
        if (set != null) {
            set.clear();
        }
    }

    /**
     * Adds a follower (an artist influenced by this artist)
     * @param artist id of a following artist
     */
    public void addFollower(String artistID) {
        appendToField(FIELD_FOLLOWERS, artistID);
    }

    /**
     * Get the followers associated with an artist
     * @return videos id set
     */
    public Set<String> getFollowers() {
        return getFieldAsStringSet(FIELD_FOLLOWERS);
    }

    /**
     * Clears all followers associated with this artist
     */
    public void clearFollowers() {
        Set<String> set = getFieldAsStringSet(FIELD_FOLLOWERS);
        if (set != null) {
            set.clear();
        }
    }

    /**
     * Adds an influencer (an artist that influenced this artist)
     * @param artist id of a following artist
     */
    public void addInfluencer(String artistID) {
        appendToField(FIELD_INFLUENCERS, artistID);
    }

    /**
     * Get the followers associated with an artist
     * @return videos id set
     */
    public Set<String> getInfluencers() {
        return getFieldAsStringSet(FIELD_INFLUENCERS);
    }

    /**
     * Clears all influencers associated with this artist
     */
    public void clearInfluencers() {
        Set<String> set = getFieldAsStringSet(FIELD_INFLUENCERS);
        if (set != null) {
            set.clear();
        }
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
     * Clears all photos associated with this artist
     */
    public void clearPhotos() {
        Set<String> set = getFieldAsStringSet(FIELD_PHOTOS);
        if (set != null) {
            set.clear();
        }
    }

    /**
     * Get the audio associated with an artist
     * @return audio map
     */
    public Set<String> getAudio() {
        return getFieldAsStringSet(FIELD_AUDIO);
    }

    /**
     * Adds audio  to an artist
     * @param audio url to audio
     */
    public void addAudio(String audio) {
        appendToField(FIELD_AUDIO, audio);
    }

    /**
     * Clears all audio associated with this artist
     */
    public void clearAudio() {
        Set<String> set = getFieldAsStringSet(FIELD_AUDIO);
        if (set != null) {
            set.clear();
        }
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
        appendToField(FIELD_EVENTS, eventId);
    }

    /**
     * Clears all events associated with this artist
     */
    public void clearEvents() {
        Set<String> set = getFieldAsStringSet(FIELD_EVENTS);
        if (set != null) {
            set.clear();
        }
    }

    /**
     * Get the collaborations associated with an artist
     * @return collaborations map
     */
    public Set<String> getCollaborations() {
        return getFieldAsStringSet(FIELD_COLLABORATIONS);
    }

    /**
     * Adds a collaboration to an artist
     * @param artistId id of the artist the current artist has collaborated with
     */
    public void addCollaboration(String artistId) {
        appendToField(FIELD_COLLABORATIONS, artistId);
    }

    /**
     * Clears all collaborations associated with this artist
     */
    public void clearCollaborations() {
        Set<String> set = getFieldAsStringSet(FIELD_COLLABORATIONS);
        if (set != null) {
            set.clear();
        }
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
        appendToField(FIELD_ALBUM, albumId);
    }

    /**
     * Clears all albums associated with this artist
     */
    public void clearAlbums() {
        Set<String> set = getFieldAsStringSet(FIELD_ALBUM);
        if (set != null) {
            set.clear();
        }
    }
}
