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
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
    private PriorityQueue<QueuedArtist> artistQueue;
    private Set<String> visitedArtists;
    private Logger logger;
    private final static String CRAWLER_STATE_FILE = "crawler.state";
    private final static int FLUSH_COUNT = 10;
    private boolean running = false;

    /**
     * Starts running the crawler
     */
    public void start() {
        if (!running) {
            running = true;
            Thread t = new Thread() {
                @Override
                public void run() {
                    discoverArtists();
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
        try {
            logger = ps.getLogger();
            lastFM = new LastFM();
            musicBrainz = new MusicBrainz();
            wikipedia = new Wikipedia();
            artistQueue = new PriorityQueue();
            visitedArtists = new HashSet();
            dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
            stateDir = ps.getString(PROP_STATE_DIR);
            createStateFileDirectory();
            loadState();
        } catch (IOException ioe) {
            throw new PropertyException(ioe, "ArtistCrawler", ps.getInstanceName(), "");
        }
    }

    /**
     * Starts discovering artists. When a new artist is encountered it is 
     * added to the datastore
     */
    private void discoverArtists() {
        int count = 0;
        primeArtistQueue();
        while (running && artistQueue.size() > 0) {
            try {
                QueuedArtist queuedArtist = artistQueue.poll();
                logger.info("Crawling " + queuedArtist + " remaining " + artistQueue.size());
                Artist artist = collectArtistInfo(queuedArtist);
                if (artist != null) {
                    artist.flush(dataStore);
                    LastArtist[] simArtists = lastFM.getSimilarArtists(queuedArtist.getArtistName());
                    for (LastArtist simArtist : simArtists) {
                        if (!visitedArtists.contains(simArtist.getArtistName())) {
                            visitedArtists.add(simArtist.getArtistName());
                            int popularity = lastFM.getPopularity(simArtist.getArtistName());
                            enqueue(simArtist, popularity);
                        }
                    }
                    if (count++ % FLUSH_COUNT == 0) {
                        saveState();
                    }
                }
            } catch (AuraException ex) {
                logger.warning("Aura Trouble during crawl " + ex);
            } catch (RemoteException ex) {
                logger.warning("Remote Trouble during crawl " + ex);
            } catch (IOException ex) {
                logger.warning("IO Trouble during crawl " + ex);
            }
        }
    }

    /**
     * Collects the information for an artist
     * @param queuedArtist the artist of interest
     * @return a fully populated artist
     * @throws com.sun.labs.aura.util.AuraException if a problem with the datastore is encountered
     * @throws java.rmi.RemoteException if a communicatin error occurs
     */
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

    /**
     * Creates the directory for the state file if necessary
     */
    private void createStateFileDirectory() throws IOException {
        File stateDirFile = new File(stateDir);
        if (!stateDirFile.exists()) {
            if (!stateDirFile.mkdirs()) {
                throw new IOException("Can't create state file directory");
            }
        }
    }

    /**
     * Primes the artist queue for discovery. The queue is primed with artists that
     * are similar to the beatles
     */
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

    /**
     * Adds an artist to the discovery queue
     * @param artist the artist to be queued
     * @param popularity the popularity of the artist
     */
    private void enqueue(LastArtist artist, int popularity) {
        artistQueue.add(new QueuedArtist(artist, popularity));
    }

    /**
     * Loads the previously saveed priority queue
     */
    private void loadState() {


        ObjectInputStream ois = null;
        try {
            File stateFile = new File(stateDir, CRAWLER_STATE_FILE);
            FileInputStream fis = new FileInputStream(stateFile);
            ois = new ObjectInputStream(fis);
            ArtistCrawlerState newState = (ArtistCrawlerState) ois.readObject();

            if (newState != null) {
                visitedArtists.addAll(newState.getVisitedArtists());
                artistQueue.clear();
                artistQueue.addAll(newState.getArtistQueue());
                logger.info("restored discovery queue with " + artistQueue.size() + " entries");
            }
        } catch (IOException ex) {
            // no worries if there was no file
        } catch (ClassNotFoundException ex) {
            logger.warning("Bad format in feedscheduler statefile " + ex);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Saves the priority queue to a file.
     */
    private void saveState() {
        FileOutputStream fos = null;
        try {
            ArtistCrawlerState acs = new ArtistCrawlerState(visitedArtists, new ArrayList(artistQueue));
            File stateFile = new File(stateDir, CRAWLER_STATE_FILE);
            fos = new FileOutputStream(stateFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(acs);
            oos.close();
        } catch (IOException ex) {
            logger.warning("Can't save the state of the crawler " + ex);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
            }
        }
    }


    /**
     * Adds musicbrainz info to the aritst
     * @param artist the artist to be augmented
     * @throws com.sun.labs.aura.util.AuraException if a datastore error occurs
     */
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

    /**
     * Adds wikipedia info to the artist
     * @param artist the artist of interest
     */
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

    /** the directory for the crawler.state */
    @ConfigString(defaultValue = "artistCrawler")
    public final static String PROP_STATE_DIR = "crawlerStateDir";
    private String stateDir;
}


/**
 * Represents an artist in the queue. The queue is sorted by inverse popularity
 * (highly popular artists move to the head of the queue).
 */
class QueuedArtist implements Serializable {
    public final static Comparator<QueuedArtist> PRIORITY_ORDER = new Comparator<QueuedArtist>() {

        public int compare(QueuedArtist o1, QueuedArtist o2) {
            return o1.getPriority() - o2.getPriority();
        }
    };

    private LastArtist lastArtist;
    private int popularity;

    public QueuedArtist() {
    }

    /**
     * Creates a QueuedArtist
     * @param artist the artist of interest
     * @param popularity the artist popularity
     */
    public QueuedArtist(LastArtist artist, int popularity) {
        lastArtist = artist;
        this.popularity = popularity;
    }

    /**
     * Gets the artist name
     * @return the artist name
     */
    public String getArtistName() {
        return lastArtist.getArtistName();
    }

    /**
     * Gets the musicbrainz ID for the artist
     * @return the MB ID for the artist
     */
    public String getMBaid() {
        return lastArtist.getMbaid();
    }

    /**
     * Gets the priority for this queued artist
     * @return the priority
     */
    public int getPriority() {
        return -popularity;
    }

    /**
     * Gets the popularity for this artist
     * @return the popularity (higher is more popular)
     */
    public int getPopularity() {
        return popularity;
    }

    @Override
    public String toString() {
        return getPopularity() + "/" + getArtistName();
    }
}

/**
 * Represents the state of the crawler so it can be easily saved
 * and restored
 */
class ArtistCrawlerState implements Serializable {

    private List<QueuedArtist> artistQueue;
    private Set<String> visitedArtists;

    /**
     * Creates the state
     * @param visited the set of visited artist names
     * @param queue the queue of artists to be visited
     */
    ArtistCrawlerState(Set<String> visited, List<QueuedArtist> queue) {
        visitedArtists = visited;
        artistQueue = queue;
    }


    /**
     * Gets the set of visited artists
     * @return the set of visited artists
     */
    Set<String> getVisitedArtists() {
        return visitedArtists;
    }

    /**
     * Gets the artist queue
     * @return the artist queue
     */
    List<QueuedArtist> getArtistQueue() {
        return artistQueue;
    }
}