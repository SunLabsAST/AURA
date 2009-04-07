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

package com.sun.labs.aura.grid.util;

import com.sun.labs.aura.grid.*;
import com.sun.caroline.platform.ConflictingNetworkSettingException;
import com.sun.caroline.platform.DuplicateNameException;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.PostgreSQLConfiguration;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.logging.Level;

/**
 *
 */
public class CreateDatabase extends ServiceAdapter {

    @ConfigString
    public static final String PROP_DB_NAME = "dbName";
    
    private String dbName;

    public String serviceName() {
        return "CreateDatabase";
    }
    
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
    }

    public void start() {
        Network network;
        try {
            network = grid.getNetwork(instance + "-auraNet");
        } catch(RemoteException ex) {
            logger.severe("Error getting network " + instance + "-auraNet");
            return;
        }
        if(network == null) {
            logger.severe("No aura network");
            return;
        }
        
        NetworkAddress internal;

        try {
            internal = gu.getAddressFor(dbName);
        } catch(Exception ex) {
            logger.severe("Error getting internal address for " + dbName + " " + ex);
            return;
        }
        
        PostgreSQLConfiguration psqlConfig = new PostgreSQLConfiguration(
                internal.getUUID(),
                null);
        
        try {
            grid.createPostgreSQLDatabase(instance + "-" + dbName, psqlConfig);
        } catch(RemoteException ex) {
            logger.severe("Error creating database: " + dbName);
        } catch(DuplicateNameException ex) {
        } catch(ConflictingNetworkSettingException ex) {
            logger.log(Level.SEVERE, "Error setting up network for database " + dbName, ex);
        }
    }

    public void stop() {
    }

}
