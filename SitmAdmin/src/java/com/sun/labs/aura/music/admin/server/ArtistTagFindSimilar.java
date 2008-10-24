/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.music.ArtistTag;
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
public class ArtistTagFindSimilar extends Worker {

    ArtistTagFindSimilar() {
        super("ArtistTag Find Similar", "Finds similar artist tags to a seed artist tag");
        param("ArtistTag", "The artist tag", "");
        param("count", "The number of similar artist tags to return", 20);
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        String artistTagName = getParam(params, "ArtistTag");
        int count = getParamAsInt(params, "count");
        ArtistTag artistTag = lookupArtistTag(mdb, artistTagName);
        List<Scored<ArtistTag>> sartistTags = mdb.artistTagFindSimilar(artistTag.getKey(), count);
        dumpArtistTags(mdb, result, sartistTags);
    }

}

