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

package com.sun.labs.aura.music.web.musicbrainz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class MusicBrainzAlbumInfo {
    private String id;
    private String title;
    private String asin;
    private Long releaseDate;
    private Set<String> artistIds;
    private Map<String, String> urlMap;
    private Map<Integer, MusicBrainzTrackInfo> trackMap;
           
    
    /** Creates a new instance of MusicBrainzAlbumInfo */
    public MusicBrainzAlbumInfo() {
        artistIds = new HashSet<String>();
        urlMap = new HashMap<String, String>();
        trackMap = new HashMap<Integer, MusicBrainzTrackInfo>();
    }

    public String getMbid() {
        return id;
    }

    public void setMbid(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public void addURL(String type, String url) {
        urlMap.put(type, url);
    }

    public Map<String, String> getURLMap() {
        return urlMap;
    }

    public String getUrl(String type) {
        return urlMap.get(type);
    }

    public void addTrack(int num, MusicBrainzTrackInfo trackInfo) {
        trackMap.put(num, trackInfo);
    }

    public Map<Integer, MusicBrainzTrackInfo> getTrackMap() {
        return trackMap;
    }

    public long getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(long rd) {
        releaseDate = new Long(rd);
    }

    public void addArtistId(String artistId) {
        artistIds.add(artistId);
    }

    public Set<String> getArtistIds() {
        return artistIds;
    }


    public void dump() {
        System.out.println("======" + title + " =========");
        System.out.println("id\t" + id);
        System.out.println("asin\t" + asin);
        if (releaseDate != null) {
            System.out.println("release\t" + new SimpleDateFormat("yyyy-MM-dd").format(new Date(releaseDate)));
        }
        for (String type : urlMap.keySet()) {
            System.out.println("url-" + type + "\t" + urlMap.get(type));
        }
        System.out.println("Artists:");
        for (String aId : artistIds) {
            System.out.println("  "+aId);
        }
        System.out.println("Tracks:");
        int trackNo = 1;
        for (MusicBrainzTrackInfo track : trackMap.values()) {
            System.out.println(" "+trackNo+") "+track.getTitle()+" ("+track.getMbid()+") "+track.getDuration());
            trackNo++;
        }
    }

}
