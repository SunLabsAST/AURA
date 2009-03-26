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
import java.util.logging.Logger;

/**
 *
 */
public class FindSimilarArtists extends ServiceAdapter {

    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";

    @ConfigString(defaultValue = "weezer")
    public static final String PROP_ARTIST_NAME = "artistName";

    private String artistName;

    @ConfigInteger(defaultValue = 10)
    public static final String PROP_RUNS = "runs";

    private int runs;

    private MusicDatabase mdb;

    @Override
    public String serviceName() {
        return "FindSimilarTest";
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        try {
            super.newProperties(ps);
            mdb = new MusicDatabase(ps.getConfigurationManager());
            artistName = ps.getString(PROP_ARTIST_NAME);
            runs = ps.getInt(PROP_RUNS);
        } catch(AuraException ex) {
            logger.log(Level.SEVERE, "Can't create musicdatabase", ex);
        }
    }

    private double runFindSimilars(MusicDatabase mdb) {
        logger.info("Parallel Find Similar");
        try {
            NanoWatch nw = new NanoWatch();
            nw.start();
            Artist artist = mdb.artistFindBestMatch(artistName);
            List<Scored<Artist>> scoredArtists = mdb.artistFindSimilar(
                    artist.getKey(), 10);
            nw.stop();
            displayArtists(scoredArtists);
            logger.info(String.format("Parallel findSimilar took %.3f", nw.
                    getTimeMillis()));
            return nw.getTimeMillis();
        } catch(AuraException ax) {
            logger.severe("Aura exception: " + ax);
            return 0;
        }
    }

    public void start() {
        double sum = 0;
        for(int i = 0; i < runs; i++) {
            logger.info("Run: " + (i + 1));
            sum += runFindSimilars(mdb);
        }
        logger.info(String.format("Average fs time: %.3f", sum / runs));
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
