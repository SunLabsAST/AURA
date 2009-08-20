/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.dbbrowser.client.viz;
import com.google.gwt.user.client.rpc.RemoteService;
import java.util.List;
import java.util.Map;

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
     * Gets the cpu load percentage for each process
     */
    public Map<String,Double> getCPULoads();

    /**
     * Gets the stats for all registered web servers
     */
    public Map<String,Double> getWebStats();

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
    
    /**
     * Gets the current log level as a string for a specific replicant
     * 
     * @param prefix which replicant to get the value from
     * @return
     */
    public String getLogLevel(String prefix);

    /**
     * Sets the log level
     * 
     * @param prefix which replicant to set, or null to set all replicants
     * @param logLevel
     */
    public boolean setLogLevel(String prefix, String logLevel);
    
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
