/*
 * MusicSearchInterfaceAsync.java
 *
 * Created on March 3, 2007, 7:45 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sun.labs.aura.music.wsitm.client.items.ArtistRecommendation;
import com.sun.labs.aura.music.wsitm.client.items.AttentionItem;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.items.ServerInfoItem;
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
       public void artistSearch(String searchString, int maxResults, AsyncCallback callback) throws WebException;
       public void artistSearchByTag(String searchString, int maxResults, AsyncCallback callback) throws WebException;
       public void tagSearch(String searchString, int maxResults, AsyncCallback callback) throws WebException;
       public void getArtistDetails(String id, boolean refresh, String simTypeName, AsyncCallback callback) throws WebException;
       public void getTagDetails(String id, boolean refresh, String simTypeName, AsyncCallback callback) throws WebException;
       public void getCommonTags(String artistID1, String artistID2, int num, String simType, AsyncCallback callback) throws WebException;
       public void getCommonTags(Map<String, Double> tagMap, String artistID, int num, AsyncCallback callback) throws WebException;
       public void getArtistOracle(AsyncCallback<ArrayList<String>> callback) throws WebException;
       public void getTagOracle(AsyncCallback<ArrayList<String>> callback) throws WebException;
       public void getLogInDetails(AsyncCallback callback) throws WebException;
       public void getNonOpenIdLogInDetails(String userKey, AsyncCallback callback) throws WebException;
       public void updateListener(ListenerDetails lD, AsyncCallback callback) throws WebException;
       public void updateUserSongRating(int rating, String artistID, AsyncCallback callback) throws WebException;
       public void fetchUserSongRating(String artistID, AsyncCallback callback) throws WebException;
       public void fetchUserSongRating(Set<String> artistID, AsyncCallback<HashMap<String,Integer>> callback) throws WebException;
       public void terminateSession(AsyncCallback callback);
       public void getSimTypes(AsyncCallback<HashMap<String, String>> callback) throws WebException;
       public void getArtistRecommendationTypes(AsyncCallback<HashMap<String, String>> callback);
       public void getDistinctiveTags(String artistID, int count, AsyncCallback callback) throws WebException;
       public void getSteerableRecommendations(Map<String, Double> tagMap, AsyncCallback callback) throws WebException;
       public void addUserTagsForItem(String itemId, Set<String> tag, AsyncCallback callback) throws WebException;
       public void addPlayAttention(String artistId, AsyncCallback callback) throws WebException;
       public void addNotInterestedAttention(String artistId, AsyncCallback callback) throws WebException;
       public void fetchUserTagsForItem(String itemId, AsyncCallback<HashSet<String>> callback) throws WebException;
       public void getArtistCompact(String artistId, AsyncCallback callback) throws WebException;
       public void getLastRatedArtists(int count, AsyncCallback<ArrayList<AttentionItem>> callback) throws WebException;
       public void getLastTaggedArtists(int count, AsyncCallback<ArrayList<AttentionItem>> callback) throws WebException;
       public void getLastPlayedArtists(int count, AsyncCallback<ArrayList<AttentionItem>> callback) throws WebException;
       public void getSimilarTags(String tagId, AsyncCallback<ItemInfo[]> callback) throws WebException;
       public void getRecommendations(String recTypeName, int cnt, AsyncCallback<ArrayList<ArtistRecommendation>> callback) throws WebException;
       public void getServerInfo(AsyncCallback<ServerInfoItem> callback) throws WebException;
}
