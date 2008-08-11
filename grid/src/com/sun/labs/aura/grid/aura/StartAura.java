package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RunState;
import com.sun.labs.util.props.ConfigBoolean;
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
        // Start up a process manager.
        ProcessRegistration pmReg = gu.createProcess(getProcessManagerName(), getProcessManagerConfig());
        gu.startRegistration(pmReg);

        //
        // Next, get a data store head and start it
        ProcessRegistration dsHeadReg = gu.createProcess(
                getDataStoreHeadName(1),
                debugRMI ? 
                    getDataStoreHeadDebugConfig(1) : 
                    getDataStoreHeadConfig());
        gu.startRegistration(dsHeadReg);

        //
        // Now, start partition clusters for each prefix
        ProcessRegistration lastReg = null;
        for(int i = 0; i < prefixCodeList.length; i++) {
            ProcessRegistration pcReg = gu.createProcess( getPartitionName(
                    prefixCodeList[i]),
                    debugRMI ? 
                        getPartitionClusterDebugConfig(prefixCodeList[i]) : 
                        getPartitionClusterConfig(prefixCodeList[i]));
            gu.startRegistration(pcReg, false);
            lastReg = pcReg;
        }

        while(lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }

        //
        // Start the replicants for each prefix
        for(int i = 0; i < prefixCodeList.length; i++) {
            ProcessRegistration repReg = gu.createProcess( getReplicantName(
                    prefixCodeList[i]), getReplicantConfig(replicantConfig,
                    prefixCodeList[i]));
            gu.startRegistration(repReg, false);
            lastReg = repReg;
        }

        while(lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }

        //
        // And finally start a stat service
        ProcessRegistration statSrvReg = gu.createProcess(
                getStatServiceName(),
                getStatServiceConfig());
        gu.startRegistration(statSrvReg);

    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        debugRMI = ps.getBoolean(PROP_DEBUG_RMI);
    }
    
    public void start() {
        try {
            getReplicantFileSystems();
            createAuraProcesses();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting Aura", e);
        }
    }

    public void stop() {
    }
}
