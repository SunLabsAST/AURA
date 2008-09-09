package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RunState;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
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
        // Get a reggie started up first thing
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
        // Start up a process manager.
        ProcessRegistration pmReg = gu.createProcess(getProcessManagerName(),
                getProcessManagerConfig());
        gu.startRegistration(pmReg);
        pmReg.waitForStateChange(100000);
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
            createAuraProcesses();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting Aura", e);
        }
    }

    public void stop() {
    }
}
