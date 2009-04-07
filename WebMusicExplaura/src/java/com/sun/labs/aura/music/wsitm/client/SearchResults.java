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

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.searchTypes;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;

/**
 *
 * @author plamere
 */
public class SearchResults implements IsSerializable {

    private boolean registeredInCdm = false;

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

    /**
     * Get the items contained in this result set. Pass the ClientDataManager to
     * have to enable tracking for the search attention
     * @param cdm
     * @return
     */
    public ItemInfo[] getItemResults(ClientDataManager cdm) {
        // Register this search
        if (!registeredInCdm && cdm!=null) {
            cdm.getSearchAttentionManager().registerSearch(query, searchType, itemResults);
            registeredInCdm = true;
        }
        
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
    
    @Override
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
