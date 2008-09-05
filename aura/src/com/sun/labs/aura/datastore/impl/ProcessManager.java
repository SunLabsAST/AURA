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
     * Creates a fully functional new partition cluster.  The cluster will
     * already have its replicant[s] associated with it and will be
     * returned once the process has started and is ready.
     * 
     * @param prefix the hash code prefix for the new partition
     * @return the fully created partition cluster
     * @throws AuraException if the cluster exists or could not be created
     */
    public PartitionCluster createPartitionCluster(DSBitSet prefix)
            throws AuraException, RemoteException;

    /**
     * Creates a fully functional new replicant.  The replicant will be
     * returned once the process has started and is ready.  It will be for
     * use within a cluster with the given prefix.
     * 
     * @param prefix the prefix of the cluster this replicant will be used with
     * @return the fully created replicant.
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public Replicant createReplicant(DSBitSet prefix)
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