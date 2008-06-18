/*
 * Album.java
 *
 * Created on April 4, 2007, 7:27 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class AlbumDetails implements IsSerializable {
    private String id;
    private String title;
    private String asin;
    
    /** Creates a new instance of Album */
    public AlbumDetails() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }
    
    public String getAlbumArt() {
         return "http://images.amazon.com/images/P/" + asin + ".01.MZZZZZZZ.jpg";
    }
    
    public String getAmazonLink() {
        return "http://www.amazon.com/gp/product/" + asin;
    }
    
    public String getHtmlWrapper() {
       StringBuffer sb = new StringBuffer();
        sb.append("<div style=\"float: left; margin-right: 10px; margin-bottom: 10px;\">");
        sb.append("<a href=\"" + getAmazonLink() + "\">");
        sb.append("<img style=\"border: 2px solid rgb(0, 0, 0);\" src=\"" + getAlbumArt() + "\" /></a>");
        sb.append("<br />\n");
        sb.append("<span style=\"font-size: 0.9em; margin-top: 0px;\">");
        sb.append("<a href=\""+ getAmazonLink() + "\">" + getTitle() +"</a>");
        sb.append("<br />\n");
        sb.append("</span>");
        sb.append("</div>");
        return sb.toString() ;
    }
}
