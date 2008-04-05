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
    private List<ScoredString> tags;
    private List<ScoredString> autotags;
    private List<ScoredString> topTerms;



    public Story() {
        tags = new ArrayList<ScoredString>();
        autotags = new ArrayList<ScoredString>();
        topTerms = new ArrayList<ScoredString>();
    }

    public List<ScoredString> getTags() {
        return tags;
    }

    public List<ScoredString> getAutotags() {
        return autotags;
    }

    public List<ScoredString> getTopTerms() {
        return topTerms;
    }

    public void addTags(ScoredString classification) {
        tags.add(classification);
    }

    public void addAutotags(ScoredString classification) {
        autotags.add(classification);
    }

    public void addTopTerms(ScoredString classification) {
        topTerms.add(classification);
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

        for (ScoredString tag : tags) {
            sb.append("    <tags score=\"" + tag.getScore() + "\">" + tag.getName() + "</tags>\n");
        }

        for (ScoredString tag : autotags) {
            sb.append("    <autotags score=\"" + tag.getScore() + "\">" + tag.getName() + "</autotags>\n");
        }

        sb.append("</story>\n");
        return sb.toString();
    }

    public String toString() {
        return toXML();
    }
}
