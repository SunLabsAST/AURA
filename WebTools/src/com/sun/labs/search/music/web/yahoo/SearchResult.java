/*
 * SearchResult.java
 *
 * Created on April 8, 2007, 7:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web.yahoo;

/**
 *
 * @author plamere
 */
public class SearchResult {
    private String title;
    private String url;
    /** Creates a new instance of SearchResult */
    public SearchResult(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
    
    public String toString() {
        return title + " " + url;
    }
}
