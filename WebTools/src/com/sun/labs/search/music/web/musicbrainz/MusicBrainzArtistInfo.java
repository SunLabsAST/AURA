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

package com.sun.labs.search.music.web.musicbrainz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class MusicBrainzArtistInfo {
    private String id;
    private int score;
    private String name;
    private String sortName;
    private int beginYear;
    private int endYear;
    private String type;
    private Map<String, String> urlMap;
    private List<MusicBrainzAlbumInfo> albums;
    private Set<String> collaborators;
    
    /** Creates a new instance of MusicBrainzArtistInfo */
    public MusicBrainzArtistInfo() {
        urlMap = new HashMap<String, String>();
        albums = new ArrayList<MusicBrainzAlbumInfo>();
        collaborators = new HashSet<String>();
    }
    
    public String getID() {
        return id;
    }
    
    void setID(String id) {
        this.id = id;
    }
    
    public int getScore() {
        return score;
    }
    
    void setScore(int score) {
        this.score = score;
    }
    
    public String getName() {
        return name;
    }
    
    void setName(String name) {
        this.name = name;
    }
    
    public String getSortName() {
        return sortName;
    }
    
    void setSortName(String sortName) {
        this.sortName = sortName;
    }
    
    public int getBeginYear() {
        return beginYear;
    }
    
    void setBeginYear(int beginYear) {
        this.beginYear = beginYear;
    }
    
    public int getEndYear() {
        return endYear;
    }
    
    void setEndYear(int endYear) {
        this.endYear = endYear;
    }
    
    public String getType() {
        return type;
    }
    
    void setType(String type) {
        this.type = type;
    }
    
    void addURL(String type, String url) {
        urlMap.put(type, url);
    }
    
    public Map<String, String> getURLMap() {
        return urlMap;
    }
    
    public String getUrl(String type) {
        return urlMap.get(type);
    }
    
    public void addAlbum(MusicBrainzAlbumInfo album) {
        albums.add(album);
    }
    
    public List<MusicBrainzAlbumInfo> getAlbums() {
        return albums;
    }
    
    public void dump() {
        System.out.println("======" + name + " =========");
        System.out.println("id\t" + id);
        System.out.println("type\t" + type);
        System.out.println("score\t" + score);
        System.out.println("sortName\t" + sortName);
        System.out.println("Year\t" + dateString(beginYear) + " to " + dateString(endYear));
        for (String type : urlMap.keySet()) {
            System.out.println("url-" + type + "\t" + urlMap.get(type));
        }
        for (String collab : collaborators) {
            System.out.println(" collab: " + collab);
        }
    }
    
    private String dateString(int year) {
        if (year == 0) {
            return "unknown";
        } else {
            return Integer.toString(year);
        }
    }

    public List<String> getCollaborators() {
        return new ArrayList<String>(collaborators);
    }

    public void addCollaborator(String id) {
        collaborators.add(id);
    }
}
