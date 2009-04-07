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

package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.ComponentRegistry;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;

/**
 * A ProcessManager that waits for processes to be started manually and returns
 * once the processes have appeared.
 */
public class ManualProcessManager implements ProcessManager,
        Configurable,
        AuraService {
    
    protected ConfigurationManager cm;
    
    protected Logger logger;
    
    public ManualProcessManager() {
        
    }

    public PartitionCluster createPartitionCluster(DSBitSet prefix,
                                                   DSBitSet owner)
            throws AuraException, RemoteException {
        //
        // Enter a loop waiting for the requested partition to appear in
        // the service registry.
        PartitionCluster target = null;
        boolean found = false;
        int iter = 0;
        while (!found && iter < 12) {
            try {
                Thread.sleep(15 * 1000);
            } catch (InterruptedException e) {
            }
            iter++;
            logger.info("Checking for PC " + prefix);
            List<ServiceItem> svcs = getServices();
            for (ServiceItem svc : svcs) {
                if (svc.service instanceof PartitionCluster) {
                    PartitionCluster pc = (PartitionCluster)svc.service;
                    if (pc.getPrefix().equals(prefix)) {
                        target = pc;
                        found = true;
                    }
                }
            }
        }

        iter = 0;
        while (found && !target.isReady() && iter < 10) {
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
            }
            iter++;
        }
        return target;
    }

    public Replicant createReplicant(DSBitSet prefix,
                                     DSBitSet owner)
            throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void snapshot(DSBitSet prefix)
            throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void finishSplit(DSBitSet oldPrefix, DSBitSet childPrefix1,
            DSBitSet childPrefix2) {
        
    }
    
    public void newProperties(PropertySheet ps) throws PropertyException {
        cm = ps.getConfigurationManager();
        logger = ps.getLogger();
    }

    public void start() {
        
    }

    public void stop() {
        
    }
    
    protected List<ServiceItem> getServices() {
        ComponentRegistry cr = cm.getComponentRegistry();
        Map<ServiceRegistrar,List<ServiceItem>> reggies = cr.getJiniServices();
        ServiceRegistrar sr = reggies.keySet().iterator().next();
        return reggies.get(sr);
    }
}
