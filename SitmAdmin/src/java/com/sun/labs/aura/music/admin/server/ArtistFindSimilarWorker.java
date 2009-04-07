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
