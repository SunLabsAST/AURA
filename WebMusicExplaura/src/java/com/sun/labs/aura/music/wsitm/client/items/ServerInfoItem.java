/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.HashMap;

/**
 *
 * @author mailletf
 */
public class ServerInfoItem implements IsSerializable {

    private HashMap<String, Integer> cacheStatus;

    private int nbrArtists;
    private int nbrPhoto;
    private int nbrVideo;
    private int nbrEvent;

    public ServerInfoItem() {}

    public void setCacheStatus(HashMap<String, Integer> cacheStatus) {
        this.cacheStatus = cacheStatus;
    }

    public HashMap<String, Integer> getCacheStatus() {
        return cacheStatus;
    }

    public void setNbrArtists(int nbrArtists) {
        this.nbrArtists = nbrArtists;
    }

    public int getNbrArtists() {
        return nbrArtists;
    }

    public void setNbrPhoto(int nbrPhoto) {
        this.nbrPhoto = nbrPhoto;
    }

    public int getNbrPhoto() {
        return nbrPhoto;
    }

    public void setNbrEvent(int nbrEvent) {
        this.nbrEvent = nbrEvent;
    }

    public int getNbrEvent() {
        return nbrEvent;
    }


}
