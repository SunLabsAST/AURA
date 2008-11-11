/*
 * VizService.java
 *
 * Created on July 18, 2008, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;
import com.google.gwt.user.client.rpc.RemoteService;
import java.util.EnumSet;
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
     */
    public List<DSHInfo> getDSHInfo();
    
    /**
     * Gets all the PC info
     */
    public List<PCInfo> getPCInfo();

    /**
     * Gets stats about a replicant
     */
    public RepStats getRepStats(String prefix);
    
    /**
     * Reset the stats for a replicant
     */
    public void resetRepStats(String prefix);
    
    /**
     * Gets a list of all the available log names for methods in the replicant
     * 
     * @return the list of names
     */
    public List<String> getRepLogNames();
    
    /**
     * Gets the list of log names that are currently being logged
     * @param prefix which replicant to check
     * @return
     */
    public List<String> getRepSelectedLogNames(String prefix);
    
    /**
     * Sets the list of log names that should be logged
     * @param prefix which replicant to set, or null to set all replicants
     * @param selected
     */
    public void setRepSelectedLogNames(String prefix, List<String> selected);
    
    public void haltPC(PCInfo pc);
    
    /**
     * Cause a Partition Cluster to split itself into two pieces
     * @param pc
     */
    public void splitPC(PCInfo pc);
    
    /**
     * Shuts down the whole datastore
     */
    public void shutDown();
}
