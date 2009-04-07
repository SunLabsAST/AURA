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
