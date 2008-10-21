/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
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
