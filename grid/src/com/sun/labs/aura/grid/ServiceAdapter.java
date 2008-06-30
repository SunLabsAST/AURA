/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid;

import com.sun.caroline.platform.Grid;
import com.sun.caroline.platform.GridFactory;
import com.sun.caroline.platform.ProcessContext;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.logging.Logger;

/**
 * A service adapter class that does some of the required setup for services
 * that want to be deployed to the grid.
 */
public abstract class ServiceAdapter implements Configurable, AuraService {

    @ConfigString(defaultValue = "live")
    public static final String PROP_INSTANCE = "instance";

    protected String instance;
    
    protected Logger logger;

    protected Grid grid;
    
    protected GridUtil gu;
    
    /**
     * Gets the name of this service, suitable for placing in error messages.
     * @return the name of this service.
     */
    public abstract String serviceName();
    
    
    /**
     * Sets the common properties for a service to be deployed.  This includes 
     * the logger, the grid instance where the service is running, and the 
     * name of the instance that it should use when setting up resources.
     * 
     * Extenders of this class should call this new properties method in their
     * own!
     * 
     * @param ps the property sheet.
     * @throws com.sun.labs.util.props.PropertyException
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();

        //
        // Get our grid reference. If we're not on grid, then throw an exception.
        ProcessContext context = GridFactory.getProcessContext();
        if(context == null) {
            throw new PropertyException(ps.getInstanceName(),
                    PROP_INSTANCE, "Cannot run " + serviceName() + " off-grid");
        }
        grid = context.getGrid();
        instance = ps.getString(PROP_INSTANCE);
        try {
            gu = new GridUtil(grid, instance);
        } catch(Exception e) {
            throw new PropertyException("network", "network",
                    "Can't create network");
        }
    }

}
