/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.sitm;

import com.sun.caroline.platform.NetworkAddress;
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
public class StartSITM extends SITM {

    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";

    private DataStore ds;

    @ConfigInteger(defaultValue = 20)
    public static final String PROP_AURA_WAIT = "auraWait";

    private int auraWait;

    public void startSITMProcesses() throws Exception {
        NetworkAddress crawlerExt =
                gu.getExternalAddressFor("sitmCrawlerExt");

        ProcessRegistration acReg =
                gu.createProcess(getArtistCrawlerName(),
                getArtistCrawlerConfig());
        UUID internal = acReg.getRegistrationConfiguration().
                getNetworkAddresses().iterator().next();
        gu.createNAT(crawlerExt.getUUID(), internal,
                "artistCrawler");
        gu.startRegistration(acReg);

        ProcessRegistration lcReg =
                gu.createProcess(getListenerCrawlerName(),
                getListenerCrawlerConfig());
        internal = lcReg.getRegistrationConfiguration().
                getNetworkAddresses().iterator().next();
        gu.createNAT(crawlerExt.getUUID(), internal,
                "listenerCrawler");
        gu.startRegistration(lcReg);

        ProcessRegistration tcReg =
                gu.createProcess(getTagCrawlerName(),
                getTagCrawlerConfig());
        logger.info("tcReg: " + tcReg);
        internal = tcReg.getRegistrationConfiguration().
                getNetworkAddresses().iterator().next();
        gu.createNAT(crawlerExt.getUUID(), internal,
                "tagCrawler");
        gu.startRegistration(tcReg);
        while(tcReg.getRunState() != RunState.RUNNING) {
            tcReg.waitForStateChange(1000000L);
        }
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
            logger.log(Level.SEVERE, "Error starting SITM", ex);
        }
    }

    public void stop() {
    }
}
