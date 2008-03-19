package com.sun.labs.aura.datastore.impl.store;

import com.sun.kt.search.DocumentVector;
import com.sun.kt.search.WeightedField;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * An interface for the search methods avaiable for partitions and replicants.
 * These methods are lower-level than those for the data store level.
 * 
 * @see ItemSearch
 */
public interface LowLevelSearch extends Remote {

    /**
     * Runs a query against the map data and returns the top n results.
     * @param query the query to run
     * @param n the number of results to return
     * @return the top results for the query, sorted by score
     */
    public List<Scored<Item>> query(String query, int n)
            throws AuraException, RemoteException;

    /**
     * Runs a query against the map data and returns the top n results.
     * @param query the query to run
     * @param sort the sorting specification to use to sort the results
     * @param n the number of results to return
     * @return the top results for the query
     */
    public List<Scored<Item>> query(String query, String sort, int n)
            throws AuraException, RemoteException;

    public DocumentVector getDocumentVector(String key)
            throws AuraException, RemoteException;
    
    public DocumentVector getDocumentVector(String key, String field)
            throws AuraException, RemoteException;
    
    public DocumentVector getDocumentVector(String key, WeightedField[] fields)
            throws AuraException, RemoteException;
    
    /**
     * Finds a the n most similar items to the given vector.
     * @param dv the vector for the item that we want to find similar items to
     * @param n the number of similar items to return
     * @return the set of items most similar to the given item, ordered by 
     * similarity to the given item.  The similarity of the items is based on 
     * all of the indexed text associated with the item in the data store.
     */
    public List<Scored<Item>> findSimilar(DocumentVector dv, int n)
            throws AuraException, RemoteException;
}
