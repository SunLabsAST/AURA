/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class MissingPhotoWorker extends Worker {

    MissingPhotoWorker() {
        super("Missing Photos", "Searches for artists with missing photos");
        param("Nodes", "the number of nodes to inspect (0 for all)", "1000");
    }

    @Override
    protected void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {

        int nodes = getParamAsInt(params, "Nodes");

        if (nodes == 0) {
            List<String> keys = getArtistIDs(mdb);
            for (String key : keys) {
                Artist artist = mdb.artistLookup(key);
                checkArtist(artist, result);
            }
        } else {
            for (int i = 0; i < nodes; i++) {
                Artist artist = selectRandomArtist(mdb);
                checkArtist(artist, result);
            }
        }
    }

    void checkArtist(Artist artist, WorkbenchResult result) {
        if (artist.getPhotos().size() == 0) {
            result.output(artist.getKey() + " " + artist.getName());
        }

    }
}
