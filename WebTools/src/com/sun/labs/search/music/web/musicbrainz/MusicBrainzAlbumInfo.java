/*
 * MusicBrainzAlbumInfo.java
 *
 * Created on April 4, 2007, 7:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web.musicbrainz;

/**
 *
 * @author plamere
 */
public class MusicBrainzAlbumInfo {
    private String id;
    private String title;
    private String asin;
           
    
    /** Creates a new instance of MusicBrainzAlbumInfo */
    public MusicBrainzAlbumInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
    
}
