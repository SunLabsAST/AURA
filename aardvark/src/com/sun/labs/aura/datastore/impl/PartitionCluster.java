package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.aura.datastore.impl.store.LowLevelSearch;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface to the Partition Cluster, used for RMI.
 */
public interface PartitionCluster extends ItemStore, LowLevelSearch, Component, Remote {

    /**
     * Get the prefix code for the objects in this partition cluster.
     * 
     * @return the prefix code
     */
    public DSBitSet getPrefix() throws RemoteException;
    
    /**
     * Add a replicant to this partition cluster
     * 
     * @param replicant the replicant to add
     */
    public void addReplicant(Replicant replicant) throws RemoteException;
}
