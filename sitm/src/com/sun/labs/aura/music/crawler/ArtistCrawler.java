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

package com.sun.labs.aura.music.crawler;

import com.echonest.api.v3.EchoNestException;
import com.echonest.api.v3.artist.ArtistAPI;
import com.echonest.api.v3.artist.Audio;
import com.echonest.api.v3.artist.Blog;
import com.echonest.api.v3.artist.DocumentList;
import com.echonest.api.v3.artist.Review;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.music.Album;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.TaggableItem.TagType;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.ArtistTagRaw;
import com.sun.labs.aura.music.CrawlableItem;
import com.sun.labs.aura.music.Event;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.Photo;
import com.sun.labs.aura.music.TaggableItem;
import com.sun.labs.aura.music.Track;
import com.sun.labs.aura.music.Video;
import com.sun.labs.aura.music.util.CommandRunner;
import com.sun.labs.aura.music.util.Commander;
import com.sun.labs.aura.music.util.LRUCache;
import com.sun.labs.aura.music.web.Utilities;
import com.sun.labs.aura.music.web.amazon.Amazon;
import com.sun.labs.aura.music.web.flickr.FlickrManager;
import com.sun.labs.aura.music.web.lastfm.LastAlbum2;
import com.sun.labs.aura.music.web.lastfm.LastFM;
import com.sun.labs.aura.music.web.lastfm.LastFM2;
import com.sun.labs.aura.music.web.lastfm.LastArtist;
import com.sun.labs.aura.music.web.lastfm.LastFM2Impl;
import com.sun.labs.aura.music.web.CannotResolveException;
import com.sun.labs.aura.music.web.lastfm.LastArtist2;
import com.sun.labs.aura.music.web.lastfm.LastItem;
import com.sun.labs.aura.music.web.lastfm.LastTrack;
import com.sun.labs.aura.music.web.lastfm.SocialTag;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainz;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainzAlbumInfo;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainzArtistInfo;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainzTrackInfo;
import com.sun.labs.aura.music.web.spotify.Spotify;
import com.sun.labs.aura.music.web.upcoming.UpcomingEvent;
import com.sun.labs.aura.music.web.upcoming.Upcoming;
import com.sun.labs.aura.music.web.wikipedia.WikiInfo;
import com.sun.labs.aura.music.web.wikipedia.Wikipedia;
import com.sun.labs.aura.music.web.youtube.Youtube2;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Counted;
import com.sun.labs.aura.util.RemoteComponentManager;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

/**
 *
 * @author plamere
 */
public class ArtistCrawler extends QueueCrawler implements AuraService, Configurable {

    private MusicBrainz musicBrainz;
    private Wikipedia wikipedia;
    private Youtube2 youtube;
    private FlickrManager flickr;
    private Upcoming upcoming;
    private Spotify spotify;
    private ArtistAPI echoNest;
    private Amazon amazon;
    private RemoteComponentManager rcmStore;
    private RemoteComponentManager rcmCrawl;
    private Util util;
    private boolean running = false;
    private final static int MAX_FAN_OUT = 5;
    private int minBlurbCount = 3;
    private ExecutorService threadPool;
    private TagFilter tagFilter;
    private MusicDatabase mdb;

    private final LRUCache<String, String> artistNameCache = new LRUCache<String, String>(75);

    private String ECHONEST_API_KEY = null;

