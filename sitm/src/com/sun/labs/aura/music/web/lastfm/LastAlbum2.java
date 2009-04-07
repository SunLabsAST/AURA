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

package com.sun.labs.aura.music.web.lastfm;

import java.io.Serializable;

/*
 *
 http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=b25b959554ed76058ac220b7b2e0a026&artist=yes&album=fragile
 */
/**
 *
 * @author plamere
 */
public class LastAlbum2 implements Serializable {
    private String name = "";
    private String artistName = "";
    private String lfmID = "";
    private String mbid = "";
    private String url = "";
    private String releaseDate = "";
    private String smallImage = "";
    private String mediumImage = "";
    private String largeImage = "";
    private String hugeImage = "";
    private int listeners = 0;
    private int playcount = 0;
    private String wikiSummary = "";
    private String wikiFull = "";


    public LastAlbum2() {
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }


    public String getLfmID() {
        return lfmID;
    }

    public void setLfmID(String lfmID) {
        this.lfmID = lfmID;
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

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }


    public String getUrl() {
        return url;
    }

    public String getHugeImage() {
        return hugeImage;
    }

    public void setHugeImage(String hugeImage) {
        this.hugeImage = hugeImage;
    }

    public String getLargeImage() {
        return largeImage;
    }

    public void setLargeImage(String largeImage) {
        this.largeImage = largeImage;
    }

    public String getMediumImage() {
        return mediumImage;
    }

    public void setMediumImage(String mediumImage) {
        this.mediumImage = mediumImage;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWikiFull() {
        return wikiFull;
    }

    public void setWikiFull(String wikiFull) {
        this.wikiFull = wikiFull;
    }

    public String getWikiSummary() {
        return wikiSummary;
    }

    public void setWikiSummary(String wikiSummary) {
        this.wikiSummary = wikiSummary;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LastAlbum2 other = (LastAlbum2) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.lfmID == null) ? (other.lfmID != null) : !this.lfmID.equals(other.lfmID)) {
            return false;
        }
        if ((this.mbid == null) ? (other.mbid != null) : !this.mbid.equals(other.mbid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 47 * hash + (this.lfmID != null ? this.lfmID.hashCode() : 0);
        hash = 47 * hash + (this.mbid != null ? this.mbid.hashCode() : 0);
        return hash;
    }

    public void dump() {
        System.out.println("  Name        : " + name);
        System.out.println("  Artist      : " + artistName);
        System.out.println("  lfmID       : " + lfmID);
        System.out.println("  mbid        : " + mbid);
        System.out.println("  releaseDate : " + releaseDate);
        System.out.println("  small image : " + smallImage);
        System.out.println("  med image   : " + mediumImage);
        System.out.println("  large image : " + largeImage);
        System.out.println("  huge image  : " + hugeImage);
        System.out.println("  listeners   : " + listeners);
        System.out.println("  playcount   : " + playcount);
        System.out.println("  wikiSummary : " + wikiSummary.length());
        System.out.println("  wikiFull    : " + wikiFull.length());
        System.out.println();
    }
}
