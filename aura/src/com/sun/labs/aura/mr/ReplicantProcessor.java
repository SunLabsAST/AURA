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

package com.sun.labs.aura.mr;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An interface for classes that want to be distributed and process the data
 * in a replicant.  Implementors of this class that wish to be used in a
 * distributed system should implement <code>Serializable</code>.
 */
public interface ReplicantProcessor extends Component, Remote {
    
    /**
     * Processes a replicant, extracting whatever data is required and possibly
     * writing an output file that can be used later.
     * @param rep the replicant to process
     * @param store the data store of which the replicant is part.  This can be
     * used to add or modify data in the replicant
     * @param outputDir a directory where an output file may be written
     * @return an output file.  If no output file is created, <code>null</code>
     * may be returned.
     */
    public File process(Replicant rep, DataStore store, File outputDir) 
            throws AuraException, RemoteException;
}
