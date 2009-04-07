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

package com.sun.labs.aura.datastore.util;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import com.sun.labs.util.props.ConfigurationManager;
import java.util.logging.Logger;

import com.sun.labs.aura.AuraServiceStarter;
import com.sun.labs.aura.datastore.DataStore;

/**
 *
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class DataStoreFactory {
    //static final String CONFIG_FILE = "SinglePathStoreConfig.xml";
    static final String CONFIG_FILE = "dataStoreHeadRetreiveConfig.xml";
    static final String DATASTORE_KEY ="dataStoreHead";
    static final String STARTER_KEY ="starter";

    public final static URL configURI;
    protected final static Map<DataStore,AuraServiceStarter> starters =
            new HashMap<DataStore,AuraServiceStarter>();
    
    static {
        configURI = DataStoreFactory.class.getResource(CONFIG_FILE);
    }
    
    public DataStoreFactory() {
    }
    
    /**
     * DataStores are not loaded directly, rather they are instantiated using
     * a ConfigurationManager.
     */
    public static DataStore getSimpleDataStore(String datadir) throws IOException {
        Logger log = Logger.getLogger(DataStoreFactory.class.getName());

        if(configURI == null) {
            throw new IllegalStateException("Missing configuration: " + CONFIG_FILE);
        }
        
        // Minion uses a separate ConfigurationManager, so it is necessary to
        // set a global property in order to affect minion. This needs to be set
        // before the configuration manager is loaded.
        if(System.getProperty("auraHome") == null) {
            System.setProperty("auraHome", datadir);
        }
        log.info("Using 'auraHome' = '" + System.getProperty("auraHome") + "'");
        
        log.info("Loading configuration: " + configURI);
        ConfigurationManager configMgr = new ConfigurationManager(configURI);
        
        DataStore dataStore = (DataStore)configMgr.lookup(DATASTORE_KEY);
        log.info("Loaded data store: " + dataStore);

        // In order to start each of the services, they need to be loaded by the
        // AuraServiceStarter through the ConfigurationManager
        if(configMgr.getComponentNames().contains(STARTER_KEY)) {
            AuraServiceStarter starter = (AuraServiceStarter)configMgr.lookup(STARTER_KEY);
            starters.put(dataStore, starter);
        }
        
        return dataStore;
    }
    
    public static void shutdownDataStore(DataStore dataStore) {
        if(starters.containsKey(dataStore)) {
            starters.get(dataStore).stopServices();
        }
    }
}
