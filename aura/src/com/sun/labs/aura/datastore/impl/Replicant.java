package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.aura.datastore.impl.store.LowLevelSearch;
import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * A replicant used for storing and retrieving data to/from disk in the
 * datastore.
 */
public interface Replicant extends ItemStore, LowLevelSearch, Component, Remote {
    public enum StatName {
        ATTEND,
        EXPLAIN_SIM,
        FIND_SIM,
        FIND_SIM_AUTOTAGS,
        GET_ALL,
        GET_ALL_ITR,
        GET_ATTN,
        GET_ATTN_CNT,
        GET_ATTN_ITR,
        GET_ATTN_SINCE,
        GET_ATTN_SINCE_CNT,
        GET_ATTN_SINCE_ITR,
        GET_AUTOTAGGED,
        GET_DV_KEY,
        GET_DV_CLOUD,
        GET_EXPLAIN,
        GET_ITEM,
        GET_ITEMS,
        GET_ITEMS_SINCE,
        GET_LAST_ATTN,
        GET_SCORED_ITEMS,
        GET_TOP_AUTOTAG_TERMS,
        GET_TOP_TERMS,
        GET_TOP_VALUES,
        GET_USER,
        NEW_ITEM,
        PUT_ITEM,
        UPDATE_ITEM,
        QUERY
    }

    /**
     * Get the prefix code for the objects in this partition cluster.
     * 
     * @return the prefix code
     */
    public DSBitSet getPrefix() throws RemoteException;

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

    /**
     * Gets a map describing the defined fields
     * 
     * @return the field descriptions in this replicant
     */
    public Map<String,FieldDescription> getFieldDescriptions()
            throws RemoteException;
}
