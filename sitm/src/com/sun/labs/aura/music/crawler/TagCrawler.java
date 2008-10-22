/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.crawler;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.Photo;
import com.sun.labs.aura.music.Video;
import com.sun.labs.aura.music.util.CommandRunner;
import com.sun.labs.aura.music.util.Commander;
import com.sun.labs.aura.music.web.flickr.FlickrManager;
import com.sun.labs.aura.music.web.lastfm.LastFM;
import com.sun.labs.aura.music.web.lastfm.LastItem;
import com.sun.labs.aura.music.web.wikipedia.Wikipedia;
import com.sun.labs.aura.music.web.yahoo.SearchResult;
import com.sun.labs.aura.music.web.yahoo.Yahoo;
import com.sun.labs.aura.music.web.youtube.Youtube;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.RemoteComponentManager;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
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
    private LastFM lastFM;
    private Logger logger;
    private Util util;
    private RemoteComponentManager rcm;
    private boolean running = false;
    private Map<String, Map<String, Tag>> tagMap = new HashMap();
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
            Thread t = new Thread() {

                @Override
                public void run() {
                    autoUpdater();
                }
            };
            t.start();
        }
    }

    /**
     * Stops the crawler
     */
    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        forceCrawl = ps.getBoolean(PROP_FORCE_CRAWL);
        updateRateInSeconds = ps.getInt(PROP_UPDATE_RATE);
        try {
            wikipedia = new Wikipedia();
            youtube = new Youtube();
            flickr = new FlickrManager();
            yahoo = new Yahoo();
            lastFM = new LastFM();
            rcm = new RemoteComponentManager(ps.getConfigurationManager(), DataStore.class);
            util = new Util(flickr, youtube);
        } catch (IOException ex) {
            logger.warning("problem connecting to components" + ex);
        }
    }

    public void autoUpdater() {

        try {
            Thread.sleep(1000 * 60 * 3);
        } catch (InterruptedException ex) {
        }

        running = true;
        while (running) {
            try {
                // start crawling after 3 minutes

                if (forceCrawl) {
                    logger.info("Forced recrawl of artist tags");
                }

                updateArtistTags();
                // BUG: make this configurable
                // check for tag updates once a day
                Thread.sleep(1000 * 60 * 60 * 24);
                forceCrawl = false;
            } catch (InterruptedException ex) {
            } catch (AuraException ex) {
                logger.warning("Problem crawling tags -  AuraException " + ex);
            } catch (RemoteException ex) {
                logger.warning("Problem crawling tags - RemoteException " + ex);
            } catch (Throwable t) {
                logger.warning("Problem crawling tags - unexpected exception " + t);
            }
        }
        running = false;
    }

    public void updateArtistTags() throws AuraException, RemoteException {
        List<Item> items = getDataStore().getAll(ItemType.ARTIST_TAG);
        List<ArtistTag> tags = new ArrayList(items.size());

        for (Item item : items) {
            tags.add(new ArtistTag(item));
        }

        Collections.sort(tags, ArtistTag.POPULARITY);
        Collections.reverse(tags);

        for (ArtistTag tag : tags) {
            updateSingleTag(tag);
        }
    }

    private void addTaggedArtists(ArtistTag artistTag) throws AuraException, RemoteException, IOException {
        // add the last.fm tags
        float popularity = 0;
        LastItem[] lartists = lastFM.getTopArtistsForTag(artistTag.getName());
        if (lartists.length > 0) {
            artistTag.clearTaggedArtists();
            for (LastItem lartist : lartists) {
                // Always add tags, no matter what. we can filter them on the way out
                if (true || getDataStore().getItem(lartist.getMBID()) != null) {
                    artistTag.addTaggedArtist(lartist.getMBID(), lartist.getFreq());
                }
                popularity += lartist.getFreq();
            }
            artistTag.setPopularity(popularity);
        }
    }

    public void updateSingleTag(ArtistTag artistTag) throws AuraException, RemoteException {
        if (needsUpdate(artistTag)) {
            logger.info("Collecting info for tag " + artistTag.getName());
            collectTagInfo(artistTag);
            logger.fine("Done collecting info for tag " + artistTag.getName());
            artistTag.flush(getDataStore());
        } else {
            logger.fine("Skipping update for tag " + artistTag.getName());
        }

    }

    private boolean needsUpdate(ArtistTag artistTag) {
        boolean stale = (System.currentTimeMillis() - artistTag.getLastCrawl() > (updateRateInSeconds * 1000L));
        boolean empty = artistTag.getDescription().length() == 0 || artistTag.getTaggedArtist().size() == 0;
        return forceCrawl || stale || empty;
    }

    /**
     * Collects the information for an artist
     * @param queuedArtist the artist of interest
     * @return a fully populated artist
     * @throws com.sun.labs.aura.util.AuraException if a problem with the datastore is encountered
     * @throws java.rmi.RemoteException if a communicatin error occurs
     */
    void collectTagInfo(final ArtistTag artistTag)
            throws AuraException, RemoteException {

        CommandRunner runner = new CommandRunner(false, logger.isLoggable(Level.FINE));

        runner.add(new Commander("flickr") {

            @Override
            public void go() throws Exception {
                List<Photo> photos = util.collectFlickrPhotos(getDataStore(), artistTag.getName(), 24);
                for (Photo photo : photos) {
                    artistTag.addPhoto(photo.getKey());
                }

            }
        });

        runner.add(new Commander("lastfm") {

            @Override
            public void go() throws Exception {
                addTaggedArtists(artistTag);
            }
        });

        runner.add(new Commander("youtube") {

            @Override
            public void go() throws Exception {
                List<Video> videos = util.collectYoutubeVideos(getDataStore(), artistTag.getName(), 24);
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
        artistTag.setLastCrawl();
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

    private DataStore getDataStore() throws AuraException {
        return (DataStore) rcm.getComponent();
    }
    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    @ConfigBoolean(defaultValue = false)
    public final static String PROP_FORCE_CRAWL = "forceCrawl";
    private boolean forceCrawl;
    @ConfigInteger(defaultValue = 60 * 60 * 24 * 7 * 2)
    public final static String PROP_UPDATE_RATE = "updateRateInSeconds";
    private int updateRateInSeconds;
}
