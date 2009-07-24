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

/**
 *
 * @author plamere
 */
public class LastArtist2 implements Serializable {
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

    public int getListenerCount() {
        return listeners;
    }

    public void setListenerCount(int listeners) {
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
