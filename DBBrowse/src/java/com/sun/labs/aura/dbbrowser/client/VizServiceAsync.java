/*
 * VizServiceAsync.java
 *
 * Created on July 18, 2008, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * The async interface for the Viz service
 */
public interface VizServiceAsync {
    public void dump(AsyncCallback callback);

    public void getDSHInfo(AsyncCallback asyncCallback);

    public void getPCInfo(AsyncCallback asyncCallback);
}
