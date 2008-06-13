/*
 * WikiInfo.java
 *
 * Created on April 3, 2007, 2:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.wikipedia;

/**
 *
 * @author plamere
 */
public class WikiInfo {
    private String summary = "None available.";
    private String fullText = "";
    private String url;
    
    
    @Override
    public String toString() {
        return url + "\n" +  summary;
    }
    
    /** Creates a new instance of WikiInfo */
    public WikiInfo(String url) {
        this.setUrl(url);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
}
