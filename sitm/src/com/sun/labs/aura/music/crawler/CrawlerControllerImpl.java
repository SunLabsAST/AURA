/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.music.crawler;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.music.web.lastfm.LastAlbum2;
import com.sun.labs.aura.music.web.lastfm.LastArtist;
import com.sun.labs.aura.music.web.lastfm.LastArtist2;
import com.sun.labs.aura.music.web.lastfm.LastFM;
import com.sun.labs.aura.music.web.CannotResolveException;
import com.sun.labs.aura.music.web.lastfm.LastFMImpl;
import com.sun.labs.aura.music.web.lastfm.LastFM2;
import com.sun.labs.aura.music.web.lastfm.LastFM2Impl;
import com.sun.labs.aura.music.web.lastfm.LastItem;
import com.sun.labs.aura.music.web.lastfm.LastTrack;
import com.sun.labs.aura.music.web.lastfm.LastUser;
import com.sun.labs.aura.music.web.lastfm.SocialTag;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author mailletf
 */
public class CrawlerControllerImpl implements AuraService, CrawlerController {

    private LastFM lastfm;
    private LastFM2 lastfm2;

    public CrawlerControllerImpl() throws IOException, AuraException {
        lastfm = new LastFMImpl();
        lastfm2 = new LastFM2Impl();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    /**
     * @deprecated
     */
    @Override
    public SocialTag[] getAlbumTags(String artistName, String trackName) throws IOException {
        return lastfm.getAlbumTags(artistName, trackName);
    }

    /**
     * @deprecated
     */
    @Override
    public SocialTag[] getTrackTags(String artistName, String trackName) throws IOException {
        return lastfm.getTrackTags(artistName, trackName);
    }

    /**
     * @deprecated
     */
    @Override
    public LastItem[] getArtistFans(String artistName) throws IOException {
        return lastfm.getArtistFans(artistName);
    }

    /**
     * @deprecated
     */
    @Override
    public LastUser getUser(String userName) throws IOException {
        return lastfm.getUser(userName);
    }

    /**
     * @deprecated
     */
    @Override
    public void setMinimumCommandPeriod(long period) throws IOException {
        lastfm.setMinimumCommandPeriod(period);
        lastfm2.setMinimumCommandPeriod(period);
    }

    /**
     * @deprecated
     */
    @Override
    public int getPopularity(String artistName) throws IOException {
        return lastfm.getPopularity(artistName);
    }

    /**
     * @deprecated
     */
    @Override
    public LastItem[] getTopArtistsForUser(String user) throws IOException {
        return lastfm.getTopArtistsForTag(user);
    }

    /**
     * @deprecated
     */
    @Override
    public LastItem[] getWeeklyArtistsForUser(String user) throws IOException {
        return lastfm.getWeeklyArtistsForUser(user);
    }

    /**
     * @deprecated
     */
    @Override
    public String[] getSimilarUsers(String user) throws IOException {
        return lastfm.getSimilarUsers(user);
    }

    @Override
    public void setTrace(boolean trace) throws IOException {
        lastfm.setTrace(trace);
        lastfm2.setTrace(trace);
    }

    /**
     * @deprecated
     */
    @Override
    public LastUser getFanFromLastFM(String url) throws IOException {
        return lastfm.getFanFromLastFM(url);
    }


    /*************
        Below LastFM 2.0 API methods
    *******/

    /**
     * @deprecated
     */
    @Override
    public SocialTag[] getArtistTags(String artistName, boolean raw) throws IOException {
        return lastfm2.getArtistTags(artistName);
    }

    @Override
    public SocialTag[] getArtistTags(String artistName) throws IOException {
        return lastfm2.getArtistTags(artistName);
    }

    @Override
    public LastArtist[] getSimilarArtists(String artist) throws IOException {
        return lastfm2.getSimilarArtists(artist);
    }

    @Override
    public LastItem[] getTopArtistsForTag(String tag) throws IOException {
        return lastfm2.getTopArtistsForTag(tag);
    }
    
    @Override
    public LastArtist2 getArtistInfoByName(String artistName) throws IOException {
        return lastfm2.getArtistInfoByName(artistName);
    }

    @Override
    public LastArtist2 getArtistInfoByMBID(String mbid) throws IOException {
        return lastfm2.getArtistInfoByMBID(mbid);
    }

    @Override
    public LastAlbum2 getAlbumInfoByName(String artistName, String albumName) throws IOException {
        return lastfm2.getAlbumInfoByName(artistName, albumName);
    }

    @Override
    public LastAlbum2 getAlbumInfoByMBID(String mbid) throws IOException {
        return lastfm2.getAlbumInfoByMBID(mbid);
    }

    @Override
    public LastTrack getTrackInfoByName(String artistName, String trackName) throws IOException {
        return lastfm2.getTrackInfoByName(artistName, trackName);
    }

    @Override
    public LastTrack getTrackInfoByMBID(String mbid) throws IOException {
        return lastfm2.getTrackInfoByMBID(mbid);
    }

    @Override
    public SocialTag[] getTrackTopTagsByName(String artistName, String trackName) throws IOException {
        return lastfm2.getTrackTopTagsByName(artistName, trackName);
    }

    @Override
    public SocialTag[] getTrackTopTagsByMBID(String mbid) throws IOException {
        return lastfm2.getTrackTopTagsByMBID(mbid);
    }

    @Override
    public String[] getNeighboursForUser(String user) throws IOException {
        return lastfm2.getNeighboursForUser(user);
    }

    @Override
    public List<Integer[]> getWeeklyChartListByUser(String user) throws IOException {
        return lastfm2.getWeeklyChartListByUser(user);
    }

    @Override
    public List<LastItem> getWeeklyArtistChartByUser(String user) throws IOException {
        return lastfm2.getWeeklyArtistChartByUser(user);
    }

    @Override
    public List<LastItem> getWeeklyArtistChartByUser(String user, int from, int to) throws IOException {
        return lastfm2.getWeeklyArtistChartByUser(user, from, to);
    }

    @Override
    public LastTrack getTrackInfo(String trackMbid, String artistName, String trackName) throws IOException, CannotResolveException {
        return lastfm2.getTrackInfo(trackMbid, artistName, trackName);
    }

    @Override
    public SocialTag[] getTrackTopTags(String trackMbid, String artistName, String trackName) throws IOException, CannotResolveException {
        return lastfm2.getTrackTopTags(trackMbid, artistName, trackName);
    }
}
