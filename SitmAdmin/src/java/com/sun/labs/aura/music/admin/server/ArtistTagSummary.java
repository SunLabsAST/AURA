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

import com.sun.labs.aura.music.ArtistTag;
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
public class ArtistTagSummary extends Worker {

    private final static long millisPerDay = 1000L * 60 * 60 * 24;

    ArtistTagSummary() {
        super("Artist Tag Summary", "Summarizes data from artist tags");
        param("count", "The number of artist tags", 100);
    }

    @Override
    void go( MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        int count = getParamAsInt(params, "count");

        int row = 0;
        List<ArtistTag> artistTags = mdb.artistTagGetMostPopular(count);
        for (ArtistTag artistTag : artistTags) {
            float pop = mdb.artistTagGetNormalizedPopularity(artistTag);
            int ageInDays = (int) ((System.currentTimeMillis() - artistTag.getLastCrawl()) / millisPerDay);

            if (row++ % 20 == 0) {
                result.output(String.format("%5s %3s %5s %4s %4s %4s %4s %s",
                        "#", "age", "pop", "bio", "img", "vid", "tArt", "name"));
            }

            result.output(String.format("%5d  %3d %5.3f %4d %4d %4d %4d %s",
                    row,
                    ageInDays,
                    pop,
                    artistTag.getDescription().length(),
                    artistTag.getPhotos().size(),
                    artistTag.getVideos().size(),
                    artistTag.getTaggedArtist().size(),
                    artistTag.getName()));
        }
    }
}
