/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid;

import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.UUID;
import java.util.logging.Level;

/**
 *
 */
public class StartAardvark extends Aardvark {

    @Override
    public String serviceName() {
        return "StartAardvark";
    }

    public void startAardvarkProcesses() throws Exception {
        //
        // Start the Feed Scheduler
        NetworkAddress crawlerNat = GridUtil.getExternalAddressFor(grid, network, "feedMgrNat");
        ProcessRegistration feedSchedReg =
                GridUtil.createProcess(grid, getSchedName(), getFeedSchedulerConfig());

        UUID internal = feedSchedReg.getRegistrationConfiguration().
                getNetworkAddresses().iterator().next();
        GridUtil.createNAT(grid, instance, crawlerNat.getUUID(), internal, "feedSched");
        GridUtil.startRegistration(feedSchedReg);

        //
        // Start a few feed crawlers
        for(int i = 0; i < numCrawlers; i++) {
            ProcessConfiguration feedMgrConfig = getFeedManagerConfig(i);
            ProcessRegistration feedMgrReg = GridUtil.createProcess(grid, getFMName(i),
                    feedMgrConfig);

            //
            // Make a dynamic NAT for this process config
            ProcessConfiguration pc = feedMgrConfig;
            internal = pc.getNetworkAddresses().iterator().next();
            GridUtil.createNAT(grid, instance, crawlerNat.getUUID(), internal, "feedMgr-" + i);
            GridUtil.startRegistration(feedMgrReg, false);
        }

        //
        // Create a recommendation manager
        ProcessRegistration recReg = GridUtil.createProcess(grid, getRecName(),
                getRecommenderConfig());
        GridUtil.startRegistration(recReg);

        //
        // And now make an Aardvark
        ProcessRegistration aardvarkReg = GridUtil.createProcess(grid, getAAName(),
                getAardvarkConfig());
        GridUtil.startRegistration(aardvarkReg);
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        numCrawlers = ps.getInt(PROP_NUM_CRAWLERS);
    }

    public void start() {
        try {
            startAardvarkProcesses();
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error starting Aardvark", ex);
        }
    }

    public void stop() {
    }
}
