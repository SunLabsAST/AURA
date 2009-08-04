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

import com.sun.labs.aura.music.web.lastfm.LastAlbum2;
import com.sun.labs.aura.music.web.lastfm.LastArtist;
import com.sun.labs.aura.music.web.lastfm.LastArtist2;
import com.sun.labs.aura.music.web.lastfm.LastFM;
import com.sun.labs.aura.music.web.lastfm.LastFM2;
import com.sun.labs.aura.music.web.CannotResolveException;
import com.sun.labs.aura.music.web.lastfm.LastItem;
import com.sun.labs.aura.music.web.lastfm.LastTrack;
import com.sun.labs.aura.music.web.lastfm.LastUser;
import com.sun.labs.aura.music.web.lastfm.SocialTag;
import com.sun.labs.aura.util.Counted;
import com.sun.labs.util.props.Component;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


/**
 *
 * @author mailletf
 */
public interface CrawlerController extends LastFM2, LastFM, Component, Remote {

    @Override
    public void setTrace(boolean trace) throws IOException, RemoteException;

    @Override
    public void setMinimumCommandPeriod(long period) throws IOException, RemoteException;

    @Override
    public LastArtist2 getArtistInfoByName(String artistName) throws IOException, RemoteException;

    @Override
    public LastArtist2 getArtistInfoByMBID(String mbid) throws IOException, RemoteException;

    @Override
    public LastAlbum2 getAlbumInfoByName(String artistName, String albumName) throws IOException, RemoteException;

    @Override
    public LastAlbum2 getAlbumInfoByMBID(String mbid) throws IOException, RemoteException;

    @Override
    public LastTrack getTrackInfoByName(String artistName, String trackName) throws IOException, RemoteException;

    @Override
    public LastTrack getTrackInfoByMBID(String mbid) throws IOException, RemoteException;

    @Override
    public LastTrack getTrackInfo(String trackMbid, String artistName, String trackName) throws IOException, CannotResolveException;

    @Override
    public SocialTag[] getTrackTopTags(String trackMbid, String artistName, String trackName) throws IOException, CannotResolveException;

    @Override
    public SocialTag[] getTrackTopTagsByName(String artistName, String trackName) throws IOException, RemoteException;

    @Override
    public SocialTag[] getTrackTopTagsByMBID(String mbid) throws IOException, RemoteException;

    @Override
    public String[] getNeighboursForUser(String user) throws IOException, RemoteException;

    @Override
    public List<Integer[]> getWeeklyChartListByUser(String user) throws IOException, RemoteException;

    @Override
    public List<LastItem> getWeeklyArtistChartByUser(String user) throws IOException, RemoteException;

    @Override
    public List<LastItem> getWeeklyArtistChartByUser(String user, int from, int to) throws IOException, RemoteException;

    @Override
    public SocialTag[] getArtistTags(String artistName) throws IOException, RemoteException;

    @Override
    public SocialTag[] getAlbumTags(String artistName, String trackName) throws IOException, RemoteException;

    @Override
    public SocialTag[] getTrackTags(String artistName, String trackName) throws IOException, RemoteException;

    @Override
    /**
     * @deprecated
     */
    public SocialTag[] getArtistTags(String artistName, boolean raw) throws IOException, RemoteException;

    @Override
    public LastItem[] getArtistFans(String artistName) throws IOException, RemoteException;

    @Override
    public LastUser getUser(String userName) throws IOException, RemoteException;

    @Override
    public int getPopularity(String artistName) throws IOException, RemoteException;

    @Override
    public LastItem[] getTopArtistsForUser(String user) throws IOException, RemoteException;

    @Override
    public LastItem[] getWeeklyArtistsForUser(String user) throws IOException, RemoteException;

    @Override
    public String[] getSimilarUsers(String user) throws IOException, RemoteException;

    @Override
    public LastArtist[] getSimilarArtists(String artist) throws IOException, RemoteException;

    @Override
    public LastUser getFanFromLastFM(String url) throws IOException, RemoteException;

    @Override
    public LastItem[] getTopArtistsForTag(String tag) throws IOException, RemoteException;

    @Override
    public List<Counted<LastAlbum2>> getTagTopAlbums(String tagName) throws IOException, RemoteException;

    @Override
    public List<Counted<LastTrack>> getTagTopTracks(String tagName) throws IOException, RemoteException;

}
