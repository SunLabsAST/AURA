package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.aura.datastore.impl.store.LowLevelSearch;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

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

    public Set<Attention> getAttentionForSource(String srcKey,
                                                Attention.Type type)
            throws AuraException, RemoteException;

}
