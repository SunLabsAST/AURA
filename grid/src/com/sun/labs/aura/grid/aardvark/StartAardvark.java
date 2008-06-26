/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.aardvark;

import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RunState;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.UUID;
import java.util.logging.Level;

/**
 *
 */
public class StartAardvark extends Aardvark {

    @ConfigComponent(type=com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";
    private DataStore ds;
    
    @ConfigInteger(defaultValue=20)
    public static final String PROP_AURA_WAIT = "auraWait";
    
    private int auraWait;
    
    @Override
    public String serviceName() {
        return "StartAardvark";
    }

    public void startAardvarkProcesses() throws Exception {
        //
        // Start the Feed Scheduler
        NetworkAddress crawlerNat = gu.getExternalAddressFor("feedMgrNat");
        ProcessRegistration feedSchedReg =
                gu.createProcess(getSchedName(), getFeedSchedulerConfig());

        UUID internal = feedSchedReg.getRegistrationConfiguration().
                getNetworkAddresses().iterator().next();
        gu.createNAT(crawlerNat.getUUID(), internal, "feedSched");
        gu.startRegistration(feedSchedReg);

        //
        // Start a few feed crawlers
        ProcessRegistration lastReg = null;
        for(int i = 0; i < numCrawlers; i++) {
            ProcessConfiguration feedMgrConfig = getFeedManagerConfig(i);
            ProcessRegistration feedMgrReg = gu.createProcess( getFMName(i),
                    feedMgrConfig);

            //
            // Make a dynamic NAT for this process config
            internal = feedMgrConfig.getNetworkAddresses().iterator().next();
            gu.createNAT(crawlerNat.getUUID(), internal, "feedMgr-" + i);
            gu.startRegistration(feedMgrReg, false);
            lastReg = feedMgrReg;
        }

        while (lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }
        
        //
        // Create a recommendation manager
        ProcessRegistration recReg = gu.createProcess( getRecName(),
                getRecommenderConfig());
        gu.startRegistration(recReg);

        //
        // And now make an Aardvark
        ProcessRegistration aardvarkReg = gu.createProcess( getAAName(),
                getAardvarkConfig());
        gu.startRegistration(aardvarkReg);
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        auraWait = ps.getInt(PROP_AURA_WAIT);
        ds = (DataStore) ps.getComponent(PROP_DATA_STORE);
    }

    public void start() {
        try {
            int tries = 0;
            while(tries < auraWait) {
                if(ds.ready()) {
                    break;
                }
                Thread.sleep(2000);
                tries++;
            }
            startAardvarkProcesses();
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error starting Aardvark", ex);
        }
    }

    public void stop() {
    }
}
