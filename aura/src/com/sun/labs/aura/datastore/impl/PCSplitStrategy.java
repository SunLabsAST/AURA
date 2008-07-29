
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
import com.sun.labs.aura.datastore.impl.store.ReverseAttentionTimeComparator;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.ResultsFilter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * A strategy that defines how the partition cluster will perform while it
 * is splitting itself.
 */
public class PCSplitStrategy implements PCStrategy {
    
    protected PCStrategy local;
    
    protected PartitionCluster remote;
    
    protected DSBitSet localPrefix;
    
    protected DSBitSet remotePrefix;
    
    /**
     * Construct a strategy for splitting a partition.  The localStrategy is
     * used for reading and writing data in the local partition cluster.  The
     * reference to a remote cluster is the target that half the data will be
     * migrated to.
     * 
     * @param localStrategy access to the local partition
     * @param localPrefix the new (lengthened) prefix for this partition
     * @param remoteCluster the remote partition
     * @param remotePrefix the new prefix for the split-off partition
     */
    public PCSplitStrategy(PCStrategy localStrategy,
                           DSBitSet localPrefix,
                           PartitionCluster remoteCluster,
                           DSBitSet remotePrefix) {
        local = localStrategy;
        this.localPrefix = localPrefix;
        remote = remoteCluster;
        this.remotePrefix = remotePrefix;
    }

    /*
     * Utility methods
     */
    protected boolean keyIsLocal(String key) throws AuraException {
        return keyIsLocal(key.hashCode());
    }
    
    protected boolean keyIsLocal(int hashCode) throws AuraException {
        return Util.keyIsLocal(hashCode, localPrefix, remotePrefix);
    }
    
    
    /*
     * Strategy implementation methods
     */
    
    
    public void defineField(ItemType itemType,
                            String field,
                            EnumSet<FieldCapability> caps,
                            FieldType fieldType)
            throws AuraException, RemoteException {
        //
        // Define a field in both copies... easy!
        local.defineField(itemType, field, caps, fieldType);
        remote.defineField(itemType, field, caps, fieldType);
    }

    public List<Item> getAll(ItemType itemType)
            throws AuraException, RemoteException {
        //
        // Get all the items from both sides and merge the list
        List<Item> localList = local.getAll(itemType);
        List<Item> remoteList = remote.getAll(itemType);
        localList.removeAll(remoteList);
        localList.addAll(remoteList);
        return localList;
    }

    public DBIterator<Item> getAllIterator(ItemType itemType) throws AuraException, RemoteException {
        DBIterator<Item> localIt = local.getAllIterator(itemType);
        DBIterator<Item> remoteIt = remote.getAllIterator(itemType);
        ArrayList<DBIterator<Item>> its = new ArrayList();
        its.add(localIt);
        its.add(remoteIt);
        MultiNoDupDBIterator<Item> mdbit = new MultiNoDupDBIterator(its);
        // should this be remoted??  maybe better to talk directly to this PC
        return mdbit;
    }

    public Item getItem(String key) throws AuraException, RemoteException {
        if (keyIsLocal(key)) {
            return local.getItem(key);
        } else {
            // Since we move items to the remote end, then delete from the
            // local end, we'll first try to grab the local, then see if there
            // is something newer on the remote end.  If we did it the other
            // way around, there's a chance we could overlap with a move and
            // not retrieve the item.
            Item l = local.getItem(key);
            Item r = remote.getItem(key);
            if (r == null) {
                return l;
            }
            return r;
        }
    }

    public User getUserForRandomString(String randStr) throws AuraException, RemoteException {
        //
        // The first 8 characters of the random string are the hash code of
        // the user.  To make things easy, the entire string is what we store.
        // We use 9 characters because the hash code may have a - (or be padded
        // with zero if it isn't)
        String hashHex = randStr.substring(0, 9);
        if (keyIsLocal(Util.hexToInt(hashHex))) {
            return local.getUserForRandomString(randStr);
        } else {
            User l = local.getUserForRandomString(randStr);
            User r = remote.getUserForRandomString(randStr);
            if (r == null) {
                return l;
            }
            return r;
        }
    }

    public Item putItem(Item item) throws AuraException, RemoteException {
        if (keyIsLocal(item.getKey())) {
            return local.putItem(item);
        } else {
            // If this item exists in local, we should do a migration of it
            // now.  How will this conflict with db iterators that are doing
            // migration in the background???
            Item ret = remote.putItem(item);
            local.deleteItem(item.getKey());
            return ret;
        }
    }

    public void deleteItem(String itemKey) throws AuraException, RemoteException {
        //
        // local or not, make sure it isn't in the local partition
        local.deleteItem(itemKey);
        if (!keyIsLocal(itemKey)) {
            // if it is remote, also remove it there
            remote.deleteItem(itemKey);
        }
    }
    
    public void deleteLocalAttention(List<Long> ids) throws AuraException, RemoteException {
        local.deleteLocalAttention(ids);
    }

