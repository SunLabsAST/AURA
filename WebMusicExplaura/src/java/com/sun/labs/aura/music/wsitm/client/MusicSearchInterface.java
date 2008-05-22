/*
 * MusicSearchInterface.java
 *
 * Created on March 3, 2007, 7:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 *
 * @author plamere
 */
public interface MusicSearchInterface extends RemoteService {
    public SearchResults artistSearch(String searchString, int maxResults);
    public SearchResults artistSearchByTag(String searchString, int maxResults);
    public SearchResults tagSearch(String searchString, int maxResults);
    public ArtistDetails getArtistDetails(String id, boolean refresh);
    public TagDetails getTagDetails(String id, boolean refresh);
    public TagTree getTagTree();
    public ItemInfo[] getCommonTags(String artistID1, String artistID2, int num) ;
}
