/*
 * ShellServiceAsync.java
 *
 * Created on June 18, 2009, 1:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.shell;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 *
 * @author ja151348
 */
public interface ShellServiceAsync {
    public void runCommand(String cmd, String script, AsyncCallback callback);
}
