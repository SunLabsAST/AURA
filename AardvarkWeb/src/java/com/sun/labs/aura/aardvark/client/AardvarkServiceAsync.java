/*
 * AardvarkServiceAsync.java
 *
 * Created on November 5, 2007, 1:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.client;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * The async version fo the AardvarkService. This is plumbing required by GWT
 */
public interface AardvarkServiceAsync {
    public void getStats(AsyncCallback callback);
    public void registerUser(String name, String feed, AsyncCallback callback);
    public void loginUser(String name, AsyncCallback callback);
    public void getRecommendations(String name, AsyncCallback callback);
    public void getFeeds(String name, AsyncCallback callback);
}
