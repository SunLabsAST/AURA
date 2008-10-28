/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.TestStatus;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author plamere
 */
class ArtistTagSearch extends Test {

    private int numTests;

    ArtistTagSearch(int numTests) {
        super("Artist Tag Search-" + numTests);
        this.numTests = numTests;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {

        for (int i = 0; i < numTests; i++) {
            ArtistTag queryArtistTag = selectRandomArtistTag(mdb);
            List<Scored<ArtistTag>> results = mdb.artistTagSearch(queryArtistTag.getName(), 10);
            for (int j = 0; j < results.size(); j++) {
                if (results.get(j).getScore() > .97f && results.get(j).getItem().getKey().equals(queryArtistTag.getKey())) {
                    return;
                }
            }
            ts.fail("Search Fail for " + queryArtistTag.getName());
        }
    }
}
