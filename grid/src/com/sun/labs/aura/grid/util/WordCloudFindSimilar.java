/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.util;

import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.util.props.ConfigStringList;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.List;
import java.util.logging.Level;

/**
 *
 */
public class WordCloudFindSimilar extends ServiceAdapter {

    @ConfigStringList(defaultList={"rock", "pop", "indie"})
    public static final String PROP_CLOUD_TAGS = "cloudTags";

    private List<String> cloudTags;

    @ConfigStringList(defaultList={"canada"})
    public static final String PROP_STICKY_TAGS = "stickyTags";

    private List<String> stickyTags;

    @ConfigStringList(defaultList={"heavy metal"})
    public static final String PROP_NEGATIVE_TAGS = "negativeTags";

    private List<String> negativeTags;

    @Override
    public String serviceName() {
        return getClass().getName();
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        cm = ps.getConfigurationManager();
        cloudTags = ps.getStringList(PROP_CLOUD_TAGS);
        stickyTags = ps.getStringList(PROP_STICKY_TAGS);
        negativeTags = ps.getStringList(PROP_NEGATIVE_TAGS);
    }

    @Override
    public void start() {

        try {
            MusicDatabase mdb = new MusicDatabase(cm);
            WordCloud cloud = new WordCloud();
            for(String s : cloudTags) {
                cloud.add(s, 0.5);
            }
            for(String s : stickyTags) {
                cloud.addStickyWord(s);
            }
            for(String s : negativeTags) {
                cloud.addBannedWord(s);
            }
            List<Scored<Artist>> sartists =
                    mdb.wordCloudFindSimilarArtists(cloud, 20);
            for(Scored<Artist> sa : sartists) {
                logger.info(String.format("%.3f %s %s",
                        sa.getScore(),
                        sa.getItem().getKey(),
                        sa.getItem().getName()));
            }
        } catch(AuraException ex) {
            logger.log(Level.SEVERE, "Aura exception", ex);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Throwable?", t);
        }
    }

    @Override
    public void stop() {
    }
}
