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

package com.sun.labs.aura.grid;

import com.sun.caroline.platform.Grid;
import com.sun.caroline.platform.GridFactory;
import com.sun.caroline.platform.ProcessContext;
import com.sun.caroline.util.GridFinder;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
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
    
    @ConfigBoolean(defaultValue=true)
    public static final String PROP_ON_GRID = "onGrid";

    protected boolean onGrid;
    
    protected Logger logger;

    protected Grid grid;
    
    protected GridUtil gu;
    
    protected ConfigurationManager cm;

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
        cm = ps.getConfigurationManager();
        onGrid = ps.getBoolean(PROP_ON_GRID);
        instance = ps.getString(PROP_INSTANCE);

        //
        // Get our grid reference. If we're not on grid, and we're supposed to be,
        // then throw an exception.
        ProcessContext context = GridFactory.getProcessContext();
        if(context == null) {
            if(onGrid) {
                throw new PropertyException(ps.getInstanceName(),
                        PROP_INSTANCE, "Cannot run " + serviceName() + " off-grid");
            }
        }
        
        //
        // If we're on grid, get the grid reference.
        if(context != null) {
            grid = context.getGrid();
        } else {
            try {
                //
                // Can we get a grid from a local config?
                GridFinder gf = new GridFinder(null);
                grid = gf.findGrid(0);
            } catch(Exception ex) {
                grid = null;
            }
        }

        if(grid != null) {
            try {
                gu = new GridUtil(grid, instance);
            } catch(Exception e) {
                throw new PropertyException(e, "grid", "grid",
                        "Can't get grid");
            }
        }
    }

}
