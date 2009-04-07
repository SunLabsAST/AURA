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

package com.sun.labs.aura.grid.ec2;

import com.sun.labs.aura.grid.ServiceStarter;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the service starter interface.
 */
public class ServiceStarterImpl implements ServiceStarter, Configurable {

    /**
     * The directory where the Aura grid distribution lives.
     */
    @ConfigString(defaultValue="/aura-dist")
    public static final String DIST_DIR = "distDir";

    private String distDir;

    @ConfigString(defaultValue="/data")
    public static final String DATA_DIR = "dataDir";

    private String dataDir;

    private Logger logger;

    private List<Process> processes;
    
    @Override
    public boolean start(String configFile, String starter) throws RemoteException {
        ProcessBuilder pb = new ProcessBuilder(new String[] {
           "java",
           "-jar",
           "/dist/grid.jar"
        });
        try {
            Process p = pb.start();
            processes.add(p);
            return true;
        } catch(IOException ex) {
            logger.log(Level.SEVERE, "Error starting process", ex);
            return false;
        }

    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
