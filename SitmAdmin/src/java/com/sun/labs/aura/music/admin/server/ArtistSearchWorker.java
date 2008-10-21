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
public class ArtistSearchWorker extends Worker {

    ArtistSearchWorker() {
        super("Artist Search", "Search for an artist by name");
        param("Artist Name", "the name of the artist", "");
        param("Num Results", "the maxiumum number of results to return", 25);
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        String query = getParam(params, "Artist Name");
        int count = getParamAsInt(params, "Num Results");
        
        List<Scored<Artist>> sartists = mdb.artistSearch(query, count);
        dump(mdb, result, sartists);
    }
}
