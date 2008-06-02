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
import com.sun.labs.aura.datastore.util.DataStoreFactory;
import com.sun.labs.aura.AuraService;

/**
 * Creates a simple DataStore and initializes it. Because JUnit tests are
 * designed to be atomic and the datastore should, at times, be shared across
 * multiple tests, the initialization is done statically.
 * 
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class DataStoreTestBase {
    static final String CONFIG_FILE = "SinglePathStoreConfig.xml";
    static final String DATASTORE_KEY ="dataStoreHead";
    static final String STARTER_KEY ="starter";
    
    Logger log;
    private DataStore dataStore;
    File testDirectory;
    AuraServiceStarter starter;
    
    public DataStoreTestBase() {
        log = TestUtilities.getLogger(DataStoreTestBase.class);
    }
    
    /**
     * DataStores are not loaded directly, rather they are instantiated using
     * a ConfigurationManager.
     */
    @Before
    public void loadConfig() throws IOException, InterruptedException {
        // Aura needs a location to put its temporary files
        testDirectory = TestUtilities.createTempDir("datastore_test");
        String dirname = testDirectory.getCanonicalPath();
        log.info("Test temp directory: " + dirname);
        dataStore = DataStoreFactory.getSimpleDataStore(dirname);
    }

    public DataStore getDataStore() {
        return dataStore;
    }
    
    @After
    public void removeDataStore() {
        // Aura will shut itself down. There is no way to tell it it do so
        // without either keeping a reference to the service starter or closing
        // the java vm.
        DataStoreFactory.shutdownDataStore(dataStore);
        
        log.info("Removing test directory: " + testDirectory);
        TestUtilities.delTree(testDirectory);
    }
}