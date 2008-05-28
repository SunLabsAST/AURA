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
import com.sun.labs.aura.music.Event;
import com.sun.labs.aura.music.Photo;
import com.sun.labs.aura.music.Video;
import com.sun.labs.util.props.ConfigurationManager;

import com.sun.labs.aura.music.web.flickr.FlickrManager;
import com.sun.labs.aura.music.web.flickr.Image;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainz;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainzAlbumInfo;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainzArtistInfo;
import com.sun.labs.aura.music.web.spotify.Spotify;
import com.sun.labs.aura.music.web.upcoming.Upcoming;
import com.sun.labs.aura.music.web.upcoming.UpcomingEvent;
import com.sun.labs.aura.music.web.youtube.Youtube;
import com.sun.labs.aura.music.web.wikipedia.WikiInfo;
import com.sun.labs.aura.music.web.wikipedia.Wikipedia;
import com.sun.labs.aura.music.web.yahoo.SearchResult;
import com.sun.labs.aura.music.web.yahoo.Yahoo;
import com.sun.labs.aura.music.web.youtube.YoutubeVideo;
import com.sun.labs.aura.music.wsitm.client.AlbumDetails;
import com.sun.labs.aura.music.wsitm.client.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.ArtistEvent;
import com.sun.labs.aura.music.wsitm.client.ArtistPhoto;
import com.sun.labs.aura.music.wsitm.client.ArtistVideo;
import com.sun.labs.aura.music.wsitm.client.Details;
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
import com.thoughtworks.xstream.XStream;
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
    
    //private File path;
    //private File dbPath;
    private LRUCache cache;
    private XStream xstream;
    private Youtube youtube;
    private MusicBrainz musicBrainz;
    private DataStore datastore;
    //private MusicDatabase mdb;
    private Wikipedia wikipedia;
    private Yahoo yahoo;
    private Spotify spotify;
    private FlickrManager flickr;
    private Upcoming upcoming;
    private Prefetcher prefetcher;
    private TagTree tree = null;
    private int expiredTimeInDays = 0;
    private boolean prefetch = true;
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
        
        youtube = new Youtube();
        musicBrainz = new MusicBrainz();
        wikipedia = new Wikipedia();
        flickr = new FlickrManager();
        upcoming = new Upcoming();
        yahoo = new Yahoo();
        spotify = new Spotify();

        xstream = new XStream();
        xstream.alias("ArtistDetails", ArtistDetails.class);
        xstream.alias("ArtistVideo", ArtistVideo.class);
        xstream.alias("ItemInfo", ItemInfo.class);
        xstream.alias("Album", AlbumDetails.class);
        xstream.alias("ArtistPhoto", ArtistPhoto.class);
        xstream.alias("ArtistEvent", ArtistEvent.class);
        xstream.alias("TagTree", TagTree.class);

        if (prefetch) {
            prefetcher = new Prefetcher();
        }
        
    }

    /**
     * Gets the details for the given artist (by id)
     *  
     * @param id  the id of the artist
     * @param refresh if true, recrawl the web for the data
     * @return  the artist details
     */
    public ArtistDetails getArtistDetails(String id, boolean refresh) throws 
            AuraException, RemoteException {
        return getArtistDetails(id, refresh, true);
    }

    public ItemInfo[] getCommonTags(String id1, String id2, int num) 
            throws AuraException, RemoteException {
        List<Scored<String>> simList = datastore.explainSimilarity(id1, id2, 
                Artist.FIELD_SOCIAL_TAGS, num);
        return scroredStringToItemInfo(simList);
    }

    private ArtistDetails getArtistDetails(String id, boolean refresh, 
            boolean prefetchMode) throws AuraException, AuraException, 
            RemoteException {
        ArtistDetails details = null;
        
        //@todo fix this
        if (false) {
        //if (refresh) {
            details = fetchArtistDetails(id);
            if (details != null) {
                synchronized (cache) {
                    cache.sput(id, details);
                    //saveDetailsToFile(details);
                }
            }
        } else {
            details = (ArtistDetails) cache.sget(id);

            if (details == null) {
                details = (ArtistDetails) loadDetailsFromStore(id);
                if (details != null) {
                    cache.sput(id, details);
                } else {
                    details = fetchArtistDetails(id);
                    if (details != null) {
                        synchronized (cache) {
                            cache.sput(id, details);
                            //@todo do this
                            // put artist details in DB
                        }
                    }
                }
            }
        }

        if (prefetchMode) {
            if (prefetcher != null) {
                prefetcher.add(details);
            }
        }
        return details;
    }

    /**
     * Fetches an artist's details from the datastore
     * @param id the artist's id
     * @return the artist's details or null if the details are not in the datastore
     */
    private ArtistDetails loadDetailsFromStore(String id) throws AuraException, 
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
        
        try {
            details.setEncodedName(URLEncoder.encode(a.getName(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Fetch photos
        Set<String> photoSet = a.getPhotos();
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
        details.setPhotos(artistPhotoArray);
        
        // Fetch videos
        Set<String> videoSet = a.getVideos();
        ArtistVideo[] artistVideoArray = new ArtistVideo[videoSet.size()];
        index=0;
        for (String v : videoSet) {
            Video dataStoreVideo = new Video(datastore.getItem(v));
            artistVideoArray[index] = new ArtistVideo();
            artistVideoArray[index].setThumbnail(dataStoreVideo.getThumbnailUrl());
            artistVideoArray[index].setTitle(dataStoreVideo.getName());
            artistVideoArray[index].setUrl(dataStoreVideo.getUrl());
            index++;
        }
        details.setVideos(artistVideoArray);
        
        // Fetch similar artists
        ItemInfo[] simArtists = scoreArtistsToItemInfo(datastore.findSimilar(id, 
                Artist.FIELD_SOCIAL_TAGS, 10, new TypeFilter(ItemType.ARTIST)));
        details.setSimilarArtists(simArtists);
        
        // Fetch albums
        Set<String> albumSet = a.getAlbums();
        AlbumDetails[] albumDetailsArray = new AlbumDetails[albumSet.size()];
        index=0;
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
        
        ItemInfo[] freqTags = new ItemInfo[NUMBER_TAGS_TO_SHOW];
        index=0;
        for (Scored<Tag> sT : scoredTags.subList(0, NUMBER_TAGS_TO_SHOW)) {
            freqTags[index] = new ItemInfo(sT.getItem().getName(),
                    sT.getItem().getName(),sT.getItem().getCount(),
                    sT.getItem().getCount());
            index++;
        }
        details.setFrequentTags(freqTags);
        
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
    public SearchResults tagSearch(String searchString, int maxResults) {
        return null;
        //@todo fix this
        /*
        logger.log("anon", "tagSearch", searchString);
        
        List<Scored<Tag>> scoredTags = mdb.tagSearch(searchString, maxResults);

        sortByTagPopularity(scoredTags);
        ItemInfo[] tagResults = new ItemInfo[scoredTags.size()];

        for (int i = 0; i < tagResults.length; i++) {
            Tag tag = scoredTags.get(i).getItem();
            double score = scoredTags.get(i).getScore();
            //@todo getFreq instead of getPopularity ok??
            double popularity = tag.getFreq();
            //@todo getName instead of getID ok??
            tagResults[i] = new ItemInfo(tag.getName(), tag.getName(), score, popularity);
        }

        SearchResults sr = new SearchResults(searchString, SearchResults.SEARCH_FOR_TAG_BY_TAG, tagResults);
        return sr;
        */
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

        SearchResults sr = new SearchResults(searchString, SearchResults.SEARCH_FOR_ARTIST_BY_TAG, artistResults);
        return sr;
    }

    public SearchResults artistSearchByTag(String searchString, int maxResults) 
            throws AuraException, RemoteException {
        logger.info("DataManager::artistSearchByTag TODOOO!: "+searchString);
        return null;

    }

    public TagDetails getTagDetails(String id, boolean refresh) {
        TagDetails details = null;

        if (refresh) {
            details = fetchTagDetails(id);
            if (details != null) {
                synchronized (cache) {
                    cache.sput(id, details);
                    //saveDetailsToFile(details);
                }
            }
        } else {
            details = (TagDetails) cache.sget(id);
            if (details == null) {
                details = (TagDetails) loadDetailsFromFile(id);
                if (details != null) {
                    cache.sput(id, details);
                } else {
                    details = fetchTagDetails(id);
                    if (details != null) {
                        synchronized (cache) {
                            cache.sput(id, details);
                            //saveDetailsToFile(details);
                        }
                    }
                }
            }
        }
        return details;
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

    /**
     * @deprecated
     * @param id
     * @return
     */
    private Details loadDetailsFromFile(String id) {
        return null;
        /*
        Details details = null;
        File file = getXmlFile(id);
        if (file.exists() && !expired(file)) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                details = (Details) xstream.fromXML(reader);
                reader.close();
                details.fixup();
            } catch (IOException ioe) {
                System.err.println("trouble reading " + file);
            } catch (BaseException e) {
                System.err.println("trouble reading xml" + file);
            }
        }
        return details;
        */
    }

    /**
     * Checks to see if a file is older than
     * the expired time
     */
/*
    boolean expired(File file) {
        if (getExpiredTimeInDays() == 0) {
            return false;
        } else {
            long staleTime = System.currentTimeMillis() -
                    getExpiredTimeInDays() * 24 * 60 * 60 * 1000L;
            return (file.lastModified() < staleTime);
        }
    }

    private void saveDetailsToFile(Details details) {
        File file = getXmlFile(details.getId());
        // System.out.println("Saving to " + file);
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            xstream.toXML(details, writer);
            writer.close();
        } catch (IOException ioe) {
            System.err.println("can't save details to " + file);
        }
    }

    private File getXmlFile(String id) {
        // there will likely be tens of thousands of these
        // xml files, we don't want to overwhelm a diretory, so
        // lets spread them out over 256 directories.'
        id = id + ".xml";
        String dir = id.substring(0, 2).toLowerCase();
        File fullPath = new File(dbPath, dir);
        if (!fullPath.exists()) {
            fullPath.mkdirs();
        }
        return new File(fullPath, id);
    }

    private File getTreeFile() {
        return new File(dbPath, "TreeData.xml");
    }
*/
    private TagDetails fetchTagDetails(final String id) {
        final TagDetails tagDetails = new TagDetails();
        final Tag tag = new Tag(); //@todo fix this mdb.getTag(id);

        tagDetails.setId(id);

        if (tag == null) {
            tagDetails.setStatus("bad tag ID");
            return tagDetails;
        } else {
            tagDetails.setName(tag.getName());

            Commanders commanders = new Commanders(singleThread);

            commanders.add(new Commander("tag info") {

                public void go() {
                    //@todo fix this
                    /*
                    tagDetails.setName(tag.getName());
                    ItemInfo[] repArtists = convertArtistsToInfo(tag.getMostRepresentativeArtists(20));
                    tagDetails.setRepresentativeArtists(repArtists);
                    ItemInfo[] simTags = convertTagsToInfo(tag.findSimilar(20), true);
                    tagDetails.setSimilarTags(simTags);
                    */
                }
            });

            commanders.add(new Commander("photos") {

                public void go() {
                    tagDetails.setPhotos(getFlickrPhotos(tag.getName(), 12));
                }
            });

            commanders.add(new Commander("videos") {

                public void go() {
                    tagDetails.setVideos(getYoutubeVideos(tag.getName(), 24));
                }
            });

            commanders.add(new Commander("wiki") {

                public void go() {
                    String wikiUrl = getWikiGenreMusicURL(tag.getName());
                    if (wikiUrl != null) {
                        tagDetails.setDescription(wikipedia.getSummaryDescription(wikiUrl));
                    }
                }
            });

            commanders.run();
        }
        return tagDetails;
    }

    private String getWikiGenreMusicURL(String genreName) {
        String url = null;
        List<SearchResult> resultList = yahoo.searchSite("en.wikipedia.org", genreName + " music");
        SearchResult result = findBestSearchResult(resultList);
        if (result != null) {
            url = result.getUrl();
        }
        return url;
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

    private ArtistDetails fetchArtistDetails(String id) throws AuraException, RemoteException {
        final ArtistDetails artistDetails = new ArtistDetails();
        Artist artist = (Artist)datastore.getItem(id);
        artistDetails.setId(id);

        if (artist == null) {
            artistDetails.setStatus("bad artist id");
        } else {
            artistDetails.setName(artist.getName());

            // Threading:
            //      item collectors can be threaded if they
            //      only write to separate areas of the artist details
            //      any dependencies in the collectors must be handled
            //      by running the dependent collectors in the same thread

            Commanders commanders = new Commanders(singleThread);
            commanders.add(new Commander("artist info") {

                public void go() {
                    try {
                        addArtistInfo(artistDetails);
                    } catch (AuraException ex) {
                        Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (RemoteException ex) {
                        Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            commanders.add(new Commander("mb-wiki") {

                public void go() {
                    try {
                        // wikipedia depends on musicbrainz info
                        addMusicBrainzInfo(artistDetails);
                    } catch (AuraException ex) {
                        Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    addWikipediaInfo(artistDetails);
                }
            });

            commanders.add(new Commander("youtube") {

                public void go() {
                    addYoutubeInfo(artistDetails);
                }
            });

            commanders.add(new Commander("flickr") {

                public void go() {
                    addFlickrInfo(artistDetails);
                }
            });

            commanders.add(new Commander("upcoming") {

                public void go() {
                    addUpcomingInfo(artistDetails);
                }
            });

            commanders.add(new Commander("spotify") {

                public void go() {
                    addSpotifyInfo(artistDetails);
                }
            });

            commanders.run();
        }
        return artistDetails;
    }

    //@todo is this necessary?!
    private void addArtistInfo(ArtistDetails artistDetails) throws AuraException, 
            RemoteException {
        Artist artist = (Artist)datastore.getItem(artistDetails.getId());
        if (artist != null) {
            String encodedName = artist.getName();
            try {
                encodedName = URLEncoder.encode(artist.getName(), "UTF-8");
            } catch (IOException ioe) {
            // the sillyist exception ever
            }
            artistDetails.setName(artist.getName());
            artistDetails.setEncodedName(encodedName);
            //@todo fix this
            /*
            artistDetails.setSimilarArtists(getSimilarArtists(artist, Artist.SimType.TAG, 20));
            artistDetails.setRecommendedArtists(getSimilarArtists(artist, Artist.SimType.USER, 10));
            artistDetails.setFrequentTags(convertTagsToInfo(artist.getMostFrequentTags(20), false));
            artistDetails.setDistinctiveTags(convertTagsToInfo(artist.getMostRepresentativeTags(20), false));
            artistDetails.setPopularity((float) artist.getPopularity());
             * */
        }
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
            ItemInfo info = new ItemInfo(scoredTag.getItem().getName(),
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

    void sortByArtistPopularity(List<Scored<Artist>> scoredArtists) {
        Collections.sort(scoredArtists, new ArtistPopularitySorter());
        Collections.reverse(scoredArtists);
    }

    void sortByTagPopularity(List<Scored<Tag>> scoredTags) {
        Collections.sort(scoredTags, new TagPopularitySorter());
        Collections.reverse(scoredTags);
    }

    private void addYoutubeInfo(ArtistDetails artistDetails) {
        artistDetails.setVideos(getYoutubeVideos(artistDetails.getName(), 24));
    }

    private void addSpotifyInfo(ArtistDetails artistDetails) {
        try {
            String id = spotify.getSpotifyIDforArtist(artistDetails.getName());
            if (id != null) {
                artistDetails.setMusicURL(id);
            }
        } catch (IOException ioe) {
            // spotify must be down, so forget about it for now
        }
    }

    private ArtistVideo[] getYoutubeVideos(String query, int num) {
        ArtistVideo[] artistVideos = null;
        try {
            List<YoutubeVideo> videos = youtube.musicSearch(query, num);
            artistVideos = new ArtistVideo[videos.size()];
            for (int i = 0; i < artistVideos.length; i++) {
                YoutubeVideo video = videos.get(i);
                artistVideos[i] = new ArtistVideo();
                artistVideos[i].setThumbnail(video.getThumbnail().toExternalForm());
                artistVideos[i].setTitle(video.getTitle());
                artistVideos[i].setUrl(video.getURL().toExternalForm());
            }
        } catch (IOException ioe) {
            System.err.println("Trouble getting videos for " + query);
            artistVideos = new ArtistVideo[0];
        }
        return artistVideos;
    }

    private void addMusicBrainzInfo(ArtistDetails artistDetails) throws AuraException {
        try {
            MusicBrainzArtistInfo mbai = musicBrainz.getArtistInfo(artistDetails.getId());
            artistDetails.setBeginYear(mbai.getBeginYear());
            artistDetails.setEndYear(mbai.getEndYear());
            artistDetails.setUrls(mbai.getURLMap());
            AlbumDetails[] albums = new AlbumDetails[mbai.getAlbums().size()];
            int index = 0;
            for (MusicBrainzAlbumInfo mbAlbum : mbai.getAlbums()) {
                AlbumDetails album = new AlbumDetails();
                album.setId(mbAlbum.getId());
                album.setTitle(mbAlbum.getTitle());
                album.setAsin(mbAlbum.getAsin());
                albums[index++] = album;
            }
            artistDetails.setAlbums(albums);

            // for each collaborator, check to see if it is in our database
            List<ItemInfo> collaborators = new ArrayList<ItemInfo>();

            for (String id : mbai.getCollaborators()) {
                Artist artist = (Artist) datastore.getItem(id);
                if (artist != null) {
                    ItemInfo ii = new ItemInfo(id, artist.getName(), 1.0, artist.getPopularity());
                    collaborators.add(ii);
                } else {
                // System.out.printf("Couldn't find Found collaborator %s for %s\n", id, artistDetails.getName());

                }
            }

            ItemInfo[] collaboratorInfo = collaborators.toArray(new ItemInfo[collaborators.size()]);
            artistDetails.setCollaborations(collaboratorInfo);
        } catch (IOException ioe) {
            System.out.println("Can't get artist info from musicbrainz for " + artistDetails.getName());
        }
    }

    private void addFlickrInfo(ArtistDetails artistDetails) {
        artistDetails.setArtistPhotos(getFlickrPhotos(artistDetails.getName(), 10));
    }

    private ArtistPhoto[] getFlickrPhotos(String query, int num) {
        Image[] images = flickr.getPhotosForArtist(query, num);
        ArtistPhoto[] photos = new ArtistPhoto[images.length];
        int index = 0;
        for (Image image : images) {
            ArtistPhoto photo = new ArtistPhoto();
            photo.setId(image.getId());
            photo.setCreatorRealName(image.getCreatorRealName());
            photo.setCreatorUserName(image.getCreatorUserName());
            photo.setImageURL(image.getImageURL());
            photo.setPhotoPageURL(image.getPhotoPageURL());
            photo.setSmallImageUrl(image.getSmallImageUrl());
            photo.setThumbNailImageUrl(image.getThumbNailImageUrl());
            photo.setTitle(image.getTitle());
            photos[index++] = photo;
        }
        return photos;
    }

    private void addUpcomingInfo(ArtistDetails artistDetails) {
        int MAX_EVENTS = 5;
        try {
            List<UpcomingEvent> events = upcoming.searchEventsByArtist(artistDetails.getName());
            int numEvents = Math.min(MAX_EVENTS, events.size());
            ArtistEvent[] artistEvents = new ArtistEvent[numEvents];
            int index = 0;
            for (UpcomingEvent event : events) {
                ArtistEvent artistEvent = new ArtistEvent();

                artistEvent.setName(event.getName());
                artistEvent.setDate(event.getDate());
                artistEvent.setEventID(event.getEventID());
                artistEvent.setVenueID(event.getVenueID());
                artistEvent.setVenue(event.getVenue());
                artistEvent.setVenueAddress(event.getVenueAddress());

                artistEvents[index++] = artistEvent;
                if (index >= MAX_EVENTS) {
                    break;
                }
            }
            artistDetails.setEvents(artistEvents);
        } catch (IOException ioe) {
            System.err.println("Can't get event info for " + artistDetails.getName());
        }
    }

    private void addWikipediaInfo(ArtistDetails artistDetails) {
        String query = (String) artistDetails.getUrls().get("Wikipedia");
        if (query == null) {
            query = artistDetails.getName();
        }

        try {
            WikiInfo wikiInfo = wikipedia.getWikiInfo(query);
            artistDetails.setBiographySummary(wikiInfo.getSummary());
        } catch (IOException ioe) {
            System.out.println("Can't get artist info from wikipedia for " + artistDetails.getName());
        }
    }

    DataStore getDataStore() {
        return datastore;
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
        for (Scored<String> sS : strList.subList(0, NUMBER_TAGS_TO_SHOW)) {
            tagsArray[index] = new ItemInfo(sS.getItem(), sS.getItem(), 
                    sS.getScore(), sS.getScore());
            index++;
        }
        return tagsArray;
    }

    class Prefetcher implements Runnable {

        private BlockingQueue<ArtistDetails> queue;
        private Thread thread;
        private int maxSize = 3;

        Prefetcher() {
            queue = new LinkedBlockingQueue<ArtistDetails>(3);
            start();
        }

        void stop() {
            thread = null;
        }

        void start() {
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        void add(ArtistDetails ad) {
            queue.offer(ad);
        }

        private void fetch(ItemInfo[] infos) {
            for (ItemInfo info : infos) {
                try {
                    //System.out.println("Prefetching " +info.getItemName());
                    getArtistDetails(info.getId(), false, false);
                } catch (AuraException ex) {
                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RemoteException ex) {
                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public void run() {
            while (thread != null) {
                try {
                    ArtistDetails ad = queue.take();
                    fetch(ad.getSimilarArtists());
                    fetch(ad.getRecommendedArtists());
                } catch (InterruptedException ie) {
                    continue;
                }
            }
        }
    }

    /**
     * Prefetch  all of the necessary data by crawling the entire
     * set of tags and artists.
     *
     * @param delayBetweenFetches seconds to wait between fetches
     */
    public void crawl(int delayBetweenFetches) {
        //@todo fix this
        //File logFile = new File(path, "crawlLog.txt");
        PrintWriter pw = null;
        wikipedia.setMinimumCommandPeriod(delayBetweenFetches);

        try {
            //@todo fix this
            //pw = new PrintWriter(logFile);
            while (true) {

                // start by getting the most popular artists and tags
                //@todo fix this
                /*
                List<Tag> popularTags = mdb.getTags(100);
                for (Tag tag : popularTags) {
                    TagDetails td = getTagDetails(tag.getID(), false);
                    logCrawl(pw, "tag", tag.getID(), td.getName(), td.getStatus());
                    for (Scored<Artist> scoredArtist : tag.getMostTaggedArtists(100)) {
                        ArtistDetails ad = getArtistDetails(scoredArtist.getItem().getID(), false);
                        logCrawl(pw, "artist", ad.getId(), ad.getName(), ad.getStatus());
                    }
                }
                */
                // then get all of the rest.

                //@todo fix this
                /*
                List<String> tagIDs = mdb.getTagIDs();
                for (String tag : tagIDs) {
                    TagDetails td = getTagDetails(tag, false);
                    logCrawl(pw, "tag", tag, td.getName(), td.getStatus());
                }
                 * */
                
                List<String> artistIDs = itemsToIDs(datastore.getAll(ItemType.ARTIST));
                for (String artist : artistIDs) {
                    ArtistDetails ad = getArtistDetails(artist, false);
                    logCrawl(pw, "artist", artist, ad.getName(), ad.getStatus());
                }
                // after we've gone through everything, wait for a day before we try again
                Thread.sleep(24 * 60 * 60 * 1000L);
            }
        } catch (AuraException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            //@todo fix this
            //System.out.println("Trouble writing log file " + logFile);
        } catch (InterruptedException ie) {
        } finally {
            pw.close();
        }
    }
    int logCount;

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
    
    private void logCrawl(PrintWriter pw, String op, String id, String name, String status) {
        pw.printf("%s %s %s %s '%s'\n", new Date(), op, status, id, name);
        System.out.printf("%d) %s %s %s %s '%s'\n", logCount++, new Date(), op, status, id, name);
        pw.flush();
    }
/*
    public static void main(String[] args) throws IOException, AuraException {
        String path = "/lab/mir/db/mdb";

        if (args.length != 0 & args.length != 1) {
            System.err.println("DataManager [path]");
            System.exit(1);
        }
        if (args.length == 1) {
            path = args[0];
        }

        try {
            //DataManager dm = new DataManager(path, 0);
            DataManager dm = DataManager.getDefault();
            dm.setExpiredTimeInDays(10);
            dm.crawl(3);
            dm.close();
        } catch (IOException ioe) {
            System.err.println("Trouble crawling " + path);
            System.exit(1);
        }
        
        
    }
*/
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
