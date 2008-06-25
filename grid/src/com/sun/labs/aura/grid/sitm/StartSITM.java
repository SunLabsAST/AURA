/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.sitm;

import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.grid.GridUtil;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.UUID;
import java.util.logging.Level;

/**
 *
 */
public class StartSITM extends SITM {

    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";

    private DataStore ds;

    @ConfigInteger(defaultValue = 20)
    public static final String PROP_AURA_WAIT = "auraWait";

    private int auraWait;

    public void startSITMProcesses() throws Exception {
        NetworkAddress crawlerExt =
                GridUtil.getExternalAddressFor(grid, network, "crawlerExt");

        ProcessRegistration acReg =
                GridUtil.createProcess(grid, getArtistCrawlerName(),
                getArtistCrawlerConfig());
        UUID internal = acReg.getRegistrationConfiguration().
                getNetworkAddresses().iterator().next();
        GridUtil.createNAT(grid, instance, crawlerExt.getUUID(), internal,
                "artistCrawler");
        GridUtil.startRegistration(acReg);

        ProcessRegistration lcReg =
                GridUtil.createProcess(grid, getListenerCrawlerName(),
                getListenerCrawlerConfig());
        internal = lcReg.getRegistrationConfiguration().
                getNetworkAddresses().iterator().next();
        GridUtil.createNAT(grid, instance, crawlerExt.getUUID(), internal,
                "listenerCrawler");
        GridUtil.startRegistration(lcReg);

        ProcessRegistration tcReg =
                GridUtil.createProcess(grid, getArtistCrawlerName(),
                getArtistCrawlerConfig());
        internal = tcReg.getRegistrationConfiguration().
                getNetworkAddresses().iterator().next();
        GridUtil.createNAT(grid, instance, crawlerExt.getUUID(), internal,
                "tagCrawler");
        GridUtil.startRegistration(tcReg);
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        auraWait = ps.getInt(PROP_AURA_WAIT);
        ds = (DataStore) ps.getComponent(PROP_DATA_STORE);
    }

    public void start() {
        try {
            
            //
            // Wait until the data store is ready.
            int tries = 0;
            while(tries < auraWait) {
                if(ds.ready()) {
                    break;
                }
                Thread.sleep(2000);
                tries++;
            }
            startSITMProcesses();
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error starting Aardvark", ex);
        }
    }

    public void stop() {
    }
}
