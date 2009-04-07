/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class HubFinder extends Worker {

    HubFinder() {
        super("Hub Finder", "Searches for similarity hubs");
        param("Nodes", "the number of nodes to inspect", "1000");
        param("Similarity Depth", "how many similar artists to use ", "10");
    }

    @Override
    protected void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {

        int nodes = getParamAsInt(params, "Nodes");
        int depth = getParamAsInt(params, "Similarity Depth");

        Map<String, Scored<Artist>> hubs = new HashMap<String, Scored<Artist>>();
        for (int i = 0; i < nodes; i++) {
            Artist artist = selectRandomArtist(mdb);
            List<Scored<Artist>> simArtists = mdb.artistFindSimilar(artist.getKey(), depth);
            for (Scored<Artist> ssartist : simArtists) {
                Artist sartist = ssartist.getItem();
                Scored<Artist> hub = hubs.get(sartist.getKey());
                if (hub == null) {
                    hub = new Scored<Artist>(sartist, 1);
                    hubs.put(sartist.getKey(), hub);
                } else {
                    hub = new Scored<Artist>(sartist, hub.getScore() + 1);
                    hubs.put(sartist.getKey(), hub);
                }
            }
        }
        List<Scored<Artist>> artists = new ArrayList<Scored<Artist>>(hubs.values());
        Collections.sort(artists, ScoredComparator.COMPARATOR);
        Collections.reverse(artists);
        dump(mdb, result, artists);
    }

}
