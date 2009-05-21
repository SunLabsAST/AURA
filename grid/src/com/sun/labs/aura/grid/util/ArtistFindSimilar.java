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

package com.sun.labs.aura.grid.util;

import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.props.ConfigInteger;
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

    @ConfigInteger(defaultValue = 500)
    public static final String PROP_NUM_ARTISTS = "numArtists";

    private int numArtists;

    @ConfigStringList(defaultList={})
    public static final String PROP_ARTISTS = "artists";

    private List<String> artists;

    ConfigurationManager cm;

    MusicDatabase mdb;

    NanoWatch nw;

    @Override
    public String serviceName() {
        return getClass().getName();
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        cm = ps.getConfigurationManager();
        try {
            mdb = new MusicDatabase(cm);
        } catch (AuraException ax) {
            ps.getLogger().severe("Error: " + ax);
        }
        numArtists = ps.getInt(PROP_NUM_ARTISTS);
        artists = ps.getStringList(PROP_ARTISTS);
        nw = new NanoWatch();
    }

    private void findSimilar(Artist artist) throws Exception {
        nw.start();
        List<Scored<Artist>> sartists =
                mdb.artistFindSimilar(artist.getKey(), 10,
                MusicDatabase.Popularity.ALL);
        nw.stop();
        logger.info(String.format("artist %s %s %.3fms", artist.getKey(), artist.getName(), nw.getLastTimeMillis()));
        for (Scored<Artist> sa : sartists) {
            logger.info(String.format(" %5.3f %s %s",
                    sa.getScore(),
                    sa.getItem().getKey(),
                    sa.getItem().getName()));
        }
    }

    @Override
    public void start() {

        try {
            if(numArtists > 0) {
                for (Artist artist : mdb.artistGetMostPopular(numArtists)) {
                    findSimilar(artist);
                }
            }

            for(String key : artists) {
                Artist artist = mdb.artistLookup(key);
                findSimilar(artist);
            }
            
            logger.info(String.format("Average fs time over %d calls: %.3fms",
                    nw.getClicks(), nw.getAvgTimeMillis()));
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
