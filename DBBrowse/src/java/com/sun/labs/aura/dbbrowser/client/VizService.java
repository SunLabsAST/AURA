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

    /**
     * Tell the back end to refresh its list of services
     */
    public void refreshSvcs();
    
    /**
     * Gets all the DSH info
     * @gwt.typeArgs <com.sun.labs.aura.dbbrowser.client.DSHInfo>
     */
    public List getDSHInfo();
    
    /**
     * Gets all the PC info
     * @gwt.typeArgs <com.sun.labs.aura.dbbrowser.client.PCInfo>
     */
    public List getPCInfo();

    public void haltPC(PCInfo pc);
    
    /**
     * Cause a Partition Cluster to split itself into two pieces
     * @param pc
     */
    public void splitPC(PCInfo pc);
}
