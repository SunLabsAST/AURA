/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid;

import com.sun.caroline.platform.ConflictingNetworkSettingException;
import com.sun.caroline.platform.DuplicateNameException;
import com.sun.caroline.platform.Grid;
import com.sun.caroline.platform.GridFactory;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.PostgreSQLConfiguration;
import com.sun.caroline.platform.ProcessContext;
import com.sun.caroline.platform.StaticNatConfiguration;
import com.sun.labs.aura.AuraService;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class CreateDatabase implements Configurable, AuraService {

    private Grid grid;
    
    private Logger logger; 
    
    @ConfigString(defaultValue = "live")
    public static final String PROP_INSTANCE = "instance";

    private String instance;
    
    @ConfigString
    public static final String PROP_DB_NAME = "dbName";
    
    private String dbName;

    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        //
        // Get our grid reference. If we're not on grid, then throw an exception.
        ProcessContext context = GridFactory.getProcessContext();
        if(context == null) {
            throw new PropertyException(ps.getInstanceName(),
                    PROP_INSTANCE, "Cannot run CreateDatabase off-grid");
        }
        grid = context.getGrid();
        instance = ps.getString(PROP_INSTANCE);
        dbName = ps.getString(PROP_DB_NAME);
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
        
        NetworkAddress external;
        try {

            external =
                    GridUtil.getExternalAddressFor(grid, network,
                    dbName + "DBNat");
        } catch(Exception ex) {
            logger.severe("Error getting external address for " + dbName + " " + ex);
            return;
        }
        
        StaticNatConfiguration dbNat = new StaticNatConfiguration(internal.getUUID(), external.getUUID());
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
