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

/**
 *
 * @author plamere
 */
public interface MusicSearchInterfaceAsync {
       public void artistSearch(String searchString, int maxResults, AsyncCallback callback) throws Exception;
       public void artistSearchByTag(String searchString, int maxResults, AsyncCallback callback) throws Exception; 
       public void tagSearch(String searchString, int maxResults, AsyncCallback callback) throws Exception; 
       public void getArtistDetails(String id, boolean refresh, String simTypeName, AsyncCallback callback) throws Exception;
       public void getTagDetails(String id, boolean refresh, String simTypeName, AsyncCallback callback) throws Exception;
       public void getTagTree(AsyncCallback callback);
       public void getCommonTags(String artistID1, String artistID2, int num, String simType, AsyncCallback callback) throws Exception;
       public void getArtistOracle(AsyncCallback callback) throws Exception;
       public void getTagOracle(AsyncCallback callback) throws Exception;
       public void getLogInDetails(AsyncCallback callback) throws Exception;
       public void getUserTagCloud(String lastfmUser, String simTypeName, AsyncCallback callback) throws Exception;
       public void getSimTypes(AsyncCallback callback) throws Exception;
}
