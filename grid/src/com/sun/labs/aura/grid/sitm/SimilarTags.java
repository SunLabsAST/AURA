/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.sitm;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
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
public class SimilarTags extends ServiceAdapter {

    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";

    private DataStore ds;
    
    @ConfigString(defaultValue="metal")
    public static final String PROP_TAG_NAME = "tagName";
    private String tagName;
    
    @ConfigInteger(defaultValue=10)
    public static final String PROP_RUNS = "runs";
    private int runs;

    @Override
    public String serviceName() {
        return "SimilarTags";
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        ds = (DataStore) ps.getComponent(PROP_DATA_STORE);
        tagName = ps.getString(PROP_TAG_NAME);
        runs = ps.getInt(PROP_RUNS);
        
        
    }
    
    private double runFindSimilars(MusicDatabase mdb) throws AuraException {
        logger.info("Parallel Find Similar");
        NanoWatch nw = new NanoWatch();
        nw.start();
        ArtistTag artistTag = mdb.artistTagFindBestMatch(tagName);
        if(artistTag == null) {
            logger.info("Can't find tag: " + tagName);
            return 0;
        }
        List<Scored<ArtistTag>> scoredArtistTags = mdb.artistTagFindSimilar(artistTag.getKey(), 100);
        nw.stop();
        displayTags(scoredArtistTags);
        logger.info(String.format("Parallel findSimilar took %.3f", nw.
                getTimeMillis()));
        return nw.getTimeMillis();
    }
    
    public void start() {
        try {
            MusicDatabase mdb = new MusicDatabase(ds);
            double sum = 0;
            for(int i = 0; i < runs; i++) {
                logger.info("Run: " + (i+1));
                sum += runFindSimilars(mdb);
            }
            logger.info(String.format("Average fs time: %.3f", sum / runs));
        } catch(AuraException ex) {
            logger.log(Level.SEVERE, "Error finding similar artists", ex);
        }
    }

    public void stop() {
    }

    private void displayTags(List<Scored<ArtistTag>> scoredTags) {
        for(Scored<ArtistTag> tag : scoredTags) {
            logger.info(String.format("%s %s %.3f", tag.getItem().getKey(),
                    tag.getItem().getName(), tag.getScore()));
        }
    }

}
