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

package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.FieldType;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.impl.store.ReverseAttentionTimeComparator;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Counted;
import com.sun.labs.aura.util.ReverseScoredComparator;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.query.Element;
import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A strategy that defines how the partition cluster will perform while it
 * is splitting itself.
 */
public class PCSplitStrategy implements PCStrategy {
    
    protected PCStrategy local;
    
    protected PartitionCluster remote;
    
    protected DSBitSet localPrefix;
    
    protected DSBitSet remotePrefix;

    private Logger logger = Logger.getLogger(getClass().getName());
    
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

    @Override
    public void defineField(String fieldName, 
            FieldType fieldType,
            EnumSet<Item.FieldCapability> caps) throws AuraException, RemoteException {
        //
        // Define a field in both copies... easy!
        local.defineField(fieldName, fieldType, caps);
        remote.defineField(fieldName, fieldType, caps);
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

    public List<Scored<Item>> getScoredItems(List<Scored<String>> keys) throws AuraException, RemoteException {
        List<Scored<Item>> ret = new ArrayList();
        for(Scored<String> key : keys) {
            ret.add(new Scored<Item>(getItem(key.getItem()), key));
        }
        return ret;
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
    
    public void deleteAttention(List<Long> ids) throws AuraException, RemoteException {
        local.deleteAttention(ids);
    }

    public DBIterator<Item> getItemsAddedSince(ItemType type, Date timeStamp) throws AuraException, RemoteException {
        ArrayList<DBIterator<Item>> its = new ArrayList<DBIterator<Item>>();
        its.add(local.getItemsAddedSince(type, timeStamp));
        its.add(remote.getItemsAddedSince(type, timeStamp));
        return new MultiNoDupDBIterator(its);
        
    }

    @Override
    public Collection<Item> getItems(Collection<String> keys) throws AuraException, RemoteException {
        Collection<Item> l = local.getItems(keys);
        Collection<Item> r = (List<Item>) remote.getItems(keys);
        l.removeAll(r);
        l.addAll(r);
        return l;
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

    public Object processAttention(AttentionConfig ac, String script, String language) throws AuraException, RemoteException {
        //
        // So we need to call the process method on both sides.  But what do
        // we do with the result? Return both as an array?  Then how does the
        // DSH handle them?  Maybe we need to do a mini collect here?  But then
        // we don't have all the data.
        // TBD: do this better!
        ArrayList result = new ArrayList();
        result.add(local.processAttention(ac, script, language));
        result.add(remote.processAttention(ac, script, language));
        return result;
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
        return new ArrayList<FieldFrequency>(l.subList(0, Math.min(l.size(), n)));
    }

    public List<Scored<String>> query(String query, String sort, int n, ResultsFilter rf) throws AuraException, RemoteException {
        List<Scored<String>> l = local.query(query, sort, n, rf);
        List<Scored<String>> r = remote.query(query, sort, n, rf);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ReverseScoredComparator.COMPARATOR);
        return new ArrayList<Scored<String>>(l.subList(0, Math.min(l.size(), n)));
    }

    public List<Scored<String>> query(Element query, String sort, int n, ResultsFilter rf) throws AuraException, RemoteException {
        List<Scored<String>> l = local.query(query, sort, n, rf);
        List<Scored<String>> r = remote.query(query, sort, n, rf);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ReverseScoredComparator.COMPARATOR);
        return new ArrayList<Scored<String>>(l.subList(0, Math.min(l.size(), n)));
    }

    public List<Scored<String>> getAutotagged(String autotag, int n) throws AuraException, RemoteException {
        List<Scored<String>> l = local.getAutotagged(autotag, n);
        List<Scored<String>> r = remote.getAutotagged(autotag, n);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ReverseScoredComparator.COMPARATOR);
        return new ArrayList<Scored<String>>(l.subList(0, Math.min(l.size(), n)));
    }

