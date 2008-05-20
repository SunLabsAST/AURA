/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.crawler;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.music.Album;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.web.lastfm.LastArtist;
import com.sun.labs.aura.music.web.lastfm.LastFM;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainz;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainzAlbumInfo;
import com.sun.labs.aura.music.web.musicbrainz.MusicBrainzArtistInfo;
import com.sun.labs.aura.music.web.wikipedia.WikiInfo;
import com.sun.labs.aura.music.web.wikipedia.Wikipedia;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.StatService;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class ArtistCrawler implements AuraService, Configurable {

    private LastFM lastFM;
    private MusicBrainz musicBrainz;
    private Wikipedia wikipedia;
    //private PersistentStringSet visitedArtists;
    private Set<String> visitedArtists;
    private PriorityQueue<QueuedArtist> artistQueue;

    public void start() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        try {
            lastFM = new LastFM();
            musicBrainz = new MusicBrainz();
            wikipedia = new Wikipedia();
            visitedArtists = new HashSet<String>();
            artistQueue = new PriorityQueue();
        } catch (IOException ioe) {
        }
    }

    public void discoverArtists() {
        primeArtistQueue();
        while (artistQueue.size() > 0) {
            try {
                QueuedArtist queuedArtist = artistQueue.poll();
                System.out.println("Crawling " + queuedArtist + " remaining " + artistQueue.size());
                //Artist artist = collectArtistInfo(queuedArtist);
                //if (artist != null) {
                if (true) {
                    //artist.flush(dataStore);
                    LastArtist[] simArtists = lastFM.getSimilarArtists(queuedArtist.getArtistName());
                    for (LastArtist simArtist : simArtists) {
                        if (!visitedArtists.contains(simArtist.getArtistName())) {
                            visitedArtists.add(simArtist.getArtistName());
                            int popularity = lastFM.getPopularity(simArtist.getArtistName());
                            enqueue(simArtist, popularity);
                        }
                    }
                }
            } catch (IOException ioe) {
                System.out.println("Trouble crawling ");
            }
        }
    }

    Artist collectArtistInfo(QueuedArtist queuedArtist) throws AuraException, RemoteException {
        Artist artist = null;
        String mbaid = queuedArtist.getMBaid();
        if (mbaid != null) {
            Item item = dataStore.getItem(mbaid);
            if (item == null) {
                item = StoreFactory.newItem(ItemType.ARTIST, mbaid, queuedArtist.getArtistName());
                artist = new Artist(item);
                addMusicBrainzInfo(artist);
                addWikipediaInfo(artist);
            }
        }
        return artist;
    }

    private void primeArtistQueue() {
        try {
            LastArtist[] simArtists = lastFM.getSimilarArtists("The Beatles");
            for (LastArtist simArtist : simArtists) {
                if (!visitedArtists.contains(simArtist.getArtistName())) {
                    visitedArtists.add(simArtist.getArtistName());
                    int popularity = lastFM.getPopularity(simArtist.getArtistName());
                    enqueue(simArtist, popularity);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ArtistCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void enqueue(LastArtist artist, int popularity) {
        //System.out.printf("%d %s\n", popularity, artist.getArtistName());
        artistQueue.add(new QueuedArtist(artist, popularity));
    }


    public Artist getArtist(String id) {
        //Item item = dataStore.getItem(id);
        //if (item == null) {
        //}
        return null;
    }

    private void addMusicBrainzInfo(Artist artist) throws AuraException {
        try {
            MusicBrainzArtistInfo mbai = musicBrainz.getArtistInfo(artist.getKey());
            artist.setBeginYear(mbai.getBeginYear());
            artist.setEndYear(mbai.getEndYear());

            for (String name : mbai.getURLMap().keySet()) {
                artist.addUrl(name, mbai.getURLMap().get(name));
            }

            for (MusicBrainzAlbumInfo mbalbum : mbai.getAlbums()) {
                String mbrid = mbalbum.getId();
                Item albumItem = dataStore.getItem(mbrid);
                if (albumItem == null) {
                    albumItem = StoreFactory.newItem(ItemType.ALBUM, mbrid, mbalbum.getTitle());
                    Album album = new Album(albumItem);
                    album.setAsin(mbalbum.getAsin());
                    album.flush(dataStore);
                }
                artist.addAlbum(mbrid);
            }

            for (String id : mbai.getCollaborators()) {
                artist.addRelatedArtist(id);
            }

        } catch (IOException ioe) {
            System.out.println("Can't get artist info from musicbrainz for " + artist.getName());
        }
    }

    private void addWikipediaInfo(Artist artist) {
        String query = (String) artist.getUrls().get("Wikipedia");
        if (query == null) {
            query = artist.getName();
        }

        try {
            WikiInfo wikiInfo = wikipedia.getWikiInfo(query);
            artist.setBioSummary(wikiInfo.getSummary());
        } catch (IOException ioe) {
            System.out.println("Can't get artist info from wikipedia for " + artist.getName());
        }
    }

    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    private DataStore dataStore;
    /**
     * The statistics service that we'll use to count things.
     */
    @ConfigComponent(type = com.sun.labs.aura.util.StatService.class)
    public static final String PROP_STAT_SERVICE = "statService";
    private StatService statService;
    public static final String COUNTER_ENTRY_PULL_COUNT = "fm.entryPullCount";
    public static final String COUNTER_FEED_ERROR_COUNT = "fm.feedErrorCount";
    public static final String COUNTER_FEED_PULL_COUNT = "fm.feedPullCount";
    /**
     * the configurable property for the default carwling period (in seconds)
     */
    @ConfigInteger(defaultValue = 3600, range = {10, 36000})
    public final static String PROP_CRAWLING_PERIOD = "crawlingPeriod";
    private int defaultCrawlingPeriod;
    /**
     * the configurable property for the number of feed discovery threads
     */
    @ConfigInteger(defaultValue = 3, range = {0, 1000})
    public final static String PROP_NUM_DISCOVERY_THREADS = "numDiscoveryThreads";
    private int numDiscoveryThreads;

    public static void main(String[] args) {
        ArtistCrawler ac = new ArtistCrawler();
        ac.newProperties(null);
        ac.discoverArtists();
    }
}

class QueuedArtist implements Comparable<QueuedArtist> {

    private LastArtist lastArtist;
    private int popularity;

    public QueuedArtist(LastArtist artist, int popularity) {
        lastArtist = artist;
        this.popularity = popularity;
    }

    public String getArtistName() {
        return lastArtist.getArtistName();
    }

    public String getMBaid() {
        return lastArtist.getMbaid();
    }

    public int getPriority() {
        return -popularity;
    }

    public int getPopularity() {
        return popularity;
    }

    public int compareTo(QueuedArtist o) {
        return getPriority() - o.getPriority();
    }

    public String toString() {
        return getPopularity() + "/" + getArtistName();
    }
}
