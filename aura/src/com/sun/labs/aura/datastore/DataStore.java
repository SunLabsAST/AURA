package com.sun.labs.aura.datastore;

import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.datastore.impl.store.ItemSearch;
import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


/**
 * This interface represents access to the entire data in the data store.
 * It provides access to the database style ItemStore interface and the
 * search engine style ItemSearch interface.
 */

public interface DataStore extends ItemStore, ItemSearch, Component, Remote {
    
    /**
     * Indicates that the data store is ready to begin operation.  This will be
     * true when all of the partition clusters have their replicants available.
     * @return <code>true</code> if the data store is ready to begin processing 
     * data, <code>false</code> otherwise.
     * @throws java.rmi.RemoteException
     */
    public boolean ready() throws RemoteException;

    public void registerPartitionCluster(PartitionCluster pc)
            throws RemoteException;
    
    public Replicant getReplicant(String prefix)
            throws RemoteException;
    
    public List<String> getPrefixes()
            throws RemoteException;
}