    public DBIterator<Item> getItemsAddedSince(ItemType type, Date timeStamp) throws AuraException, RemoteException {
        ArrayList<DBIterator<Item>> its = new ArrayList<DBIterator<Item>>();
        its.add(local.getItemsAddedSince(type, timeStamp));
        its.add(remote.getItemsAddedSince(type, timeStamp));
        return new MultiNoDupDBIterator(its);
        
    }

    public List<Item> getItems(User user, Type attnType, ItemType itemType) throws AuraException, RemoteException {
        List<Item> l = local.getItems(user, attnType, itemType);
        List<Item> r = remote.getItems(user, attnType, itemType);
        l.removeAll(r);
        l.addAll(r);
        return l;
    }

    public List<Attention> getAttention(AttentionConfig ac) throws AuraException, RemoteException {
        List<Attention> l = local.getAttention(ac);
        List<Attention> r = remote.getAttention(ac);
        l.removeAll(r);
        l.addAll(r);
        return l;
    }

    public DBIterator<Attention> getAttentionIterator(AttentionConfig ac) throws AuraException, RemoteException {
        ArrayList<DBIterator<Attention>> its =
                new ArrayList<DBIterator<Attention>>();
        its.add(local.getAttentionIterator(ac));
        its.add(remote.getAttentionIterator(ac));
        return new MultiNoDupDBIterator(its);
    }

    public Long getAttentionCount(AttentionConfig ac) throws AuraException, RemoteException {
        Long ret = local.getAttentionCount(ac);
        ret += remote.getAttentionCount(ac);
        // the returned value may be an approximation
        return ret;
    }

    public List<Attention> getAttentionSince(AttentionConfig ac, Date timeStamp) throws AuraException, RemoteException {
        List<Attention> l = local.getAttentionSince(ac, timeStamp);
        List<Attention> r = remote.getAttentionSince(ac, timeStamp);
        l.removeAll(r);
        l.addAll(r);
        return l;
    }

    public DBIterator<Attention> getAttentionSinceIterator(AttentionConfig ac, Date timeStamp) throws AuraException, RemoteException {
        ArrayList<DBIterator<Attention>> its =
                new ArrayList<DBIterator<Attention>>();
        its.add(local.getAttentionSinceIterator(ac, timeStamp));
        its.add(remote.getAttentionSinceIterator(ac, timeStamp));
        return new MultiNoDupDBIterator(its);
    }

    public Long getAttentionSinceCount(AttentionConfig ac, Date timeStamp) throws AuraException, RemoteException {
        Long ret = local.getAttentionSinceCount(ac, timeStamp);
        ret += remote.getAttentionSinceCount(ac, timeStamp);
        return ret;
    }

