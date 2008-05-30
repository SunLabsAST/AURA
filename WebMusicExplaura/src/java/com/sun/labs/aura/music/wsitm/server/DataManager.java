/*
 * DataManager.java
 *
 * Created on April 1, 2007, 6:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.server;

import java.io.UnsupportedEncodingException;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Album;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.Event;
import com.sun.labs.aura.music.Photo;
import com.sun.labs.aura.music.Video;
import com.sun.labs.util.props.ConfigurationManager;

import com.sun.labs.aura.music.web.wikipedia.Wikipedia;
import com.sun.labs.aura.music.web.yahoo.SearchResult;
import com.sun.labs.aura.music.web.yahoo.Yahoo;
import com.sun.labs.aura.music.wsitm.client.AlbumDetails;
import com.sun.labs.aura.music.wsitm.client.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.ArtistEvent;
import com.sun.labs.aura.music.wsitm.client.ArtistPhoto;
import com.sun.labs.aura.music.wsitm.client.ArtistVideo;
import com.sun.labs.aura.music.wsitm.client.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.SearchResults;
import com.sun.labs.aura.music.wsitm.client.TagDetails;
import com.sun.labs.aura.music.wsitm.client.TagTree;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author plamere
 */
public class DataManager implements Configurable {

    private static final String DATASTORE_KEY ="dataStoreHead";
    private static final String CACHE_SIZE ="cacheSize";
    
    private static final int DEFAULT_CACHE_SIZE=250;
    private static final int NUMBER_TAGS_TO_SHOW=15;
    
    private Logger logger = Logger.getLogger("");
    
    Logger log;
    ConfigurationManager configMgr;
    
    private LRUCache cache;
    private DataStore datastore;
    private int expiredTimeInDays = 0;
    private boolean singleThread = false;
    
    /**
     * Creates a new instance of the datamanager
     * @param path  the path to the database
     * @param cacheSize  the size of the cache
     * @throws java.io.IOException  
     */
    public DataManager(DataStore ds, int cacheSize) throws IOException {
        
        logger.info("Instantiating new DataManager");
        
        //int cacheSize = (int)configMgr.lookup(CACHE_SIZE);
        cache = new LRUCache(cacheSize);
        //mdb = new MusicDatabase(path);
        datastore = ds;
    }
    
    /**
     * Get the common tags between two artists
     * @param id1 the id of artist 1
     * @param id2 the id of artist 2
     * @param num number of tags to retreive
     * @return the common tags
     */
    public ItemInfo[] getCommonTags(String id1, String id2, int num) 
            throws AuraException, RemoteException {
        List<Scored<String>> simList = datastore.explainSimilarity(id1, id2, 
                Artist.FIELD_SOCIAL_TAGS, num);
        return scroredStringToItemInfo(simList);
    }
    
