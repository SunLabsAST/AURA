/*
 * VizService.java
 *
 * Created on July 18, 2008, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;
import com.google.gwt.user.client.rpc.RemoteService;
import java.util.List;

/**
 * Provides visualization data for the Viz UI.
 */
public interface VizService extends RemoteService{
    public String dump();
    
    public List getDSHInfo();
    
    public List getPCInfo();

    public void haltPC(PCInfo pc);
}
