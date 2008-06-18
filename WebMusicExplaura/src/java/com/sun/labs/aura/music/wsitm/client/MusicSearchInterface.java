/*
 * MusicSearchInterface.java
 *
 * Created on March 3, 2007, 7:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.sun.labs.aura.music.wsitm.client.items.TagDetails;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.google.gwt.user.client.rpc.RemoteService;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public interface MusicSearchInterface extends RemoteService {
    public SearchResults artistSearch(String searchString, int maxResults) throws Exception;
    public SearchResults artistSearchByTag(String searchString, int maxResults) throws Exception;
    public SearchResults tagSearch(String searchString, int maxResults) throws Exception;
    public ArtistDetails getArtistDetails(String id, boolean refresh, String simTypeName) throws Exception ;
    public TagDetails getTagDetails(String id, boolean refresh, String simTypeName) throws Exception;
    public TagTree getTagTree();
    public ItemInfo[] getCommonTags(String artistID1, String artistID2, int num, String simType) throws Exception;
    public List<String> getArtistOracle() throws Exception;
    public List<String> getTagOracle() throws Exception;
    public logInDetails getUserTagCloud(String lastfmUser, String simTypeName) throws Exception;
    public logInDetails getLogInDetails() throws Exception;
    public Map<String, String> getSimTypes() throws Exception;
}
