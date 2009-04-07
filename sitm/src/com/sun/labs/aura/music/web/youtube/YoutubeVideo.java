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

package com.sun.labs.aura.music.web.youtube;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Comparator;

/**
 *
 * @author plamere
 */
public class YoutubeVideo  implements Serializable {
   private static final long serialVersionUID = 615715594L;

    public final static Comparator<YoutubeVideo> PLAY_ORDER = new Comparator<YoutubeVideo>() {

        public int compare(YoutubeVideo o1, YoutubeVideo o2) {
            return o1.getViewCount() - o2.getViewCount();
        }
    };

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
    private URL flashURL;
            

    /** Creates a new instance of YoutubeVideo */
    public YoutubeVideo() {
    }

    public YoutubeVideo(String url) throws IOException {
        setURL(new URL(url));
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

    public URL getFlashURL() {
        return flashURL;
    }

    public void setFlashURL(URL flashURL) {
        this.flashURL = flashURL;
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
}
