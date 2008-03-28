/*
 * DBServiceAsync.java
 *
 * Created on February 27, 2008, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 *
 * @author ja151348
 */
public interface DBServiceAsync {
    public void searchItemByKey(String key, AsyncCallback asyncCallback);

    public void searchItemByName(String key, AsyncCallback asyncCallback);
    
    public void searchItemByGen(String query, AsyncCallback asyncCallback);
    
    public void getAttentionForSource(String key, AsyncCallback asyncCallback);

    public void getAttentionForTarget(String key, AsyncCallback asyncCallback);
}
