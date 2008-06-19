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
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;

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
       public void getTagTree(AsyncCallback callback);
       public void getCommonTags(String artistID1, String artistID2, int num, String simType, AsyncCallback callback) throws WebException;
       public void getArtistOracle(AsyncCallback callback) throws WebException;
       public void getTagOracle(AsyncCallback callback) throws WebException;
       public void getLogInDetails(AsyncCallback callback) throws WebException;
       public void getUserTagCloud(String lastfmUser, String simTypeName, AsyncCallback callback) throws WebException;
       public void updateListener(ListenerDetails lD, AsyncCallback callback) throws WebException;
       public void getSimTypes(AsyncCallback callback) throws WebException;
}
