package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.aura.datastore.impl.store.LowLevelSearch;
import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

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
    
    /**
     * Gets a replicant managed by this cluster.
     * @return one of this cluster's replicants
     * @throws java.rmi.RemoteException
     */
    public Replicant getReplicant() throws RemoteException;

    /**
     * Returns true if this replicant is ready to be used.
     * @return true if ready
     */
    public boolean isReady() throws RemoteException;
    
    /**
     * Gets a map describing the defined fields
     * 
     * @return the field descriptions in this partition
     */
    public Map<String,FieldDescription> getFieldDescriptions()
            throws RemoteException;

    /**
     * Splits the data in this partition cluster.  A new partition cluster
     * is created and data is migrated to that cluster.  Once all data
     * has been moved, the data store heads are told of the new configuration.
     * 
     * @throws AuraException if an errors occurs or the current state does not
     * allow a split (the split is already happening)
     */
    public void split() throws AuraException, RemoteException;
    
    /**
     * Resumes a split operation in the event of a failure.  This method should
     * be called on the parent in the split -- that is, it should be called
     * on the partition that is part of the active data store tree.  The
     * partition that is passed in should be the child, not registered in
     * the tree.
     * 
     * @param remote the partition being split into
     * @throws AuraException if an error occurs
     */
    public void resumeSplit(PartitionCluster remote) throws AuraException, RemoteException;
    
    /**
     * Delete the attention that is related to the given item either as a 
     * source or a target.
     * 
     * @param itemKey the item key that we're removing attention for
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void removeAttention(String itemKey)
            throws AuraException, RemoteException;
        
}
