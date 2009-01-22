/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.echonest;

import java.io.Serializable;

/**
 *
 * @author plamere
 */
public class EchoArtist implements Serializable {
    private static final long serialVersionUID = 7654321L;

    private String name;
    private String id;
    private String mbid;

    public EchoArtist(String name, String id, String mbid) {
        this.name = name;
        this.id = id;
        this.mbid = mbid;
    }

    public String getId() {
        return id;
    }

    public String getMbid() {
        return mbid;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EchoArtist other = (EchoArtist) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return name;
    }

}
