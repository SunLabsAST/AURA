/*
 * Image.java
 *
 * Created on April 3, 2007, 8:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web.flickr;

/**
 *
 * @author plamere
 */
public class Image {
    private String id;
    private String title;
    private String creatorUserName;
    private String creatorRealName;
    
    private String imageURL;
    private String smallImageUrl;
    private String thumbNailImageUrl;
    private String photoPageURL;
    
    
    /**
     * Creates a new instance of Image
     */
    public Image() {
    }
    
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
    
    public void dump() {
        System.out.printf("===== %s ======\n", title);
        System.out.printf("        ID: %s\n", id);
        System.out.printf("   Creator: %s (%s)\n", creatorUserName, creatorRealName);
        System.out.printf("       URL: %s\n", imageURL);
        System.out.printf(" PhotoPage: %s\n", getPhotoPageURL());
        System.out.printf("SmallImage: %s\n", getSmallImageUrl());
        System.out.printf("ThumbImage: %s\n", getThumbNailImageUrl());
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