    public List<Scored<String>> getTopAutotagTerms(String autotag, int n) throws AuraException, RemoteException {
        List<Scored<String>> l = local.getTopAutotagTerms(autotag, n);
        List<Scored<String>> r = remote.getTopAutotagTerms(autotag, n);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ScoredComparator.COMPARATOR);
        Collections.reverse(l);
        return new ArrayList<Scored<String>>(l.subList(0, Math.min(l.size(), n)));
    }

    public List<Scored<String>> findSimilarAutotags(String autotag, int n) throws AuraException, RemoteException {
        List<Scored<String>> l = local.findSimilarAutotags(autotag, n);
        List<Scored<String>> r = remote.findSimilarAutotags(autotag, n);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ScoredComparator.COMPARATOR);
        Collections.reverse(l);
        return new ArrayList<Scored<String>>(l.subList(0, Math.min(l.size(), n)));
    }

    public List<Scored<String>> explainSimilarAutotags(String a1, String a2, int n) throws AuraException, RemoteException {
        List<Scored<String>> l = local.explainSimilarAutotags(a1, a2, n);
        List<Scored<String>> r = remote.explainSimilarAutotags(a1, a2, n);
        l.removeAll(r);
        l.addAll(r);
        Collections.sort(l, ReverseScoredComparator.COMPARATOR);
        return new ArrayList<Scored<String>>(l.subList(0, Math.min(l.size(), n)));
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

    public List<Counted<String>> getTopTermCounts(String key, String field,
                                                  int n)
            throws AuraException, RemoteException {
        List<Counted<String>> ret = local.getTopTermCounts(key, field, n);
        if(!keyIsLocal(key)) {
            List<Counted<String>> rem = remote.getTopTermCounts(key, field, n);
            if(rem != null) {
                ret = rem;
            }
        }
        return ret;
    }

    public List<Counted<String>> getTermCounts(String term, String field, int n)
            throws AuraException, RemoteException {
        return local.getTermCounts(term, field, n);
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

    public MarshalledObject<DocumentVector> getDocumentVector(String key, SimilarityConfig config) throws RemoteException, AuraException {
        //
        // fetch the local copy then see if there is a newer one on the remote
        // end if it is supposed to live there
        MarshalledObject<DocumentVector> ret = local.getDocumentVector(key, config);
        if (!keyIsLocal(key)) {
            MarshalledObject<DocumentVector> r = remote.getDocumentVector(key, config);
            if (r != null) {
                ret = r;
            }
        }
        return ret;
    }

    public MarshalledObject<DocumentVector> getDocumentVector(WordCloud cloud, SimilarityConfig config) throws RemoteException, AuraException {
        // we're just looking for a cloud from one partition, so we'll
        // arbitrarily pick the local one
        return local.getDocumentVector(cloud, config);
    }

    public MarshalledObject<List<Scored<String>>> findSimilar(MarshalledObject<DocumentVector> dv,
            MarshalledObject<SimilarityConfig> config) throws AuraException, RemoteException {
        try {
            List<Scored<String>> l = local.findSimilar(dv, config).get();
            List<Scored<String>> r = remote.findSimilar(dv, config).get();
            l.removeAll(r);
            l.addAll(r);
            Collections.sort(l,
                    ReverseScoredComparator.COMPARATOR);
            return new MarshalledObject<List<Scored<String>>>(l.subList(0, config.get().getN()));
        } catch(IOException ex) {
            logger.log(Level.SEVERE, "Error unmarshalling", ex);
            return null;
        } catch(ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Error unmarshalling", ex);
            return null;
        }
    }

    public List<String> getSupportedScriptLanguages()
            throws AuraException, RemoteException {
        //
        // Both sides are running the same code, so we'll just talk to one
        // to get an answer here.
        return local.getSupportedScriptLanguages();
    }

    public void close() throws AuraException, RemoteException {
        local.close();
        remote.close();
    }

}
