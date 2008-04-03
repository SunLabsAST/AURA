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

    /**
     * Gets the highest weighted terms in a given item's aura
     * @param key the key of the document for which we want the terms
     * @param field the field from which the terms should be drawn.  A value
     * of <code>null</code> will pull terms from all fields
     * @param n the number of highest weighted terms to return
     * @return a list of the top weighted terms in the aura of the document.  Note
     * that there may be fewer than <code>n</code> terms returned.
     */
    public List<Scored<String>> getTopTerms(String key, String field, int n)
            throws AuraException, RemoteException;

    /**
     * Gets an explanation as to why a given autotag would be applied to 
     * a given document.
     * 
     * @param key the key of th item for which we want an explanation
     * @param autoTag the autotag that we want to explain
     * @param n the number of terms to return
     * @return a list of the terms that contribute the most towards the
     * autotagging.  The score associated with a term is the proportion of 
     * contribution towards the autotagging.
     */
    public List<Scored<String>> getExplanation(String key, String autoTag,
            int n)
            throws AuraException, RemoteException;
    
    /**
     * Gets the items that have had a given autotag applied to them.
     * @param autotag the tag that we want items to have been assigned
     * @param n the number of items that we want
     * @return a list of the items that have had a given autotag applied.  The
     * list is ordered by the confidence of the tag assignment
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Scored<Item>> getAutotagged(String autotag, int n)
            throws AuraException, RemoteException;

    /**
     * Gets the top terms in the classifier that is used to assign a given 
     * autotag.
     * @param autotag the tag whose terms we want
     * @param n the number of terms to return
     * @return
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Scored<String>> getTopAutotagTerms(String autotag, int n)
            throws AuraException, RemoteException;
}