    public List<Attention> getLastAttention(AttentionConfig ac, int count) throws AuraException, RemoteException {
        List<Attention> l = local.getLastAttention(ac, count);
        List<Attention> r = remote.getLastAttention(ac, count);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, new ReverseAttentionTimeComparator());
        return new ArrayList<Attention>(l.subList(0, count));
    }

    public Attention attend(Attention att) throws AuraException, RemoteException {
        if (keyIsLocal(att.hashCode())) {
            return local.attend(att);
        } else {
            return remote.attend(att);
        }
    }

    public List<Attention> attend(List<Attention> attns) throws AuraException, RemoteException {
        List<Attention> forLocal = new ArrayList<Attention>();
        List<Attention> forRemote = new ArrayList<Attention>();
        for (Attention a : attns) {
            if (keyIsLocal(a.hashCode())) {
                forLocal.add(a);
            } else {
                forRemote.add(a);
            }
        }
        List<Attention> results = new ArrayList<Attention>();
        results.addAll(local.attend(forLocal));
        results.addAll(remote.attend(forRemote));
        return results;
    }

    public void removeAttention(String srcKey, String targetKey, Type type) throws AuraException, RemoteException {
        local.removeAttention(srcKey, targetKey, type);
        remote.removeAttention(srcKey, targetKey, type);
    }

    public void removeAttention(String itemKey) throws AuraException, RemoteException {
        local.removeAttention(itemKey);
        remote.removeAttention(itemKey);
    }

    public List<Attention> getLastAttentionForSource(String srcKey, Type type, int count) throws AuraException, RemoteException {
        List<Attention> l = local.getLastAttentionForSource(srcKey, type, count);
        List<Attention> r = remote.getLastAttentionForSource(srcKey, type, count);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, new ReverseAttentionTimeComparator());
        return new ArrayList<Attention>(l.subList(0, count));
    }

    public void addItemListener(ItemType itemType, ItemListener listener) throws AuraException, RemoteException {
        //
        // need to add the listener to both
        local.addItemListener(itemType, listener);
        remote.addItemListener(itemType, listener);
    }

    public void removeItemListener(ItemType itemType, ItemListener listener) throws AuraException, RemoteException {
        local.removeItemListener(itemType, listener);
        remote.removeItemListener(itemType, listener);
    }

    public long getItemCount(ItemType itemType) throws AuraException, RemoteException {
        long ret = local.getItemCount(itemType);
        ret += remote.getItemCount(itemType);
        return ret;
    }

    public List<FieldFrequency> getTopValues(String field, int n, boolean ignoreCase) throws AuraException, RemoteException {
        List<FieldFrequency> l = local.getTopValues(field, n, ignoreCase);
        List<FieldFrequency> r = remote.getTopValues(field, n, ignoreCase);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l);
        Collections.reverse(l);
        return new ArrayList<FieldFrequency>(l.subList(0, n));
    }

    public List<Scored<Item>> query(String query, String sort, int n, ResultsFilter rf) throws AuraException, RemoteException {
        List<Scored<Item>> l = local.query(query, sort, n, rf);
        List<Scored<Item>> r = remote.query(query, sort, n, rf);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ScoredComparator.COMPARATOR);
        Collections.reverse(l);
        return new ArrayList<Scored<Item>>(l.subList(0, n));
    }

    public List<Scored<Item>> getAutotagged(String autotag, int n) throws AuraException, RemoteException {
        List<Scored<Item>> l = local.getAutotagged(autotag, n);
        List<Scored<Item>> r = remote.getAutotagged(autotag, n);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ScoredComparator.COMPARATOR);
        Collections.reverse(l);
        return new ArrayList<Scored<Item>>(l.subList(0, n));
    }

    public List<Scored<String>> getTopAutotagTerms(String autotag, int n) throws AuraException, RemoteException {
        List<Scored<String>> l = local.getTopAutotagTerms(autotag, n);
        List<Scored<String>> r = remote.getTopAutotagTerms(autotag, n);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ScoredComparator.COMPARATOR);
        Collections.reverse(l);
        return new ArrayList<Scored<String>>(l.subList(0, n));
    }

    public List<Scored<String>> findSimilarAutotags(String autotag, int n) throws AuraException, RemoteException {
        List<Scored<String>> l = local.findSimilarAutotags(autotag, n);
        List<Scored<String>> r = remote.findSimilarAutotags(autotag, n);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ScoredComparator.COMPARATOR);
        Collections.reverse(l);
        return new ArrayList<Scored<String>>(l.subList(0, n));
    }

    public List<Scored<String>> explainSimilarAutotags(String a1, String a2, int n) throws AuraException, RemoteException {
        List<Scored<String>> l = local.explainSimilarAutotags(a1, a2, n);
        List<Scored<String>> r = remote.explainSimilarAutotags(a1, a2, n);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ScoredComparator.COMPARATOR);
        Collections.reverse(l);
        return new ArrayList<Scored<String>>(l.subList(0, n));
    }

    public WordCloud getTopTerms(String key, String field, int n) throws AuraException, RemoteException {
        //
        // fetch the local copy, then see if there is anything newer on the
        // remote end if it is supposed to live there
        WordCloud ret = local.getTopTerms(key, field, n);
        if (!keyIsLocal(key)) {
            WordCloud r = remote.getTopTerms(key, field, n);
            if (r != null) {
                ret = r;
            }
        }
        return ret;
    }

    public List<Scored<String>> getExplanation(String key, String autoTag, int n) throws AuraException, RemoteException {
        //
        // fetch the local copy then see if there is a newer one on the remote
        // end if it is supposed to live there
        List<Scored<String>> ret = local.getExplanation(key, autoTag, n);
        if (!keyIsLocal(key)) {
            List<Scored<String>> r = remote.getExplanation(key, autoTag, n);
            if (r != null) {
                ret = r;
            }
        }
        return ret;
    }

    public DocumentVector getDocumentVector(String key, SimilarityConfig config) throws RemoteException, AuraException {
        //
        // fetch the local copy then see if there is a newer one on the remote
        // end if it is supposed to live there
        DocumentVector ret = local.getDocumentVector(key, config);
        if (!keyIsLocal(key)) {
            DocumentVector r = remote.getDocumentVector(key, config);
            if (r != null) {
                ret = r;
            }
        }
        return ret;
    }

    public DocumentVector getDocumentVector(WordCloud cloud, SimilarityConfig config) throws RemoteException, AuraException {
        // we're just looking for a cloud from one partition, so we'll
        // arbitrarily pick the local one
        return local.getDocumentVector(cloud, config);
    }

    public List<Scored<Item>> findSimilar(DocumentVector dv, SimilarityConfig config) throws AuraException, RemoteException {
        List<Scored<Item>> l = local.findSimilar(dv, config);
        List<Scored<Item>> r = remote.findSimilar(dv, config);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ScoredComparator.COMPARATOR);
        Collections.reverse(l);
        return new ArrayList<Scored<Item>>(l.subList(0, config.getN()));        
    }

    public void close() throws AuraException, RemoteException {
        local.close();
        remote.close();
    }

}
