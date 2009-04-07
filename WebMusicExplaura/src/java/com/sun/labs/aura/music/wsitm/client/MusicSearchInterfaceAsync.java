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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ArtistRecommendation;
import com.sun.labs.aura.music.wsitm.client.items.AttentionItem;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
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
public interface MusicSearchInterfaceAsync {

    public void getRandomPopularArtists(int nbr, AsyncCallback<ArtistCompact[]> callback) throws WebException;
    public void addSearchAttention(String userKey, String target, searchTypes sT, String searchStr, AsyncCallback callback) throws WebException;
    public void artistSearch(String searchString, int maxResults, AsyncCallback callback) throws WebException;
    public void artistSearchByTag(String searchString, int maxResults, AsyncCallback callback) throws WebException;
    public void tagSearch(String searchString, int maxResults, AsyncCallback callback) throws WebException;
    public void getArtistDetails(String id, boolean refresh, String simTypeName, String popularity, AsyncCallback callback) throws WebException;
    public void getSimilarArtists(String id, String simTypeName, String popularity, AsyncCallback<ArrayList<ScoredC<ArtistCompact>>> callback) throws WebException;
    public void getTagDetails(String id, boolean refresh, String simTypeName, AsyncCallback callback) throws WebException;
    public void getCommonTags(String artistID1, String artistID2, int num, String simType, AsyncCallback callback) throws WebException;
    public void getComboTagCloud(String artistID1, String artistID2, int num, String simType, AsyncCallback callback) throws WebException;
    public void getCommonTags(Map<String, ScoredTag> tagMap, String artistID, int num, AsyncCallback callback) throws WebException;
    public void getRepresentativeArtistsOfTag(String tagId, AsyncCallback<ArrayList<ScoredC<ArtistCompact>>> callback) throws WebException;
    public void getArtistOracle(AsyncCallback<ArrayList<ScoredC<String>>> callback) throws WebException;
    public void getTagOracle(AsyncCallback<ArrayList<ScoredC<String>>> callback) throws WebException;
    public void getLogInDetails(AsyncCallback callback) throws WebException;
    public void getNonOpenIdLogInDetails(String userKey, AsyncCallback callback) throws WebException;
    public void updateListener(ListenerDetails lD, AsyncCallback callback) throws WebException;
    public void updateUserSongRating(int rating, String artistID, AsyncCallback callback) throws WebException;
    public void fetchUserSongRating(String artistID, AsyncCallback callback) throws WebException;
    public void fetchUserSongRating(HashSet<String> artistID, AsyncCallback<HashMap<String, Integer>> callback) throws WebException;
    public void terminateSession(AsyncCallback callback);
    public void getSimTypes(AsyncCallback<HashMap<String, String>> callback) throws WebException;
    public void getArtistRecommendationTypes(AsyncCallback<HashMap<String, String>> callback);
    public void getDistinctiveTags(String artistID, int count, AsyncCallback callback) throws WebException;
    public void getSteerableRecommendations(Map<String, ScoredTag> tagMap, String popularity, AsyncCallback<ArrayList<ScoredC<ArtistCompact>>> callback) throws WebException;
    public void getRecommendationsFromString(String tagQuery, AsyncCallback<RecsNTagsContainer> callback) throws WebException;
    public void addUserTagsForItem(String itemId, Set<String> tag, AsyncCallback callback) throws WebException;
    public void addPlayAttention(String artistId, AsyncCallback callback) throws WebException;
    public void addNotInterestedAttention(String artistId, AsyncCallback callback) throws WebException;
    public void fetchUserTagsForItem(String itemId, AsyncCallback<HashSet<String>> callback) throws WebException;
    public void getArtistCompact(String artistId, AsyncCallback callback) throws WebException;
    public void getLastRatedArtists(int count, boolean returnDistinct, AsyncCallback<ArrayList<AttentionItem<ArtistCompact>>> callback) throws WebException;
    public void getLastTaggedArtists(int count, boolean returnDistinct, AsyncCallback<ArrayList<AttentionItem<ArtistCompact>>> callback) throws WebException;
    public void getLastPlayedArtists(int count, boolean returnDistinct, AsyncCallback<ArrayList<AttentionItem<ArtistCompact>>> callback) throws WebException;
    public void getSimilarTags(String tagId, AsyncCallback<ItemInfo[]> callback) throws WebException;
    public void getRecommendations(String recTypeName, int cnt, AsyncCallback<ArrayList<ArtistRecommendation>> callback) throws WebException;
    public void getServerInfo(AsyncCallback<ServerInfoItem> callback) throws WebException;

    public void triggerException(AsyncCallback callback) throws WebException;
}
