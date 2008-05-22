/*
 * ArtistPhoto.java
 *
 * Created on April 4, 2007, 6:27 AM
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
public class ArtistPhoto implements IsSerializable {
    private String id;
    private String title;
    private String creatorUserName;
    private String creatorRealName;
    
    private String imageURL;
    private String smallImageUrl;
    private String thumbNailImageUrl; 
    private String photoPageURL;
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getImageURL() {
        return imageURL;
    }
    
    
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    
    
    public String getSmallImageUrl() {
        return smallImageUrl;
    }
    
    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String toString() {
        return title + ": " + imageURL + "\n";
    }
    
    public String getPhotoPageURL() {
        return photoPageURL;
    }

    public void setPhotoPageURL(String photoPageURL) {
        this.photoPageURL = photoPageURL;
    }

    public String getThumbNailImageUrl() {
        return thumbNailImageUrl;
    }

    public void setThumbNailImageUrl(String thumbNailImageUrl) {
        this.thumbNailImageUrl = thumbNailImageUrl;
    }
    
    public String getCreatorPage() {
        return "http://www.flickr.com/photos/" + getCreatorUserName();
    }
    
    public String getHtmlWrapper() {
        StringBuffer sb = new StringBuffer();
        sb.append("<div style=\"float: left; margin-right: 10px; margin-bottom: 10px;\">");
        sb.append("<img style=\"border: 2px solid rgb(0, 0, 0);\" src=\"" + getSmallImageUrl() + "\"/>");
        sb.append("<br />\n");
        sb.append("<span style=\"font-size: 0.7em; margin-top: 0px;\">");
        sb.append("By <a href=\"" + getCreatorPage() + "\" target=\"window1\" \">" + getCreatorRealName()+"</a>.");
        sb.append("</span>");
        sb.append("</div>");
        return sb.toString();
    }
    
    public String getRichHtmlWrapper() {
        String creatorName = getCreatorRealName();
        if (creatorName.length() == 0) {
            creatorName = getCreatorUserName();
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<div>");
        sb.append("<span style=\"font-size: 1.em; margin-top: 0px;\">" + getTitle() + "</span>\n");
        sb.append("<br />\n");
        sb.append("<a href=\"" +  getPhotoPageURL() + "\" target=\"Window1\">");
        sb.append("   <img style=\"border: 2px solid rgb(0, 0, 0);\" src=\"" + getImageURL() + "\"/>");
        sb.append("</a>");
        sb.append("<br />\n");
        sb.append("<span style=\"font-size: 0.7em; margin-top: 0px;\">");
        sb.append("By <a href=\"" + getCreatorPage() + "\" target=\"Window1\" >" + creatorName +"</a>.");
        sb.append("</span>");
        sb.append("</div>");
        return sb.toString();
    }

    public String getCreatorUserName() {
        return creatorUserName;
    }

    public void setCreatorUserName(String creatorUserName) {
        this.creatorUserName = creatorUserName;
    }

    public String getCreatorRealName() {
        return creatorRealName;
    }

    public void setCreatorRealName(String createorRealName) {
        this.creatorRealName = createorRealName;
    }
}