    public ArtistCrawler() {
        super("Artist", "artist_crawler.state");
        crawlQueue = new PriorityBlockingQueue<QueuedItem>(1000, QueueCrawler.PRIORITY_ORDER);

        try {
            Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("com/sun/labs/aura/music/resource/api.properties"));
            ECHONEST_API_KEY = properties.getProperty("ECHONEST_API_KEY");
        } catch (Exception ex) {
            logger.severe("No EchoNest API key available. " +
                    "Please set properties file (com/sun/labs/aura/music/resource/api.properties) to use.");
        }
    }

    /**
     * Starts running the crawler
     */
    @Override
    public void start() {
        if (!running) {
            running = true;
            {
                Runnable discoverer = new Runnable() {
                    // this thread discovers new artists until
                    // we reach the maxArtists
                    public void run() {
                        addAllTags();
                        discoverArtists();
                    }
                };

                threadPool.submit(discoverer);
            }

            {
                Runnable updater = new Runnable() {

                    // this thread keeps all of our artists
                    // fresh and updated
                    public void run() {
                        itemUpdater(ItemType.ARTIST);
                    }
                };
                threadPool.submit(updater);
            }

            {
                Runnable updater = new Runnable() {

                    // this thread keeps all of our albums
                    // fresh and updated
                    public void run() {
                        itemUpdater(ItemType.ALBUM);
                    }
                };
                threadPool.submit(updater);
            }

            {
                Runnable updater = new Runnable() {

                    // this thread keeps all of our artists
                    // fresh and updated
                    public void run() {
                        newItemUpdater(ItemType.ARTIST);
                    }
                };
                threadPool.submit(updater);
            }

            {
                Runnable updater = new Runnable() {

                    // this thread keeps all of our albums
                    // fresh and updated
                    public void run() {
                        newItemUpdater(ItemType.ALBUM);
                    }
                };
                threadPool.submit(updater);
            }

            {
                Runnable updater = new Runnable() {

                    // this thread keep the play counts in artists for all
                    // listeners up to date. the listener crawler periodically
                    // updates the listener totals so we need to also update ours
                    public void run() {
                        try {
                            // Give the system a while before we start this
                            Thread.sleep(60 * 1000L);
                        } catch (InterruptedException ex) {
                        }
                        periodicallyUpdateListenerPlayCounts();
                    }
                };
                threadPool.submit(updater);
            }

        }
    }

    /**
     * Stops the crawler
     */
    @Override
    public void stop() {
        running = false;
        logger.info("Saving artist crawler queue state because of shutdown");
        saveState();
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        try {
            logger = ps.getLogger();
            musicBrainz = new MusicBrainz();
            wikipedia = new Wikipedia();
            youtube = new Youtube2();
            flickr = new FlickrManager();
            amazon = new Amazon();
            upcoming = new Upcoming();
            spotify = new Spotify();
            echoNest = new ArtistAPI(ECHONEST_API_KEY);

            rcmStore = new RemoteComponentManager(ps.getConfigurationManager(), DataStore.class);
            rcmCrawl = new RemoteComponentManager(ps.getConfigurationManager(), CrawlerController.class);

            stateDir = ps.getString(PROP_STATE_DIR);
            updateRateInSeconds = ps.getInt(PROP_UPDATE_RATE);
            newCrawlPeriod = ps.getInt(PROP_NEW_CRAWL_PERIOD);
            maxArtists = ps.getInt(PROP_MAX_ARTISTS);
            crawlAlbumBlurbs = ps.getBoolean(PROP_CRAWL_ALBUM_BLURBS);
            maxBlurbPages = ps.getInt(PROP_MAX_BLURB_PAGES);
            crawlEchoNestReviewsBlogs = ps.getBoolean(PROP_CRAWL_ECHONEST_REVIEWSBLOGS);
            util = new Util(flickr, youtube);
            tagFilter = new TagFilter();
            createStateFileDirectory();
            loadState();
            threadPool = Executors.newCachedThreadPool();
            mdb = new MusicDatabase(ps.getConfigurationManager());
        } catch (AuraException ex) {
            throw new PropertyException(ex, ps.getInstanceName(),
                    "musicDatabase", "problems with the music database");
        } catch (IOException ioe) {
            throw new PropertyException(ioe, "ArtistCrawler", ps.getInstanceName(), "");
        } catch (EchoNestException ex) {
            Logger.getLogger(ArtistCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void update(String id) throws AuraException, RemoteException {
        final Item item = getDataStore().getItem(id);
        if (item == null) {
            throw new AuraException("can't find item with ID " + id);
        }
        if (item.getType() != Item.ItemType.ARTIST) {
            throw new IllegalArgumentException("bad item type, should be ARTIST");
        }
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                Artist artist = new Artist(item);
                try {
                    updateArtist(artist, false);
                } catch (AuraException ex) {
                    logger.warning("Problem updating artist " + item.getKey() + " " + ex);
                } catch (RemoteException ex) {
                    logger.warning("Remote exception while updating artist " + item.getKey() + " " + ex);
                }
            }
        };

        threadPool.submit(updater);
    }

    /**
     * Get's an artist name from it's mbid. Uses local caching
     * @param artistId id of artist for which to fetch the name
     * @return the artist name
     * @throws RemoteException
     * @throws AuraException
     */
    private String getArtistName(String artistId) throws RemoteException, AuraException {
        String artistName;
        synchronized (artistNameCache) {
            if (artistNameCache.containsKey(artistId)) {
                artistName = artistNameCache.get(artistId);
            } else {
                try {
                    artistName = new Artist(getDataStore().getItem(artistId)).getName();
                    artistNameCache.put(artistId, artistName);

                } catch (NullPointerException npe) {
                    // If artist is not in the store, look in the crawl queue
                    QueuedItem qI = new QueuedItem(artistId);
                    if (crawlQueue.contains(qI)) {
                        Iterator<QueuedItem> iqi = crawlQueue.iterator();
                        boolean done = false;
                        while (iqi.hasNext() && !done) {
                            qI = iqi.next();
                            if (qI.getKey().equals(artistId)) {
                                if (qI.getName()!=null) {
                                    artistNameCache.put(artistId, qI.getName());
                                    return qI.getName();
                                } else {
                                    done = true;
                                }
                            }
                        }
                    }
                    
                    // Try to get artistname from musicbrainz and enqueue it since it seems to be missing
                    // in theory we should never end up here
                    try {
                        logger.warning("!! Attempting to get artist name from musicbrainz and enqueue it. This should not happen.");
                        MusicBrainzArtistInfo mbai = musicBrainz.getArtistInfo(artistId);
                        artistName = mbai.getName();
                        enqueue(new LastArtist(artistName, artistId), -1);
                        artistNameCache.put(artistId, artistName);
                    } catch (IOException ex) {
                        logger.warning(ex+" when trying to enqueue missing artist in crawl queue.");
                    }
                    artistName = null;
                }
            }
        }
        return artistName;
    }

    private DataStore getDataStore() throws AuraException {
        return (DataStore) rcmStore.getComponent();
    }

    /**
     * @deprecated
     */
    private LastFM getLastFM() throws AuraException, RemoteException {
        return (CrawlerController) rcmCrawl.getComponent();
    }

    private LastFM2 getLastFM2() throws AuraException, RemoteException {
        return (CrawlerController) rcmCrawl.getComponent();
    }

    private void addAllTags() {
        try {
            Set<String> tagNames = new HashSet<String>(tagFilter.getAllCanonicalTags());
            createTags(tagNames);
        } catch (AuraException e) {
            logger.warning("Can't add all tags " + e);
        } catch (RemoteException e) {
            logger.warning("Can't add all tags " + e);
        }
    }

    /**
     * Given a collection of tag names, ensure that they are all added  as
     * ARTIST_TAG items to the datastore
     * @param tagNames the list of tag names
     */
    private void createTags(Collection<String> tagNames) throws AuraException, RemoteException {
        for (String tagName : tagNames) {
            // make sure the tags are part of the database
            String id = ArtistTag.nameToKey(tagName);
            if (getDataStore().getItem(id) == null) {
                Item item = StoreFactory.newItem(ItemType.ARTIST_TAG, id, tagName);
                getDataStore().putItem(item);
                logger.fine("Adding tag " + item.getKey());
            }
        }
    }
    
    @Override
    public void add(String newID) throws AuraException, RemoteException {
        Item item = getDataStore().getItem(newID);
        if (item == null) {
            item = StoreFactory.newItem(ItemType.ARTIST, newID, "(unknown");
            Artist artist = new Artist(item);
            try {
                addMusicBrainzInfo(artist);
                updateArtist(artist, true);
            } catch (IOException ioe) {
                throw new AuraException("Can't get critical artist info from musicbrainz for " + newID + " " + ioe);
            }
        } else {
            throw new AuraException("item with ID " + newID + " already exists");
        }
    }

    /**
     * Starts discovering artists. When a new artist is encountered it is 
     * added to the datastore
     */
    private void discoverArtists() {
        long lastTime = 0L;
        try {
            if (crawlQueue.size() == 0) {
                primeArtistQueue("The Beatles");

                try {
                    if (getDataStore().getItemCount(ItemType.USER)>0) {
                        // If there are listeners in the store and we are priming the artist queue,
                        // there probably was a problem loading the state file. Ensure attention coherence
                        assertArtistAttentionCoherence();
                    }
                } catch (RemoteException ex) {
                    logger.warning("Problem ("+ex+") running played attention coherence check");
                    ex.printStackTrace();
                }
            }
        } catch (AuraException ae) {
            logger.severe("ArtistCrawler Can't talk to the datastore, abandoning crawl");
            return;
        }

        while (running) {
            if (crawlQueue.size() > 0) {
                try {

                    // if we've reached maxartists we are done
                    if (getDataStore().getItemCount(ItemType.ARTIST) >= maxArtists) {
                        logger.info("Artist discovery crawler reached max artists, shutting down");
                        break;
                    }

                    QueuedItem queuedArtist = (QueuedItem) crawlQueue.poll();
                    long curTime = System.currentTimeMillis();
                    logger.info("Crawling " + queuedArtist + " remaining " +
                            crawlQueue.size() + " time " + (curTime - lastTime) + " ms");
                    lastTime = curTime;

                    Artist artist = collectArtistInfo(queuedArtist);
                    if (artist != null) {
                        incrementModCounter();
                    }
                } catch (AuraException ex) {
                    logger.warning("Aura Trouble during crawl " + ex);
                    ex.printStackTrace();
                } catch (RemoteException ex) {
                    logger.warning("Remote Trouble during crawl " + ex);
                } catch (IOException ex) {
                    logger.warning("IO Trouble during crawl " + ex);
                } catch (Throwable t) {
                    logger.severe("Unexpected error during artist discovery crawl " + t);
                    t.printStackTrace();
                }
            } else {
                try {
                    // If discovery queue is empty, sleep fo a while as the listener crawler
                    // might add more new artists to crawl
                    logger.fine("ArtistDiscovery sleeping because queue is empty");
                    Thread.sleep(5 * 60 * 1000L);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    private Map<String, Integer> strToBlurbs(List<String> reviews) {
        return strToBlurbs(reviews, new HashMap<String, Integer>());
    }

    private Map<String, Integer> strToBlurbs(List<String> reviews, Map<String, Integer> map) {
        for (String review : reviews) {
            if (review == null || review.isEmpty()) {
                continue;
            }
            review = Utilities.detag(review);
            String words = normalizeText(" " + review + " ");
            for (String tag : tagFilter.getAllTagAliases()) {
                int count = findMatches(tag, words);
                if (count > 0) {
                    String mappedTag = tagFilter.mapTagName(tag);
                    Integer c = map.get(mappedTag);
                    if (c == null) {
                        c = Integer.valueOf(0);
                    }
                    map.put(mappedTag, c + count);
                }
            }
        }
        return map;
    }

    private void updateItems(ItemType iT, boolean force, long period) throws AuraException, RemoteException, InterruptedException {

        String itemsTypeStr = iT.toString().toLowerCase();
        CrawlableItem cI = null;

        List<Scored<String>> scoredItems = getAllItemsSortedByLastCrawl(iT);
        for (Scored<String> sitem : scoredItems) {

            if (iT == ItemType.ARTIST) {
                cI = new Artist(getDataStore().getItem(sitem.getItem()));
            } else if (iT == ItemType.ALBUM) {
                cI = new Album(getDataStore().getItem(sitem.getItem()));
            } else {
                throw new IllegalArgumentException("Unsupported item type.");
            }

            if (force || needsUpdate(cI)) {
                if (addToCrawlList(cI.getKey())) {
                    try {
                        logger.info("  Updating " + itemsTypeStr + " " + cI.getName());
                        updateItemWithErrorRecovery(cI, false);
                        try {
                            Thread.sleep(period);
                        } catch (InterruptedException e) {
                            break;
                        }
                    } finally {
                        removeFromCrawlList(cI.getKey());
                    }
                } else {
                    logger.fine("Skipping item " + cI.getKey() +
                            " because another process is already crawling it");
                }
            } else {
                logger.fine("    " + cI.getName() + " is up to date");
            }
        }
    }

    private List<Scored<String>> getAllItemsSortedByLastCrawl(ItemType iT) throws AuraException, RemoteException {

        if (iT != ItemType.ALBUM && iT != ItemType.ARTIST && iT != ItemType.TRACK) {
            throw new IllegalArgumentException("Invalid item type.");
        }

        List<Scored<String>> itemList = new ArrayList();
        DBIterator iter = getDataStore().getAllIterator(iT);

        try {
            CrawlableItem cI = null;
            while (iter.hasNext()) {
                Item item = (Item) iter.next();
                if (iT == ItemType.ALBUM) {
                    cI = new Album(item);
                } else if (iT == ItemType.ARTIST) {
                    cI = new Artist(item);
                } else if (iT == ItemType.TRACK) {
                    cI = new Track(item);
                }
                itemList.add(new Scored<String>(item.getKey(), cI.getLastCrawl()));
                cI = null;
            }

        } finally {
            iter.close();
        }
        Collections.sort(itemList, ScoredComparator.COMPARATOR);
        return itemList;
    }

    private void itemUpdater(ItemType iT) {
        String itemsTypeStr = iT.toString().toLowerCase();
        FixedPeriod fixedPeriod = new FixedPeriod(updateRateInSeconds * 1000L);
        try {
            while (running) {
                fixedPeriod.start();
                Long startTime = new Date().getTime();
                logger.info(">> "+itemsTypeStr+" update starting at "+startTime);
                try {
                    updateItems(iT, false, 2000L);
                } catch (AuraException ex) {
                    logger.warning("trouble in "+itemsTypeStr+" updater, retrying in a while" + ex);
                } catch (RemoteException ex) {
                    logger.warning("trouble in "+itemsTypeStr+" updater, retrying in a while " + ex);
                } catch (InterruptedException ex) {
                    logger.info(itemsTypeStr+" updater, interrupted, retrying in a while");
                } catch (Throwable t) {
                    logger.severe("Unexpected error during "+itemsTypeStr+" updater crawl, retrying in a while" + t);
                    t.printStackTrace();
                }
                logger.info(">> "+itemsTypeStr+" update completed. Took "+(new Date().getTime()-startTime));
                fixedPeriod.end();
            }
        } catch (InterruptedException ex) {
            logger.info(itemsTypeStr+" updater interrupted, shutting down.");
        }
    }

    private void newItemUpdater(ItemType iT) {
        long lastCrawl = 0;
        FixedPeriod fp = new FixedPeriod(newCrawlPeriod * 1000L);
        while (running) {

            try {
                fp.start();
                lastCrawl = crawlNewItems(lastCrawl, iT);
                fp.end();
            } catch (InterruptedException ex) {
            } catch (AuraException ex) {
                logger.warning("AuraException while crawling " + iT + " " + ex);
            } catch (RemoteException ex) {
                logger.warning("Remote exception while crawling " + iT + " " + ex);
            }
        }
    }

    private long crawlNewItems(long lastCrawl, ItemType iT) throws AuraException, RemoteException {
        long maxCrawl = lastCrawl;
        List<CrawlableItem> artists = getNewItemsAddedSince(lastCrawl + 1, iT);
        logger.fine("New artists check found " + artists.size() + " new artists");
        for (CrawlableItem cI : artists) {
            if (cI.getItem().getTimeAdded() > maxCrawl) {
                maxCrawl = cI.getItem().getTimeAdded();
            }
            if (addToCrawlList(cI.getKey())) {
                try {
                    logger.info("  Crawling new "+iT+" " + cI.getName());
                    if (iT == ItemType.ARTIST) {
                        updateArtist((Artist) cI, false);
                    } else if (iT == ItemType.ALBUM) {
                        try {
                            updateAlbumAndTracks((Album) cI);
                        } catch (IOException ex) {
                            // We should only be getting this error if the main music brainz call failed
                            logger.info("IOException when trying to update new album "+cI.getKey());
                        }
                    }
                } finally {
                    removeFromCrawlList(cI.getKey());
                }
            } else {
                logger.fine("Skipping item " + cI.getKey() +
                        " because another process is already crawling it");
            }
        }
        return maxCrawl;
    }

    private void debug(long when) throws AuraException, RemoteException {
        logger.info("*** DEBUG found " + getNewItemsAddedSince(when, ItemType.ARTIST).size() + " new artists since " + new Date(when));
    }

    private List<CrawlableItem> getNewItemsAddedSince(long lastCrawl, ItemType iT) throws AuraException, RemoteException {
        List<CrawlableItem> cIList = new ArrayList();

        DBIterator iter = getDataStore().getItemsAddedSince(iT, new Date(lastCrawl));
        try {
            while (iter.hasNext()) {
                Item item = (Item) iter.next();
                CrawlableItem cI = null;
                if (iT == ItemType.ARTIST) {
                    cI = new Artist(item);
                } else if (iT == ItemType.ALBUM) {
                    cI = new Album(item);
                }
                if (cI.getUpdateCount() == 0) {
                    cIList.add(cI);
                }
            }
        } finally {
            iter.close();
        }
        logger.fine("Num "+iT+" created since " + new Date(lastCrawl) + " is " + cIList.size());
        return cIList;
    }

    private boolean needsUpdate(CrawlableItem item) {
        return (item.getUpdateCount() == 0L) ||
                ((System.currentTimeMillis() - item.getLastCrawl()) > updateRateInSeconds * 1000L);
    }

    private boolean worthVisiting(LastArtist lartist) throws AuraException, RemoteException {
        if (lartist.getMbaid() == null || lartist.getMbaid().length() == 0) {
            return false;
        }

        if (inArtistQueue(lartist.getMbaid())) {
            return false;
        }

        if (getDataStore().getItem(lartist.getMbaid()) != null) {
            return false;
        }
        return true;
    }

    private boolean inArtistQueue(String mbaid) {
        for (Object qartist : crawlQueue) {
            if (((QueuedItem)qartist).getKey().equals(mbaid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collects the information for an artist
     * @param queuedArtist the artist of interest
     * @return a fully populated artist
     * @throws com.sun.labs.aura.util.AuraException if a problem with the datastore is encountered
     * @throws java.rmi.RemoteException if a communicatin error occurs
     */
    private Artist collectArtistInfo(QueuedItem queuedArtist) throws AuraException, RemoteException, IOException {
        String mbaid = queuedArtist.getKey();
        if (mbaid != null && mbaid.length() > 0) {
            Item item = getDataStore().getItem(mbaid);
            if (item == null) {

                item = StoreFactory.newItem(ItemType.ARTIST, mbaid, queuedArtist.getName());
                Artist artist = new Artist(item);
                artist.setPopularity(queuedArtist.getPopularity());
                updateArtist(artist, true);
                return artist;
            }
        }
        return null;
    }

    private void updateItemWithErrorRecovery(CrawlableItem item, boolean discoverMode) {
        int maxRetries = 5;
        boolean done = false;

        while (!done && maxRetries-- > 0) {
            try {
                if (item.getType() == ItemType.ARTIST) {
                    updateArtist((Artist)item, discoverMode);
                } else if (item.getType() == ItemType.ALBUM) {
                    updateAlbumAndTracks((Album)item);
                }
                done = true;
            } catch (AuraException ex) {
                logger.warning("AuraExeption while crawling " + item.getName() + " retrying. " + ex.getMessage());
                ex.printStackTrace();
            } catch (RemoteException ex) {
                logger.warning("RemoteException while crawling " + item.getName() + " retrying. " + ex.getMessage());
                ex.printStackTrace();
            } catch (Throwable t) {
                logger.warning("Unexpected exception  while crawling " + item.getName() + " retrying. " + t.getMessage());
                t.printStackTrace();
            }
            if (!done) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    private void updateArtist(final Artist artist, final boolean discoverMode)
            throws AuraException, RemoteException {

        addMusicBrainzInfoIfNecessary(artist);

        CommandRunner runner = new CommandRunner(false, logger.isLoggable(Level.FINE));

        runner.add(new Commander("last.fm1") {

            @Override
            public void go() throws Exception {
                updateLastFMPopularity(artist);
            }
        });

        runner.add(new Commander("last.fm2") {

            @Override
            public void go() throws Exception {
                addLastFmTags(artist);
                addLastFMCounts(artist);
                if (discoverMode) {
                    addSimilarArtistsToQueue(artist);
                }
            }
        });

        runner.add(new Commander("wikipedia") {

            @Override
            public void go() throws Exception {
                addWikipediaInfo(artist);
            }
        });

        runner.add(new Commander("flickr") {

            @Override
            public void go() throws Exception {
                addFlickrPhotos(artist);
            }
        });

        /*runner.add(new Commander("spotify") {

            @Override
            public void go() throws Exception {
                addSpotifyInfo(artist);
            }
        });*/

        runner.add(new Commander("echonest") {

            @Override
            public void go() throws Exception {
                addEchoNestInfo(artist);
            }
        });

        runner.add(new Commander("amazon") {

            @Override
            public void go() throws Exception {
                if (crawlAlbumBlurbs) {
                    crawlAmazonForBlurbs(artist);
                }
            }
        });

        /*runner.add(new Commander("youtube") {

            @Override
            public void go() throws Exception {
                addYoutubeVideos(artist);
            }
        });*/

        runner.add(new Commander("upcoming") {

            @Override
            public void go() throws Exception {
                addUpcomingInfo(artist);
            }
        });

        runner.add(new Commander("listenerPlayCount") {

            @Override
            public void go() throws Exception {
                artist.clearListenersPlayCounts();
                // This updates the aggregated play count for the listeners present
                // in the datastore.
                artist.setListenersPlayCount(mdb.getListenersIdsForArtist(artist.getKey(), Integer.MAX_VALUE));
            }
        });

        try {
            runner.go();
        } catch (Exception e) {
            // if we get an exception when we are crawling, 
            // we still have some good data, so log the problem
            // but still return the artist so we can add it to the store
            logger.warning("Artist Crawler Exception " + e);
        }
        artist.setLastCrawl();
        artist.incrementUpdateCount();
        artist.flush(getDataStore());
    }


    /**
     * Makes sure that all the artists for which we have added "played" attention 
     * are either in the store or the crawl queue. Enqueue them if they are not
     * found at either place
     */
    private void assertArtistAttentionCoherence() throws AuraException, RemoteException {

        logger.info("ArtistAttentionCoherence: Running coherence check");

        int cnt = 0;
        Set<String> artistIds = mdb.getAttendedToArtists(null, Attention.Type.PLAYED, Integer.MAX_VALUE);
        logger.info("ArtistAttentionCoherence: Got " + artistIds.size() + " unique artists from store");
        for (String artistId : artistIds) {
            // Look in the store
            Item item = getDataStore().getItem(artistId);
            if (item==null) {
                // Look in the crawl queue
                QueuedItem qA = new QueuedItem(artistId, -1);
                if (!crawlQueue.contains(qA)) {
                    logger.finer("ArtistAttentionCoherence: Added " + artistId + " to crawl queue");
                    crawlQueue.add(qA);
                    cnt++;
                }
            }
        }
        logger.info("ArtistAttentionCoherence: Queued " + cnt + " artists");
        incrementModCounter(cnt);
    }

    private void periodicallyUpdateListenerPlayCounts() {
        // Update every week
        FixedPeriod fp = new FixedPeriod(6 * 60 * 60 * 1000L);
        while (running) {
            try {
                fp.start();
                updateListenerPlayCounts();
                fp.end();
            } catch (AuraException ex) {
                logger.warning("Aura Exception while updating listener play cnts " + ex);
                ex.printStackTrace();
            } catch (RemoteException ex) {
                logger.warning("Remote Exception while updating listener play cnts " + ex);
            } catch (IOException ex) {
                logger.warning("IO Exception while updating listener play cnts " + ex);
                ex.printStackTrace();
            } catch (Throwable t) {
                logger.severe("Unexpected error during listener play cnts updating " + t);
                t.printStackTrace();
            }
        }
    }

    /**
     * For each artist, update the listener play count vector.
     * @throws AuraException
     * @throws RemoteException
     */
    private void updateListenerPlayCounts() throws AuraException, RemoteException {

        // Go through all artists
        DBIterator<Item> iter = getDataStore().getAllIterator(ItemType.ARTIST);
        try {
            long startTime = System.currentTimeMillis();
            logger.info(("ArtistCrawler: Starting updating of listener play counts for all artists at "+startTime));
            while (iter.hasNext()) {
                Artist originalArtist = new Artist(iter.next());

                // We need a lock on the item to do this update so wait until we can get one
                while (!addToCrawlList(originalArtist.getKey())) {
                    try {
                        Thread.sleep(2 * 1000L);
                    } catch (InterruptedException ex) {
                    }
                }
                try {
                    // Once we have a lock, refetch the item to make sure we won't be
                    // overwriting some modifications
                    Artist a = new Artist(getDataStore().getItem(originalArtist.getKey()));

                    a.clearListenersPlayCounts();

                    for (Counted<String> cL : mdb.getListenersIdsForArtist(a.getKey(), Integer.MAX_VALUE)) {
                        a.setListenersPlayCount(cL.getItem(), (int)cL.getCount());
                    }
                    mdb.flush(a);
                } finally {
                    removeFromCrawlList(originalArtist.getKey());
                }
            }
            logger.info("ArtistCrawler: Finished updating listener play counts in "+(System.currentTimeMillis()-startTime));
        } finally {
            iter.close();
        }

    }

    private void addMusicBrainzInfoIfNecessary(Artist artist) throws AuraException, RemoteException {

        boolean needsUpdate = false;

        // refresh every 10 refresh periods
        if (artist.getUpdateCount() % 10 == 0) {
            needsUpdate = true;
        }
        
        // if an artist has no start year, we probably have not
        // successfully crawled the MB data for this artist yet
        if (needsUpdate || artist.getBeginYear() == 0) {
            try {
                addMusicBrainzInfo(artist);
            } catch (IOException ex) {
                logger.info("Trouble getting musicbrainz info for " + artist.getName());
            }
        }
    }

    private void updateLastFMPopularity(Artist artist) {
        try {
            int popularity = getLastFM().getPopularity(artist.getName());
            artist.setPopularity(popularity);
        } catch (Exception ioe) {
            // can't get popularity data from last.fm
            // so just skip the update for now
        }
    }

    /**
     * Adds the playcount and listener count for the given artist from the last.fm api
     * @param artist artist for which to add counts
     * @throws RemoteException
     * @throws IOException
     * @throws AuraException
     */
    private void addLastFMCounts(Artist artist) throws RemoteException, IOException, AuraException {
        try {
            LastArtist2 lA2 = getLastFM2().getArtistInfo(artist.getKey(), artist.getName());
            artist.addLastfmPlayCount(lA2.getPlaycount());
            artist.addLastfmListenerCount(lA2.getListenerCount());
        } catch (CannotResolveException ex) {
            logger.info(ex.getMessage());
        }
    }

    /**
     * Primes the artist queue for discovery. The queue is primed with artists that
     * are similar to the beatles
     */
    private void primeArtistQueue(String artistName) throws AuraException {
        if (crawlQueue.size() == 0) {
            try {
                logger.info("Priming queue with " + artistName);
                LastArtist[] simArtists = getLastFM2().getSimilarArtists(artistName);
                for (LastArtist simArtist : simArtists) {
                    if (worthVisiting(simArtist)) {
                        int popularity = getLastFM().getPopularity(simArtist.getArtistName());
                        logger.info("  adding  " + simArtist.getArtistName() + " pop: " + popularity);
                        enqueue(simArtist, popularity);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ArtistCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public synchronized boolean enqueue(LastItem item, int popularity) throws RemoteException {
        LastArtist lA = new LastArtist(item.getName(), item.getMBID());
        return enqueue(lA, popularity);   
    }

    /**
     * Adds an artist to the discovery queue
     * @param artist the artist to be queued
     * @param popularity the popularity of the artist. If -1 is passed, the
     * popularity will be determined prior to enqueing the artist
     */
    private synchronized boolean enqueue(LastArtist artist, int popularity) throws RemoteException {
        QueuedItem qA = new QueuedItem(artist, popularity);
        if (!crawlQueue.contains(qA)) {
            // If the popularity was not determined by the calling object, determine it now
            if (qA.getPopularity() == -1) {
                int pop = 50;
                try {
                    pop = getLastFM().getPopularity(qA.getName());
                } catch (Throwable t) {
                    logger.warning("Exception "+t+" when trying to determine artist popularity");
                    t.printStackTrace();
                }

                qA = new QueuedItem(artist, pop);
            }

            crawlQueue.add(qA);
            incrementModCounter();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds musicbrainz info to the aritst
     * @param artist the artist to be augmented
     * @throws com.sun.labs.aura.util.AuraException if a datastore error occurs
     */
    private void addMusicBrainzInfo(Artist artist) throws AuraException, RemoteException, IOException {
        MusicBrainzArtistInfo mbai = musicBrainz.getArtistInfo(artist.getKey());
        artist.setBeginYear(mbai.getBeginYear());
        artist.setEndYear(mbai.getEndYear());
        artist.setName(mbai.getName());

        for (String name : mbai.getURLMap().keySet()) {
            artist.addUrl(name, mbai.getURLMap().get(name));
        }

        for (MusicBrainzAlbumInfo mbalbum : mbai.getAlbums()) {
            int pages = maxBlurbPages / mbai.getAlbums().size();
            if (pages <= 1) {
                pages = 1;
            }
            String mbrid = mbalbum.getId();
            Item albumItem = getDataStore().getItem(mbrid);
            Album album = null;
            if (albumItem == null) {
                albumItem = StoreFactory.newItem(ItemType.ALBUM, mbrid, mbalbum.getTitle());
            }
            album = new Album(albumItem);
            album.setAsin(mbalbum.getAsin());
            for (String id : mbalbum.getArtistIds()) {
                album.addArtistId(id);
            }
            album.flush(getDataStore());

            artist.addAlbum(mbrid);
        }

        for (String id : mbai.getCollaborators()) {
            artist.addRelatedArtist(id);
        }
    }

    /**
     * Updates the information about an album and the tracks it contains.
     */
    private void updateAlbumAndTracks(Album album) throws AuraException, RemoteException, IOException {

        MusicBrainzAlbumInfo mbalbum = musicBrainz.getAlbumInfo(album.getKey());

        try {
            album.setReleaseDate(mbalbum.getReleaseDate());
        } catch (NullPointerException e) {
        }

        // If album does not have a valid artist id, find it and add it.
        // TODO. This should eventually removed since albums are now created
        // with an artist id already added to them
        if (album.getArtistId().isEmpty()) {
            List<Counted<String>> lcS = getDataStore().getTermCounts(album.getKey(),
                    Artist.FIELD_ALBUM, 10, new TypeFilter(ItemType.ARTIST));
            for (Counted<String> cS : lcS) {
                album.addArtistId(cS.getItem());
                logger.info("  Added artist:" + cS.getItem() + " to album:" + album.getKey());
            }
        }

        // add urls
        for (Entry<String, String> e : mbalbum.getURLMap().entrySet()) {
            album.addUrl(e.getKey(), e.getValue());
        }

        // add tracks
        for (Entry<Integer, MusicBrainzTrackInfo> e : mbalbum.getTrackMap().entrySet()) {
            album.addTrack(e.getKey(), e.getValue().getMbid());

            Item trackItem = getDataStore().getItem(e.getValue().getMbid());
            Track track;
            if (trackItem == null) {
                trackItem = StoreFactory.newItem(ItemType.TRACK, e.getValue().getMbid(), e.getValue().getTitle());
                logger.info("    > Creating track '" + e.getValue().getTitle() + "' for album '" + album.getTitle() + "'");
            }
            track = new Track(trackItem);
            track.addAlbumId(album.getKey());
            for (String id : album.getArtistId()) {
                track.addArtistId(id);
            }
            track.setSecs(e.getValue().getDuration());

            try {
                addLastFmTags(track);

                String artistName = getArtistName(track.getArtistId().iterator().next());
                try {
                    LastTrack lt = getLastFM2().getTrackInfo(track.getKey(), artistName, track.getName());
                    track.addLastfmListenerCount(lt.getListenerCount());
                    track.addLastfmPlayCount(lt.getPlaycount());
                    if (lt.getWikiContent() != null && !lt.getWikiContent().isEmpty()) {
                        track.setSummary(lt.getWikiContent());
                    }
                    track.setStreamableLastfm(lt.getStreamable());

                } catch (CannotResolveException ex) {
                    // we can't resolve so bail out
                    logger.info(ex.getMessage());
                }
            } catch (IOException io) {
                // We got an io exception for one track. log it and keep going
                // so we update all tracks
                logger.warning("IOException while updating track " + track.getKey());
            }

            track.incrementUpdateCount();
            track.setLastCrawl();
            track.flush(getDataStore());
        }

        // Try to find a summary. First look at last.fm and then wikipedia
        // if we have a valid link
        LastAlbum2 la2 = null;
        try {
            String artistName = getArtistName(album.getArtistId().iterator().next());
            if (artistName != null) {
                la2 = getLastFM2().getAlbumInfoByName(artistName, album.getTitle());
                if (album.getReleaseDate() == 0) {
                    try {
                        album.setReleaseDate(LastFM2Impl.lfm2DateFormater.parse(la2.getReleaseDate().trim()).getTime());
                    } catch (ParseException ex) {
                        logger.fine("Unable to determine release date for album " + album.getKey() + ". " + ex);
                    }
                }
            } else {
                logger.warning("Album " + album.getKey() + " is pointing to an artist " +
                        "that does not exist (" + album.getArtistId().iterator().next() + ")");
            }
        } catch (IOException io) {
            logger.warning("Warning. Problem (" + io + ") getting album (" + album.getKey() + ") info.");
            io.printStackTrace();
        }

        if (la2 != null && !la2.getWikiFull().isEmpty()) {
            album.setSummary(la2.getWikiFull());
        } else {
            if (album.getUrls().containsKey("Wikipedia")) {
                try {
                    WikiInfo wikiInfo = wikipedia.getWikiInfo(album.getUrls().get("Wikipedia"));
                    album.setSummary(wikiInfo.getSummary());
                } catch (IOException io) {
                    logger.warning("Warning. Problem (" + io + ") getting album (" + album.getKey() + ") summary.");
                }
            }
        }

        album.incrementUpdateCount();
        album.setLastCrawl();
        album.flush(getDataStore());
    }

    /**
     * Crawl all albums for a given artist to collect reviews and use them to
     * as blurb tags
     */
    private void crawlAmazonForBlurbs(Artist artist) throws AuraException, RemoteException {

        List<String> allReviews = new ArrayList<String>();

        int maxAlbums = artist.getAlbums().size();
        int pages = getNumBlurbPages(artist);

        if (pages > 0) {

            for (String albumID : artist.getAlbums()) {
                Item albumItem = getDataStore().getItem(albumID);
                if (albumItem != null) {

                    Album album = new Album(getDataStore().getItem(albumID));
                    try {
                        if (album.getAsin() != null && album.getAsin().length() > 0) {
                            allReviews.addAll(amazon.lookupReviews(album.getAsin(), pages));
                        }
                    } catch (IOException ioe) {
                        logger.warning("Trouble collecting reviews from " +
                                album.getTitle() + " for " + artist.getName());
                    }
                }
            }

            Map<String, Integer> blurbMap = strToBlurbs(allReviews);
            int curSize = artist.getTags(TagType.BLURB).size();
            // if we found more tags than before, replace the
            // old ones with the new ones.
            if (blurbMap.size() > curSize) {
                for (Map.Entry<String, Integer> entry : blurbMap.entrySet()) {
                    if (entry.getValue() >= minBlurbCount) {
                        artist.setTag(TagType.BLURB, entry.getKey(), entry.getValue());
                    }
                }
            }

            // some debugging code
            if (logger.isLoggable(Level.FINE)) {
                List<Tag> tags = artist.getTags(TagType.BLURB);
                logger.fine("====== Blurbs for " + artist.getName() + " === albums: " + maxAlbums);
                for (Tag tag : tags) {
                    logger.fine(String.format("(%s,%d)", tag.getName(), tag.getCount()));
                }
            }
        }
    }

    /**
     * Gets the number of blurb pages we should collect for this artist
     * @param artist the artist of interest
     * @return the number of blurb pages to crawl per album
     */
    private int getNumBlurbPages(Artist artist) {
        // getting al the blurbs can be expensive, so we do it in phases
        // first phase - just get one page per album
        // second phase -  get 5 pages per album
        // third phase -  get upto 20 pages per album
        // fourth phase -  only update the full, every 4th update

        long updateCount = artist.getUpdateCount();

        if (updateCount == 0) {
            return 1;
        }
        if (updateCount == 1) {
            return 5;
        }
        if (updateCount == 2) {
            return 20;
        }
        if (updateCount > 2) {
            if ((updateCount - 2) % 4 == 0) {
                return 20;
            }
        }
        return 0;
    }

    private void addSpotifyInfo(Artist artist) throws IOException {
        String id = spotify.getSpotifyIDforArtist(artist.getName());
        artist.setSpotifyID(id);
    }

    private void addEchoNestInfo(Artist artist) throws IOException, EchoNestException {

        // Get echonest id if we don't already have it
        if (artist.getEchoNestId() == null) {
            for (com.echonest.api.v3.artist.Artist tA : echoNest.searchArtist(artist.getName(), false)) {
                try {
                    if (echoNest.getUrls(tA).get("mb_url").contains(artist.getKey())) {
                        artist.setEchoNestId(tA.getId());
                        break;
                    }
                } catch (NullPointerException e) {
                    // If the echonest doesn't have the artist's mbid, skip it
                    break;
                }
            }
            if (artist.getEchoNestId() == null) {
                logger.fine("No EchoNest match for artist '" + artist.getName() + "'");
                return;
            }
        }

        // Get audio urls
        DocumentList<Audio> urls = echoNest.getAudio(artist.getEchoNestId(), 0, 15);
        for (Audio a : urls.getDocuments()) {
            artist.addAudio(a.getLink());
        }
        if (urls.getTotal() == 0) {
            logger.fine("No echonest audio found for " + artist.getName());
        } else {
            logger.fine("crawled " + urls.getTotal() + " audio files from echonest for " + artist.getName());
        }

        // Get familiarity and hotttnesss
        artist.setFamiliarity(echoNest.getFamiliarity(artist.getEchoNestId()));
        artist.setHotttnesss(echoNest.getHotness(artist.getEchoNestId()));

        // Get blogs and reviews. Crawl them every 5 updates except if one of them is empty
        if (crawlEchoNestReviewsBlogs && (artist.getUpdateCount() % 5 == 0 ||
                Math.min(artist.getTags(TagType.BLOG_EN).size(), 
                artist.getTags(TagType.REVIEW_EN).size())==0)) {

            // Maximum number of "pages" to get
            int maxI;
            if (artist.getUpdateCount() < 10) {
                maxI = 0;
            } else {
                maxI = 3;
            }

            List<String> reviews = new ArrayList<String>();
            for (int i=0; i<=maxI; i++) {
                for (Review r : echoNest.getReviews(artist.getEchoNestId(), i*15, 15).getDocuments()) {
                    if (!artist.crawledEchoNestDocId(r.getId())) {
                        if (r.getReviewText() != null && !r.getReviewText().isEmpty()) {
                            reviews.add(r.getReviewText());
                            artist.addCrawledEchoNestDocId(r.getId());
                        } else if (r.getSummary() != null && !r.getSummary().isEmpty()) {
                            reviews.add(r.getSummary());
                            artist.addCrawledEchoNestDocId(r.getId());
                        }
                    }
                }
            }
            artist.incrementTags(TagType.REVIEW_EN, strToBlurbs(reviews));

            List<String> blogs = new ArrayList<String>();
            for (int i=0; i<=maxI; i++) {
                for (Blog b : echoNest.getBlogs(artist.getEchoNestId(), i*15, 15).getDocuments()) {
                    if (!artist.crawledEchoNestDocId(b.getId())) {
                        blogs.add(b.getSummary());
                        artist.addCrawledEchoNestDocId(b.getId());
                    }
                }
            }
            artist.incrementTags(TagType.BLOG_EN, strToBlurbs(blogs));
        }
    }

    /**
     * Adds wikipedia info to the artist
     * @param artist the artist of interest
     */
    private void addWikipediaInfo(Artist artist) throws IOException {
        String query = (String) artist.getUrls().get("Wikipedia");
        if (query == null) {
            query = artist.getName();
        }
        WikiInfo wikiInfo = wikipedia.getWikiInfo(query);
        artist.setBioSummary(wikiInfo.getSummary());
        addBioTags(artist, wikiInfo.getFullText());
    }

    private void addBioTags(Artist artist, String description) {
        String words = normalizeText(" " + description + " ");
        for (String tag : tagFilter.getAllTagAliases()) {
            int count = findMatches(tag, words);
            if (count > 0) {
                artist.setTag(TagType.BIO, tagFilter.mapTagName(tag), count);
            // logger.info("Adding " + tag + ":" + count);
            }
        }
    }

    private String normalizeText(String s) {
        s = s.replaceAll("[^\\w\\s]", " ").toLowerCase();
        s = s.replaceAll("[\\s]+", " ");
        return s;
    }

    private int findMatches(String tag, String text) {
        Pattern p = Pattern.compile("\\s" + tag + "\\s");
        Matcher m = p.matcher(text);
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }

    private void addYoutubeVideos(Artist artist) throws AuraException, RemoteException, IOException {
        List<Video> videos = util.collectYoutubeVideos(getDataStore(), artist.getName(), 24);
        artist.clearVideos();
        for (Video video : videos) {
            artist.addVideo(video.getKey());
        }
    }

    private void addFlickrPhotos(Artist artist) throws AuraException, RemoteException, IOException {
        List<Photo> photos = util.collectFlickrPhotos(getDataStore(), artist.getName(), 24);
        artist.clearPhotos();
        for (Photo photo : photos) {
            artist.addPhoto(photo.getKey());
        }
    }

    /**
     * Add filtered social and raw tags to a taggable item
     * @param item taggable item like artist, album or track
     * @throws AuraException
     * @throws RemoteException
     * @throws IOException
     */
    private void addLastFmTags(TaggableItem item) throws AuraException, RemoteException, IOException {
        SocialTag[] tags = null;
        if (item.getType() == ItemType.ARTIST) {
            tags = getLastFM2().getArtistTags(item.getName());

        } else if (item.getType() == ItemType.TRACK) {
            // We need the track's name and the artist's name. Try to get them
            // from cache or fetch from store
            Track t = (Track) item;
            String artistId = t.getArtistId().iterator().next();
            String artistName = getArtistName(artistId);
            if (artistName == null) {
                logger.warning("Could not determine artist name for artist "+artistId+
                        " for track "+t.getKey()+". Adding of tags aborted.");
                return;
            }
            
            /*
             * Try to fetch the track's tags by artistname/trackname and then by mbid.
             * Try to figure out if it's failing because we can't resolve the names and mbid
             * so we don't retry multiple times as there is in this case no hope of having this
             * work
             * */
            try {
                tags = getLastFM2().getTrackTopTags(item.getKey(), artistName, item.getName());
            } catch (CannotResolveException ex) {
                // we can't resolve so bail out
                logger.info(ex.getMessage());
                return;
            }
        } else {
            throw new AuraException("Adding last.fm tags to item of type " +
                    item.getType() + " is not supported");
        }

        for (SocialTag tag : tags) {

            int normFreq = (tag.getFreq() + 1) * (tag.getFreq() + 1);

            // Add raw tag. We need to verify the existance of the tag item because
            // unlike social tags, we do not know in advance if it's in the store
            String normTagName = Utilities.normalize(tag.getName());
            String id = ArtistTagRaw.nameToKey(normTagName);
            if (getDataStore().getItem(id) == null) {
                Item newRawTag = StoreFactory.newItem(ItemType.ARTIST_TAG_RAW, id, normTagName);
                getDataStore().putItem(newRawTag);
                logger.finer("Adding raw tag " + newRawTag.getKey());
            }
            item.setTag(TagType.SOCIAL_RAW, normTagName, normFreq);

            
            // Add social tag
            String tagName = tagFilter.mapTagName(tag.getName());
            if (tagName != null) {
                item.setTag(TagType.SOCIAL, tagName, normFreq);
            }
        }
    }

    private void addSimilarArtistsToQueue(Artist artist) throws AuraException, RemoteException, IOException {
        LastArtist[] simArtists = getLastFM2().getSimilarArtists(artist.getName());
        int fanOut = 0;
        for (LastArtist simArtist : simArtists) {
            if (worthVisiting(simArtist)) {
                int popularity = getLastFM().getPopularity(simArtist.getArtistName());
                enqueue(simArtist, popularity);
                if (fanOut++ >= MAX_FAN_OUT) {
                    break;
                }
            }
        }
    }

    private void addUpcomingInfo(Artist artist) throws AuraException, RemoteException, IOException {
        int count = 0;
        int MAX_EVENTS = 5;
        List<UpcomingEvent> events = upcoming.searchEventsByArtist(artist.getName());
        artist.clearEvents();
        for (UpcomingEvent event : events) {
            Item item = StoreFactory.newItem(ItemType.EVENT, event.getEventID(), event.getName());
            Event itemEvent = new Event(item);
            itemEvent.setName(event.getName());
            itemEvent.setDate(event.getDate());
            itemEvent.setVenueName(event.getVenue());
            itemEvent.flush(getDataStore());
            artist.addEvent(itemEvent.getKey());
            if (count++ >= MAX_EVENTS) {
                break;
            }
        }
    }

    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    @ConfigComponent(type = CrawlerController.class)
    public final static String PROP_CRAWLER_CONTROLLER = "crawlerController";
    @ConfigInteger(defaultValue = 100000)
    public final static String PROP_MAX_ARTISTS = "maxArtists";
    private int maxArtists;
    @ConfigBoolean(defaultValue = true)
    public final static String PROP_CRAWL_ALBUM_BLURBS = "crawlAlbumBlurbs";
    private boolean crawlAlbumBlurbs;
    @ConfigInteger(defaultValue = 10)
    public final static String PROP_MAX_BLURB_PAGES = "maxBlurbPages";
    private int maxBlurbPages;
    @ConfigBoolean(defaultValue = true)
    public final static String PROP_CRAWL_ECHONEST_REVIEWSBLOGS = "crawlEchoNestReviewsBlogs";
    private boolean crawlEchoNestReviewsBlogs;
}

