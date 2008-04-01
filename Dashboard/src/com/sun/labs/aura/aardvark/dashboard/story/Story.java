/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.dashboard.story;

import com.sun.labs.aura.aardvark.dashboard.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author plamere
 */
public class Story {
    private float score;
    private String title;
    private String source;
    private String description;
    private String url;
    private String imageUrl;
    private int length;
    private long pulltime;
    private List<Classification> classifications;



    public Story() {
        classifications = new ArrayList<Classification>();
    }

    public List<Classification> getClassifications() {
        return classifications;
    }

    public void addClassification(Classification classification) {
        classifications.add(classification);
    }

    public String getDescription() {
        return description == null ? "(none)" : description ;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSource() {
        return source == null ? "(unknown)" : source ;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getTitle() {
        return title == null ? "(untitled)" : title ;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getPulltime() {
        return pulltime;
    }

    public void setPulltime(long pulltime) {
        this.pulltime = pulltime;
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<story score=\"" + score + "\">\n");
        if (title != null) {
            sb.append("    <title>" + title + "</title>\n");
        }
        if (source != null) {
            sb.append("    <source>" + source + "</source>\n");
        }

        if (description != null) {
            sb.append("    <description>" + description + "</description>\n");
        }

        if (url != null) {
            sb.append("    <url>" + url + "</url>\n");
        }

        if (imageUrl != null) {
            sb.append("    <imageUrl>" + imageUrl + "</imageUrl>\n");
        }

        for (Classification classification : classifications) {
            sb.append("    <class score=\"" + classification.getScore() + "\">" + classification.getName() + "</class>\n");
        }

        sb.append("</story>\n");
        return sb.toString();
    }

    public String toString() {
        return toXML();
    }
}
