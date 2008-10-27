/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.Constants.Bool;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class ArtistSummary extends Worker {

    private final static long millisPerDay = 1000L * 60 * 60 * 24;

    ArtistSummary() {
        super("Artist Summary", "Summarizes data from artists");
        param("count", "The number of artists", 100);
        param("full", "Give a full report", Bool.values(), Bool.FALSE);
    }

    @Override
    void go( MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        int count = getParamAsInt(params, "count");
        boolean full = getParamAsEnum(params, "full") == Bool.TRUE;
        List<Artist> artists = mdb.artistGetMostPopular(count);

        int row = 0;
        for (Artist artist : artists) {
            int ageInDays = (int) ((System.currentTimeMillis() - artist.getLastCrawl()) / millisPerDay);
            float pop = mdb.artistGetNormalizedPopularity(artist);

            if (full) {
                if (row++ % 20 == 0) {
                    result.output(String.format("%5s %3s %5s %4s %4s %4s %4s %4s %4s %4s %4s %4s %4s %4s %4s %4s %s",
                            "#", "age", "pop", "pull", "albm", "audi", "atag", "bio", "btag", "strt", "end", "blrb", "img", "stag", "url", "vid", "name"));
                }

                result.output(String.format("%5d %3d %5.3f %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %s",
                        row,
                        ageInDays,
                        pop,
                        artist.getUpdateCount(),
                        artist.getAlbums().size(),
                        artist.getAudio().size(),
                        artist.getAutoTags().size(),
                        artist.getBioSummary().length(),
                        artist.getBioTags().size(),
                        artist.getBeginYear(),
                        artist.getEndYear(),
                        artist.getBlurbTags().size(),
                        artist.getPhotos().size(),
                        artist.getSocialTags().size(),
                        artist.getUrls().size(),
                        artist.getVideos().size(),
                        artist.getName()));
            } else {
                if (row++ % 20 == 0) {
                    result.output(String.format("%5s %3s %5s %4s %4s %4s %4s %4s %4s %s",
                            "#", "age", "pop", "pull", "albm", "bio",  "img", "stag", "vid", "name"));
                }

                result.output(String.format("%5d %3d %5.3f %4d %4d %4d %4d %4d %4d %s",
                        row,
                        ageInDays,
                        pop,
                        artist.getUpdateCount(),
                        artist.getAlbums().size(),
                        artist.getBioSummary().length(),
                        artist.getPhotos().size(),
                        artist.getSocialTags().size(),
                        artist.getVideos().size(),
                        artist.getName()));
            }
        }
    }
}
