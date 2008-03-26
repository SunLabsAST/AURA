package com.sun.labs.aura.datastore.impl.store;

import com.sun.kt.search.DocumentVector;
import com.sun.kt.search.FieldFrequency;
import com.sun.kt.search.ResultsFilter;
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
     * Gets the most frequent values for the named field.
     * @param field the field for which we want the most frequent values
     * @param n the number of most-frequent values to return
     * @param ignoreCase if <code>true</code>, ignore the case of string values
     * when computing the frequencies
     * @return a list of the most frequent values and their associated frequencies.
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<FieldFrequency> getTopValues(String field, int n,
            boolean ignoreCase) throws AuraException, RemoteException;

    /**
     * Runs a query against the map data and returns the top n results.
     * @param query the query to run
     * @param n the number of results to return
     * @return the top results for the query, sorted by score
     */
    public List<Scored<Item>> query(String query, int n, ResultsFilter rf)
            throws AuraException, RemoteException;

    /**
     * Runs a query against the map data and returns the top n results.
     * @param query the query to run
     * @param sort the sorting specification to use to sort the results
     * @param n the number of results to return
     * @return the top results for the query
     */
    public List<Scored<Item>> query(String query, String sort, int n, ResultsFilter rf)
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
    public List<Scored<Item>> findSimilar(DocumentVector dv, int n, ResultsFilter rf)
            throws AuraException, RemoteException;
}
