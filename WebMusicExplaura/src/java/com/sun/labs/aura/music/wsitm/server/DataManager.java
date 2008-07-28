/*
 * DataManager.java
 *
 * Created on April 1, 2007, 6:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.server;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.music.Album;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.Event;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.Listener.Gender;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.Photo;
import com.sun.labs.aura.music.SimType;
import com.sun.labs.aura.music.Video;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.searchTypes;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.aura.music.wsitm.client.items.Details;
import com.sun.labs.aura.music.wsitm.client.items.AlbumDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistEvent;
import com.sun.labs.aura.music.wsitm.client.items.ArtistPhoto;
import com.sun.labs.aura.music.wsitm.client.items.ArtistVideo;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.SearchResults;
import com.sun.labs.aura.music.wsitm.client.items.TagDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class DataManager implements Configurable {

    private static final String MDB_KEY ="MusicDatabase";
    private static final String CACHE_SIZE ="cacheSize";

    private static final int DEFAULT_CACHE_SIZE = 500;
    private static final int NUMBER_TAGS_TO_SHOW = 20;
    private static final int NUMBER_SIM_ARTISTS = 20;
    private static final int NBR_REC_LISTENER = 20;
    private static final int SEC_TO_LIVE_IN_CACHE = 604800; // 1 week
    private static final int NUMBER_ARTIST_ORACLE = 1000;
    private static final int NUMBER_TAGS_ORACLE = 500;

    private Logger logger = Logger.getLogger("");

    ConfigurationManager configMgr;

    private Map<String, ExpiringLRUCache> cache;
    private MusicDatabase mdb;
    private int expiredTimeInDays = 0;

    private static final String beatlesMDID="b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d";
    private float beatlesPopularity=-1;

    private List<String> artistOracle;
    private List<String> tagOracle;

    private Map<String, SimType> simTypes;

    /**
     * Creates a new instance of the datamanager
     * @param path  the path to the database
     * @param cacheSize  the size of the cache
     * @throws java.io.IOException
     */
    public DataManager(MusicDatabase mdb, int cacheSize) {

        logger.info("Instantiating new DataManager with cache size of "+cacheSize);

        //int cacheSize = (int)configMgr.lookup(CACHE_SIZE);
        cache = new HashMap<String, ExpiringLRUCache>();
        for (SimType s : mdb.getSimTypes()) {
            cache.put(s.getName(), new ExpiringLRUCache(cacheSize, SEC_TO_LIVE_IN_CACHE));
        }
        this.mdb = mdb;

        artistOracle = new ArrayList<String>();
        tagOracle = new ArrayList<String>();

        try {
            logger.info("Fetching "+NUMBER_ARTIST_ORACLE+" most popular artists...");
            artistOracle = mdb.artistGetMostPopularNames(NUMBER_ARTIST_ORACLE);
            logger.info("Fetching "+NUMBER_TAGS_ORACLE+" most popular tags...");
            tagOracle = mdb.artistTagGetMostPopularNames(NUMBER_TAGS_ORACLE);
            logger.info("DONE");

            beatlesPopularity=mdb.artistLookup(beatlesMDID).getPopularity();
        } catch (AuraException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        simTypes = new HashMap<String, SimType>();
        for (SimType s : mdb.getSimTypes()) {
            simTypes.put(s.getName(), s);
        }

        logger.info("DataManager ready.");
    }

    public List<String> getArtistOracle() {
        return artistOracle;
    }

    public List<String> getTagOracle() {
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
        return scoredTagStringToItemInfo(simList);
    }

    /**
     * Get the common tags between a tagMap and an artist
     * @param tagMap tag map containing tags and their weights
     * @param artistId
     * @param num number of tags to retreive
     * @return the commons tags
     * @throws com.sun.labs.aura.util.AuraException
     */
    public ItemInfo[] getCommonTags(Map<String, Double> tagMap, String artistId, int num) throws AuraException {
        List<Scored<String>> simList = mdb.artistExplainSimilarity(mapToWordCloud(tagMap), artistId, num);
        return scoredTagStringToItemInfo(simList);
    }

    /**
     * Gets the details for the given artist (by id)
     * @param id  the id of the artist
     * @param refresh if true, ignore cache and load from datastore
     * @param simTypeName name of the symType to use
     * @return  the artist details
     */
    public ArtistDetails getArtistDetails(String id, boolean refresh, String simTypeName)
            throws AuraException, RemoteException {
        ArtistDetails details = null;

        details = (ArtistDetails) cache.get(simTypeName).sget(id);
        if (details == null || refresh) {
            details = (ArtistDetails) loadArtistDetailsFromStore(id, simTypes.get(simTypeName));
            if (details != null) {
                cache.get(simTypeName).sput(id, details);
            } else {
                return null;
            }
        }
        return details;
    }

    /**
     * Fetches an artist's details from the datastore
     * @param id the artist's id
     * @return the artist's details or null if the details are not in the datastore
     */
    private ArtistDetails loadArtistDetailsFromStore(String id, SimType simType) 
            throws AuraException, RemoteException {
        logger.info("loading artist from store :: " + id);
        Artist a = mdb.artistLookup(id);
        if (a == null) {
            return null;
        } else {
            return artistToArtistDetails(a, simType);
        }
    }

    private ArtistCompact artistToArtistCompact(Artist a) throws AuraException,
            RemoteException {

        ArtistCompact aC = new ArtistCompact();

        aC.setName(a.getName());
        aC.setBeginYear(a.getBeginYear());
        aC.setEndYear(a.getEndYear());
        aC.setBiographySummary(a.getBioSummary());
        aC.setId(a.getKey());
        aC.setPhotos(getArtistPhotoFromIds(a.getPhotos()));
        aC.setPopularity(a.getPopularity());
        aC.setSpotifyId(a.getSpotifyID());

        if (beatlesPopularity!=-1) {
            aC.setNormPopularity(a.getPopularity()/beatlesPopularity);
        } else {
            aC.setNormPopularity(-1);
        }

        try {
            aC.setEncodedName(URLEncoder.encode(a.getName(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            aC.setEncodedName("Error converting name");
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Fetch albums
        Set<String> albumSet = a.getAlbums();
        AlbumDetails[] albumDetailsArray = new AlbumDetails[albumSet.size()];
        int index=0;
        for (String ad : albumSet) {
            Album storeAlbum = mdb.albumLookup(ad);
            albumDetailsArray[index] = new AlbumDetails();
            albumDetailsArray[index].setAsin(storeAlbum.getAsin());
            albumDetailsArray[index].setId(storeAlbum.getKey());
            albumDetailsArray[index].setTitle(storeAlbum.getTitle());
            index++;
        }
        aC.setAlbums(albumDetailsArray);

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
    private ArtistDetails artistToArtistDetails(Artist a, SimType simType)
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

        if (beatlesPopularity!=-1) {
            details.setNormPopularity(a.getPopularity()/beatlesPopularity);
        } else {
            details.setNormPopularity(-1);
        }

        try {
            details.setEncodedName(URLEncoder.encode(a.getName(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            details.setEncodedName("Error converting name");
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Fetch similar artists
        List<Scored<Artist>> scoredArtists = simType.findSimilarArtists(a.getKey(), NUMBER_SIM_ARTISTS);
        sortByArtistPopularity(scoredArtists);
        ArtistCompact[] simArtist = new ArtistCompact[scoredArtists.size()];
        for (int i=0; i<scoredArtists.size(); i++) {
            simArtist[i] = artistToArtistCompact(scoredArtists.get(i).getItem());
        }
        details.setSimilarArtists(simArtist);

        // Fetch albums
        Set<String> albumSet = a.getAlbums();
        AlbumDetails[] albumDetailsArray = new AlbumDetails[albumSet.size()];
        int index=0;
        for (String ad : albumSet) {
            Album storeAlbum = mdb.albumLookup(ad);
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
        Event storeEvent;
        index=0;
        for (String e : eventsSet) {
            storeEvent = mdb.eventLookup(e);
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
        if (collSet!=null && collSet.size()>0) {
            List<ArtistCompact> artistColl = new ArrayList<ArtistCompact>();
            Artist tempArtist;
            for (String aID : collSet) {
                tempArtist = mdb.artistLookup(aID);
                // If the related artist is not in our database, skip it
                if (tempArtist==null) {
                    continue;
                }
                artistColl.add(artistToArtistCompact(tempArtist));
            }
            if (artistColl.size()>0) {
                details.setCollaborations(artistColl.toArray(new ArtistCompact[0]));
            }
        }

        // Fetch list of distinctive tags
        WordCloud distinctiveTags = mdb.artistGetDistinctiveTagNames(a.getKey(), NUMBER_TAGS_TO_SHOW);
        details.setDistinctiveTags(WordCloudToIntemInfo(distinctiveTags));

        return details;
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
        Details aD = getDetailsInAnyCache(artistID);
        if (aD != null) {
            return ((ArtistDetails)aD).getDistinctiveTags();
        } else {
            return WordCloudToIntemInfo(mdb.artistGetDistinctiveTagNames(artistID, count));
        }
    }

    /**
     * Search for social tags
     * @param searchString the search string
     * @param maxResults  the maximum results to return
     * @return search results
     */
    public SearchResults tagSearch(String searchString, int maxResults)
            throws AuraException, RemoteException {
        logger.info("DataManager::tagSearch: "+searchString);
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
        logger.info("DataManager::artistSearch: "+searchString);
        ItemInfo[] artistResults = scoredArtistToItemInfo(mdb.artistSearch(searchString, maxResults));

        SearchResults sr = new SearchResults(searchString,
                searchTypes.SEARCH_FOR_ARTIST_BY_TAG, artistResults);
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
        logger.info("DataManager::artistSearchByTag: "+searchString);

        ArtistTag tag = mdb.artistTagLookup(searchString);
        if (tag==null) {
            // found no results! treat this
            logger.info("DataManager::artistSearchByTag. No results found for : "+searchString);
            return null;
        }

        ArrayList<ItemInfo> tagResults = new ArrayList<ItemInfo>();
        for (Tag t : tag.getTaggedArtist()) {
            if (tagResults.size()>=maxResults) {
                break;
            }
            Artist a = mdb.artistLookup(t.getTerm());
            if (a!=null) {
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
        int index=0;
        for (String v : videoSet) {
            Video dataStoreVideo = mdb.videoLookup(v);
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
        int index=0;
        for (String p : photoSet) {
            Photo dataStorePhoto = mdb.photoLookup(p);
            artistPhotoArray[index] = new ArtistPhoto();
            artistPhotoArray[index].setCreatorRealName(dataStorePhoto.getCreatorRealName());
            artistPhotoArray[index].setCreatorUserName(dataStorePhoto.getCreatorUserName());
            artistPhotoArray[index].setId(p);
            artistPhotoArray[index].setImageURL(dataStorePhoto.getImgUrl());
            artistPhotoArray[index].setSmallImageUrl(dataStorePhoto.getSmallImgUrl());
            artistPhotoArray[index].setThumbNailImageUrl(dataStorePhoto.getThumbnailUrl());
            artistPhotoArray[index].setTitle(dataStorePhoto.getTitle());
            artistPhotoArray[index].setPhotoPageURL(dataStorePhoto.getPhotoPageUrl());
            index++;
        }
        return artistPhotoArray;
    }

     /**
      * Gets the details for the given tag (by id)
      * @param id the id of the tag
      * @param refresh if true, ignore cache and load from datastore
      * @return the tag details
      */
    public TagDetails getTagDetails(String id, boolean refresh, String simTypeName) throws AuraException,
            RemoteException {
        TagDetails details = null;

        details = (TagDetails) getDetailsInAnyCache(id);
        if (details == null || refresh) {
            details = (TagDetails) loadTagDetailsFromStore(id);
            if (details != null) {
                cache.get(simTypeName).sput(id, details);
            } else {
                return null;
            }
        }
        return details;
    }
    
    public ItemInfo[] getSimilarTags(String tagId) throws AuraException, RemoteException {
        
        // Look in cache
        TagDetails details = null;
        details = (TagDetails) getDetailsInAnyCache(tagId);
        
        if (details == null) {
            // Fetch similar tags
            List<Scored<ArtistTag>> simTags = mdb.artistTagFindSimilar(tagId, NUMBER_TAGS_TO_SHOW);
            sortArtistTag(simTags, Sorter.sortFields.POPULARITY);
            return scoredArtistTagToItemInfo(simTags);
        } else {
            return details.getSimilarTags();
        }

        
        
    }

    /**
     * Perform a sync between a listener and listenerdetails, givin priority to
     * what's contained in the listenerDetails
     * @param l
     * @param lD
     * @return updated listener
     */
    private Listener syncListeners(Listener l, ListenerDetails lD, SimType simType,
            boolean updateRecommendations) throws AuraException, RemoteException {

        if (lD.gender!=null) {
            if (lD.gender.equals("M")) {
                l.setGender(Gender.Male);
            } else if (lD.gender.equals("F")) {
                l.setGender(Gender.Female);
            }
        } else if (l.getGender()!=null) {
            if (l.getGender()==Gender.Female) {
                lD.gender="F";
            } else if (l.getGender()==Gender.Male) {
                lD.gender="M";
            }
        }

        if (lD.country!=null) {
            l.setLocaleCountry(lD.country);
        } else if (l.getLocaleCountry()!=null) {
            lD.country=l.getLocaleCountry();
        }

        if (lD.pandoraUser!=null) {
            l.setPandoraName(lD.pandoraUser);
        } else if (l.getPandoraName()!=null) {
            lD.pandoraUser=l.getPandoraName();
        }

        if (lD.lastfmUser!=null) {
            l.setLastFmName(lD.lastfmUser);
        } else if (l.getLastFmName()!=null) {
            lD.lastfmUser=l.getLastFmName();
        }

        if (updateRecommendations) {
            // Fetch info for recommended artists
            ArrayList<ArtistCompact> aCompact = new ArrayList<ArtistCompact>();
            for (Scored<Artist> a : mdb.getRecommendations(l.getKey(), NBR_REC_LISTENER)) {
                aCompact.add(artistToArtistCompact(a.getItem()));
            }
            lD.recommendations = aCompact.toArray(new ArtistCompact[0]);
        }

        return l;
    }

    private ListenerDetails listenerToListenerDetails(Listener l, ListenerDetails lD,
            SimType simType, boolean updateRecommendations) throws AuraException, RemoteException {

        if (l.getGender()!=null) {
            if (l.getGender()==Gender.Female) {
                lD.gender="F";
            } else if (l.getGender()==Gender.Male) {
                lD.gender="M";
            }
        }

        if (l.getLocaleCountry()!=null) {
            lD.country=l.getLocaleCountry();
        }

        if (l.getPandoraName()!=null) {
            lD.pandoraUser=l.getPandoraName();
        }

        if (l.getLastFmName()!=null) {
            lD.lastfmUser=l.getLastFmName();
        }

        if (updateRecommendations) {
            // Fetch info for recommended artists
            ArrayList<ArtistCompact> aCompact = new ArrayList<ArtistCompact>();
            for (Scored<Artist> a : mdb.getRecommendations(l.getKey(), NBR_REC_LISTENER)) {
                aCompact.add(artistToArtistCompact(a.getItem()));
            }
            lD.recommendations = aCompact.toArray(new ArtistCompact[0]);
        }

        ArrayList<ItemInfo> iI = new ArrayList<ItemInfo>();
        List<Tag> lT = l.getSocialTags();
        if (lT != null && lT.size() > 0) {
            Collections.sort(lT, new Comparator<Tag>() {

                public int compare(Tag o1, Tag o2) {
                    return new Integer(o1.getFreq()).compareTo(o2.getFreq());
                }
            });
            double maxSize = lT.get(0).getFreq();
            int nbr = 0;
            for (Tag t : lT) {
                iI.add(new ItemInfo(ArtistTag.nameToKey(t.getName()), t.getName(), t.getFreq() / maxSize, t.getFreq() / maxSize));
                if (nbr++ > 50) {
                    break;
                }
            }
        }
        lD.userTagCloud = iI.toArray(new ItemInfo[0]);

        return lD;
    }

    public ListenerDetails establishNonOpenIdUserConnection(String userKey)
            throws AuraException, RemoteException {

        ListenerDetails lD = new ListenerDetails();
        Listener l = null;

        l = mdb.getListener(userKey);

        if (l == null) {
            logger.info("Non openID user '" + userKey + "' does not exist.");
            throw new AuraException("User '" + userKey + "' does not exist.");
        } else {
            logger.info("Non openID user '" + userKey + "' fetched.");
        }

        lD = listenerToListenerDetails(l, lD, simTypes.get(simTypes.keySet().iterator().next()), true);
        lD.openID = userKey;
        lD.loggedIn = true;
        
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

        l = mdb.getListener(lD.openID);

        if (l == null) {
            logger.info("Creating new user in datastore: " + lD.openID);
            l = mdb.enrollListener(lD.openID);
        } else {
            logger.info("Retrieved user from datastore: " + lD.openID);
        }

        l = syncListeners(l, lD, simTypes.get(simTypes.keySet().iterator().next()), true);
        mdb.updateListener(l);

        return lD;

    }

    public List<Attention> getLastAttentionData(String userId, Type type, int count) throws AuraException, RemoteException {
        return mdb.getLastAttentionData(userId, type, count);
    }

    public void updateUser(ListenerDetails lD) throws AuraException, RemoteException {
        Listener l = syncListeners(mdb.getListener(lD.openID), lD, null, false);
        mdb.updateListener(l);
    }

    public void updateUserSongRating(String userId, int rating, String artistId)
            throws AuraException, RemoteException {

        logger.info("Setting rating "+rating+" for artist " + artistId + " for user "+ userId);
        mdb.addRating(userId, artistId, rating);
    }

    public int fetchUserSongRating(String userId, String artistID)
            throws AuraException, RemoteException {

        logger.info("Fetching rating for artist " + artistID + " for user "+ userId);
        return mdb.getLatestRating(userId, artistID);
    }

    public Map<String,Integer> fetchUserSongRating(String userId, Set<String> artistID)
            throws AuraException, RemoteException {

        Map<String, Integer> ratingMap = new HashMap<String, Integer>();

        logger.info("Fetching rating for artist " + artistID + " for user " + userId);
        for (String aID : artistID) {
            ratingMap.put(aID, mdb.getLatestRating(userId, aID));
        }

        return ratingMap;
    }

    public ArtistCompact getArtistCompact(String artistId) throws AuraException, RemoteException {
        return artistToArtistCompact(mdb.artistLookup(artistId));
    }

    public ArtistCompact[] getSteerableRecommendations(Map<String, Double> tagMap)
            throws AuraException, RemoteException {

        List<Scored<Artist>> lsA = mdb.wordCloudFindSimilarArtists(mapToWordCloud(tagMap), NUMBER_SIM_ARTISTS);
        ArtistCompact[] aCArray = new ArtistCompact[lsA.size()];
        int index = 0;
        for (Scored<Artist> sA : lsA) {
            aCArray[index++] = artistToArtistCompact(sA.getItem());
        }
        return aCArray;
    }

    private WordCloud mapToWordCloud(Map<String, Double> tagMap) {
        WordCloud wC = new WordCloud();
        for (String tagName : tagMap.keySet()) {
            wC.add(new Scored<String>(tagName, tagMap.get(tagName)));
        }
        return wC;
    }

    public Set<String> fetchUserTagsForItem(String listenerId, String itemId)
            throws AuraException, RemoteException {
        Set<String> tags = new HashSet<String>();
        tags.addAll(mdb.getTags(listenerId, itemId));
        return tags;
    }

    public void addUserTagForItem(String listenerId, String itemId, String tag) 
            throws AuraException, RemoteException {
        mdb.addTag(listenerId, itemId, tag);
    }

    public TagDetails loadTagDetailsFromStore(String id) throws AuraException,
            RemoteException {
        logger.info("searching for tag :"+id);
        ArtistTag tag = mdb.artistTagLookup(id);
        if (tag==null) {
            logger.info("null on "+id);
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
        List<Scored<Tag>> taggedArtists = sortTag(tag.getTaggedArtist(),TagSorter.sortFields.COUNTorSCORE);
        details.setRepresentativeArtists(tagToItemInfo(taggedArtists.subList(0, getMax(taggedArtists,NUMBER_TAGS_TO_SHOW)),true));

        // Fetch similar tags
        List<Scored<ArtistTag>> simTags = mdb.artistTagFindSimilar(id, NUMBER_TAGS_TO_SHOW);
        sortArtistTag(simTags, Sorter.sortFields.POPULARITY);
        details.setSimilarTags(scoredArtistTagToItemInfo(simTags));

        return details;
    }

    /**
     * Returns the maximum between the supplied list's size or the maximum
     * amount of tags we are allowed to show
     * @param l list to check
     * @return
     */
    private final int getMax(List l, final int maxNumber) {
        int listSize = l.size();
        if (listSize<maxNumber) {
            return listSize;
        } else {
            return maxNumber;
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void close() throws AuraException, RemoteException {

        //@todo fix this
        //logger.close();
    }

    private void sortByArtistPopularity(List<Scored<Artist>> scoredArtists) {
        Collections.sort(scoredArtists, new ArtistPopularitySorter());
        Collections.reverse(scoredArtists);
    }

    private List<Scored<Tag>> sortTag(List<Tag> tags, Sorter.sortFields sortBy) {
        List<Scored<Tag>> scoredTags = new ArrayList<Scored<Tag>>();
        for (Tag t : tags) {
            scoredTags.add(new Scored(t,t.getCount()));
        }
        return sortScoredTag(scoredTags,sortBy);
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
        sortArtistTag(scoredTags,sortBy);
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
                aD = (ArtistDetails) getDetailsInAnyCache(t.getName());
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

    public Map<String, String> getSimTypes() {
        Map<String, String> simTypes = new HashMap<String, String>();
        for (SimType s : mdb.getSimTypes()) {
            simTypes.put(s.getName(), s.getDescription());
        }
        return simTypes;
    }

    /**
     * Search for a key in all simType caches
     * @param searchKey the key to search for
     * @return the details object found, if any
     */
    private Details getDetailsInAnyCache(String searchKey) {
        Details d;
        for (String k : cache.keySet()) {
            d = (Details) cache.get(k).sget(searchKey);
            if (d!=null) {
                return d;
            }
        }
        return null;
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
        for (Scored<String> sS : strList.subList(0, getMax(strList,NUMBER_TAGS_TO_SHOW))) {
            try {
                ArtistTag aT = mdb.artistTagLookup(ArtistTag.nameToKey(sS.getItem()));
                Float popularity;
                if (aT==null) {
                    popularity = new Float(0.0);
                } else {
                    popularity = aT.getPopularity();
                }
                tagsArray.add(new ItemInfo(sS.getItem(), sS.getItem(), sS.getScore(),
                        popularity));
            } catch (AuraException ex) {
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return tagsArray.subList(0,this.getMax(tagsArray,NUMBER_TAGS_TO_SHOW)).toArray(new ItemInfo[0]);
    }

    /**
     * Extracts the IDs from a list of items and returns them in a list
     * @param itemIDs list of items
     * @return list of the items' IDs
     */
    private List<String> itemsToIDs(List<Item> itemIDs) {
        List<String> stringIDs = new ArrayList();
        for (Item i : itemIDs) {
            stringIDs.add(i.getKey());
        }
        return stringIDs;
    }

    public int getExpiredTimeInDays() {
        return expiredTimeInDays;
    }

    public void setExpiredTimeInDays(int expiredTimeInDays) {
        this.expiredTimeInDays = expiredTimeInDays;
    }

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
        this.name=name;
        this.score=score;
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
        this.score+=score;
    }
}

class ExpiringLRUCache {

    private LRUCache<String, Object> cache;
    private long time2live; // time objects can live in cache in millisec

    public ExpiringLRUCache(int maxSize, int time2live) {
        cache = new LRUCache(maxSize);
        this.time2live = time2live*1000;
    }

    public Object sget(String s) {
        ObjectContainer o = (ObjectContainer) cache.sget(s);
        if (o == null || o.getExpiration() < System.currentTimeMillis()) {
            return null;
        } else {
            return o.getObject();
        }
    }

    public Object sput(String s, Object o) {
        return cache.sput(s, new ObjectContainer(System.currentTimeMillis() + time2live, o));
    }

    private class LRUCache<String, Object> extends LinkedHashMap<String, Object> {

        private int maxSize;

        LRUCache(int maxSize) {
            this.maxSize = maxSize;
        }

        // BUG sort out this sync stuff
        synchronized public Object sget(String s) {
            return get(s);
        }

        synchronized public Object sput(String s, Object o) {
            return put(s, o);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            boolean remove = size() > maxSize;
            return remove;
        }
     }

    private class ObjectContainer {

        private Object obj;
        private long expiration;

        public ObjectContainer(long creationTime, Object obj) {
            this.expiration=creationTime;
            this.obj=obj;
        }

        public Object getObject() {
            return obj;
        }

        public long getExpiration() {
            return expiration;
        }
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
        this.sortBy=sortBy;
    }

}

class TagSorter extends Sorter implements Comparator<Scored<Tag>> {

    public TagSorter(sortFields sortBy) {
        super(sortBy);
    }

    private final double getField(Scored<Tag> o) {
        if (sortBy==sortFields.COUNTorSCORE) {
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
        if (sortBy==sortFields.COUNTorSCORE) {
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
