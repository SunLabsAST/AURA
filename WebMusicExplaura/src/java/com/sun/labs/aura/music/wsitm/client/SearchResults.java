/*
 * SearchResults.java
 *
 * Created on March 5, 2007, 2:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sun.labs.aura.music.wsitm.client.AbstractSearchWidget.searchTypes;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;

/**
 *
 * @author plamere
 */
public class SearchResults implements IsSerializable {
    
    private String query;
    private String status;
    private searchTypes searchType;
    private ItemInfo[] itemResults = null;
    
    public SearchResults() {
    }
    
    /**
     * Creates a new instance of SearchResults
     */
    public SearchResults(String query, searchTypes searchType, ItemInfo[] itemResults) {
        this.query = query;
        this.itemResults = itemResults;
        this.searchType = searchType;
        this.status = "OK";
    }
    
    public SearchResults(String error) {
        this.status = error;
    }
    
    public boolean isOK() {
        return "OK".equals(getStatus());
    }
    
    public ItemInfo[] getItemResults() {
        return itemResults;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getQuery() {
        return query;
    }
    
    public searchTypes getSearchType() {
        return searchType;
    }
    
    public String toString() {
        if (searchType == searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST) {
            return "artistSearch:" + query;
        } else if (searchType == searchTypes.SEARCH_FOR_ARTIST_BY_TAG) {
            return "artistSearchByTag:" + query;
        } else {
            return "tagSearch:" + query;
        }
    }
}
