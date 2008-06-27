/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
            internal =
                    GridUtil.getAddressFor(grid, network, dbName);
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
