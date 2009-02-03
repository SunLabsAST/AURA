/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.sxsw;

import com.sun.labs.aura.music.web.googlemaps.Location;
import com.sun.labs.aura.music.web.lastfm.LastArtist2;
import com.sun.labs.aura.music.web.lastfm.SocialTag;
import com.sun.labs.aura.music.web.youtube.YoutubeVideo;
import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author plamere
 */
public class Artist implements Serializable {

    private static final long serialVersionUID = 34567874L;
    public final static Comparator<Artist> POPULARITY_SORT = new Comparator<Artist>() {

        public int compare(Artist a1, Artist a2) {
            return a1.getArtistInfo().getListeners() - a2.getArtistInfo().getListeners();
        }
    };

    public final static Comparator<Artist> NAME_LENGTH_SORT = new Comparator<Artist>() {

        public int compare(Artist a1, Artist a2) {
            return a1.getName().length() - a2.getName().length();
        }
    };

    public final static Comparator<Artist> WORD_LENGTH_SORT = new Comparator<Artist>() {

        public int compare(Artist a1, Artist a2) {
            return wordLength(a1.getName()) - wordLength(a2.getName());
        }

        int wordLength(String s) {
            return s.split("\\s").length;
        }
    };

    public final static Comparator<Artist> HOTNESS_SORT = new Comparator<Artist>() {

        public int compare(Artist a1, Artist a2) {
            if (a1.getHotness() > a2.getHotness()) {
                return 1;
            } else if (a1.getHotness() < a2.getHotness()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    public final static Comparator<Artist> RISING_SORT = new Comparator<Artist>() {

        public int compare(Artist a1, Artist a2) {
            if (a1.getRising() > a2.getRising()) {
                return 1;
            } else if (a1.getRising() < a2.getRising()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    public final static Comparator<Artist> ALPHA_SORT = new Comparator<Artist>() {
        public int compare(Artist a1, Artist a2) {
            return a1.getSortName().compareTo(a2.getSortName());
        }
    };

    private String name;
    private String url;
    private String where;
    private String echoID;
    private float hotness;
    private LastArtist2 artistInfo;
    private SocialTag[] tags;
    private YoutubeVideo[] videos;
    private Location location;

    public LastArtist2 getArtistInfo() {
        return artistInfo;
    }

    public void clearAll() {
        url = null;
        where = null;
        echoID = null;
        artistInfo = null;
        tags = null;
        videos = null;
        location = null;
    }

    public void setArtistInfo(LastArtist2 artistInfo) {
        this.artistInfo = artistInfo;
    }

    public String getSortName() {
         return getName().toLowerCase().replaceFirst("^the\\s+", "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SocialTag[] getTags() {
        return tags;
    }

    public void setTags(SocialTag[] tags) {
        this.tags = tags;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public YoutubeVideo[] getVideos() {
        return videos;
    }

    public void setVideos(YoutubeVideo[] videos) {
        this.videos = videos;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String toString() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean hasTag(String tag) {
        for (SocialTag stag : tags) {
            if (tag.equals(stag.getName())) {
                return true;
            }
        }
        return false;
    }

    public String getEchoID() {
        return echoID;
    }

    public void setEchoID(String echoID) {
        this.echoID = echoID;
    }

    public float getHotness() {
        return hotness;
    }

    public float getRising() {
        int listeners = getArtistInfo().getListeners() + 1;
        return getHotness() / listeners;
    }

    public void setHotness(float hotness) {
        this.hotness = hotness;
    }

    public int getTagFreq(String tag) {
        for (SocialTag stag : tags) {
            if (tag.equals(stag.getName())) {
                return stag.getFreq();
            }
        }
        return 0;
    }
}
