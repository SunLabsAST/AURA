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
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class ArtistFindSimilarWorker extends Worker {

    ArtistFindSimilarWorker() {
        super("Artist Find Similar", "Finds similar artists to a seed artist");
        param("Artist name", "The name of the artist", "");
        param("count", "The number of similar artists to return", 20);
        param("Popularity",  "The desired popularity of the results", MusicDatabase.Popularity.values(), MusicDatabase.Popularity.ALL);
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        String artistName = getParam(params, "Artist name");
        int count = getParamAsInt(params, "count");
        MusicDatabase.Popularity pop = (MusicDatabase.Popularity) getParamAsEnum(params, "Popularity");
        Artist artist = lookupByNameOrKey(mdb, artistName);
        List<Scored<Artist>> sartists = mdb.artistFindSimilar(artist.getKey(), count, pop);
        dump(mdb, result, sartists);
    }

}
