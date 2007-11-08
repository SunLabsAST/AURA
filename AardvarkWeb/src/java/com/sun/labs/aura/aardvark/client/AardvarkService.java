/*
 * AardvarkService.java
 *
 * Created on November 5, 2007, 1:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.client;
import com.google.gwt.user.client.rpc.RemoteService;

/**
 * The client/server interface for the aardvark web client
 */
public interface AardvarkService extends RemoteService{
    public WiStats getStats();
    public WiUserStatus registerUser(String name, String feed);
    public WiUserStatus loginUser(String name);
    public WiEntrySummary[] getRecommendations(String name);
}
