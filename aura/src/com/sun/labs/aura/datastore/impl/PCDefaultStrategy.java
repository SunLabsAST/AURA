package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.FieldCapability;
import com.sun.labs.aura.datastore.Item.FieldType;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.ResultsFilter;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * This is the default strategy that the PartitionCluster uses to read and
 * write data from its replicants.  This is the strategy that is used in a
 * standard environment where everything is basically working smoothly.
 */
public class PCDefaultStrategy implements PCStrategy {

    protected Replicant replicant;
    
    public PCDefaultStrategy(Replicant replicant) {
        this.replicant = replicant;
    }
    
    public void defineField(ItemType itemType, String field, EnumSet<FieldCapability> caps, FieldType fieldType) throws AuraException, RemoteException {
        replicant.defineField(itemType, field, caps, fieldType);
    }

    public List<Item> getAll(ItemType itemType) throws AuraException, RemoteException {
        return replicant.getAll(itemType);
    }

    public DBIterator<Item> getAllIterator(ItemType itemType) throws AuraException, RemoteException {
        return replicant.getAllIterator(itemType);
    }

    public Item getItem(String key) throws AuraException, RemoteException {
        return replicant.getItem(key);
    }

    @Override
    public Collection<Item> getItems(Collection<String> keys) throws AuraException, RemoteException {
        return replicant.getItems(keys);
    }

    public List<Scored<Item>> getScoredItems(List<Scored<String>> keys) throws AuraException, RemoteException {
        return replicant.getScoredItems(keys);
    }

    public User getUserForRandomString(String randStr) throws AuraException, RemoteException {
        return replicant.getUserForRandomString(randStr);
    }

    public Item putItem(Item item) throws AuraException, RemoteException {
        return replicant.putItem(item);
    }

    public void deleteItem(String itemKey) throws AuraException, RemoteException {
        replicant.deleteItem(itemKey);
    }

    public void deleteAttention(List<Long> ids) throws AuraException, RemoteException {
        replicant.deleteAttention(ids);
    }
    
    public DBIterator<Item> getItemsAddedSince(ItemType type, Date timeStamp) throws AuraException, RemoteException {
        return replicant.getItemsAddedSince(type, timeStamp);
    }

    public List<Item> getItems(User user, Type attnType, ItemType itemType) throws AuraException, RemoteException {
        return replicant.getItems(user, attnType, itemType);
    }

    public List<Attention> getAttention(AttentionConfig ac) throws AuraException, RemoteException {
        return replicant.getAttention(ac);
    }

    public DBIterator<Attention> getAttentionIterator(AttentionConfig ac) throws AuraException, RemoteException {
        return replicant.getAttentionIterator(ac);
    }

    public Long getAttentionCount(AttentionConfig ac) throws AuraException, RemoteException {
        return replicant.getAttentionCount(ac);
    }

    public List<Attention> getAttentionSince(AttentionConfig ac, Date timeStamp) throws AuraException, RemoteException {
        return replicant.getAttentionSince(ac, timeStamp);
    }

    public DBIterator<Attention> getAttentionSinceIterator(AttentionConfig ac, Date timeStamp) throws AuraException, RemoteException {
        return replicant.getAttentionSinceIterator(ac, timeStamp);
    }

    public Long getAttentionSinceCount(AttentionConfig ac, Date timeStamp) throws AuraException, RemoteException {
        return replicant.getAttentionSinceCount(ac, timeStamp);
    }

    public List<Attention> getLastAttention(AttentionConfig ac, int count) throws AuraException, RemoteException {
        return replicant.getLastAttention(ac, count);
    }

    public Attention attend(Attention att) throws AuraException, RemoteException {
        return replicant.attend(att);
    }

    public List<Attention> attend(List<Attention> attns) throws AuraException, RemoteException {
        return replicant.attend(attns);
    }

    public void removeAttention(String srcKey, String targetKey, Type type) throws AuraException, RemoteException {
        replicant.removeAttention(srcKey, targetKey, type);
    }

    public void removeAttention(String itemKey) throws AuraException, RemoteException {
        replicant.removeAttention(itemKey);
    }

    public List<Attention> getLastAttentionForSource(String srcKey, Type type, int count) throws AuraException, RemoteException {
        return replicant.getLastAttentionForSource(srcKey, type, count);
    }

    public void addItemListener(ItemType itemType, ItemListener listener) throws AuraException, RemoteException {
        replicant.addItemListener(itemType, listener);
    }

    public void removeItemListener(ItemType itemType, ItemListener listener) throws AuraException, RemoteException {
        replicant.removeItemListener(itemType, listener);
    }

    public long getItemCount(ItemType itemType) throws AuraException, RemoteException {
        return replicant.getItemCount(itemType);
    }

    public List<FieldFrequency> getTopValues(String field, int n, boolean ignoreCase) throws AuraException, RemoteException {
        return replicant.getTopValues(field, n, ignoreCase);
    }

    public List<Scored<String>> query(String query, String sort, int n, ResultsFilter rf) throws AuraException, RemoteException {
        return replicant.query(query, sort, n, rf);
    }

    public List<Scored<String>> getAutotagged(String autotag, int n) throws AuraException, RemoteException {
        return replicant.getAutotagged(autotag, n);
    }

    public List<Scored<String>> getTopAutotagTerms(String autotag, int n) throws AuraException, RemoteException {
        return replicant.getTopAutotagTerms(autotag, n);
    }

    public List<Scored<String>> findSimilarAutotags(String autotag, int n) throws AuraException, RemoteException {
        return replicant.findSimilarAutotags(autotag, n);
    }

    public List<Scored<String>> explainSimilarAutotags(String a1, String a2, int n) throws AuraException, RemoteException {
        return replicant.explainSimilarAutotags(a1, a2, n);
    }

    public WordCloud getTopTerms(String key, String field, int n) throws AuraException, RemoteException {
        return replicant.getTopTerms(key, field, n);
    }

    public List<Scored<String>> getExplanation(String key, String autoTag, int n) throws AuraException, RemoteException {
        return replicant.getExplanation(key, autoTag, n);
    }

    public DocumentVector getDocumentVector(String key, SimilarityConfig config) throws RemoteException, AuraException {
        return replicant.getDocumentVector(key, config);
    }

    public DocumentVector getDocumentVector(WordCloud cloud, SimilarityConfig config) throws RemoteException, AuraException {
        return replicant.getDocumentVector(cloud, config);
    }

    public List<Scored<String>> findSimilar(DocumentVector dv, SimilarityConfig config) throws AuraException, RemoteException {
        return replicant.findSimilar(dv, config);
    }

    public void close() throws AuraException, RemoteException {
        //
        // do something here?
        // replicant.close();
    }

}
