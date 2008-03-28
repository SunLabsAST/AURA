
package com.sun.labs.aura.datastore.impl.store;

import com.sun.kt.search.WeightedField;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.rmi.RemoteException;
import java.util.SortedSet;

/**
 * ItemSearch describes the search methods available for use in the data store.
 */
public interface ItemSearch {
    /**
     * Finds a the n most similar items to the given item.
     * @param key the item that we want to find similar items for
     * @param n the number of similar items to return
     * @return the set of items most similar to the given item, ordered by 
     * similarity to the given item.  The similarity of the items is based on 
     * all of the indexed text associated with the item in the data store.
     */
    public SortedSet<Scored<Item>> findSimilar(String key, int n)
            throws AuraException, RemoteException;

    /**
     * Finds the n most-similar items to the given item, based on the data in the 
     * provided field.
     * @param key the item for which we want similar items
     * @param field the name of the field that should be used to find similar
     * items
     * @param n the number of similar items to return
     * @return the set of items most similar to the given item, based on the 
     * data indexed into the given field.  Note that the returned set may be
     * smaller than the number of items requested!
     */
    public SortedSet<Scored<Item>> findSimilar(String key, String field, int n)
            throws AuraException, RemoteException;
    
    /**
     * Finds the n most-similar items to the given items, based on a combination
     * of the data held in the provided fields.
     * @param key the item for which we want similar items
     * @param fields the fields (and associated weights) that we should use to 
     * compute the similarity between items.
     * @param n the number of similar items to return
     * @return the set of items most similar to the given item, based on the data
     * in the provided fields.   Note that the returned set may be
     * smaller than the number of items requested!
     */
    public SortedSet<Scored<Item>> findSimilar(String key, WeightedField[] fields, int n)
            throws AuraException, RemoteException;

}