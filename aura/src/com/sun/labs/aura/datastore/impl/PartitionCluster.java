package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.aura.datastore.impl.store.LowLevelSearch;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

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

    public List<Attention> getAttentionForSource(String srcKey,
                                                Attention.Type type)
            throws AuraException, RemoteException;

    /**
     * Delete the attention that is related to the given item either as a 
     * source or a target.
     * 
     * @param itemKey the item key that we're removing attention for
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void deleteAttention(String itemKey)
            throws AuraException, RemoteException;
    
}
