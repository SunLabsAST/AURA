/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.lastfm;

/**
 *
 * @author plamere
 */
public class LastArtist {
    private String artistName;
    private String mbaid;

    public LastArtist(String artistName, String mbaid) {
        this.artistName = artistName;
        this.mbaid = mbaid;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getMbaid() {
        return mbaid;
    }

}
