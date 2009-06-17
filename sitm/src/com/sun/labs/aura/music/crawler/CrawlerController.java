/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.crawler;

import com.sun.labs.aura.music.web.lastfm.LastAlbum2;
import com.sun.labs.aura.music.web.lastfm.LastArtist;
import com.sun.labs.aura.music.web.lastfm.LastArtist2;
import com.sun.labs.aura.music.web.lastfm.LastFM;
import com.sun.labs.aura.music.web.lastfm.LastFM2;
import com.sun.labs.aura.music.web.lastfm.LastItem;
import com.sun.labs.aura.music.web.lastfm.LastTrack;
import com.sun.labs.aura.music.web.lastfm.LastUser;
import com.sun.labs.aura.music.web.lastfm.SocialTag;
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

}
