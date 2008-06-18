/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.crawler;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.web.lastfm.LastItem;
import com.sun.labs.aura.music.web.lastfm.LastFM;
import com.sun.labs.aura.music.web.pandora.Pandora;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemSchedulerImpl;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class ListenerCrawler extends ItemSchedulerImpl {
    private LastFM lastfm;
    private Pandora pandora;
    private MusicDatabase mdb;
    private boolean running = false;
    private Logger logger;
    private int numThreads;

    @Override
    public void start() {
        if (!running) {
            running = true;
            for (int i = 0; i < numThreads; i++) {
                Thread t = new Thread() {

                    @Override
                    public void run() {
                        crawlAllListeners();
                    }
                };
                t.start();
            }
        }
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        logger = ps.getLogger();
        numThreads = ps.getInt(PROP_NUM_THREADS);
    }

    private void crawlAllListeners() {
        while (running) {
            String userID = null;
            try {
                userID = getNextItemKey();
                Listener listener = mdb.getListener(userID);
                crawlListener(listener);
            } catch (InterruptedException ex) {
                break;
            } catch (AuraException ex) {
                logger.warning("AuraException while crawling user " + userID + " " + ex);
            } catch (IOException ex) {
                logger.warning("IOException while crawling user " + userID + " " + ex);
            } finally {
                releaseItem(userID, SCHEDULE_DEFAULT);
            }
        }
    }

    private void crawlListener(Listener listener) throws AuraException, RemoteException, IOException {
        logger.info("Crawling " + listener.getName());
        int state = listener.getState();
        if (listener.getLastFmName() != null) {
            if ((state & Listener.STATE_INITIAL_LASTFM_CRAWL) != Listener.STATE_INITIAL_LASTFM_CRAWL) {
                fullCrawlLastFM(listener);
                state |= Listener.STATE_INITIAL_LASTFM_CRAWL;
                listener.setState(state);
                listener.flush(dataStore);
            } else {
                weeklyCrawlLastFM(listener);
            }
        }
        weeklyCrawlPandora(listener);
    }

    private void fullCrawlLastFM(Listener listener) throws AuraException, IOException {
        logger.info("Full crawl for " + listener.getName());
        LastItem[] artists = lastfm.getTopArtistsForUser(listener.getLastFmName());
        for (LastItem artistItem : artists) {
            if (artistItem.getMBID() != null) {
                Artist artist = mdb.artistLookup(artistItem.getMBID());
                if (artist != null) {
                    mdb.addPlayAttention(listener, artist.getKey(), artistItem.getFreq());
                }
            }
        }
    }

    private void weeklyCrawlPandora(Listener listener) throws AuraException, IOException {
        if (listener.getPandoraName() != null) {
            logger.info("Pandora crawl for " + listener.getName());
            Set<String> favs = mdb.getFavoriteArtists(listener, 100000);
            List<String> artists = pandora.getFavoriteArtistNamesForUser(listener.getPandoraName());
            for (String artistName : artists) {
                Artist artist = mdb.artistFindBestMatch(artistName);
                if (artist != null && !favs.contains(artist.getKey())) {
                    mdb.addFavoriteAttention(listener, artist.getKey());
                }
            }
        }
    }

    private void weeklyCrawlLastFM(Listener listener) throws AuraException, IOException {
        logger.info("Weekly crawl for " + listener.getName());
        LastItem[] artists = lastfm.getWeeklyArtistsForUser(listener.getLastFmName());
        for (LastItem artistItem : artists) {
            if (artistItem.getMBID() != null) {
                Artist artist = mdb.artistLookup(artistItem.getMBID());
                if (artist != null) {
                    mdb.addPlayAttention(listener, artist.getKey(), artistItem.getFreq());
                }
            }

        }
    }
    /**
     * the configurable property for the number of threads used by this manager
     */
    @ConfigInteger(defaultValue = 1, range = {0, 1000})
    public final static String PROP_NUM_THREADS = "numThreads";
}
