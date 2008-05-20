/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.datastore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import com.sun.labs.util.props.ConfigurationManager;
import java.util.logging.Logger;

import com.sun.labs.aura.TestUtilities;
import com.sun.labs.aura.AuraServiceStarter;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.StoreFactory;

/**
 * Creates a simple DataStore adds a user to it and then removes that user.
 * 
 * @author Will Holcomb <will.holcomb@sun.com>
 */
public class DataStoreTestBase {
    static final String CONFIG_FILE = "SinglePathStoreConfig.xml";
    static final String DATASTORE_KEY ="dataStoreHead";
    static final String STARTER_KEY ="starter";
    
    Logger log;
    ConfigurationManager configMgr;
    File testDirectory;
    AuraServiceStarter starter;
    
    public DataStoreTestBase() {
        log = TestUtilities.getLogger(getClass());
    }

    /**
     * DataStores are not loaded directly, rather they are instantiated using
     * a ConfigurationManager.
     */
    @Before
    public void loadConfig() throws IOException {
        // Aura needs a location to put its temporary files
        testDirectory = TestUtilities.createTempDir("datastore_test");
        String dirname = testDirectory.getCanonicalPath();
        log.info("Test temp directory: " + dirname);
        
        URL configURI = getClass().getResource(CONFIG_FILE);
        assertNotNull(configURI);
        
        log.info("Loading configuration: " + configURI);
        configMgr = new ConfigurationManager(configURI);
        
        // Minion uses a separate ConfigurationManager, so it is necessary to
        // set a global property in order to affect minion.
        System.setProperty("auraHome", dirname);
        
        // In order to start each of the services, they need to be loaded by the
        // AuraServiceStarter through the ConfigurationManager
        starter = (AuraServiceStarter)configMgr.lookup("starter");
    }

    @After
    public void tearDown() {
        log.info("Shutting down aura services");
        starter.stopServices();
        
        log.info("Removing test directory: " + testDirectory);
        TestUtilities.delTree(testDirectory);
    }
}