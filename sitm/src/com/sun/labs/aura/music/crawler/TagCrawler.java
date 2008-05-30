/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.crawler;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.Photo;
import com.sun.labs.aura.music.Video;
import com.sun.labs.aura.music.util.CommandRunner;
import com.sun.labs.aura.music.util.Commander;
import com.sun.labs.aura.music.web.flickr.FlickrManager;
import com.sun.labs.aura.music.web.wikipedia.Wikipedia;
import com.sun.labs.aura.music.web.yahoo.SearchResult;
import com.sun.labs.aura.music.web.yahoo.Yahoo;
import com.sun.labs.aura.music.web.youtube.Youtube;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class TagCrawler implements AuraService, Configurable {

    private Wikipedia wikipedia;
    private Youtube youtube;
    private FlickrManager flickr;
    private Yahoo yahoo;
    private Logger logger;
    private Util util;
    private boolean running = false;
    private Map<String, Map<String, Tag>> tagMap = new HashMap();
    private final int MIN_ARTISTS = 10;   // make me configurable

    private final int MIN_ARTIST_POPULARITY = 10;   // make me configurable

    static Set skipSet;
    

    static {
        skipSet = new HashSet<String>();
        skipSet.add("http://en.wikipedia.org/wiki/Musical_genre");
    }

    /**
     * Starts running the crawler
     */
    public void start() {
        if (!running) {
            running = true;
            Thread t = new Thread() {

                @Override
                public void run() {
                }
            };
            t.start();
        }
    }

    /**
     * Stops the crawler
     */
    public void stop() {
        running = false;
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        wikipedia = new Wikipedia();
        youtube = new Youtube();
        flickr = new FlickrManager();
        yahoo = new Yahoo();
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        util = new Util(dataStore, flickr, youtube);
    }

    /**
     * Starts discovering artists. When a new artist is encountered it is 
     * added to the datastore
     */
    public void discoverArtistTags() throws AuraException, RemoteException {
        List<Item> items = dataStore.getAll(ItemType.ARTIST);
        for (Item item : items) {
            Artist artist = new Artist(item);
            float popularity = artist.getPopularity();
            if (popularity >= MIN_ARTIST_POPULARITY) {
                logger.info("Crawling tags from artist " + artist.getName());
                List<Tag> tags = artist.getSocialTags();

                for (Tag tag : tags) {
                    int normalizedCount =  (int) Math.rint(popularity * tag.getCount() / 100.);
                    // System.out.printf("norm count for %s/%s is %d\n", artist.getName(), tag.getName(), normalizedCount);
                    accumulateTag(tag.getName(), artist.getKey(), normalizedCount);
                }
            }
        }
        pruneAndWriteTags();
    }

    private void accumulateTag(String tagName, String artistMBAID, int count) {
        Map<String, Tag> artistTagMap = tagMap.get(tagName);
        if (artistTagMap == null) {
            artistTagMap = new HashMap();
            tagMap.put(tagName, artistTagMap);
        }
        Tag tag = artistTagMap.get(artistMBAID);
        if (tag == null) {
            tag = new Tag(artistMBAID, 0);
            artistTagMap.put(artistMBAID, tag);
        }
        tag.accum(count);
    }

    private void pruneAndWriteTags() throws AuraException, RemoteException {
        for (Map.Entry<String, Map<String, Tag>> entry : tagMap.entrySet()) {
            if (entry.getValue().size() > MIN_ARTISTS) {
                ArtistTag artistTag = new ArtistTag(entry.getKey());
                int sum = 0;
                for (Tag tag : entry.getValue().values()) {
                    sum += tag.getCount();
                    artistTag.addTaggedArtist(tag.getName(), tag.getCount());
                }
                logger.info("Adding tag " + artistTag.getName() + " artists " + artistTag.getTaggedArtist().size());
                artistTag.setPopularity(sum);
                artistTag.flush(dataStore);
            }
        }
    }

    public void updateArtistTags(int max) throws AuraException, RemoteException {
        List<Item> items = dataStore.getAll(ItemType.ARTIST_TAG);
        List<ArtistTag> tags = new ArrayList(items.size());

        for (Item item : items) {
            tags.add(new ArtistTag(item));
        }

        Collections.sort(tags, ArtistTag.POPULARITY);
        Collections.reverse(tags);

        int count = 0;
        for (ArtistTag tag : tags) {
            updateSingleTag(tag);
            if (count++ > max) {
                break;
            }
        }
    }

    public void updateSingleTag(ArtistTag artistTag) throws AuraException, RemoteException {
        logger.info("Collecting info for tag " + artistTag.getName());
        collectTagInfo(artistTag);
        artistTag.flush(dataStore);
    }

    /**
     * Collects the information for an artist
     * @param queuedArtist the artist of interest
     * @return a fully populated artist
     * @throws com.sun.labs.aura.util.AuraException if a problem with the datastore is encountered
     * @throws java.rmi.RemoteException if a communicatin error occurs
     */
    void collectTagInfo(final ArtistTag artistTag) throws AuraException, RemoteException {

        CommandRunner runner = new CommandRunner(false, logger.isLoggable(Level.INFO));

        runner.add(new Commander("flickr") {

            @Override
            public void go() throws Exception {
                List<Photo> photos = util.collectFlickrPhotos(artistTag.getName(), 24);
                for (Photo photo : photos) {
                    artistTag.addPhoto(photo.getKey());
                }
            }
        });

        runner.add(new Commander("youtube") {

            @Override
            public void go() throws Exception {
                List<Video> videos = util.collectYoutubeVideos(artistTag.getName(), 24);
                for (Video video : videos) {
                    artistTag.addVideo(video.getKey());
                }
            }
        });

        runner.add(new Commander("wikipedia") {

            @Override
            public void go() throws Exception {
                String wikiUrl = getWikiGenreMusicURL(artistTag.getName());
                if (wikiUrl != null) {
                    artistTag.setDescription(wikipedia.getSummaryDescription(wikiUrl));
                }
            }
        });


        try {
            runner.go();
        } catch (Exception e) {
            // if we get an exception when we are crawling, 
            // we still have some good data, so log the problem
            // but still return the artist so we can add it to the store
            logger.warning("Exception " + e);
        }
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

    private SearchResult findBestSearchResult(List<SearchResult> results) {
        // there are some search results that we want to filter out
        for (SearchResult sr : results) {
            if (!skipSet.contains(sr.getUrl())) {
                return sr;
            }
        }
        return null;
    }
    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    private DataStore dataStore;
}
