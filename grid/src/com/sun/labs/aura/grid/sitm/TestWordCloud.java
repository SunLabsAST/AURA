/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.sitm;

import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.List;
import java.util.logging.Level;

/**
 *
 */
public class TestWordCloud extends ServiceAdapter {
    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";
    public static final String wcs = "(90s,0.6789348446308936)(alternative,1.0)(american,0.5565439078660953)(college rock,0.4512262004440529)(emo,0.8993133596024468)(geek rock,-0.8338539953253963)(indie,0.9100140550161206)(nerd rock,0.887944857185779)(pop,0.5398786998573312)(punk,0.5398805839077495)(rock,0.9864281539412807)";

    private MusicDatabase mdb;
    
    @Override
    public String serviceName() {
        return "WordCloudTest";
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        try {
            super.newProperties(ps);
            mdb = new MusicDatabase(ps.getConfigurationManager());
        } catch (AuraException ex) {
            ps.getLogger().severe("Can't create musicdatabase");
        }
    }
    
    private double runFindSimilar(MusicDatabase mdb) throws AuraException {
        logger.info("Find Similar Word Cloud");
        NanoWatch nw = new NanoWatch();
        nw.start();
        WordCloud wc = WordCloud.convertStringToWordCloud(wcs);
        logger.info("wc: " + wc);
        List<Scored<Artist>> scoredArtists = mdb.wordCloudFindSimilarArtists(wc, 10);
        nw.stop();
        displayArtists(scoredArtists);
        logger.info(String.format("findSimilar took %.3f", nw.
                getTimeMillis()));
        return nw.getTimeMillis();
    }
    
    public void start() {
        try {
            runFindSimilar(mdb);
        } catch(AuraException ex) {
            logger.log(Level.SEVERE, "Error finding similar artists", ex);
        }
    }

    public void stop() {
    }

    private void displayArtists(List<Scored<Artist>> scoredArtists) {
        for(Scored<Artist> artist : scoredArtists) {
            logger.info(String.format(" %.3f %s %s", artist.getScore(), 
                    artist.getItem().getKey(),
                    artist.getItem().getName()));
        }
    }

}
