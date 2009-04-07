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

package com.sun.labs.aura.grid.sitm;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.grid.ServiceAdapter;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SkimTags extends ServiceAdapter {

    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";
    MusicDatabase mdb;

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
    
    Map<String,List<Scored<Integer>>> m;

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        try {
            super.newProperties(ps);
            mdb = new MusicDatabase(ps.getConfigurationManager());
            tagName = ps.getString(PROP_TAG_NAME);
            runs = ps.getInt(PROP_RUNS);
            m = new LinkedHashMap<String, List<Scored<Integer>>>();
        } catch (AuraException ex) {
            Logger.getLogger(SkimTags.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private double findSimilarTags(MusicDatabase mdb, double skim) throws AuraException {
        NanoWatch nw = new NanoWatch();
        nw.start();
        ArtistTag artistTag = mdb.artistTagFindBestMatch(tagName);
        if(artistTag == null) {
            logger.info("Can't find tag: " + tagName);
            return 0;
        }
        List<Scored<ArtistTag>> scoredArtistTags = mdb.artistTagFindSimilar(artistTag.getKey(), 100);
        int rank = 1;
        for(Scored<ArtistTag> artist: scoredArtistTags) {
            List<Scored<Integer>> l = m.get(artist.getItem().getKey());
            if(l == null) {
                l = new ArrayList();
                m.put(artist.getItem().getKey(), l);
            }
            l.add(new Scored<Integer>(rank++, skim));
        }
        nw.stop();
        return nw.getTimeMillis();
    }
    
    public void start() {
        try {
            double skim = 1;
            while(skim >= 0.20) {
                mdb.setSkimPercent(skim);
                findSimilarTags(mdb, skim);
                skim -= 0.05;
            }
            int rank = 1;
            for(Map.Entry<String,List<Scored<Integer>>> e : m.entrySet()) {
                logger.info(rank++ + " " + e.getKey() + " " + e.getValue());
            }
        } catch(AuraException ex) {
            logger.log(Level.SEVERE, "Error finding similar artists", ex);
        }
    }

    public void stop() {
    }

    private void displayTags(List<Scored<ArtistTag>> scoredTags) {
        int i = 1;
        for(Scored<ArtistTag> tag : scoredTags) {
            System.out.println(String.format("%4d %s %s", i++, 
                    tag.getItem().getKey(),
                    tag.getItem().getName()));
        }
    }

}
