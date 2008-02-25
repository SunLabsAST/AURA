package com.sun.labs.aura.datastore;

import com.sun.labs.aura.AuraServiceStarter;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for an all-in-one-VM data store.
 */
public class DataStoreTest {

    AuraServiceStarter starter;
    DataStore store;
    
    public DataStoreTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        try {
            //
            // Start up the services in the configuration and then get our
            // data store.
            ConfigurationManager cm =
                    new ConfigurationManager(getClass().
                    getResource("/com/sun/labs/aura/resource/fullConfig.xml"));
            starter = (AuraServiceStarter) cm.lookup("starter");
            store = (DataStore) cm.lookup("dataStoreHead");
        } catch(IOException ex) {
            Logger.getLogger(DataStoreTest.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch(PropertyException ex) {
            Logger.getLogger(DataStoreTest.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        
    }

    @After
    public void tearDown() {
        starter.stopServices();
    }

    @Test
    public void testAdd() throws AuraException, RemoteException {
        Item x = StoreFactory.newItem(ItemType.FEED, "http://blogs.sun.com/searchguy", "Steve's blog");
        store.putItem(x);
        Item y = store.getItem("http://blogs.sun.com/searchguy");
        assertEquals(x.getKey(), y.getKey());
        assertEquals(x.getName(), y.getName());
    }
}