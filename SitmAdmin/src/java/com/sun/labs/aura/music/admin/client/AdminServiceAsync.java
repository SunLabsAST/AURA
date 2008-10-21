/*
 * AdminServiceAsync.java
 *
 * Created on February 27, 2008, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.admin.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Map;

/**
 *
 */
public interface AdminServiceAsync {
    public void getStatistics(AsyncCallback asyncCallback);
    public void addArtist(java.lang.String mbaid, AsyncCallback asyncCallback);
    public void addListener(java.lang.String userKey, AsyncCallback asyncCallback);
    public void addApplication(java.lang.String applicationID, AsyncCallback asyncCallback);
    public void getTests(boolean shortTests, AsyncCallback asyncCallback);
    public void runTest(String test, AsyncCallback asyncCallback);
    public void getWorkerDescriptions(AsyncCallback asyncCallback);
    public void runWorker(String name, Map<String, String> params, AsyncCallback asyncCallback);
}
