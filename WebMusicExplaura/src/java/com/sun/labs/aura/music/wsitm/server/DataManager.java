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

package com.sun.labs.aura.music.wsitm.server;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Album;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.Event;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.Listener.Gender;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.music.Photo;
import com.sun.labs.aura.music.Recommendation;
import com.sun.labs.aura.music.RecommendationProfile;
import com.sun.labs.aura.music.RecommendationSummary;
import com.sun.labs.aura.music.RecommendationType;
import com.sun.labs.aura.music.SimType;
import com.sun.labs.aura.music.Video;
import com.sun.labs.aura.music.util.ExpiringLRUCache;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.searchTypes;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.aura.music.wsitm.client.items.AlbumDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistEvent;
import com.sun.labs.aura.music.wsitm.client.items.ArtistPhoto;
import com.sun.labs.aura.music.wsitm.client.items.ArtistVideo;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.SearchResults;
import com.sun.labs.aura.music.wsitm.client.WebException;
import com.sun.labs.aura.music.wsitm.client.items.TagDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ArtistRecommendation;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.items.RecsNTagsContainer;
import com.sun.labs.aura.music.wsitm.client.items.ScoredC;
import com.sun.labs.aura.music.wsitm.client.items.ScoredTag;
import com.sun.labs.aura.music.wsitm.client.items.ServerInfoItem;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author plamere
 */
public class DataManager implements Configurable {

    private static final String MDB_KEY = "MusicDatabase";
    private static final int NUMBER_TAGS_TO_SHOW = 20;
    private static final int NUMBER_SIM_ARTISTS = 15;
    private static final int NBR_REC_LISTENER = 20;
    private static final int SEC_TO_LIVE_IN_CACHE = 604800; // 1 week
    private static final int NUMBER_ARTIST_ORACLE = 1000;
    private static final int NUMBER_TAGS_ORACLE = 500;

    private Logger logger = Logger.getLogger("");
    ConfigurationManager configMgr;
    private ExpiringLRUCache cache;
    private MusicDatabase mdb;
    private int expiredTimeInDays = 0;

    private static final String beatlesMDID = "b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d";
    private static final String ANONYMOUS_USER_KEY = "ANONYMOUS_USER_KEY";
    private float beatlesPopularity = -1;

    private ArrayList<ScoredC<String>> artistOracle;
    private ArrayList<ScoredC<String>> tagOracle;
    private Map<String, SimType> simTypes;

    private HashSet<String> BANNED_NAMES;
    private HashSet<String> BANNED_MBIDs;

