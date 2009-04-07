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
