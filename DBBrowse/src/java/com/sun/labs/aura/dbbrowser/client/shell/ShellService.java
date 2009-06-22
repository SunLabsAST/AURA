/*
 * ShellService.java
 *
 * Created on June 18, 2009, 1:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.shell;
import com.google.gwt.user.client.rpc.RemoteService;

/**
 *
 * @author ja151348
 */
public interface ShellService extends RemoteService{
    public ShellResult runCommand(String cmd, String script);
}