    /**
     * Creates a new instance of the datamanager
     * @param path  the path to the database
     * @param cacheSize  the size of the cache
     * @throws java.io.IOException
     */
    public DataManager(MusicDatabase mdb, int cacheSize) {

        logger.info("Instantiating new DataManager with cache size of " + cacheSize);

        cache = new ExpiringLRUCache(cacheSize, SEC_TO_LIVE_IN_CACHE);

        if (mdb == null) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, "MusicDatabase is null");
            throw new WebException(WebException.errorMessages.INIT_ERROR);
        }
        this.mdb = mdb;

        BANNED_NAMES = new HashSet<String>();
        BANNED_MBIDs = new HashSet<String>();
        BANNED_NAMES.add("Anal Cunt");
        BANNED_MBIDs.add("8cde362e-2c23-41d3-834f-5015aa3b334f");
        BANNED_NAMES.add("The Kinks");
        BANNED_MBIDs.add("17b53d9f-5c63-4a09-a593-dde4608e0db9");

        artistOracle = new ArrayList<ScoredC<String>>();
        tagOracle = new ArrayList<ScoredC<String>>();

        try {

            logger.info("Fetching " + NUMBER_ARTIST_ORACLE + " most popular artists from datastore");
            for (Artist a : mdb.artistGetMostPopular(MusicDatabase.PopularityMetric.LastFM, NUMBER_ARTIST_ORACLE)) {
                if (!BANNED_MBIDs.contains(a.getKey())) {
                    artistOracle.add(new ScoredC<String>(a.getName(), a.getPopularity()));
                }
            }

            logger.info("Fetching " + NUMBER_TAGS_ORACLE + " most popular tags");
            for (ArtistTag aT : mdb.artistTagGetMostPopular(NUMBER_ARTIST_ORACLE)) {
                tagOracle.add(new ScoredC<String>(aT.getName(), aT.getPopularity()));
            }
            logger.info("DONE");

            beatlesPopularity = mdb.artistLookup(beatlesMDID).getPopularity();

        } catch (AuraException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        simTypes = new HashMap<String, SimType>();
        for (SimType s : mdb.getSimTypes()) {
            simTypes.put(s.getName(), s);
        }

        // Create the anonymous user if he doesn't exist
        Listener l;
        try {
            l = mdb.getListener(ANONYMOUS_USER_KEY);
            if (l==null) {
                mdb.enrollListener(ANONYMOUS_USER_KEY);
            }
        } catch (AuraException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        logger.info("DataManager ready.");
    }

    public void onDestroy() {
    }

    public ArrayList<ScoredC<String>> getArtistOracle() {
        return artistOracle;
    }

    public ArrayList<ScoredC<String>> getTagOracle() {
        return tagOracle;
    }

    /**
     * Get the common tags between two artists
     * @param id1 the id of artist 1
     * @param id2 the id of artist 2
     * @param num number of tags to retreive
     * @return the common tags
     */
    public ItemInfo[] getCommonTags(String id1, String id2, int num, String simType)
            throws AuraException, RemoteException {
        List<Scored<String>> simList = simTypes.get(simType).explainSimilarity(id1, id2, num);

        // If the similarity type is related, we're getting artist ids so we must replace them with the artist's name
        if (simType.equals("Related")) {

            ArrayList<ItemInfo> artistResult = new ArrayList<ItemInfo>();

            for (Scored<String> ss : simList) {
                logger.finest("rel:"+ss.getItem());
                ArtistCompact aC = this.getArtistCompact(ss.getItem());
                if (aC != null) {
                    double score = ss.getScore();
                    double popularity = aC.getPopularity();
                    artistResult.add(new ItemInfo(aC.getId(), aC.getName(), score, popularity));
                }
            }
            return artistResult.toArray(new ItemInfo[0]);

        } else {
            return scoredTagStringToItemInfo(simList);
        }
        
    }

    /**
     * Returns a random list of artists from the most popular ones
     * @param nbr Number of artists to return
     * @return List of random artists compacts
     */
    public ArtistCompact[] getRandomPopularArtists(int nbr) throws AuraException, RemoteException {

        Random r = new Random();

        HashMap<String, ArtistCompact> artistSource = new HashMap<String, ArtistCompact>();
        for (Artist sA : mdb.artistGetMostPopular(MusicDatabase.PopularityMetric.Hotttnesss, nbr)) {
            if (sA.getHotttnesss() > 0.75) {
                artistSource.put(sA.getName(), artistToArtistCompact(sA));
            }
        }

        // If we don't have enough artists with the results from the echonest,
        // complement with random popular artists
        int tries = 0;
        while (artistSource.size() < nbr*2) {
            String name = artistOracle.get(r.nextInt(artistOracle.size())).getItem();
            // Add only the name of the artist to the map and differ loading
            // the actual object to if we really return it to the client
            if (!artistSource.containsKey(name)) {
                artistSource.put(name, null);
            }
            if (++tries>nbr*5) {
                break;
            }
        }

        // Return a random selection from our pool of artists
        ArrayList<ArtistCompact> aC = new ArrayList<ArtistCompact>();
        String[] keys = artistSource.keySet().toArray(new String[0]);
        int offset = r.nextInt(nbr*2-1);
        for (int i=0; i<nbr; i++) {
            String name = keys[((i+offset)%keys.length)];
            ArtistCompact aCC = artistSource.get( name );
            if (aCC==null) {
                List<Scored<Artist>> lsa = mdb.artistSearch(name, 1);
                ArtistDetails aD = artistToArtistDetails(lsa.get(0).getItem());
                cache.sput(aD.getId(), aD);
                aCC = aD.toArtistCompact();
            }
            aC.add(aCC);
        }
        return aC.toArray(new ArtistCompact[0]);
    }

    /**
     * Get the common tags between a tagMap and an artist
     * @param tagMap tag map containing tags and their weights
     * @param artistId
     * @param num number of tags to retreive
     * @return the commons tags
     * @throws com.sun.labs.aura.util.AuraException
     */
    public ItemInfo[] getCommonTags(Map<String, ScoredTag> tagMap, String artistId, int num) throws AuraException {
        List<Scored<String>> simList = mdb.artistExplainSimilarity(mapToWordCloud(tagMap), artistId, num);
        return scoredTagStringToItemInfo(simList);
    }

    /**
     * Updates the ArtistDetails object with similar artists based on the given simtype and popularity
     */
    private ArtistDetails updateSimilarArtists(ArtistDetails aD, SimType sT, Popularity pop) throws AuraException, RemoteException {
        
        aD.setSimilarArtists(getSimilarArtists(aD.getId(), sT, pop));
        return aD;
    }
    
    public ArrayList<ScoredC<ArtistCompact>> getSimilarArtists(String id, SimType sT, Popularity pop) throws AuraException, RemoteException {

        List<Scored<Artist>> scoredArtists = sT.findSimilarArtists(id, NUMBER_SIM_ARTISTS, pop);
        // return artists in socred order
        sortByArtistPopularity(scoredArtists);

        // collect all of the similar artists, but skip the seed artist
        ArrayList<ScoredC<ArtistCompact>> scoredArtistsC = new ArrayList<ScoredC<ArtistCompact>>();
        for (int i = 0; i < scoredArtists.size(); i++) {
            if (!id.equals(scoredArtists.get(i).getItem().getKey())) {
                scoredArtistsC.add(new ScoredC<ArtistCompact>(artistToArtistCompact(scoredArtists.get(i).getItem()), scoredArtists.get(i).getScore()));
            }
        }
        return scoredArtistsC;
    }

    /**
     * Gets the details for the given artist (by id)
     * @param id  the id of the artist
     * @param refresh if true, ignore cache and load from datastore
     * @param simTypeName name of the symType to use
     * @param popularity popularity enum element to use
     * @return  the artist details
     */
    public ArtistDetails getArtistDetails(String id, boolean refresh, String simTypeName, String popularity)
            throws AuraException, RemoteException {
        ArtistDetails details = null;

        details = (ArtistDetails) cache.sget(id);
        if (details == null || refresh) {
            details = (ArtistDetails) loadArtistDetailsFromStore(id);
            if (details != null) {
                cache.sput(id, details);
            } else {
                return null;
            }
        }
        return updateSimilarArtists(details, simTypes.get(simTypeName), stringToPopularity(popularity));
    }

    /**
     * Fetches an artist's details from the datastore
     * @param id the artist's id
     * @return the artist's details or null if the details are not in the datastore
     */
    private ArtistDetails loadArtistDetailsFromStore(String id)
            throws AuraException, RemoteException {
        logger.finest("loading artist from store :: " + id);
        Artist a = mdb.artistLookup(id);
        if (a == null) {
            return null;
        } else {
            return artistToArtistDetails(a);
        }
    }

    private String getThumbnailImageURL(Artist a) throws AuraException {
        String url = null;

        if (url == null) {
            Set<String> photoIDs = a.getPhotos();
            if (photoIDs.size() > 0) {
                String[] ids = photoIDs.toArray(new String[photoIDs.size()]);
                url = Photo.idToThumbnail(ids[0]);
            }
        }

        if (url == null) {
            Set<String> albumIDs = a.getAlbums();
            if (albumIDs.size() > 0) {
                String[] ids = albumIDs.toArray(new String[albumIDs.size()]);
                Album album = mdb.albumLookup(ids[0]);
                url = album.getAlbumArt();
            }
        }
        if (url == null) {
            url = "nopic.gif";
        }
        return url;
    }

    private ArtistCompact artistToArtistCompact(Artist a) throws AuraException,
            RemoteException {

        ArtistCompact aC = new ArtistCompact();

        aC.setName(a.getName());
        aC.setBeginYear(a.getBeginYear());
        aC.setEndYear(a.getEndYear());
        aC.setBiographySummary(a.getBioSummary());
        aC.setId(a.getKey());
        aC.setImageURL(getThumbnailImageURL(a));
        aC.setPopularity(a.getPopularity());
        aC.setSpotifyId(a.getSpotifyID());
               
        HashSet<String> hS = new HashSet<String>();
        hS.addAll(a.getAudio());
        aC.setAudio(hS);

        if (beatlesPopularity != -1) {
            aC.setNormPopularity(a.getPopularity() / beatlesPopularity);
        } else {
            aC.setNormPopularity(-1);
        }

        try {
            aC.setEncodedName(URLEncoder.encode(a.getName(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            aC.setEncodedName("Error converting name");
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }


        // Fetch list of distinctive tags
        WordCloud wC = mdb.artistGetDistinctiveTagNames(a.getKey(), NUMBER_TAGS_TO_SHOW);
        aC.setDistinctiveTags(WordCloudToIntemInfo(wC));

        return aC;
    }

    /**
     * Convert an artist to an ArtistDetails object
     * @param a artist to convert
     * @param simType similarity type to use to find similar artists
     * @return artistdetails object
     */
    private ArtistDetails artistToArtistDetails(Artist a)
            throws AuraException, RemoteException {

        ArtistDetails details = new ArtistDetails();

        details.setName(a.getName());
        details.setBeginYear(a.getBeginYear());
        details.setEndYear(a.getEndYear());
        details.setBiographySummary(a.getBioSummary());
        details.setId(a.getKey());
        details.setUrls(a.getUrls());
        details.setPhotos(getArtistPhotoFromIds(a.getPhotos()));
        details.setVideos(getArtistVideoFromIds(a.getVideos()));
        details.setPopularity(a.getPopularity());
        details.setSpotifyId(a.getSpotifyID());

        HashSet<String> hS = new HashSet<String>();
        hS.addAll(a.getAudio());
        details.setAudio(hS);

        if (beatlesPopularity != -1) {
            details.setNormPopularity(a.getPopularity() / beatlesPopularity);
        } else {
            details.setNormPopularity(-1);
        }

        try {
            details.setEncodedName(URLEncoder.encode(a.getName(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            details.setEncodedName("Error converting name");
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Fetch albums
        Set<String> albumSet = a.getAlbums();
        AlbumDetails[] albumDetailsArray = new AlbumDetails[albumSet.size()];
        int index = 0;
        for (Album storeAlbum : mdb.albumLookup(albumSet)) {
            albumDetailsArray[index] = new AlbumDetails();
            albumDetailsArray[index].setAsin(storeAlbum.getAsin());
            albumDetailsArray[index].setId(storeAlbum.getKey());
            albumDetailsArray[index].setTitle(storeAlbum.getTitle());
            index++;
        }
        details.setAlbums(albumDetailsArray);

        // Fetch upcoming events
        Set<String> eventsSet = a.getEvents();
        ArtistEvent[] eventsArray = new ArtistEvent[eventsSet.size()];
        index = 0;

        for (Event storeEvent : mdb.eventLookup(eventsSet)) {
            eventsArray[index] = new ArtistEvent();
            eventsArray[index].setDate(storeEvent.getDate());
            eventsArray[index].setEventID(storeEvent.getKey());
            eventsArray[index].setName(storeEvent.getName());
            eventsArray[index].setVenue(storeEvent.getVenueName());
            index++;
        }
        details.setEvents(eventsArray);

        // Fetch related artists
        Set<String> collSet = a.getRelatedArtists();
        if (collSet != null && collSet.size() > 0) {
            List<ArtistCompact> artistColl = new ArrayList<ArtistCompact>();
            for (Artist tempArtist : mdb.artistLookup(collSet)) {
                // If the related artist is not in our database, skip it
                if (tempArtist == null) {
                    continue;
                }
                artistColl.add(artistToArtistCompact(tempArtist));
            }
            if (artistColl.size() > 0) {
                details.setCollaborations(artistColl.toArray(new ArtistCompact[0]));
            }
        }

        // Fetch list of distinctive tags
        WordCloud distinctiveTags = mdb.artistGetDistinctiveTagNames(a.getKey(), NUMBER_TAGS_TO_SHOW);
        details.setDistinctiveTags(WordCloudToIntemInfo(distinctiveTags));

        return details;
    }

    /**
     * Add search attention
     * @param userKey user who performed the search
     * @param searchValue the query (which should include the type of search
     * (artist, by tag, for tag)
     * @param target the item that the user clicks on from the search list (so
     * if a use searches for 'josh' and on the search results list, clicks on
     * 'joshua radin' then the key for artist 'joshua radin' shoud be the target of the search
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void addSearchAttention(String userKey, String searchValue, String target)
            throws AuraException, RemoteException {
        if (userKey==null) {
            userKey = ANONYMOUS_USER_KEY;
        } else {
            userKey = decryptUserKey(userKey);
        }
        if (target!=null) {
            mdb.addAttention(userKey, target, Attention.Type.SEARCH, searchValue, null);
        }
    }

    /**
     * Get distinctive tags for an artist
     * @param artistID
     * @param count number of tags to retrieve
     * @return
     */
    public ItemInfo[] getDistinctiveTags(String artistID, int count) throws AuraException {
        //
        // Look in the cache to see if we already have the artist
        ArtistDetails aD = (ArtistDetails) cache.sget(artistID);
        if (aD != null) {
            return aD.getDistinctiveTags();
        } else {
            return WordCloudToIntemInfo(mdb.artistGetDistinctiveTagNames(artistID, count));
        }
    }

    public void saveTagCloud() throws AuraException {

        //TagCloud tC = mdb.tagCloudCreate(MDB_KEY, MDB_KEY);
        //mdb.tag

    }

    /**
     * Search for social tags
     * @param searchString the search string
     * @param maxResults  the maximum results to return
     * @return search results
     */
    public SearchResults tagSearch(String searchString, int maxResults)
            throws AuraException, RemoteException {
        logger.finest("DataManager::tagSearch: " + searchString);
        ItemInfo[] tagResults = scoredArtistTagToItemInfo(mdb.artistTagSearch(searchString, maxResults));

        SearchResults sr = new SearchResults(searchString,
                searchTypes.SEARCH_FOR_TAG_BY_TAG, tagResults);
        return sr;
    }

    /**
     * Search for an artist
     * @param maxResults maximum results to return
     * @return search results
     */
    public SearchResults artistSearch(String searchString, int maxResults) throws AuraException, RemoteException {
        logger.finest("DataManager::artistSearch: " + searchString);
        ItemInfo[] artistResults = scoredArtistToItemInfo(mdb.artistSearch(searchString, maxResults));

        SearchResults sr = new SearchResults(searchString,
                searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST, artistResults);
        return sr;
    }

    /**
     * Search for artists that have been tagged by a particular tag
     * @param searchString tag
     * @param maxResults maximum number of results to return
     * @return results
     */
    public SearchResults artistSearchByTag(String searchString, int maxResults)
            throws AuraException, RemoteException {
        logger.finest("DataManager::artistSearchByTag: " + searchString);

        ArtistTag tag = mdb.artistTagLookup(searchString);
        if (tag == null) {
            // found no results! treat this
            logger.finest("DataManager::artistSearchByTag. No results found for : " + searchString);
            return null;
        }

        ArrayList<ItemInfo> tagResults = new ArrayList<ItemInfo>();
        for (Tag t : tag.getTaggedArtist()) {
            if (tagResults.size() >= maxResults) {
                break;
            }
            Artist a = mdb.artistLookup(t.getTerm());
            if (a != null) {
                tagResults.add(new ItemInfo(t.getName(), a.getName(), t.getCount(), t.getFreq()));
            }
        }
        SearchResults sr = new SearchResults(searchString,
                searchTypes.SEARCH_FOR_ARTIST_BY_TAG,
                tagResults.toArray(new ItemInfo[0]));
        return sr;
    }

    /**
     * Converts a set of video ids to an array of ArtistVideo
     * @param videoSet set of video ids
     * @return VideoPhoto array
     */
    private ArtistVideo[] getArtistVideoFromIds(Set<String> videoSet)
            throws AuraException, RemoteException {
        ArtistVideo[] artistVideoArray = new ArtistVideo[videoSet.size()];
        int index = 0;
        for (Video dataStoreVideo : mdb.videoLookup(videoSet)) {
            artistVideoArray[index] = new ArtistVideo();
            artistVideoArray[index].setThumbnail(dataStoreVideo.getThumbnailUrl());
            artistVideoArray[index].setTitle(dataStoreVideo.getName());
            artistVideoArray[index].setUrl(dataStoreVideo.getUrl());
            index++;
        }
        return artistVideoArray;
    }

    /**
     * Converts a set of photo ids to an array of ArtistPhoto
     * @param photoSet set of photo ids
     * @return ArtistPhoto array
     */
    private ArtistPhoto[] getArtistPhotoFromIds(Set<String> photoSet)
            throws AuraException, RemoteException {
        ArtistPhoto[] artistPhotoArray = new ArtistPhoto[photoSet.size()];
        int index = 0;
        for (Photo dataStorePhoto : mdb.photoLookup(photoSet)) {
            artistPhotoArray[index] = new ArtistPhoto();
            artistPhotoArray[index].setCreatorRealName(dataStorePhoto.getCreatorRealName());
            artistPhotoArray[index].setCreatorUserName(dataStorePhoto.getCreatorUserName());
            artistPhotoArray[index].setId(dataStorePhoto.getKey());
            artistPhotoArray[index].setImageURL(dataStorePhoto.getImgUrl());
            artistPhotoArray[index].setSmallImageUrl(dataStorePhoto.getSmallImgUrl());
            artistPhotoArray[index].setThumbNailImageUrl(dataStorePhoto.getThumbnailUrl());
            artistPhotoArray[index].setTitle(dataStorePhoto.getTitle());
            artistPhotoArray[index].setPhotoPageURL(dataStorePhoto.getPhotoPageUrl());
            index++;
        }
        return artistPhotoArray;
    }

    private static boolean hasStickyPrefix(String tag) {
        return tag.startsWith("+");
    }

    private static boolean hasBannedPrefix(String tag) {
        return tag.startsWith("-");
    }

    private static String norm(String tag) {
        return tag.replaceFirst("[-\\+]+", "").trim();
    }

    /**
     * Converts a string representation of a wordcloud to a WordCloud. The string
     * can be of the form '(tag, weight)(+tag, weight)(-tag)(-tag, -.1)(tag)(+tag)
     * can be of the form '(tag, weight)(+tag, weight)(-tag)
     * or 'tag,tag,+tag'
     * or 'tag,tag,+tag,-tag'
     * + indicates a sticky tag, - indicates a banned tag
     * @param wc the string representation
     * @return a wordcloud or null
     */
    public HashMap<String, ScoredTag> convertStringToTagMap(String wc) throws AuraException {
        HashMap<String, ScoredTag> tagMap = new HashMap<String, ScoredTag>();
        Pattern pattern = Pattern.compile("(\\(([^,\\)]*)(,\\s*(-*[\\d\\.]+)\\s*)*)\\)");
        Matcher matcher = pattern.matcher(wc);
        while (matcher.find()) {
            String tag = matcher.group(2).trim();
            String sweight = "1";

            if (matcher.groupCount() > 3) {
                String s = matcher.group(4);
                if (s != null) {
                    sweight = s.trim();
                }
            }

            // Try to match
            String normTag = norm(tag);
            List<Scored<ArtistTag>> lstTags = mdb.artistTagSearch(normTag, 1);
            if (lstTags != null && lstTags.size() >= 1) {
                ArtistTag aT = lstTags.get(0).getItem();
                ScoredTag sT = new ScoredTag(aT.getName(), Double.valueOf(sweight), hasStickyPrefix(tag));

                if (hasBannedPrefix(tag)) {
                    sT.setScore(-1*sT.getScore());
                }
                tagMap.put(sT.getName(), sT);
            }
        }

        return tagMap;
    }

    public RecsNTagsContainer getRecommendationsFromString(String tagQueryString)
            throws AuraException, RemoteException {

        HashMap<String, ScoredTag> tagMap = convertStringToTagMap(tagQueryString);
        if (tagMap != null && tagMap.size() > 0) {
            // Extract popularity
            // regexbuddy string : [\w]*&popularity=([\w]+)
            Pattern regex = Pattern.compile("[\\w]*&popularity=([\\w]+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher regexMatcher = regex.matcher(tagQueryString);
            String popularity = "head";
            while (regexMatcher.find()) {
                popularity = regexMatcher.group(1);
            }

            ArrayList<ScoredC<ArtistCompact>> recs = getSteerableRecommendations(tagMap, popularity);
            return new RecsNTagsContainer(recs, tagMap);
        } else {
            return null;
        }
    }

    public ServerInfoItem getServerInfo() throws RemoteException, AuraException {

        ServerInfoItem info = new ServerInfoItem();

        HashMap<String, Integer> cacheInfo = new HashMap<String, Integer>();
        cacheInfo.put("ArtistDetails cache", cache.getSize());
        info.setCacheStatus(cacheInfo);

        DataStore dS = mdb.getDataStore();
        //info.setDataStoreNbrReplicants(dS.getPrefixes().size());

        HashMap<String, Integer> items = new HashMap<String, Integer>();
        for (ItemType t : ItemType.values()) {
            int count = (int) dS.getItemCount(t);
            if (count > 0) {
                items.put(t.toString(), count);
            }
        }
        info.setItemCnt(items);

        return info;
    }

    /**
     * Gets the details for the given tag (by id)
     * @param id the id of the tag
     * @param refresh if true, ignore cache and load from datastore
     * @return the tag details
     */
    public TagDetails getTagDetails(String id, boolean refresh) throws AuraException,
            RemoteException {
        TagDetails details = null;

        details = (TagDetails) cache.sget(id);
        if (details == null || refresh) {
            details = (TagDetails) loadTagDetailsFromStore(id);
            if (details != null) {
                cache.sput(id, details);
            } else {
                return null;
            }
        }
        return details;
    }

    public ItemInfo[] getSimilarTags(String tagId) throws AuraException, RemoteException {

        // Look in cache
        TagDetails details = null;
        details = (TagDetails) cache.sget(tagId);

        if (details == null) {
            // Fetch similar tags
            List<Scored<ArtistTag>> simTags = mdb.artistTagFindSimilar(tagId, NUMBER_TAGS_TO_SHOW);
            sortArtistTag(simTags, Sorter.sortFields.POPULARITY);
            return scoredArtistTagToItemInfo(simTags);
        } else {
            return details.getSimilarTags();
        }
    }

    public ArrayList<ScoredC<ArtistCompact>> getRepresentativeArtistsOfTag(String tagId) throws AuraException, RemoteException {

        // Look in cache
        TagDetails details = null;
        details = (TagDetails) cache.sget(tagId);

        if (details == null) {
            details = this.getTagDetails(tagId, false);
        }
        return details.getScoredRepArtists();
    }

    /**
     * Perform a sync between a listener and listenerdetails, givin priority to
     * what's contained in the listenerDetails
     * @param l
     * @param lD
     * @return updated listener
     */
    private Listener syncListeners(Listener l, ListenerDetails lD,
            boolean updateRecommendations) throws AuraException, RemoteException {

        if (lD.getGender() != null) {
            String gender = lD.getGender().toString();
            for (Gender g : Gender.values()) {
                if (g.toString().equals(gender)) {
                    l.setGender(g);
                }
            }
        } else if (l.getGender() != null) {
            lD.setGender(l.getGender().toString());
        }

        if (lD.getCountry() != null) {
            l.setLocaleCountry(lD.getCountry());
        } else if (l.getLocaleCountry() != null) {
            lD.setCountry(l.getLocaleCountry());
        }

        if (lD.getPandoraUser() != null) {
            l.setPandoraName(lD.getPandoraUser());
        } else if (l.getPandoraName() != null) {
            lD.setPandoraUser(l.getPandoraName());
        }

        if (lD.getLastFmUser() != null) {
            l.setLastFmName(lD.getLastFmUser());
        } else if (l.getLastFmName() != null) {
            lD.setLastFmUser(l.getLastFmName());
        }

        if (updateRecommendations) {
            // Fetch info for recommended artists
            ArrayList<ArtistCompact> aCompact = new ArrayList<ArtistCompact>();
            try {
                for (Scored<Artist> a : mdb.getRecommendations(l.getKey(), NBR_REC_LISTENER)) {
                    aCompact.add(artistToArtistCompact(a.getItem()));
                }
                lD.setRecommendations(aCompact.toArray(new ArtistCompact[0]));
            } catch (NullPointerException e) {
                // @todo remove this when fixed on server
                logger.severe("null pointer exception on get recommendations!!!!!! fix this!!!");
            }
        }

        return l;
    }

    private ListenerDetails listenerToListenerDetails(Listener l, ListenerDetails lD,
            boolean updateRecommendations) throws AuraException, RemoteException {

        if (l.getGender() != null) {
            lD.setGender(l.getGender().toString());
        }

        if (l.getLocaleCountry() != null) {
            lD.setCountry(l.getLocaleCountry());
        }

        if (l.getPandoraName() != null) {
            lD.setPandoraUser(l.getPandoraName());
        }

        if (l.getLastFmName() != null) {
            lD.setLastFmUser(l.getLastFmName());
        }

        if (updateRecommendations) {
            // Fetch info for recommended artists
            ArrayList<ArtistCompact> aCompact = new ArrayList<ArtistCompact>();
            for (Scored<Artist> a : mdb.getRecommendations(l.getKey(), NBR_REC_LISTENER)) {
                aCompact.add(artistToArtistCompact(a.getItem()));
            }
            lD.setRecommendations(aCompact.toArray(new ArtistCompact[0]));
        }

        ArrayList<ItemInfo> iI = new ArrayList<ItemInfo>();
        List<Scored<ArtistTag>> lT = mdb.listenerGetDistinctiveTags(l.getKey(), 50);
        if (lT != null && lT.size() > 0) {
            double maxSize = lT.get(0).getScore();
            for (Scored<ArtistTag> sAT : lT) {
                iI.add(new ItemInfo(sAT.getItem().getKey(), sAT.getItem().getName(), sAT.getScore() / maxSize, sAT.getItem().getPopularity()));
            }
        }
        lD.setUserTagCloud(iI.toArray(new ItemInfo[0]));

        return lD;
    }

    /**
     * Place holder for encryption function
     * @param openID
     * @return
     */
    public String encryptUserKey(String openID) {
        return openID;
    }

    /**
     * Place holder for decryption function
     * @param encryptedKey
     * @return
     */
    private String decryptUserKey(String encryptedKey) {
        return encryptedKey;
    }

    public ListenerDetails establishNonOpenIdUserConnection(String userKey)
            throws AuraException, RemoteException {

        ListenerDetails lD = new ListenerDetails();
        Listener l = null;

        l = mdb.getListener(userKey);

        if (l == null) {
            logger.warning("Non openID user '" + userKey + "' does not exist.");
            throw new AuraException("User '" + userKey + "' does not exist.");
        } else {
            logger.finer("Non openID user '" + userKey + "' fetched.");
        }

        lD = listenerToListenerDetails(l, lD, true);
        lD.setOpenId(userKey);
        lD.setUserKey(encryptUserKey(userKey));
        lD.setIsLoggedIn(true);

        return lD;
    }

    /**
     * Perform the datastore operations necessary when a user successfully logs in
     * @param lD his listenerDetails obtained from his active session
     * @return his listenerDetails with any modifications that were necessary
     */
    public ListenerDetails establishUserConnection(ListenerDetails lD)
            throws AuraException, RemoteException {

        Listener l = null;

        l = mdb.getListener(lD.getOpenId());

        if (l == null) {
            logger.fine("Creating new user in datastore: " + lD.getOpenId());
            l = mdb.enrollListener(lD.getOpenId());
        } else {
            logger.fine("Retrieved user from datastore: " + lD.getOpenId());
        }

        l = syncListeners(l, lD, true);
        mdb.updateListener(l);

        // Get the user tag cloud
        ArrayList<ItemInfo> iI = new ArrayList<ItemInfo>();
        List<Scored<ArtistTag>> lT = mdb.listenerGetDistinctiveTags(l.getKey(), 50);
        if (lT != null && lT.size() > 0) {
            double maxSize = lT.get(0).getScore();
            for (Scored<ArtistTag> sAT : lT) {
                iI.add(new ItemInfo(sAT.getItem().getKey(), sAT.getItem().getName(), sAT.getScore() / maxSize, sAT.getItem().getPopularity()));
            }
        }
        lD.setUserTagCloud(iI.toArray(new ItemInfo[0]));

        return lD;

    }

    public void addItemAttention(String userId, String artistId, Type attentionType) throws AuraException, RemoteException {
        if (attentionType == Type.PLAYED) {
            try {
                mdb.addPlayAttention(userId, artistId, 1);
            } catch (Exception e) {
                logger.warning("exception!! " + e.toString());
                throw new AuraException(e.toString());
            }
        } else if (attentionType == Type.VIEWED) {
            mdb.addViewedAttention(userId, artistId);
        } else {
            throw new AuraException("Attention type not yet implemented (" + attentionType.toString() + ")");
        }
    }

    public List<Attention> getLastAttentionData(String userId, Type type, int count) throws AuraException, RemoteException {
        return mdb.getLastAttentionData(userId, type, count);
    }

    public void updateUser(ListenerDetails lD) throws AuraException, RemoteException {
        Listener l = syncListeners(mdb.getListener(lD.getOpenId()), lD, false);
        mdb.updateListener(l);
    }

    public void updateUserSongRating(String userId, int rating, String artistId)
            throws AuraException, RemoteException {

        logger.finest("Setting rating " + rating + " for artist " + artistId + " for user " + userId);
        mdb.addRating(userId, artistId, rating);
    }

    public int fetchUserSongRating(String userId, String artistID)
            throws AuraException, RemoteException {

        logger.finest("Fetching rating for artist " + artistID + " for user " + userId);
        return mdb.getLatestRating(userId, artistID);
    }

    public HashMap<String, Integer> fetchUserSongRating(String userId, Set<String> artistID)
            throws AuraException, RemoteException {

        HashMap<String, Integer> ratingMap = new HashMap<String, Integer>();

        logger.finest("Fetching rating for artist " + artistID + " for user " + userId);
        for (String aID : artistID) {
            ratingMap.put(aID, mdb.getLatestRating(userId, aID));
        }

        return ratingMap;
    }

    /**
     * Load an artist compact from the cache or from the store
     */
    public ArtistCompact getArtistCompact(String artistId) throws AuraException, RemoteException {
        
        ArtistDetails details = null;

        details = (ArtistDetails) cache.sget(artistId);
        if (details == null) {
            details = (ArtistDetails) loadArtistDetailsFromStore(artistId);
            if (details != null) {
                cache.sput(artistId, details);
                return details.toArtistCompact();
            } else {
                return null;
            }
        } else {
            return details.toArtistCompact();
        }
    }

    /**
     * Converts a string representing a popularity to the corresponding Popularity enum element
     * @param pop string representation of popularity element
     * @return popularity element. Defaults to ALL if popularity is not matched
     */
    public Popularity stringToPopularity(String pop) {
        pop = pop.toUpperCase();
        for (Popularity p : Popularity.values()) {
            if (p.toString().equals(pop)) {
                return p;
            }
        }
        return Popularity.ALL;
    }
    
    public SimType stringToSimType(String sT) throws AuraException {
        SimType s = simTypes.get(sT);
        if (s == null) {
            throw new AuraException("Invalid similarity type '"+sT+"'");
        } else {
            return s;
        }
    }

    public ArrayList<ScoredC<ArtistCompact>> getSteerableRecommendations(Map<String, ScoredTag> tagMap, String popularity)
            throws AuraException, RemoteException {

        List<Scored<Artist>> lsA = mdb.wordCloudFindSimilarArtists(mapToWordCloud(tagMap), 
                NUMBER_SIM_ARTISTS, stringToPopularity(popularity));
        ArrayList<ScoredC<ArtistCompact>> aCArray = new ArrayList<ScoredC<ArtistCompact>>();
        for (Scored<Artist> sA : lsA) {
            ArtistDetails aD = artistToArtistDetails(sA.getItem());
            cache.sput(aD.getId(), aD);
            aCArray.add(new ScoredC(aD.toArtistCompact(), sA.getScore()));
        }
        return aCArray;
    }

    private WordCloud mapToWordCloud(Map<String, ScoredTag> tagMap) {
        WordCloud wC = new WordCloud();
        for (String tagName : tagMap.keySet()) {
            ScoredTag sT = tagMap.get(tagName);
            wC.add(new Scored<String>(tagName, sT.getScore()));
            if (sT.getScore()<0) {
                wC.addBannedWord(tagName);
            } else if (sT.isSticky()) {
                wC.addStickyWord(tagName);
            }
        }
        return wC;
    }

    public HashSet<String> fetchUserTagsForItem(String listenerId, String itemId)
            throws AuraException, RemoteException {
        HashSet<String> tags = new HashSet<String>();
        tags.addAll(mdb.getTags(listenerId, itemId));
        return tags;
    }

    public void addUserTagForItem(String listenerId, String itemId, String tag)
            throws AuraException, RemoteException {
        mdb.addTag(listenerId, itemId, tag);
    }

    public TagDetails loadTagDetailsFromStore(String id) throws AuraException,
            RemoteException {
        logger.finest("searching for tag :" + id);
        ArtistTag tag = mdb.artistTagLookup(id);
        if (tag == null) {
            logger.finest("null on " + id);
            return null;
        }
        TagDetails details = new TagDetails();

        details.setDescription(tag.getDescription());
        details.setId(id);
        details.setName(tag.getName());
        details.setPhotos(getArtistPhotoFromIds(tag.getPhotos()));
        details.setVideos(getArtistVideoFromIds(tag.getVideos()));
        details.setPopularity(tag.getPopularity());

        try {
            details.setEncodedName(URLEncoder.encode(tag.getName(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            details.setEncodedName("Error encoding name");
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Fetch representative artists
        List<Scored<Tag>> taggedArtists = sortTag(
                filterTaggedArtists(tag.getTaggedArtist()), TagSorter.sortFields.COUNTorSCORE);

        ArrayList<ScoredC<ArtistCompact>> lac = new ArrayList<ScoredC<ArtistCompact>>();
        for (Scored<Tag> sT : taggedArtists.subList(0, getMax(taggedArtists, NUMBER_TAGS_TO_SHOW))) {
            lac.add(new ScoredC<ArtistCompact>(getArtistCompact(sT.getItem().getName()), sT.getScore()));
        }
        details.setRepresentativeArtists(lac);

        // Fetch similar tags
        List<Scored<ArtistTag>> simTags = mdb.artistTagFindSimilar(id, NUMBER_TAGS_TO_SHOW);
        sortArtistTag(simTags, Sorter.sortFields.POPULARITY);
        details.setSimilarTags(scoredArtistTagToItemInfo(simTags));

        return details;
    }

    private List<Tag> filterTaggedArtists(List<Tag> taggedArtists) throws AuraException {
        List<Tag> filteredList = new ArrayList<Tag>();

        for (Tag tag : taggedArtists) {
            if (mdb.artistLookup(tag.getName()) != null) {
                filteredList.add(tag);
            }
        }
        return filteredList;
    }

    /**
     * Returns the maximum between the supplied list's size or the maximum
     * amount of tags we are allowed to show
     * @param l list to check
     * @return
     */
    private final int getMax(List l, final int maxNumber) {
        int listSize = l.size();
        if (listSize < maxNumber) {
            return listSize;
        } else {
            return maxNumber;
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void close() throws AuraException, RemoteException {        //@todo fix this
        //logger.close();
    }

    private void sortByArtistPopularity(List<Scored<Artist>> scoredArtists) {
        Collections.sort(scoredArtists, new ArtistPopularitySorter());
        Collections.reverse(scoredArtists);
    }

    private List<Scored<Tag>> sortTag(List<Tag> tags, Sorter.sortFields sortBy) {
        List<Scored<Tag>> scoredTags = new ArrayList<Scored<Tag>>();
        for (Tag t : tags) {
            scoredTags.add(new Scored(t, t.getCount()));
        }
        return sortScoredTag(scoredTags, sortBy);
    }

    private List<Scored<Tag>> sortScoredTag(List<Scored<Tag>> scoredTags, Sorter.sortFields sortBy) {
        Collections.sort(scoredTags, new TagSorter(sortBy));
        Collections.reverse(scoredTags);
        return scoredTags;
    }

    private void sortArtistTag(List<Scored<ArtistTag>> scoredTags, Sorter.sortFields sortBy) {
        Collections.sort(scoredTags, new ArtistTagSorter(sortBy));
        Collections.reverse(scoredTags);
    }

    private List<Scored<ArtistTag>> sortArtistTag(List<ArtistTag> tags, Sorter.sortFields sortBy) {
        List<Scored<ArtistTag>> scoredTags = new ArrayList<Scored<ArtistTag>>();
        for (ArtistTag t : tags) {
            scoredTags.add(new Scored(t, t.getPopularity()));
        }
        sortArtistTag(scoredTags, sortBy);
        return scoredTags;
    }

    /**
     * Converts a list of scored tags to an array of itemInfo
     * @param tags List of scored tags
     * @param isArtist set true if the tag object actually contains artist information
     * @return
     */
    private ItemInfo[] tagToItemInfo(List<Scored<Tag>> tags, boolean isArtist)
            throws AuraException {

        Artist a;
        ArtistDetails aD;
        String artistName;
        ItemInfo[] artistTagResults = new ItemInfo[tags.size()];

        for (int i = 0; i < artistTagResults.length; i++) {
            Tag t = tags.get(i).getItem();
            double score = tags.get(i).getScore();
            double popularity = t.getFreq();
            if (isArtist) {
                // We need to fetch each artist's name. First look in the cache if we have the details
                aD = (ArtistDetails) cache.sget(t.getName());
                if (aD == null) {
                    a = mdb.artistLookup(t.getName());
                    artistName = a.getName();
                } else {
                    artistName = aD.getName();
                }
                artistTagResults[i] = new ItemInfo(t.getName(), artistName, score, popularity);
            } else {
                artistTagResults[i] = new ItemInfo(ArtistTag.nameToKey(t.getName()), t.getName(), score, popularity);
            }
        }
        return artistTagResults;
    }

    private ItemInfo[] WordCloudToIntemInfo(WordCloud wC) {

        ItemInfo[] tagResults = new ItemInfo[wC.size()];

        int index = 0;
        for (Scored<String> sS : wC) {
            tagResults[index] = new ItemInfo(ArtistTag.nameToKey(sS.getItem()),
                    sS.getItem(), sS.getScore(), sS.getScore());
            index++;
        }
        return tagResults;
    }

    /**
     * Converts a list of scored artists and returns them in an iteminfo array
     * @param scoredArtists scored list of items (that will be cast as artists)
     * @return
     */
    private ItemInfo[] scoredArtistTagToItemInfo(List<Scored<ArtistTag>> scoredArtists) throws AuraException {

        ItemInfo[] artistTagResults = new ItemInfo[scoredArtists.size()];

        for (int i = 0; i < artistTagResults.length; i++) {
            ArtistTag artistTag = scoredArtists.get(i).getItem();
            double score = scoredArtists.get(i).getScore();
            double popularity = mdb.artistTagGetNormalizedPopularity(artistTag);
            artistTagResults[i] = new ItemInfo(artistTag.getKey(), artistTag.getName(), score, popularity);
        }

        return artistTagResults;
    }

    public HashMap<String, String> getSimTypes() {
        logger.finest("Getting sim types");
        HashMap<String, String> storeSimTypes = new HashMap<String, String>();
        for (SimType s : this.simTypes.values()) {
            storeSimTypes.put(s.getName(), s.getDescription());
        }
        return storeSimTypes;
    }

    public HashMap<String, String> getArtistRecommendationTypes() {
        logger.finest("Getting rec types");
        HashMap<String, String> recTypeMap = new HashMap<String, String>();
        for (RecommendationType rT : mdb.getArtistRecommendationTypes()) {
            recTypeMap.put(rT.getName(), rT.getDescription());
        }
        return recTypeMap;
    }

    public ArrayList<ArtistRecommendation> getRecommendations(String recTypeName, String userId, int cnt) throws AuraException, RemoteException {
        logger.finest("Getting recommendations for user " + userId + " using recType:" + recTypeName);
        ArrayList<ArtistRecommendation> aR = new ArrayList<ArtistRecommendation>();

        RecommendationType recType = mdb.getArtistRecommendationType(recTypeName);
        RecommendationSummary rS = recType.getRecommendations(userId, cnt, new Rp());
        for (Recommendation r : rS.getRecommendations()) {
            ArtistCompact aC = this.getArtistCompact(r.getId());
            if (aC != null) {
                if (recType.getType() == ItemType.ARTIST) {
                    aR.add(new ArtistRecommendation(aC, scoredArtistIdsToItemInfo(r.getExplanation()), r.getScore(), rS.getExplanation()));
                } else {
                    aR.add(new ArtistRecommendation(aC, scoredTagStringToItemInfo(r.getExplanation()), r.getScore(), rS.getExplanation()));
                }
            }
        }
        return aR;
    }

    public class Rp implements RecommendationProfile {
    }

    /**
     * Converts a list of scored tags and returns them in an iteminfo array
     * @param scoredArtists scored list of items (that will be cast as artists)
     * @return
     */
    private ItemInfo[] scoredTagToItemInfo(List<Scored<Tag>> scoredTags) {

        ItemInfo[] tagResults = new ItemInfo[scoredTags.size()];

        for (int i = 0; i < tagResults.length; i++) {
            Tag t = scoredTags.get(i).getItem();
            double score = scoredTags.get(i).getScore();
            double popularity = t.getCount();
            tagResults[i] = new ItemInfo(ArtistTag.nameToKey(t.getName()), t.getName(), score, popularity);
        }

        return tagResults;
    }

    /**
     * Converts a list of scored artists and returns them in an iteminfo array
     * @param scoredArtists scored list of items (that will be cast as artists)
     * @param fetchTags fetch the distinctive tags and spotify id and store them in ItemInfo?
     * @return
     */
    private ItemInfo[] scoredArtistToItemInfo(List<Scored<Artist>> scoredArtists) throws AuraException {

        ItemInfo[] artistResults = new ItemInfo[scoredArtists.size()];

        for (int i = 0; i < artistResults.length; i++) {
            Artist artist = scoredArtists.get(i).getItem();
            double score = scoredArtists.get(i).getScore();
            double popularity = artist.getPopularity();
            artistResults[i] = new ItemInfo(artist.getKey(), artist.getName(), score, popularity);
        }

        return artistResults;
    }

    /**
     * Converts a list of scored string representing tag ids to an itemInfo array
     * @param strList
     * @return item info array
     */
    private ItemInfo[] scoredTagStringToItemInfo(List<Scored<String>> strList) {
        List<ItemInfo> tagsArray = new ArrayList<ItemInfo>();
        for (Scored<String> sS : strList.subList(0, getMax(strList, NUMBER_TAGS_TO_SHOW))) {
            try {
                ArtistTag aT = mdb.artistTagLookup(ArtistTag.nameToKey(sS.getItem()));
                Float popularity;
                if (aT == null) {
                    popularity = new Float(0.0);
                } else {
                    popularity = aT.getPopularity();
                }
                tagsArray.add(new ItemInfo(ArtistTag.nameToKey(sS.getItem()), sS.getItem(), sS.getScore(),
                        popularity));
            } catch (AuraException ex) {
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return tagsArray.subList(0, this.getMax(tagsArray, NUMBER_TAGS_TO_SHOW)).toArray(new ItemInfo[0]);
    }

    /**
     * Converts a list of scored string representing tag ids to an itemInfo array
     * @param strList
     * @return item info array
     */
    private ItemInfo[] scoredArtistIdsToItemInfo(List<Scored<String>> strList) throws RemoteException {
        List<ItemInfo> idsArray = new ArrayList<ItemInfo>();
        for (Scored<String> sS : strList.subList(0, getMax(strList, NUMBER_TAGS_TO_SHOW))) {
            try {
                ArtistCompact aC = this.getArtistCompact(sS.getItem());
                if (aC == null) {
                    continue;
                }

                idsArray.add(new ItemInfo(aC.getId(), sS.getItem(), sS.getScore(),
                        aC.getPopularity()));
            } catch (AuraException ex) {
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return idsArray.subList(0, this.getMax(idsArray, NUMBER_TAGS_TO_SHOW)).toArray(new ItemInfo[0]);
    }

    public int getExpiredTimeInDays() {
        return expiredTimeInDays;
    }

    public void setExpiredTimeInDays(int expiredTimeInDays) {
        this.expiredTimeInDays = expiredTimeInDays;
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        //@todo fix this
        //log.log("annon", "WebMusicExplaura (Datamanager) newProperties called", "");
        mdb = (MusicDatabase) ps.getComponent(MDB_KEY);
    //int cacheSize = (Integer)ps.getComponent(CACHE_SIZE);

    }

    public MusicDatabase getMusicDatabase() {
        return mdb;
    }
}


class TagScoreAccumulator {

    private String name;
    private double score;

    public TagScoreAccumulator(String name, double score) {
        this.name = name;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return ArtistTag.nameToKey(name);
    }

    public void accum(double score) {
        this.score += score;
    }
}

class ArtistPopularitySorter implements Comparator<Scored<Artist>> {

    public int compare(Scored<Artist> o1, Scored<Artist> o2) {
        double s1 = o1.getItem().getPopularity();
        double s2 = o2.getItem().getPopularity();
        if (s1 > s2) {
            return 1;
        } else if (s1 < s2) {
            return -1;
        } else {
            return 0;
        }
    }
}

abstract class Sorter {

    protected sortFields sortBy;

    public static enum sortFields {
        COUNTorSCORE,
        POPULARITY
    }

    public Sorter(sortFields sortBy) {
        this.sortBy = sortBy;
    }
}

class TagSorter extends Sorter implements Comparator<Scored<Tag>> {

    public TagSorter(sortFields sortBy) {
        super(sortBy);
    }

    private final double getField(Scored<Tag> o) {
        if (sortBy == sortFields.COUNTorSCORE) {
            return o.getItem().getCount();
        } else {
            return o.getItem().getFreq();
        }
    }

    public int compare(Scored<Tag> o1, Scored<Tag> o2) {
        //@todo freq instead of pop
        double s1 = getField(o1);
        double s2 = getField(o2);
        if (s1 > s2) {
            return 1;
        } else if (s1 < s2) {
            return -1;
        } else {
            return 0;
        }
    }
}

class ArtistTagSorter extends Sorter implements Comparator<Scored<ArtistTag>> {

    public ArtistTagSorter(sortFields sortBy) {
        super(sortBy);
    }

    private final double getField(Scored<ArtistTag> o) {
        if (sortBy == sortFields.COUNTorSCORE) {
            return o.getScore();
        } else {
            return o.getItem().getPopularity();
        }
    }

    public int compare(Scored<ArtistTag> o1, Scored<ArtistTag> o2) {
        //@todo freq instead of pop
        double s1 = getField(o1);
        double s2 = getField(o2);
        if (s1 > s2) {
            return 1;
        } else if (s1 < s2) {
            return -1;
        } else {
            return 0;
        }
    }
}
