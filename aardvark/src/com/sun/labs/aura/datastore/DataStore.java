package com.sun.labs.aura.datastore;

import com.sun.labs.aura.datastore.impl.store.ItemStore;
import java.rmi.Remote;


/**
 * This interface represents access to the entire data in the data store.
 * It provides access to the database style ItemStore interface and the
 * search engine style ItemSearch interface.
 */

public interface DataStore extends ItemStore, Remote {

}
