
package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.ResultsFilter;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Interface for a PartitionCluster's implementation strategy.  A strategy
 * determines how the PartitionCluster will carry out each method that is
 * called on it.
 */
public interface PCStrategy {
    /**
     * Define a field to the search engine
     */
    public void defineField(ItemType itemType,
                            String field,
                            EnumSet<Item.FieldCapability> caps, 
                            Item.FieldType fieldType)
            throws AuraException, RemoteException;
    
    /**
     * Get all the items of a particular type
     */
    public List<Item> getAll(ItemType itemType)
            throws AuraException, RemoteException;
    
    /**
     * Get an iterator over all items of a particular type
     */
    public DBIterator<Item> getAllIterator(ItemType itemType)
            throws AuraException, RemoteException;

    /**
     * Get a particular item
     */
    public Item getItem(String key) throws AuraException, RemoteException;

    /**
     * Get a user by the random string associated with the user
     */
    public User getUserForRandomString(String randStr)
            throws AuraException, RemoteException;
    
    /**
     * Put an item into the replicants
     */
    public Item putItem(Item item) throws AuraException, RemoteException;

    /**
     * Deletes just an item from the item store, not touching the attention.
     */
    public void deleteItem(String itemKey) throws AuraException, RemoteException;
    
    /**
     * Delete a set of attentions by ID
     * 
     * @param ids
     */
    public void deleteLocalAttention(List<Long> ids) throws AuraException, RemoteException;
    
    /**
     * Gets the items of a particular type added since a particular time
     */
    public DBIterator<Item> getItemsAddedSince(ItemType type, Date timeStamp)
            throws AuraException, RemoteException;

    /**
     * Gets the items of a particular type that a particular user has paid a
     * particular type of attention to
     */
    public List<Item> getItems(User user, Type attnType, ItemType itemType)
            throws AuraException, RemoteException;

    /**
     * Gets attention matching the provided config
     */
    public List<Attention> getAttention(AttentionConfig ac)
            throws AuraException, RemoteException;

    /**
     * Gets an iterator over attention matching the provided config
     */
    public DBIterator<Attention> getAttentionIterator(AttentionConfig ac)
            throws AuraException, RemoteException;

    /**
     * Gets the number of attentions matching the provided config
     */
    public Long getAttentionCount(AttentionConfig ac)
            throws AuraException, RemoteException;

    /**
     * Gets the matching attention since a particular time
     */
    public List<Attention> getAttentionSince(AttentionConfig ac,
                                             Date timeStamp)
            throws AuraException, RemoteException;

    /**
     * Gets an iterator over the matching attention since a particular time
     */
    public DBIterator<Attention> getAttentionSinceIterator(AttentionConfig ac,
                                                           Date timeStamp)
            throws AuraException, RemoteException;

    /**
     * Gets the number of matching attentions since a particular time
     */
    public Long getAttentionSinceCount(AttentionConfig ac,
                                       Date timeStamp)
            throws AuraException, RemoteException;

    /**
     * Gets the most recent N attentions matching the provided config
     */
    public List<Attention> getLastAttention(AttentionConfig ac,
                                            int count)
            throws AuraException, RemoteException;

    /**
     * Record an attention in the replicants
     */
    public Attention attend(Attention att)
            throws AuraException, RemoteException;

    /**
     * Record a set of attention in the replicants
     */
    public List<Attention> attend(List<Attention> attns)
            throws AuraException, RemoteException;

    /**
     * Remove attention of a particular type between src and target
     */
    public void removeAttention(String srcKey, String targetKey,
                                Attention.Type type)
            throws AuraException, RemoteException;

    /**
     * Remove attention related to this item (as src or target)
     */
    public void removeAttention(String itemKey)
            throws AuraException, RemoteException;
    

    /**
     * Gets the most recent attention of a particular type for the given
     * key as a source
     */
    public List<Attention> getLastAttentionForSource(String srcKey,
                                                          Type type,
                                                          int count)
            throws AuraException, RemoteException;

    /**
     * Add an item listener
     */
    public void addItemListener(ItemType itemType, ItemListener listener)
            throws AuraException, RemoteException;

    /**
     * Remove an item listener
     */
    public void removeItemListener(ItemType itemType, ItemListener listener)
            throws AuraException, RemoteException;

    /**
     * Get a count of all items of a particular type
     */
    public long getItemCount(ItemType itemType)
            throws AuraException, RemoteException;

    /**
     * Get the top field values of a particular field
     */
    public List<FieldFrequency> getTopValues(String field,
                                             int n,
                                             boolean ignoreCase)
            throws AuraException, RemoteException;

    /**
     * Run a query with a sort
     */
    public List<Scored<Item>> query(String query,
                                    String sort,
                                    int n,
                                    ResultsFilter rf) 
            throws AuraException, RemoteException;

    /**
     * Gets items auto-tagged with a particular tag
     */
    public List<Scored<Item>> getAutotagged(String autotag, int n)
            throws AuraException, RemoteException;

    /**
     * Gets the top terms for an auto tag
     */
    public List<Scored<String>> getTopAutotagTerms(String autotag, int n)
            throws AuraException, RemoteException;

    /**
     * Find autotags similar to the given autotag
     */
    public List<Scored<String>> findSimilarAutotags(String autotag, int n)
            throws AuraException, RemoteException;

    /**
     * Provide explanation terms for similarity between autotags
     */
    public List<Scored<String>> explainSimilarAutotags(String a1, String a2,
                                                       int n)
            throws AuraException, RemoteException;

    /**
     * Get top terms as a word cloud for a field in an item
     */
    public WordCloud getTopTerms(String key, String field, int n)
            throws AuraException, RemoteException;

    /**
     * Get explanation terms for why a key was given an autotag
     */
    public List<Scored<String>> getExplanation(String key, String autoTag,
            int n)
            throws AuraException, RemoteException;

    /**
     * Get a doc vector for an item
     */
    public DocumentVector getDocumentVector(String key,
                                            SimilarityConfig config)
            throws RemoteException, AuraException;

    /**
     * Get a doc vector for a word cloud
     */
    public DocumentVector getDocumentVector(WordCloud cloud,
                                            SimilarityConfig config)
            throws RemoteException, AuraException;

    /**
     * Find similar items to the provided doc vector
     */
    public List<Scored<Item>> findSimilar(DocumentVector dv,
                                          SimilarityConfig config)
            throws AuraException, RemoteException;

    /**
     * Close things up
     */
    public void close() throws AuraException, RemoteException;

}
