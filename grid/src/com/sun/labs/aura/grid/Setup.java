/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid;

import com.sun.caroline.platform.Grid;
import com.sun.caroline.platform.GridFactory;
import com.sun.caroline.platform.ProcessContext;
import com.sun.caroline.platform.StorageManagementException;
import com.sun.labs.aura.AuraService;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sets up the basic grid infrastructure.
 */
public class Setup implements AuraService {

    @ConfigString(defaultValue = "live")
    public static final String PROP_INSTANCE = "instance";

    private String instance;

    Grid grid; 
    
    Logger logger;
    
    public void start() {
        try {
            //
            // sys.packages is where certain grid tools will live such as unzip
            // and the glassfish code
            GridUtil.getFS(grid, "sys.packages");
        } catch(RemoteException ex) {
            logger.severe("Error getting system packages: " + ex);
        } catch(StorageManagementException ex) {
            logger.severe("Error getting system packages: " + ex);
        }
        
        try {
        GridUtil.getFS(grid, instance + "-aura.dist");
        } catch(RemoteException ex) {
            logger.severe("Error getting code filesystem: " + ex);
        } catch(StorageManagementException ex) {
            logger.severe("Error getting code filesystem: " + ex);
        }
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();

        //
        // Get our grid reference. If we're not on grid, then throw an exception.
        ProcessContext context = GridFactory.getProcessContext();
        if(context == null) {
            throw new PropertyException(ps.getInstanceName(),
                    PROP_INSTANCE, "Cannot run AuraDeploy off-grid");
        }
        grid = context.getGrid();

        instance = ps.getString(PROP_INSTANCE);
    }
    
    public void stop() {
    }

}
