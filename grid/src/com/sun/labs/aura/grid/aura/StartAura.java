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

package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.BaseFileSystem;
import com.sun.caroline.platform.BaseFileSystemConfiguration;
import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RunState;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.Map;
import java.util.logging.Level;

/**
 * An on-grid class to deploy Aura.
 */
public class StartAura extends Aura {
    
    @ConfigBoolean(defaultValue=false)
    public static final String PROP_DEBUG_RMI = "debugRMI";
    private boolean debugRMI;

    @ConfigInteger(defaultValue=4)
    public static final String PROP_NUM_HEADS = "numHeads";

    private int numHeads;

    @ConfigBoolean(defaultValue=false)
    public static final String PROP_COMBINED_REPLICANT = "combinedReplicant";

    private boolean combinedReplicant;

    @ConfigString(defaultValue="")
    public static final String PROP_COLLECTOR_PREFIX = "collectorPrefix";

    private String collectorPrefix;

    public String serviceName() {
        return "StartAura";
    }
    
    /**
     * Assuming a fully built infrastructure, this uses the network and
     * file systems already there to start up the aura processes
     * 
     * @throws java.lang.Exception
     */
    public void createAuraProcesses() throws Exception {
        //
        // Get a reggie started up first thing, if one isn't already running.
        ProcessRegistration regReg = gu.createProcess(getReggieName(),
                getReggieConfig());
        gu.startRegistration(regReg);
        
        //
        // A stat service
        ProcessRegistration statSrvReg = gu.createProcess(
                getStatServiceName(),
                getStatServiceConfig());
        gu.startRegistration(statSrvReg);

        //
        // A login service
        ProcessRegistration loginSrvReg = gu.createProcess(
                getLoginServiceName(),
                getLoginServiceConfig());
        gu.startRegistration(loginSrvReg);

        //
        // Start up a process manager.
        ProcessRegistration pmReg = gu.createProcess(getProcessManagerName(),
                getProcessManagerConfig());
        gu.startRegistration(pmReg);
        while(pmReg.getRunState() != RunState.RUNNING) {
            pmReg.waitForStateChange(100000);
        }
        Thread.sleep(1000);
        
        //
        // Next, get some data store heads and start them
        for(int i = 0; i < numHeads; i++) {
            ProcessRegistration dsHeadReg = gu.createProcess(
                    getDataStoreHeadName(i),
                    debugRMI ? getDataStoreHeadDebugConfig(i) : getDataStoreHeadConfig(i));
            gu.startRegistration(dsHeadReg);
        }

        ProcessRegistration lastReg = null;
        if(combinedReplicant) {
            //
            // Start the combinde replicant/clusters for each prefix
            for(String prefix : repFSMap.keySet()) {
                ProcessRegistration repReg;
                repReg = gu.createProcess(getReplicantName(
                        prefix), getPCReplicantConfig(prefix));
                gu.startRegistration(repReg, false);
                lastReg = repReg;
            }

        } else {
            //
            // Now, start partition clusters for each prefix
            for(String prefix : repFSMap.keySet()) {
                ProcessRegistration pcReg =
                        gu.createProcess(getPartitionName(
                        prefix),
                        debugRMI ? getPartitionClusterDebugConfig(prefix) :
                             getPartitionClusterConfig(prefix));
                gu.startRegistration(pcReg, false);
                lastReg = pcReg;
            }

            while(lastReg.getRunState() != RunState.RUNNING) {
                lastReg.waitForStateChange(1000000L);
            }

            //
            // Start the replicants for each prefix
            for(String prefix : repFSMap.keySet()) {
                ProcessConfiguration pc;
                if(prefix.equals(collectorPrefix)) {
                    pc = getReplicantCollectConfig(prefix);
                } else if(debugRMI) {
                    pc = getDebugReplicantConfig(replicantConfig, prefix);
                } else {
                    pc = getReplicantConfig(replicantConfig, prefix);
                }
                ProcessRegistration repReg =
                        gu.createProcess(getReplicantName(prefix), pc);
                NetworkAddress extaddr = gu.getExternalAddressFor(
                        getReplicantName(prefix) + "-ext",
                        getReplicantName(prefix));
                NetworkAddress intaddr = gu.getAddressFor(getReplicantName(prefix));
                gu.createNAT(extaddr.getUUID(), intaddr.getUUID(), getReplicantName(prefix) + "-nat");
                gu.startRegistration(repReg, false);
                lastReg = repReg;
            }
        }

        while(lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }

        //
        // Start the partition clusters for prefixes that were in a splitting
        // state.
        for(String prefix : ownedFSMap.keySet()) {
            ProcessConfiguration pc = null;
            //
            // Determine the owner of this partition in order to re-pair
            // for splitting
            BaseFileSystem fs = (BaseFileSystem)ownedFSMap.get(prefix);
            BaseFileSystemConfiguration conf = fs.getConfiguration();
            Map<String,String> md = conf.getMetadata();
            String owner = md.get("owner");
            
            ProcessRegistration pcReg = gu.createProcess( getPartitionName(
                    prefix),
                    getPartitionClusterConfig(prefix, false, owner));
            gu.startRegistration(pcReg, false);
            lastReg = pcReg;
        }
        
        while(lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }

        //
        // Start the replicants for each prefix in the owned map
        for(String prefix : ownedFSMap.keySet()) {
            ProcessRegistration repReg = gu.createProcess( getReplicantName(
                    prefix), getReplicantConfig(replicantConfig,
                    prefix));
            gu.startRegistration(repReg, false);
            lastReg = repReg;
        }

        while(lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }

    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        debugRMI = ps.getBoolean(PROP_DEBUG_RMI);
        numHeads = ps.getInt(PROP_NUM_HEADS);
        combinedReplicant = ps.getBoolean(PROP_COMBINED_REPLICANT);
        collectorPrefix = ps.getString(PROP_COLLECTOR_PREFIX);
    }
    
    public void start() {
        try {
            if(repFSMap.size() == 0) {
                //
                // If there's no file systems, then create them!
                createReplicantFileSystems();
            }
            createLoginSvcFileSystem(instance);
            createAuraProcesses();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting Aura", e);
        }
    }

    public void stop() {
    }
}
