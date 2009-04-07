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

package com.sun.labs.aura.music.wsitm.client.items;

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
        //sb.append("<span style=\"font-size: 1.em; margin-top: 0px;\">" + getTitle() + "</span>\n");
        //sb.append("<br />\n");
        sb.append("<a href=\"" +  getPhotoPageURL() + "\" target=\"Window1\">");
        sb.append("   <center><img style=\"border: 2px solid rgb(0, 0, 0);\" src=\"" + getImageURL() + "\"/></center>");
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
