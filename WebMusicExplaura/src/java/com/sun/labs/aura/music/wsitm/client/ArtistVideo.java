/*
 * ArtistVideo.java
 *
 * Created on March 30, 2007, 11:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class ArtistVideo implements IsSerializable {
    private String url;
    private String thumbnail;
    private String title;
    
    /** Creates a new instance of ArtistVideo */
    public ArtistVideo() {
    }
    
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getThumbnail() {
        return thumbnail;
    }
    
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getHtmlWrapper() {
        StringBuffer sb = new StringBuffer();
        sb.append("<span id=\"videow\">");
      //  sb.append("<a href=\"" + getUrl() + "\">");
        sb.append("<img style=\"border: 2px solid rgb(0, 0, 0);\" src=\"" + getThumbnail() + "\" />");
      //  sb.append("</a>");
        sb.append("</span>");
        return sb.toString();
    }
}