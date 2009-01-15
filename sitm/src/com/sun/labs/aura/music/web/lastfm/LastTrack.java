/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.lastfm;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author plamere
 */
public class LastTrack {
    private String name = "";
    private String mbid = "";
    private String lfmid = "";
    private String url = "";
    private int duration;
    private boolean streamble;
    private int listeners;
    private int playcount;
    private String artistName = "";
    private String artistMbid = "";
    private String artistUrl = "";
    private String albumArtist = "";
    private String albumTitle = "";
    private String albumMbid = "";
    private String albumUrl = "";
    private int albumPosition;
    private String smallImage = "";
    private String mediumImage = "";
    private String largeImage = "";
    private List<String> topTags;
    private String wikiSummary = "";
    private String wikiContent = "";

    public LastTrack() {
        topTags = new ArrayList<String>();
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public String getAlbumMbid() {
        return albumMbid;
    }

    public void setAlbumMbid(String albumMbid) {
        this.albumMbid = albumMbid;
    }

    public int getAlbumPosition() {
        return albumPosition;
    }

    public void setAlbumPosition(int albumPosition) {
        this.albumPosition = albumPosition;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    public String getArtistMbid() {
        return artistMbid;
    }

    public void setArtistMbid(String artistMbid) {
        this.artistMbid = artistMbid;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistUrl() {
        return artistUrl;
    }

    public void setArtistUrl(String artistUrl) {
        this.artistUrl = artistUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLargeImage() {
        return largeImage;
    }

    public void setLargeImage(String largeImage) {
        this.largeImage = largeImage;
    }

    public String getLfmid() {
        return lfmid;
    }

    public void setLfmid(String lfmid) {
        this.lfmid = lfmid;
    }

    public int getListeners() {
        return listeners;
    }

    public void setListeners(int listeners) {
        this.listeners = listeners;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    public String getMediumImage() {
        return mediumImage;
    }

    public void setMediumImage(String mediumImage) {
        this.mediumImage = mediumImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlaycount() {
        return playcount;
    }

    public void setPlaycount(int playcount) {
        this.playcount = playcount;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
    }

    public boolean isStreamble() {
        return streamble;
    }

    public void setStreamble(boolean streamble) {
        this.streamble = streamble;
    }

    public List<String> getTopTags() {
        return topTags;
    }

    public void addTopTag(String tag) {
        topTags.add(tag);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public String getWikiContent() {
        return wikiContent;
    }

    public void setWikiContent(String wikiContent) {
        this.wikiContent = wikiContent;
    }

    public String getWikiSummary() {
        return wikiSummary;
    }

    public void setWikiSummary(String wikiSummary) {
        this.wikiSummary = wikiSummary;
    }

    public void dump() {
        System.out.println(" name:          " + name );
        System.out.println(" mbid:          " + mbid );
        System.out.println(" lfmid:         " + lfmid );
        System.out.println(" url:           " + url );
        System.out.println(" duration:      " + duration );
        System.out.println(" streamble:     " + streamble );
        System.out.println(" listeners:     " + listeners );
        System.out.println(" playcount:     " + playcount );
        System.out.println(" artistName:    " + artistName );
        System.out.println(" artistMbid:    " + artistMbid );
        System.out.println(" artistUrl:     " + artistUrl );
        System.out.println(" albumArtist:   " + albumArtist );
        System.out.println(" albumTitle:    " + albumTitle );
        System.out.println(" albumMbid:     " + albumMbid );
        System.out.println(" albumUrl:      " + albumUrl );
        System.out.println(" albumPosition: " + albumPosition );
        System.out.println(" smallImage:    " + smallImage );
        System.out.println(" mediumImage:   " + mediumImage );
        System.out.println(" largeImage:    " + largeImage );
        System.out.println(" topTags:       " + topTags.size() );
        System.out.println(" wikiSummary:   " + wikiSummary.length() );
        System.out.println(" wikiContent:   " + wikiContent.length() );
        System.out.println("");
    }
}