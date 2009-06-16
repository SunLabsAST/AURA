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

package com.sun.labs.aura.grid.sitm;

import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RunState;
import com.sun.labs.aura.datastore.DataStore;
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

        ProcessRegistration ccReg =
                gu.createProcess(getCrawlerControllerName(),
                getCrawlerControllerConfig());
        UUID internal = ccReg.getRegistrationConfiguration().
                getNetworkAddresses().iterator().next();
        gu.createNAT(crawlerExt.getUUID(), internal,
                "crawlerController");
        gu.startRegistration(ccReg);

        ProcessRegistration acReg =
                gu.createProcess(getArtistCrawlerName(),
                getArtistCrawlerConfig());
        internal = acReg.getRegistrationConfiguration().
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
