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
import com.echonest.api.v3.artist.DocumentList;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.music.Album;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.Event;
import com.sun.labs.aura.music.Photo;
import com.sun.labs.aura.music.Video;
import com.sun.labs.aura.music.util.CommandRunner;
import com.sun.labs.aura.music.util.Commander;
import com.sun.labs.aura.music.web.Utilities;
import com.sun.labs.aura.music.web.amazon.Amazon;
import com.sun.labs.aura.music.web.flickr.FlickrManager;
import com.sun.labs.aura.music.web.lastfm.LastFM;
import com.sun.labs.aura.music.web.lastfm.LastFM2;
import com.sun.labs.aura.music.web.lastfm.LastArtist;
import com.sun.labs.aura.music.web.lastfm.LastItem;
import com.sun.labs.aura.music.web.lastfm.SocialTag;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainz;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainzAlbumInfo;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainzArtistInfo;
import com.sun.labs.aura.music.web.spotify.Spotify;
import com.sun.labs.aura.music.web.upcoming.UpcomingEvent;
import com.sun.labs.aura.music.web.upcoming.Upcoming;
import com.sun.labs.aura.music.web.wikipedia.WikiInfo;
import com.sun.labs.aura.music.web.wikipedia.Wikipedia;
import com.sun.labs.aura.music.web.youtube.Youtube2;
import com.sun.labs.aura.util.AuraException;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
                        artistUpdater();
                    }
                };
                threadPool.submit(updater);
            }

            {
                Runnable updater = new Runnable() {

                    // this thread keeps all of our artists
                    // fresh and updated
                    public void run() {
                        newArtistUpdater();
                    }
                };
                threadPool.submit(updater);
            }
        }
    }

    /**
     * Stops the crawler
     */
    public void stop() {
        running = false;
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
            util = new Util(flickr, youtube);
            tagFilter = new TagFilter();
            createStateFileDirectory();
            loadState();
            threadPool = Executors.newCachedThreadPool();
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
            throw new AuraException("bad item type, should be ARTIST");
        }
        Runnable updater = new Runnable() {

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

    private DataStore getDataStore() throws AuraException {
        return (DataStore) rcmStore.getComponent();
    }

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
     * Given a collection of tag names, ensure that they are all added to
     * the database
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
            primeArtistQueue("The Beatles");
        } catch (AuraException ae) {
            logger.severe("ArtistCrawler Can't talk to the datastore, abandoning crawl");
            return;
        }

        while (running && crawlQueue.size() > 0) {
            try {

                // if we've reached maxartists we are done
                if (getDataStore().getItemCount(ItemType.ARTIST) >= maxArtists) {
                    logger.info("Artist discovery crawler reached max artists, shutting down");
                    break;
                }

                QueuedItem queuedArtist = (QueuedItem) crawlQueue.poll();
                long curTime = System.currentTimeMillis();
                logger.info("Crawling " + queuedArtist + " remaining " + crawlQueue.size() + " time " + (curTime - lastTime) + " ms");
                lastTime = curTime;

                Artist artist = collectArtistInfo(queuedArtist);
                if (artist != null) {
                    incrementModCounter();
                }
            } catch (AuraException ex) {
                logger.warning("Aura Trouble during crawl " + ex);
            } catch (RemoteException ex) {
                logger.warning("Remote Trouble during crawl " + ex);
            } catch (IOException ex) {
                logger.warning("IO Trouble during crawl " + ex);
            } catch (Throwable t) {
                logger.severe("Unexpected error during artist discovery crawl " + t);
            }
        }
    }

    private void updateArtists(boolean force, long period) throws AuraException, RemoteException, InterruptedException {
        List<Scored<String>> scoredArtists = getAllArtistsSortedByLastCrawl();
        for (Scored<String> sartist : scoredArtists) {
            Artist artist = new Artist(getDataStore().getItem(sartist.getItem()));
            if (force || needsUpdate(artist)) {
                logger.info("  Updating artist " + artist.getName());
                updateArtistWithErrorRecovery(artist, false);
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                logger.fine(artist.getName() + " is up to date");
            }
        }
    }

    private List<Scored<String>> getAllArtistsWithPopularity() throws AuraException, RemoteException {
        List<Scored<String>> artistList = new ArrayList();

        DBIterator iter = getDataStore().getAllIterator(ItemType.ARTIST);

        try {
            while (iter.hasNext()) {
                Item item = (Item) iter.next();
                Artist artist = new Artist(item);
                artistList.add(new Scored<String>(artist.getKey(), artist.getPopularity()));
            }

        } finally {
            iter.close();
        }
        Collections.sort(artistList, ScoredComparator.COMPARATOR);
        Collections.reverse(artistList);
        return artistList;
    }

    private List<Scored<String>> getAllArtistsSortedByLastCrawl() throws AuraException, RemoteException {
        List<Scored<String>> artistList = new ArrayList();

        DBIterator iter = getDataStore().getAllIterator(ItemType.ARTIST);

        try {
            while (iter.hasNext()) {
                Item item = (Item) iter.next();
                Artist artist = new Artist(item);
                artistList.add(new Scored<String>(artist.getKey(), artist.getLastCrawl()));
            }

        } finally {
            iter.close();
        }
        Collections.sort(artistList, ScoredComparator.COMPARATOR);
        return artistList;
    }

    private void artistUpdater() {
        FixedPeriod fixedPeriod = new FixedPeriod(updateRateInSeconds * 1000L);
        try {
            while (running) {
                fixedPeriod.start();
                try {
                    updateArtists(false, 2000L);
                } catch (AuraException ex) {
                    logger.warning("trouble in artist updater, retrying in a while" + ex);
                } catch (RemoteException ex) {
                    logger.warning("trouble in artist updater, retrying in a while " + ex);
                } catch (InterruptedException ex) {
                    logger.info("artist updater, interrupted, retrying in a while");
                } catch (Throwable t) {
                    logger.severe("Unexpected error during artist updater crawl, retrying in a while" + t);
                }
                fixedPeriod.end();
            }
        } catch (InterruptedException ex) {
            logger.info("artist updater interrupted, shutting down.");
        }
    }

    private void newArtistUpdater() {
        long lastCrawl = 0;
        FixedPeriod fp = new FixedPeriod(newCrawlPeriod * 1000L);
        while (running) {

            try {
                fp.start();
                lastCrawl = crawlNewArtists(lastCrawl);
                fp.end();
            } catch (InterruptedException ex) {
            } catch (AuraException ex) {
                logger.warning("AuraException while crawling users" + ex);
            } catch (RemoteException ex) {
                logger.warning("Remote exception while crawling users" + ex);
            }
        }
    }

    private long crawlNewArtists(long lastCrawl) throws AuraException, RemoteException {
        long maxCrawl = lastCrawl;
        List<Artist> artists = getNewArtistsAddedSince(lastCrawl + 1);
        logger.fine("New artists check found " + artists.size() + " new artists");
        for (Artist artist : artists) {
            if (artist.getItem().getTimeAdded() > maxCrawl) {
                maxCrawl = artist.getItem().getTimeAdded();
            }
            logger.info("  Crawling new artist " + artist.getName());
            updateArtist(artist, false);
        }
        return maxCrawl;
    }

    private void debug(long when) throws AuraException, RemoteException {
        logger.info("*** DEBUG found " + getNewArtistsAddedSince(when).size() + " new artists since " + new Date(when));
    }

    private List<Artist> getNewArtistsAddedSince(long lastCrawl) throws AuraException, RemoteException {
        List<Artist> artistList = new ArrayList();

        DBIterator iter = getDataStore().getItemsAddedSince(ItemType.ARTIST, new Date(lastCrawl));
        try {
            while (iter.hasNext()) {
                Item item = (Item) iter.next();
                Artist artist = new Artist(item);
                if (artist.getUpdateCount() == 0) {
                    artistList.add(artist);
                }
            }
        } finally {
            iter.close();
        }
        logger.fine("Num artists created since " + new Date(lastCrawl) + " is " + artistList.size());
        return artistList;
    }

    private boolean needsUpdate(Artist artist) {
        return (artist.getUpdateCount() == 0L) ||
                ((System.currentTimeMillis() - artist.getLastCrawl()) > updateRateInSeconds * 1000L);
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
                final Artist artist = new Artist(item);
                artist.setPopularity(queuedArtist.getPopularity());
                updateArtist(artist, true);
                return artist;
            }
        }
        return null;
    }

    private void updateArtistWithErrorRecovery(Artist artist, boolean discoverMode) {
        int maxRetries = 5;
        boolean done = false;

        while (!done && maxRetries-- > 0) {
            try {
                updateArtist(artist, discoverMode);
                done = true;
            } catch (AuraException ex) {
                logger.warning("AuraExeption while crawling " + artist.getName() + " retrying. " + ex.getMessage());
            } catch (RemoteException ex) {
                logger.warning("RemoteException while crawling " + artist.getName() + " retrying. " + ex.getMessage());
            } catch (Throwable t) {
                logger.warning("Unexpected exception  while crawling " + artist.getName() + " retrying. " + t.getMessage());
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
        runner.add(new Commander("last.fm") {

            @Override
            public void go() throws Exception {
                addLastFmTags(artist);
                updateLastFMPopularity(artist);
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
     * @param popularity the popularity of the artist
     */
    private synchronized boolean enqueue(LastArtist artist, int popularity) throws RemoteException {
        QueuedItem qA = new QueuedItem(artist, popularity);
        if (!crawlQueue.contains(qA)) {
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
        Map<String, Integer> blurbMap = new HashMap();
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
                album = new Album(albumItem);
                album.setAsin(mbalbum.getAsin());
                album.flush(getDataStore());
            } else {
                album = new Album(albumItem);
            }
            artist.addAlbum(mbrid);
        }

        for (String id : mbai.getCollaborators()) {
            artist.addRelatedArtist(id);
        }
    }

    private void crawlAmazonForBlurbs(Artist artist) throws AuraException, RemoteException {
        Map<String, Integer> blurbMap = new HashMap();
        int maxAlbums = artist.getAlbums().size();
        int pages = getNumBlurbPages(artist);

        if (pages > 0) {

            for (String albumID : artist.getAlbums()) {
                Item albumItem = getDataStore().getItem(albumID);
                if (albumItem != null) {
                    Album album = new Album(getDataStore().getItem(albumID));
                    crawlAlbumBlurbs(artist, album, blurbMap, pages);
                }
            }

            int curSize = artist.getBlurbTags().size();
            // if we found more tags than before, replace the
            // old ones with the new ones.
            if (blurbMap.size() > curSize) {
                for (Map.Entry<String, Integer> entry : blurbMap.entrySet()) {
                    if (entry.getValue() >= minBlurbCount) {
                        artist.setBlurbTag(entry.getKey(), entry.getValue());
                    }
                }
            }

            // some debugging code
            if (logger.isLoggable(Level.INFO)) {
                List<Tag> tags = artist.getBlurbTags();
                logger.info("====== Blurbs for " + artist.getName() + " === albums: " + maxAlbums);
                for (Tag tag : tags) {
                    logger.info(String.format("(%s,%d)", tag.getName(), tag.getCount()));
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

        int updateCount = artist.getUpdateCount();

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

    // for a particular album of an artist, got to amazon and collect a set
    // of reviews (there are about 5 reviews per page) and scrape the words for
    // blurbtags
    private void crawlAlbumBlurbs(Artist artist, Album album, Map<String, Integer> map, int maxPages) {
        try {
            if (album.getAsin() != null && album.getAsin().length() > 0) {
                List<String> reviews = amazon.lookupReviews(album.getAsin(), maxPages);
                for (String review : reviews) {
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
            }
        } catch (IOException ioe) {
            logger.warning("Trouble collecting reviews from " + album.getTitle() + " for " + artist.getName());
        }
    }

    private void addSpotifyInfo(Artist artist) throws IOException {
        String id = spotify.getSpotifyIDforArtist(artist.getName());
        artist.setSpotifyID(id);
    }

    private void addEchoNestInfo(Artist artist) throws IOException, EchoNestException {

        // Get echonest id if we don't already have it
        if (artist.getEchoNestId() == null) {
            for (com.echonest.api.v3.artist.Artist tA : echoNest.searchArtist(artist.getName(), false)) {
                if (echoNest.getUrls(tA).get("mb_url").contains(artist.getKey())) {
                    artist.setEchoNestId(tA.getId());
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
            logger.info("No echonest audio found for " + artist.getName());
        } else {
            logger.fine("crawled " + urls.getTotal() + " audio files from echonest for " + artist.getName());
        }

        // Get familiarity and hotttnesss
        artist.setFamiliarity(echoNest.getFamiliarity(artist.getEchoNestId()));
        artist.setHotttnesss(echoNest.getHotness(artist.getEchoNestId()));
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
                artist.setBioTag(tagFilter.mapTagName(tag), count);
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

    private void addLastFmTags(Artist artist) throws AuraException, RemoteException, IOException {
        SocialTag[] tags = getLastFM2().getArtistTags(artist.getName());
        for (SocialTag tag : tags) {

            int normFreq = (tag.getFreq() + 1) * (tag.getFreq() + 1);

            // Add raw tag
            artist.setSocialTagRaw(tag.getName(), normFreq);

            // Add social tag
            String tagName = tagFilter.mapTagName(tag.getName());
            if (tagName != null) {
                artist.setSocialTag(tagName, normFreq);
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
}

