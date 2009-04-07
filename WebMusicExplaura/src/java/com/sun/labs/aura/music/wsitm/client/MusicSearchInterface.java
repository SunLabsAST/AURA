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

import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.items.TagDetails;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.google.gwt.user.client.rpc.RemoteService;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ArtistRecommendation;
import com.sun.labs.aura.music.wsitm.client.items.AttentionItem;
import com.sun.labs.aura.music.wsitm.client.items.RecsNTagsContainer;
import com.sun.labs.aura.music.wsitm.client.items.ScoredC;
import com.sun.labs.aura.music.wsitm.client.items.ScoredTag;
import com.sun.labs.aura.music.wsitm.client.items.ServerInfoItem;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.searchTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author plamere
 */
public interface MusicSearchInterface extends RemoteService {

    public ArtistCompact[] getRandomPopularArtists(int nbr) throws WebException;
    public ArrayList<ScoredC<String>> getArtistOracle() throws WebException;
    public ArrayList<ScoredC<String>> getTagOracle() throws WebException;
    public HashMap<String, String> getSimTypes() throws WebException;
    
    public SearchResults artistSearch(String searchString, int maxResults) throws WebException;
    public SearchResults artistSearchByTag(String searchString, int maxResults) throws WebException;
    public SearchResults tagSearch(String searchString, int maxResults) throws WebException;
    public void addSearchAttention(String userKey, String target, searchTypes sT, String searchStr) throws WebException;

    public ArtistDetails getArtistDetails(String id, boolean refresh, String simTypeName, String popularity) throws WebException ;
    public ArtistCompact getArtistCompact(String artistId) throws WebException;
    public ArrayList<ScoredC<ArtistCompact>> getSimilarArtists(String id, String simTypeName, String popularity) throws WebException;
    public TagDetails getTagDetails(String id, boolean refresh, String simTypeName) throws WebException;
    public ItemInfo[] getCommonTags(String artistID1, String artistID2, int num, String simType) throws WebException;
    public ItemInfo[] getCommonTags(Map<String, ScoredTag> tagMap, String artistID, int num) throws WebException;
    public ItemInfo[] getComboTagCloud(String artistID1, String artistID2, int num, String simType)  throws WebException;
    public ArrayList<ScoredC<ArtistCompact>> getRepresentativeArtistsOfTag(String tagId) throws WebException;

    public Map<String, String> getArtistRecommendationTypes();
    public ItemInfo[] getDistinctiveTags(String artistID, int count) throws WebException;
    public ArrayList<ScoredC<ArtistCompact>> getSteerableRecommendations(Map<String, ScoredTag> tagMap, String popularity) throws WebException;
    public RecsNTagsContainer getRecommendationsFromString(String tagQuery) throws WebException;
    
    public HashMap<String,Integer> fetchUserSongRating(HashSet<String> artistID) throws WebException;
    public void terminateSession();
    public ListenerDetails getLogInDetails() throws WebException;
    public ListenerDetails getNonOpenIdLogInDetails(String userKey) throws WebException;
    public void updateListener(ListenerDetails lD) throws WebException;
    public void updateUserSongRating(int rating, String artistID) throws WebException;
    public Integer fetchUserSongRating(String artistID) throws WebException;
    public void addPlayAttention(String artistId) throws WebException;
    public void addNotInterestedAttention(String artistId) throws WebException;    
    public Set<String> fetchUserTagsForItem(String itemId) throws WebException;
    public void addUserTagsForItem(String itemId, Set<String> tag) throws WebException;
    public ArrayList<AttentionItem<ArtistCompact>> getLastRatedArtists(int count, boolean returnDistinct) throws WebException;
    public ArrayList<AttentionItem<ArtistCompact>> getLastTaggedArtists(int count, boolean returnDistinct) throws WebException;
    public ArrayList<AttentionItem<ArtistCompact>> getLastPlayedArtists(int count, boolean returnDistinct) throws WebException;
    public ItemInfo[] getSimilarTags(String tagId) throws WebException;
    public ArrayList<ArtistRecommendation> getRecommendations(String recTypeName, int cnt) throws WebException;
    public ServerInfoItem getServerInfo() throws WebException;

    public void triggerException() throws WebException;
}