    /**
     * Gets the details for the given artist (by id)
     * @param id  the id of the artist
     * @param refresh if true, ignore cache and load from datastore
     * @return  the artist details
     */
    public ArtistDetails getArtistDetails(String id, boolean refresh) 
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
        return details;
    }

    /**
     * Fetches an artist's details from the datastore
     * @param id the artist's id
     * @return the artist's details or null if the details are not in the datastore
     */
    private ArtistDetails loadArtistDetailsFromStore(String id) throws AuraException, 
            RemoteException {
        
        ArtistDetails details = new ArtistDetails();
        Artist a = new Artist(datastore.getItem(id));
        if (a==null) {
            return null;
        }
        
        details.setName(a.getName());
        details.setBeginYear(a.getBeginYear());
        details.setEndYear(a.getEndYear());
        details.setBiographySummary(a.getBioSummary());
        details.setId(id);
        details.setPopularity(a.getPopularity());
        details.setUrls(a.getUrls());
        details.setPhotos(getArtistPhotoFromIds(a.getPhotos()));
        details.setVideos(getArtistVideoFromIds(a.getVideos()));

        try {
            details.setEncodedName(URLEncoder.encode(a.getName(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            details.setEncodedName("Error converting name");
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        // Fetch similar artists
        ItemInfo[] simArtists = scoreArtistsToItemInfo(datastore.findSimilar(id, 
                Artist.FIELD_SOCIAL_TAGS, 10, new TypeFilter(ItemType.ARTIST)));
        details.setSimilarArtists(simArtists);
        
        // Fetch albums
        Set<String> albumSet = a.getAlbums();
        AlbumDetails[] albumDetailsArray = new AlbumDetails[albumSet.size()];
        int index=0;
        for (String ad : albumSet) {
            Album storeAlbum = new Album(datastore.getItem(ad));
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
        index=0;
        for (String e : eventsSet) {
            Event storeEvent = new Event(datastore.getItem(e));
            eventsArray[index] = new ArtistEvent();
            eventsArray[index].setDate(storeEvent.getDate());
            eventsArray[index].setEventID(storeEvent.getKey());
            eventsArray[index].setName(storeEvent.getName());
            eventsArray[index].setVenue(storeEvent.getVenueName());
            index++;
        }
        details.setEvents(eventsArray);
        
        // Fetch collaborations
        Set<String> collSet = a.getCollaborations();
        ItemInfo[] artistColl = new ItemInfo[collSet.size()];
        Artist tempA;
        index=0;
        for (String aID : collSet) {
            tempA = new Artist(datastore.getItem(aID));
            artistColl[index] = new ItemInfo(aID, tempA.getName(), 
                    tempA.getPopularity(), tempA.getPopularity());
        }
        details.setCollaborations(artistColl);
        
        // Fetch and sort frequent tags
        List<Tag> tagList = a.getSocialTags();
        List<Scored<Tag>> scoredTags = new ArrayList<Scored<Tag>>();
        
        for (Tag t : tagList) {
            scoredTags.add(new Scored(t,t.getCount()));
        }
        sortByTagPopularity(scoredTags);
        /*
        ItemInfo[] freqTags = new ItemInfo[NUMBER_TAGS_TO_SHOW];
        index=0;
        for (Scored<Tag> sT : scoredTags.subList(0, NUMBER_TAGS_TO_SHOW)) {
            freqTags[index] = new ItemInfo(sT.getItem().getName(),
                    sT.getItem().getName(),sT.getItem().getCount(),
                    sT.getItem().getCount());
            index++;
        }
        details.setFrequentTags(freqTags);
        */
        details.setFrequentTags(convertTagsToInfo(scoredTags,true));
        
        // Fetch list of distinctive tags
        List<Scored<String>> topTags = datastore.getTopTerms(a.getKey(), 
                Artist.FIELD_SOCIAL_TAGS, NUMBER_TAGS_TO_SHOW);
        details.setDistinctiveTags(scroredStringToItemInfo(topTags));
        
        //details.setMusicURL(id);
        //details.setImageURL((String) a.getPhotos().toArray()[0]);
        
        /*
        details.setUrls(cache)
        details.setRecommendedArtists(recommendedArtists)
        details.setCollaborations(collaborations);
        */
        
        return details;
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
        
        String query = "(aura-type = artist_tag) <AND> (aura-name <matches> \"*" + searchString + "*\")";
        ItemInfo[] tagResults = scoreArtistTagToItemInfo(datastore.query(query, "-score", maxResults, null));

        SearchResults sr = new SearchResults(searchString, 
                SearchResults.SEARCH_FOR_TAG_BY_TAG, tagResults);
        return sr;
    }

    /**
     * Search for an artist
     * @param maxResults maximum results to return
     * @return search results
     */
    public SearchResults artistSearch(String searchString, int maxResults) throws AuraException, RemoteException {
        logger.info("DataManager::artistSearch: "+searchString);
        
        String query = "(aura-type = artist) <AND> (aura-name <matches> \"*" + searchString + "*\")";
        ItemInfo[] artistResults = scoreArtistsToItemInfo(datastore.query(query, "-score", maxResults, null));

        SearchResults sr = new SearchResults(searchString, 
                SearchResults.SEARCH_FOR_ARTIST_BY_TAG, artistResults);
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
        
        ArtistTag tag = new ArtistTag(datastore.getItem("artist-tag:"+searchString));
        if (tag==null) {
            // found no results! treat this
            return null;
        }
        
        ArrayList<ItemInfo> tagResults = new ArrayList<ItemInfo>();
        for (Tag t : tag.getTaggedArtist()) {
            if (tagResults.size()>=maxResults) {
                break;
            }
            Artist a = new Artist(datastore.getItem(t.getTerm()));
            if (a!=null) {
                tagResults.add(new ItemInfo(t.getName(), a.getName(), t.getCount(), t.getFreq()));
            }
        }
        SearchResults sr = new SearchResults(searchString, 
                SearchResults.SEARCH_FOR_ARTIST_BY_TAG, 
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
            Video dataStoreVideo = new Video(datastore.getItem(v));
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
            Photo dataStorePhoto = new Photo(datastore.getItem(p));
            artistPhotoArray[index] = new ArtistPhoto();
            artistPhotoArray[index].setCreatorRealName(dataStorePhoto.getCreatorRealName());
            artistPhotoArray[index].setCreatorUserName(dataStorePhoto.getCreatorUserName());
            artistPhotoArray[index].setId(p);
            artistPhotoArray[index].setImageURL(dataStorePhoto.getImgUrl());
            artistPhotoArray[index].setSmallImageUrl(dataStorePhoto.getSmallImgUrl());
            artistPhotoArray[index].setThumbNailImageUrl(dataStorePhoto.getThumbnailUrl());
            artistPhotoArray[index].setTitle(dataStorePhoto.getTitle());
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
    
    public TagDetails loadTagDetailsFromStore(String id) throws AuraException,
            RemoteException {

        Item iTag = datastore.getItem(id);
        if (iTag == null) {
            return null;
        }
        ArtistTag tag = new ArtistTag(iTag);
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

        List<Scored<Tag>> taggedArtists = sortByTagPopularity(tag.getTaggedArtist());
        details.setRepresentativeArtists(convertTagsToInfo(taggedArtists.subList(0, getMax(taggedArtists)),true));

        return details;
    }

    /**
     * Returns the maximum between the supplied list's size or the maximum 
     * amount of tags we are allowed to show
     * @param l list to check
     * @return
     */
    private int getMax(List l) {
        int listSize = l.size();
        if (listSize<NUMBER_TAGS_TO_SHOW) {
            return listSize;
        } else {
            return NUMBER_TAGS_TO_SHOW;
        }
    }
    
    public Logger getLogger() {
        return logger;
    }

    public void close() throws AuraException, RemoteException {
        datastore.close();
        //@todo fix this
        //logger.close();
    }

    public TagTree getTagTree() {
        //@todo fix this
        return null;
        /*
        if (tree == null) {
            File file = getTreeFile();
            if (file.exists()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    tree = (TagTree) xstream.fromXML(reader);
                    reader.close();
                } catch (IOException ioe) {
                    System.err.println("trouble reading " + file);
                }

            } else {
                tree = getTagTree(null, null);
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    xstream.toXML(tree, writer);
                    writer.close();
                } catch (IOException ioe) {
                    System.err.println("can't save details to " + file);
                }
            }
        }
        return tree;
         **/
    }

    public TagTree getTagTree(String id, String name) {
        return null;
        //@todo fix this
        /*
        if (name == null) {
            name = "root";
        }
        List<Tag> childTags = mdb.getChildren(id);
        System.out.println("gtt " + id + " n " + name + " #c " + childTags);
        TagTree[] children = new TagTree[childTags.size()];
        for (int i = 0; i < children.length; i++) {
            Tag ctag = childTags.get(i);
            children[i] = getTagTree(ctag.getID(), ctag.getName());
        }
        return new TagTree(id, name, children);
        */
    }
    
    static Set skipSet;

    static {
        skipSet = new HashSet<String>();
        skipSet.add("http://en.wikipedia.org/wiki/Musical_genre");
    }

    private SearchResult findBestSearchResult(List<SearchResult> results) {
        // there are some search results that we want to filter out
        for (SearchResult sr : results) {
            if (!skipSet.contains(sr.getUrl())) {
                return sr;
            }
        }
        return null;
    }

    private ItemInfo[] getSimilarArtists(Artist artist, String field, int size) throws AuraException, RemoteException {
        double THRESHOLD = .0;
        System.out.println("Finding similar for " + artist.getName());
        
        
        List<Scored<Item>> scoredArtists = datastore.findSimilar(artist.getName(), 
                field, size, new TypeFilter(ItemType.ARTIST));
        int count = 0;
        for (Scored<Item> scoredArtist : scoredArtists) {
            if (scoredArtist.getScore() < THRESHOLD) {
                break;
            }
            count++;
        }

        if (count < scoredArtists.size()) {
            scoredArtists = scoredArtists.subList(0, count);
        }
        return null; //@todo fix this convertArtistsToInfo(scoredArtists);
    }

    private ItemInfo[] convertTagsToInfo(List<Scored<Tag>> tags, boolean sortByPopularity) {
        ItemInfo[] itemInfos = new ItemInfo[tags.size()];
        if (sortByPopularity) {
            sortByTagPopularity(tags);
        }
        for (int i = 0; i < tags.size(); i++) {
            Scored<Tag> scoredTag = tags.get(i);
            //@todo tags don't have ids. 1st param should be id
            ItemInfo info = new ItemInfo(scoredTag.getItem().getTerm(),
                    scoredTag.getItem().getName(), scoredTag.getScore(),
                    //@todo freq instead of pop
                    scoredTag.getItem().getFreq());
            itemInfos[i] = info;
        }
        return itemInfos;
    }

    private ItemInfo[] convertArtistsToInfo(List<Scored<Artist>> artists) {
        ItemInfo[] itemInfos = new ItemInfo[artists.size()];

        sortByArtistPopularity(artists);

        for (int i = 0; i < artists.size(); i++) {
            Scored<Artist> scored = artists.get(i);
            //@todo tags don't have ids. 1st param should be id
            ItemInfo info = new ItemInfo(scored.getItem().getName(),
                    scored.getItem().getName(),
                    scored.getScore(), scored.getItem().getPopularity());
            itemInfos[i] = info;
        }
        return itemInfos;
    }

    private void sortByArtistPopularity(List<Scored<Artist>> scoredArtists) {
        Collections.sort(scoredArtists, new ArtistPopularitySorter());
        Collections.reverse(scoredArtists);
    }

    private void sortByTagPopularity(List<Scored<Tag>> scoredTags) {
        Collections.sort(scoredTags, new TagPopularitySorter());
        Collections.reverse(scoredTags);
    }

    private List<Scored<Tag>> sortByTagPopularity(List<Tag> tags) {
        List<Scored<Tag>> scoredTags = new ArrayList<Scored<Tag>>();
        for (Tag t : tags) {
            scoredTags.add(new Scored(t, t.getCount()));
        }
        sortByTagPopularity(scoredTags);
        return scoredTags;
    }
    
    DataStore getDataStore() {
        return datastore;
    }

    /**
     * Converts a list of scored artists and returns them in an iteminfo array 
     * @param scoredArtists scored list of items (that will be cast as artists)
     * @return
     */
    private ItemInfo[] scoreArtistTagToItemInfo(List<Scored<Item>> scoredArtists) {

        ItemInfo[] artistResults = new ItemInfo[scoredArtists.size()];

        for (int i = 0; i < artistResults.length; i++) {
            ArtistTag artist = new ArtistTag(scoredArtists.get(i).getItem());
            double score = scoredArtists.get(i).getScore();
            double popularity = artist.getPopularity();
            artistResults[i] = new ItemInfo(artist.getKey(), artist.getName(), score, popularity);
        }

        return artistResults;
    }
    
    /**
     * Converts a list of scored artists and returns them in an iteminfo array 
     * @param scoredArtists scored list of items (that will be cast as artists)
     * @return
     */
    private ItemInfo[] scoreArtistsToItemInfo(List<Scored<Item>> scoredArtists) {

        ItemInfo[] artistResults = new ItemInfo[scoredArtists.size()];

        for (int i = 0; i < artistResults.length; i++) {
            Artist artist = new Artist(scoredArtists.get(i).getItem());
            double score = scoredArtists.get(i).getScore();
            double popularity = artist.getPopularity();
            artistResults[i] = new ItemInfo(artist.getKey(), artist.getName(), score, popularity);
        }

        return artistResults;
    }

    /**
     * Converts a list of scored string to an itemInfo array
     * @param strList
     * @return item info array
     */
    private ItemInfo[] scroredStringToItemInfo(List<Scored<String>> strList) {
        ItemInfo[] tagsArray = new ItemInfo[NUMBER_TAGS_TO_SHOW];
        int index = 0;
        for (Scored<String> sS : strList.subList(0, getMax(strList))) {
            tagsArray[index] = new ItemInfo(sS.getItem(), sS.getItem(), 
                    sS.getScore(), sS.getScore());
            index++;
        }
        return tagsArray;
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
        datastore = (DataStore) ps.getComponent(DATASTORE_KEY);
        //int cacheSize = (Integer)ps.getComponent(CACHE_SIZE);
        
    }
/*
    @Override
    public void start() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
 * */
}
class LRUCache<String, Object> extends LinkedHashMap<String, Object> {

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

    protected boolean removeEldestEntry(Map.Entry eldest) {
        boolean remove = size() > maxSize;
        return remove;
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

class TagPopularitySorter implements Comparator<Scored<Tag>> {

    public int compare(Scored<Tag> o1, Scored<Tag> o2) {
        //@todo freq instead of pop
        double s1 = o1.getItem().getFreq();
        double s2 = o2.getItem().getFreq();
        if (s1 > s2) {
            return 1;
        } else if (s1 < s2) {
            return -1;
        } else {
            return 0;
        }
    }
}

class Commanders {

    private boolean trace = true;
    private boolean singleThread;
    private List<Commander> commanders = new ArrayList<Commander>();

    Commanders(boolean singleThread) {
        this.singleThread = singleThread;
    }

    void add(Commander c) {
        commanders.add(c);
    }

    void run() {
        if (singleThread) {
            for (Commander c : commanders) {
                c.run();
            }
        } else {
            for (Commander c : commanders) {
                c.start();
            }

            for (Commander c : commanders) {
                try {
                    c.join();
                } catch (InterruptedException ie) {
                }
            }
        }
        if (trace) {
            for (Commander c : commanders) {
                System.out.println("   " + c);
            }
        }
    }
}

abstract class Commander extends Thread {

    private String name;
    private long executeTime;

    Commander(String name) {
        this.name = name;
    }

    public final void run() {
        long startTime = System.currentTimeMillis();
        go();
        executeTime = System.currentTimeMillis() - startTime;
    }

    abstract void go();

    public String toString() {
        return name + ":" + executeTime + " ms";
    }
}
