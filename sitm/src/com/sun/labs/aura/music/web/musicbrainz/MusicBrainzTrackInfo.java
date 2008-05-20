/*
 * MusicBrainzTrackInfo.java
 *
 * Created on February 21, 2007, 10:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.musicbrainz;

/**
 *
 * @author plamere
 */
public interface MusicBrainzTrackInfo {
    String getAlbumID();

    String getAlbumName();

    String getArtistID();

    String getArtistName();


    String getTrackID();

    String getTrackName();
    
}
