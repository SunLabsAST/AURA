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

import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A class that handles the creation of processes for the various components
 * of the data store.
 */
public interface ProcessManager extends Component, Remote {
    /**
     * Names all all the stats a ProcessManager can generate (may be for other
     * processes).
     */
    public enum StatName {
        PERCENT_CPU,
    }

    /**
     * Creates a fully functional new partition cluster.  The cluster will
     * already have its replicant[s] associated with it and will be
     * returned once the process has started and is ready.
     * 
     * @param prefix the hash code prefix for the new partition
     * @param owner prefix of the partition managing this new partition
     * @return the fully created partition cluster
     * @throws AuraException if the cluster exists or could not be created
     */
    public PartitionCluster createPartitionCluster(DSBitSet prefix,
                                                   DSBitSet owner)
            throws AuraException, RemoteException;

    /**
     * Creates a fully functional new replicant.  The replicant will be
     * returned once the process has started and is ready.  It will be for
     * use within a cluster with the given prefix.
     * 
     * @param prefix the prefix of the cluster this replicant will be used with
     * @param owner prefix of the partition managing the partition of this replicant
     * @return the fully created replicant.
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public Replicant createReplicant(DSBitSet prefix,
                                     DSBitSet owner)
            throws AuraException, RemoteException;

    /**
     * Inidicates to the process manager that a split of a given prefix into two
     * new child prefixes is completed.
     * 
     * @param oldPrefix the prefix of the partition cluster that was split
     * @param childPrefix1 the prefix of the first new child partition cluster.
     * This will be the new prefix for the data stored in the old partition cluster.
     * @param childPrefix2 the prefix of the second new child partition cluster
     */
    public void finishSplit(DSBitSet oldPrefix, DSBitSet childPrefix1,
            DSBitSet childPrefix2)
            throws AuraException, RemoteException;

    /**
     * Takes a snapshot of a replicant's data.
     * @param prefix the prefix of the replicant for which we want a snapshot.
     */
    public void snapshot(DSBitSet prefix)
            throws AuraException, RemoteException;
}
