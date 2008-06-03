/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.datastore;

import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import static org.junit.Assert.*;

import java.util.logging.Logger;
import com.sun.labs.aura.TestUtilities;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.StoreFactory;


/**
 * 
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class UserManagementTest extends DataStoreTestBase {
    final static String userKey = "Test Key";

    Logger log; 
    
    public UserManagementTest() {
        log = TestUtilities.getLogger(getClass());
    }
    
    @Test
    public void manageUsers() throws AuraException, RemoteException {
        DataStore dataStore = super.getDataStore();
        String userName = "Test User";
        
        User user = StoreFactory.newUser(userKey, userName);
        dataStore.putUser(user);
        log.info("Added User: " + user.getName() + " (" + user.getKey() + ")");
        
        User retreivedUser = dataStore.getUser(userKey);
        assertEquals(user, retreivedUser);
        
        dataStore.deleteUser(userKey);
        
        retreivedUser = dataStore.getUser(userKey);
        assertNull(retreivedUser);

        dataStore.deleteUser(userKey);
    }
}