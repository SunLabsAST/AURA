/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import com.sun.labs.aura.datastore.impl.DataStoreHead;

/**
 *
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class DataStoreFactory {
    //static final String CONFIG_FILE = "SinglePathStoreConfig.xml";
    static final String CONFIG_FILE = "dataStoreHeadRetreiveConfig.xml";
    static final String DATASTORE_KEY ="dataStoreHead";
    static final String STARTER_KEY ="starter";

    public static URL configURI;
    protected static Map<DataStore,AuraServiceStarter> starters =
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