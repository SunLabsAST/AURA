package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.impl.store.ItemStore;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A replicant used for storing and retrieving data to/from disk in the
 * datastore.
 */
public interface Replicant extends ItemStore, Remote {
    /**
     * Get the prefix code for the objects in this partition cluster.
     * 
     * @return the prefix code
     */
    public DSBitSet getPrefix() throws RemoteException;
}
