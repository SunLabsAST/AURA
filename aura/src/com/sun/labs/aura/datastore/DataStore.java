package com.sun.labs.aura.datastore;

import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.store.ItemSearch;
import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * This interface represents access to the entire data in the data store.
 * It provides access to the database style ItemStore interface and the
 * search engine style ItemSearch interface.
 */

public interface DataStore extends ItemStore, ItemSearch, Component, Remote {

    public void registerPartitionCluster(PartitionCluster pc)
            throws RemoteException;
}
