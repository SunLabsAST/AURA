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
 * A replicant used for storing and retrieving data to/from disk in the
 * datastore.
 */
public interface Replicant extends ItemStore, LowLevelSearch, Component, Remote {
    /**
     * Get the prefix code for the objects in this partition cluster.
     * 
     * @return the prefix code
     */
    public DSBitSet getPrefix() throws RemoteException;

    public List<Attention> getAttentionForSource(String srcKey,
                                                Attention.Type type)
            throws AuraException, RemoteException;
    
    /**
     * Delete the attention that is related to the given item either as a 
     * source or a target.  (Generally: isSrc should be true for users and
     * otherwise false)
     * 
     * @param itemKey the item key that we're removing attention for
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void removeAttention(String itemKey)
            throws AuraException, RemoteException;
    
    /**
     * Delete a set of attention by ID
     * 
     * @param ids a list of attention ids to delete
     */
    public void deleteAttention(List<Long> ids)
            throws AuraException, RemoteException;
    
    /**
     * Gets the on-disk size of the database component of the replicant in
     * bytes.
     * 
     * @return the size in bytes
     */
    public long getDBSize() throws RemoteException;
    
    /**
     * Gets the on-disk size of the index component of the replicant in bytes.
     * 
     * @return the size in bytes
     */
    public long getIndexSize() throws RemoteException;

}
