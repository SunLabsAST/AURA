/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.cluster.Cluster;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Counted;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.query.Element;
import java.rmi.RemoteException;
import java.util.List;

/**
 * ItemSearch describes the search methods available for use in the data store.
 */
public interface ItemSearch {

    /**
     * Clusters a set of items into k clusters based on the data in the given
     * field.
     * @param keys the keys of the items to cluster
     * @param field the field holding the data that we'll cluster on
     * @param k the number of clusters to return
     * @return a list of <code>k</code> or fewer clusters
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Cluster> cluster(List<String> keys, String field, int k) throws AuraException, RemoteException;

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
     * Finds a the n most similar items to the given item.
     * @param key the item that we want to find similar items for
     * @param config the configuration to use for this find similar operation
     * @return a list of items most similar to the given item, in order from most
     * to least similar.  The similarity of the items is based on 
     * all of the indexed text associated with the item in the data store.
     */
    public List<Scored<Item>> findSimilar(String key, SimilarityConfig config)
            throws AuraException, RemoteException;

    /**
     * Finds a the n most similar items to the given items.
     * @param keys the items that we want to find similar items for
     * @param config the configuration to use for this find similar operation
     * @return a list of items most similar to the given item, in order from most
     * to least similar.  The similarity of the items is based on 
     * all of the indexed text associated with the item in the data store.
     */
    public List<Scored<Item>> findSimilar(List<String> keys, SimilarityConfig config)
            throws AuraException, RemoteException;

    /**
     * Finds a the n most similar items to the given items.
     * @param cloud a word cloud for which we want to find similar items
     * @param config the configuration to use for this find similar operation
     * @return a list of items most similar to the given item, in order from most
     * to least similar.  The similarity of the items is based on 
     * all of the indexed text associated with the item in the data store.
     */
    public List<Scored<Item>> findSimilar(WordCloud cloud, SimilarityConfig config)
            throws AuraException, RemoteException;

    /**
     * Runs a query against the map data and returns the top n results.
     * @param query the query to run
     * @param n the number of results to return
     * @param rf a (possibly <code>null</code>) filter to apply to the results
     * retrieved from the data store
     * @return the top results for the query, orderd by score
     */
    public List<Scored<Item>> query(String query, int n, ResultsFilter rf)
            throws AuraException, RemoteException;

    /**
     * Runs a query against the map data and returns the top n results.
     * @param query the query to run
     * @param sort the sorting specification to use to sort the results
     * @param n the number of results to return
     * @param rf a (possibly <code>null</code>) filter to apply to the results
     * retrieved from the data store
     * @return the top results for the query, orderd by the given sort
     * criteria.
     */
    public List<Scored<Item>> query(String query, String sort, int n,
            ResultsFilter rf)
            throws AuraException, RemoteException;

    /**
     * Runs a query against the data and returns the top n results
     * @param query the query to run, expressed using the Minion query API
     * @param n the number of results to return
     * @param rf a (possibly <code>null</code>) filter to apply to the results
     * retrieved from the data store
     * @return
     */
    public List<Scored<Item>> query(Element query, int n,
            ResultsFilter rf)
            throws AuraException, RemoteException;

    /**
     * Runs a query against the map data and returns the top n results.
     * @param query the query to run, expressed using the Minion query API
     * @param sort the sorting specification to use to sort the results
     * @param n the number of results to return
     * @param rf a (possibly <code>null</code>) filter to apply to the results
     * retrieved from the data store
     * @return the top results for the query, orderd by the given sort
     * criteria.
     */
    public List<Scored<Item>> query(Element query, String sort, int n,
            ResultsFilter rf)
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
    public WordCloud getTopTerms(String key, String field, int n)
            throws AuraException, RemoteException;


    /**
     * Gets the terms with the highest occurrence counts for a particular field in
     * a particular document.  This can be used to fetch raw occurrence data
     * for terms from the data store.
     * @param key the key for which we want the data
     * @param field the field that we want occurrence counts for
     * @param n the number of terms to return.  If this value is less than zero,
     * all terms and their counts will be returned.
     * @return the list of the top <code>n</code> terms and their counts.
     * The list will be returned in decreasing count order.
     * @throws AuraException
     * @throws RemoteException
     */
    public List<Counted<String>> getTopTermCounts(String key, String field, int n)
            throws AuraException, RemoteException;

    /**
     * Gets the counts for a given term in a given field across all of the
     * items in the collection.
     * @param term the term to look for.
     * @param field the field of interest
     * @param n the number of counts to return.  If this value is less than zero,
     * all counts will be returned.
     * @return A list of counted strings.  The item in the counted string will
     * be the key of a document where the term occurs, and the count will be the
     * number of times that the term occurs in that document.
     * @throws AuraException
     * @throws RemoteException
     */
    public List<Counted<String>> getTermCounts(String term, String field, int n, ResultsFilter rf)
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
     * Explains the similarity between two items.  The explaination consists of
     * the terms that the documents have in common, along with a score indicating
     * the importance of terms to the similarity.
     * @param key1 the key of the first item
     * @param key2 the key of the second item
     * @param config the similarity configuration that details how the explanation
     * should be built
     * @return a list of scored strings.  The string is the term that the two
     * items have in common and the score is the contributio of this term to the
     * similarity between the two items.
     */
    public List<Scored<String>> explainSimilarity(String key1, String key2,
            SimilarityConfig config) 
            throws AuraException, RemoteException;
    
    /**
     * Explains the similarity between a word cloud and an item.  The explaination consists of
     * the terms that the documents have in common, along with a score indicating
     * the importance of terms to the similarity.
     * @param cloud the word cloud
     * @param key the key of the item
     * @param config the similarity configuration that details how the explanation
     * should be built
     * @return a list of scored strings.  The string is the term that the two
     * items have in common and the score is the contributio of this term to the
     * similarity between the two items.
     */
    public List<Scored<String>> explainSimilarity(WordCloud cloud, String key,
            SimilarityConfig config) 
            throws AuraException, RemoteException;
    
    /**
     * Explains the similarity between two word clouds.  The explaination consists of
     * the terms that the documents have in common, along with a score indicating
     * the importance of terms to the similarity.
     * @param cloud1 the first word cloud
     * @param cloud2 the second word cloud
     * @param config the similarity configuration that details how the explanation
     * should be built
     * @return a list of scored strings.  The string is the term that the two
     * items have in common and the score is the contributio of this term to the
     * similarity between the two items.
     */
    public List<Scored<String>> explainSimilarity(WordCloud cloud1, WordCloud cloud2,
            SimilarityConfig config) 
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
    /**
     * Find the top n most similar autotags to the given autotag.
     * @param autotag the tag we want similar tags for
     * @param n the number of tags to return
     * @return the n most similar autotags
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Scored<String>> findSimilarAutotags(String autotag, int n)
            throws AuraException, RemoteException;

    /**
     * Explains the similarity between two autotags as a set of weighted
     * terms that have the highest contribution to the similarity.
     * @param a1 the first autotag
     * @param a2 the second autotag
     * @param n the number of most important terms to return
     * @return a list of the most important terms contributing to the similarity
     * between the autotags.
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Scored<String>> explainSimilarAutotags(String a1, String a2,
            int n)
            throws AuraException, RemoteException;
}
