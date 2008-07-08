/*
 * MusicSearchInterface.java
 *
 * Created on March 3, 2007, 7:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.items.TagDetails;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.google.gwt.user.client.rpc.RemoteService;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author plamere
 */
public interface MusicSearchInterface extends RemoteService {
    public SearchResults artistSearch(String searchString, int maxResults) throws WebException;
    public SearchResults artistSearchByTag(String searchString, int maxResults) throws WebException;
    public SearchResults tagSearch(String searchString, int maxResults) throws WebException;
    public ArtistDetails getArtistDetails(String id, boolean refresh, String simTypeName) throws WebException ;
    public TagDetails getTagDetails(String id, boolean refresh, String simTypeName) throws WebException;
    public TagTree getTagTree();
    public ItemInfo[] getCommonTags(String artistID1, String artistID2, int num, String simType) throws WebException;
    public List<String> getArtistOracle() throws WebException;
    public List<String> getTagOracle() throws WebException;
    public ListenerDetails getUserTagCloud(String lastfmUser, String simTypeName) throws WebException;
    public ListenerDetails getLogInDetails() throws WebException;
    public ListenerDetails getNonOpenIdLogInDetails(String userKey) throws WebException;
    public void updateListener(ListenerDetails lD) throws WebException;
    public void updateUserSongRating(ListenerDetails lD, int rating, String artistID) throws WebException;
    public Integer fetchUserSongRating(ListenerDetails lD, String artistID) throws WebException;
    public Map<String,Integer> fetchUserSongRating(ListenerDetails lD, Set<String> artistID) throws WebException;
    public void terminateSession();
    public Map<String, String> getSimTypes() throws WebException;
    public ItemInfo[] getDistinctiveTags(String artistID, int count) throws WebException;
    public ArtistCompact[] getSteerableRecommendations(Map<String, Double> tagMap) throws WebException;
}
