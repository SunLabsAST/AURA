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
import com.sun.labs.util.props.ConfigStringList;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.List;
import java.util.logging.Level;

/**
 *
 */
public class ArtistFindSimilar extends ServiceAdapter {

    @ConfigStringList(defaultList={"weezer", "the tragically hip", "cake", "uncle tupelo", "wilco"})
    public static final String PROP_ARTISTS = "artists";

    private List<String> artists;

    ConfigurationManager cm;
    
    @Override
    public String serviceName() {
        return getClass().getName();
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        cm = ps.getConfigurationManager();
        artists = ps.getStringList(PROP_ARTISTS);
    }

    @Override
    public void start() {

        try {
            MusicDatabase mdb = new MusicDatabase(cm);
            for(int i = 0; i < 5; i++) {
                for(String an : artists) {
                    Artist artist = mdb.artistFindBestMatch(an);
                    logger.info("artist: " + artist.getName() + " " + artist.
                            getKey());
                    List<Scored<Artist>> sartists =
                            mdb.artistFindSimilar(artist.getKey(), 10,
                            MusicDatabase.Popularity.ALL);
                    for(Scored<Artist> sa : sartists) {
                        logger.info(String.format("%5.3f %s %s", sa.getScore(), sa.getItem().
                                getKey(), sa.getItem().getName()));
                    }
                }
            }
        } catch (AuraException ex) {
            logger.log(Level.SEVERE, "Aura exception", ex);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Throwable?", t);
        }
    }

    @Override
    public void stop() {
    }

}
