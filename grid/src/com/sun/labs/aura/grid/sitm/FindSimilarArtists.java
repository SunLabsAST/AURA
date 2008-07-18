/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.sitm;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.List;
import java.util.logging.Level;

/**
 *
 */
public class FindSimilarArtists extends ServiceAdapter {

    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";

    private DataStore ds;
    
    @ConfigString(defaultValue="weezer")
    public static final String PROP_ARTIST_NAME = "artistName";
    private String artistName;
    
    @ConfigInteger(defaultValue=2)
    public static final String PROP_RUNS = "runs";
    private int runs;

    @Override
    public String serviceName() {
        return "FindSimilarTest";
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        logger.info("instance: " + instance);
        ds = (DataStore) ps.getComponent(PROP_DATA_STORE);
        artistName = ps.getString(PROP_ARTIST_NAME);
        runs = ps.getInt(PROP_RUNS);
    }
    
    private void runFindSimilars(MusicDatabase mdb, Artist artist) throws AuraException {
        logger.info("Parallel Find Similar");
        NanoWatch nw = new NanoWatch();
        nw.start();
        List<Scored<Artist>> scoredArtists = mdb.artistFindSimilar(
                artist.getKey(), 10);
        nw.stop();
        displayArtists(scoredArtists);
//        logger.info(String.format("Parallel findSimilar took %.3f", nw.
//                getTimeMillis()));
//        logger.info("Serial Find Similar");
//        nw.reset();
//        nw.start();
//        scoredArtists = mdb.artistSerialFindSimilar(artist.getKey(),
//                Artist.FIELD_SOCIAL_TAGS, 10);
//        nw.stop();
//        displayArtists(scoredArtists);
//        logger.info(String.format("Serial findSimilar took %.3f", nw.
//                getTimeMillis()));
//        logger.info("Find Similar, Return keys");
//        nw.reset();
//        nw.start();
//        scoredArtists = mdb.artistFindSimilarKey(artist.getKey(),
//                Artist.FIELD_SOCIAL_TAGS, 10);
//        nw.stop();
//        displayArtists(scoredArtists);
//        logger.info(String.format("Key return findSimilar took %.3f", nw.
//                getTimeMillis()));
    }
    
    public void start() {
        try {
            MusicDatabase mdb = new MusicDatabase(ds);
            Artist artist = mdb.artistFindBestMatch(artistName);
            
            for(int i = 0; i < runs; i++) {
                logger.info("Run: " + (i+1));
                runFindSimilars(mdb, artist);
            }
        } catch(AuraException ex) {
            logger.log(Level.SEVERE, "Error finding similar artists", ex);
        }
    }

    public void stop() {
    }

    private void displayArtists(List<Scored<Artist>> scoredArtists) {
        for(Scored<Artist> artist : scoredArtists) {
            logger.info(String.format("%s %s %.3f", artist.getItem().getKey(),
                    artist.getItem().getName(), artist.getScore()));
        }
    }

}
