package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.BaseFileSystem;
import com.sun.caroline.platform.BaseFileSystemConfiguration;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RunState;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigInteger;
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

        //
        // Now, start partition clusters for each prefix
        ProcessRegistration lastReg = null;
        for(String prefix : repFSMap.keySet()) {
            ProcessRegistration pcReg = gu.createProcess( getPartitionName(
                    prefix),
                    debugRMI ? 
                        getPartitionClusterDebugConfig(prefix) : 
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
            ProcessRegistration repReg = gu.createProcess( getReplicantName(
                    prefix), getReplicantConfig(replicantConfig,
                    prefix));
            gu.startRegistration(repReg, false);
            lastReg = repReg;
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
