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

package com.sun.labs.aura.music.util;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

/**
 * A class to test the speed of similarity options.
 */
public class SimilarityRunner implements Configurable, AuraService {
    
    @ConfigComponent(type=com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";
    

    @ConfigString(defaultValue="weezer")
    public static final String PROP_ARTIST_NAME = "artistName";
    
    private String artistName;
    
    @ConfigString(defaultValue="metal")
    public static final String PROP_TAG_NAME = "tagName";
    private String tagName;
    
    @ConfigInteger(defaultValue=100) 
    public static final String PROP_RUNS = "runs";
    
    private int runs;
    
    private MusicDatabase mdb;
    
    private Logger logger;
    
    private ConfigurationManager cm;
    
    private void displayArtists(List<Scored<Artist>> scoredArtists) {
        for(Scored<Artist> artist : scoredArtists) {
            logger.info(String.format("%s %s %.3f", artist.getItem().getKey(),
                    artist.getItem().getName(), artist.getScore()));
        }
    }

    private double findSimilarArtist() throws AuraException {
        logger.info("FSA");
        NanoWatch nw = new NanoWatch();
        nw.start();
        Artist artist = mdb.artistFindBestMatch(artistName);
        if(artist == null) {
            logger.info("Can't find artist: " + artistName);
            return 0;
        }
        List<Scored<Artist>> scoredArtists = mdb.artistFindSimilar(
                artist.getKey(), 10);
        nw.stop();
        displayArtists(scoredArtists);
        logger.info(String.format("FSA took %.3f", nw.
                getTimeMillis()));
        return nw.getTimeMillis();
    }

    private void displayTags(List<Scored<ArtistTag>> scoredTags) {
        for(Scored<ArtistTag> tag : scoredTags) {
            logger.info(String.format("%s %s %.3f", tag.getItem().getKey(),
                    tag.getItem().getName(), tag.getScore()));
        }
    }
    
    private double findSimilarTags() throws AuraException {
        logger.info("FST");
        NanoWatch nw = new NanoWatch();
        nw.start();
        ArtistTag artistTag = mdb.artistTagFindBestMatch(tagName);
        if(artistTag == null) {
            logger.info("Can't find tag: " + tagName);
            return 0;
        }
        List<Scored<ArtistTag>> scoredArtistTags = mdb.artistTagFindSimilar(
                artistTag.getKey(), 100);
        nw.stop();
        displayTags(scoredArtistTags);
        logger.info(String.format("FSA took %.3f", nw.
                getTimeMillis()));
        return nw.getTimeMillis();
    }
    
    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        cm = ps.getConfigurationManager();
        try {
            mdb = new MusicDatabase(ps.getConfigurationManager());
        } catch(AuraException ex) {
            throw new PropertyException(ex, ps.getInstanceName(),
                    PROP_DATA_STORE, "Error getting music database");
        }
        artistName = ps.getString(PROP_ARTIST_NAME);
        tagName = ps.getString(PROP_TAG_NAME);
        runs = ps.getInt(PROP_RUNS);
    }
    
    public static void main(String[] args) throws IOException, AuraException {
    }

    public void start() {
        try {
            double sum = 0;
            for(int i = 0; i < runs; i++) {
                sum += findSimilarArtist();
            }
            logger.info(String.format("Average FSA: %.3f", sum / runs));
            sum = 0;
            for(int i = 0; i < runs; i++) {
                sum += findSimilarTags();
            }
            logger.info(String.format("Average FST: %.3f", sum / runs));
        } catch (Exception ex) {
            logger.severe("Error: " + ex);
        }
    }

    public void stop() {
        cm.shutdown();
    }

}
