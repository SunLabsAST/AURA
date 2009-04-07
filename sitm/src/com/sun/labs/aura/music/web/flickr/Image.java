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

package com.sun.labs.aura.music.web.flickr;

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
