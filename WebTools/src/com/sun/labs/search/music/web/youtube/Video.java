/*
 * Video.java
 *
 * Created on March 29, 2007, 7:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web.youtube;

import java.net.URL;

/**
 *
 * @author plamere
 */
public class Video implements Comparable<Video> {
    private String author;
    private String id;
    private String title;
    private int length;
    private float rating;
    private int ratingCount;
    private String description;
    private int viewCount;
    private int commentCount;
    private String tags;
    private URL url;
    private URL thumbnail;
            
   
    
    /** Creates a new instance of Video */
    public Video() {
    }

    public String getAuthor() {
        return author;
    }

    void setAuthor(String author) {
        this.author = author;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public int getLength() {
        return length;
    }

    void setLength(int length) {
        this.length = length;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public int getViewCount() {
        return viewCount;
    }

    void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getTags() {
        return tags;
    }

    void setTags(String tags) {
        this.tags = tags;
    }

    public URL getURL() {
        return url;
    }

    void setURL(URL url) {
        this.url = url;
    }

    public URL getThumbnail() {
        return thumbnail;
    }

    void setThumbnail(URL thumbnail) {
        this.thumbnail = thumbnail;
    }
    
    public String toString() {
        return title + "-" + description + " views: " + viewCount;
    }
    
    public void dump() {
        System.out.println("===== " + title + " ========");
        System.out.println("Author      : " + author);
        System.out.println("id          : " + id);
        System.out.println("length      : " + length);
        System.out.println("rating      : " + rating);
        System.out.println("ratingCount : " + ratingCount);
        System.out.println("description : " + description);
        System.out.println("viewCount   : " + viewCount);
        System.out.println("commentCount: " + commentCount);
        System.out.println("tags        : " + tags);
        System.out.println("URL         : " + url);
        System.out.println("thumbnail   : " + thumbnail);
    }

    public int compareTo(Video o) {
        if (viewCount < o.viewCount) {
            return -1;
        } else if (viewCount > o.viewCount) {
            return 1;
        } else {
            return 0;
        }
    }

}
