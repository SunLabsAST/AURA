package com.sun.labs.aura.datastore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;



/**
 *
 * @author Will Holcomb
 */
public class CreateUserTest extends DataStoreTestBase {

    public CreateUserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }
    
    @Test
    public void addUser() throws AuraException, RemoteException {
        DataStore dataStore = (DataStore)configMgr.lookup(DATASTORE_KEY);
        String userKey = "Test Key";
        
        User user = StoreFactory.newUser(userKey, "Test User");
        dataStore.putUser(user);
        log.info("Added User: " + user.getName() + " (" + user.getKey() + ")");
        
        User retreivedUser = dataStore.getUser(userKey);
        assertEquals(user, retreivedUser);
    }
}