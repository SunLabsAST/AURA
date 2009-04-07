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

package com.sun.labs.aura.grid.rp;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * An abstract base class for something that wants to process the data in a replicant.
 * The newProperties method can be used to set up the data store and the 
 * replicant to be processed.
 * 
 * <p>
 * 
 * Replicant processors will be started with a file system mounted at
 * /files/out where they can write a keyed output file.  The data store processor
 * responsible for orchestrating the replicant processors will merge the various
 * output files when all of the replicants in the data store have been processed.
 * 
 * @see com.sun.labs.aura.util.io.KeyedOutputStream
 * 
 */
public abstract class ReplicantProcessor implements AuraService, Configurable {

    /**
     * The data store from which we'll be processing a replicant.
     */
    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";

    protected DataStore dataStore;

    /**
     * The prefix of the replicant that we want to process.
     */
    @ConfigString
    public static final String PROP_PREFIX = "prefix";

    protected String prefix;

    protected Replicant replicant;
    
    protected Logger logger;
    
    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        prefix = ps.getString(PROP_PREFIX);
        try {
            replicant = dataStore.getReplicant(prefix);
        } catch(RemoteException ex) {
            throw new PropertyException(ex, ps.getInstanceName(), PROP_PREFIX,
                    "Unable to find replicant for prefix " + prefix);
        }
        if(replicant == null) {
            throw new PropertyException(ps.getInstanceName(), PROP_PREFIX,
                    "Unable to find replicant for prefix " + prefix);
        }
    }
}
