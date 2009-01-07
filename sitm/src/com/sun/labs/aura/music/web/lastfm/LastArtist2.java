/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.lastfm;

/**
 *
 * @author plamere
 */
public class LastArtist2 {
    private String name = "";
    private String mbid = "";
    private String url = "";
    private String smallImage = "";
    private String mediumImage = "";
    private String largeImage = "";
    private boolean streamable;
    private int listeners;
    private int playcount;
    private String bioSummary = "";
    private String bioFull = "";

    public String getBioFull() {
        return bioFull;
    }

    public void setBioFull(String bioFull) {
        this.bioFull = bioFull;
    }

    public String getBioSummary() {
        return bioSummary;
    }

    public void setBioSummary(String bioSummary) {
        this.bioSummary = bioSummary;
    }

    public String getLargeImage() {
        return largeImage;
    }

    public void setLargeImage(String largeImage) {
        this.largeImage = largeImage;
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

    public boolean isStreamable() {
        return streamable;
    }

    public void setStreamable(boolean streamable) {
        this.streamable = streamable;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void dump(boolean condensed) {
        System.out.println("Name      : " + name);
        System.out.println("MBID      : " + mbid);
        System.out.println("URL       : " + url);
        System.out.println("Small     : " + smallImage);
        System.out.println("Medium    : " + mediumImage);
        System.out.println("Large     : " + largeImage);
        System.out.println("Streamable: " + streamable);
        System.out.println("Listeners : " + listeners);
        System.out.println("Playcount : " + playcount);
        if (condensed) {
            System.out.println("BioSummary: " + condense(bioSummary, 40));
            System.out.println("BioFull   : " + condense(bioFull, 40));
        } else {
            System.out.println("BioSummary: " + bioSummary);
            System.out.println("BioFull   : " + bioFull);
        }
    }

    private String condense(String s, int len) {
        if (s.length() > len) {
            s = s.substring(0, len);
        }
        return s;
    }
}
