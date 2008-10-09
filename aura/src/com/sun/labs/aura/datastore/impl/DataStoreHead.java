package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.cluster.Cluster;
import com.sun.labs.aura.cluster.KMeans;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.impl.store.ReverseAttentionTimeComparator;
import com.sun.labs.aura.util.ReverseScoredComparator;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.pipeline.StopWords;
import com.sun.labs.minion.retrieval.MultiDocumentVectorImpl;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A instance of an access point into the Data Store.  Data Store Heads
 * provided high availability and redundant access for the data in the store.
 * Each head can distribute data 
 * 
 */
public class DataStoreHead implements DataStore, Configurable, AuraService {

    protected BinaryTrie<PartitionCluster> trie = null;

    protected ExecutorService executor;

    protected ConfigurationManager cm = null;

    protected boolean closed = false;

    protected Logger logger;
    
    @ConfigComponent(type=com.sun.labs.minion.pipeline.StopWords.class,mandatory=false)
    public static final String PROP_STOPWORDS = "stopwords";
    protected StopWords stop;

    @ConfigBoolean(defaultValue = true)
    public static final String PROP_PARALLEL_GET = "parallelGet";

    private boolean parallelGet;

    public DataStoreHead() {
        trie = new BinaryTrie<PartitionCluster>();
        executor = Executors.newCachedThreadPool();
    }

    public void defineField(ItemType itemType, String field)
            throws AuraException, RemoteException {
        defineField(itemType, field, null, null);
    }

    public void defineField(ItemType itemType, String field,
            EnumSet<Item.FieldCapability> caps,
            Item.FieldType fieldType) throws AuraException, RemoteException {

        Set<PartitionCluster> clusters = trie.getAll();
        for(PartitionCluster pc : clusters) {
            pc.defineField(itemType, field, caps, fieldType);
        }
    }

    public List<Item> getAll(final ItemType itemType)
            throws AuraException, RemoteException {
        //
        // Get all the items of this type from all the partitions and combine
        // the sets.  No particular ordering is guaranteed so the merge is
        // simple.  First, set up the infrastructure to call all the clusters:
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<List<Item>>> callers = new HashSet<Callable<List<Item>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller<List<Item>>(p) {
                public List<Item> call() throws AuraException, RemoteException {
                    return pc.getAll(itemType);
                }
            });
        }

        List<Item> ret = new ArrayList<Item>();

        //
        // Now issue the call and get the answers
        try {
            List<Future<List<Item>>> results = executor.invokeAll(callers);
            for (Future<List<Item>> future : results) {
                ret.addAll(future.get());
            }
        } catch (InterruptedException e) {
            //
            // Threads got interrupted... what gives??
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }

        return ret;
    }

    public DBIterator<Item> getAllIterator(final ItemType itemType)
            throws AuraException, RemoteException {
        //
        // Get all the items of this type from all the partitions and combine
        // the sets.  No particular ordering is guaranteed so the merge is
        // simple.  First, set up the infrastructure to call all the clusters:
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<DBIterator<Item>>> callers = new HashSet<Callable<DBIterator<Item>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller<DBIterator<Item>>(p) {
                public DBIterator<Item> call() throws AuraException, RemoteException {
                    return pc.getAllIterator(itemType);
                }
            });
        }

        //
        // Try to run the whole thing and get a set of DBIterators out
        Set<DBIterator<Item>> iterators = new HashSet<DBIterator<Item>>();
        try {
            List<Future<DBIterator<Item>>> results =
                    executor.invokeAll(callers);
            for (Future<DBIterator<Item>> future : results) {
                iterators.add(future.get());
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }

        //
        // Now throw all the DBIterators together into a list so we can
        // iterate over all of them.  Since no particular ordering is
        // promised by this method, we'll use a simple composite iterator.
        MultiDBIterator<Item> mdbi = new MultiDBIterator<Item>(iterators);
        return (DBIterator<Item>) cm.getRemote(mdbi);
    }

    public Item getItem(String key) throws AuraException, RemoteException {

        NanoWatch nw = new NanoWatch();
        nw.start();
        //
        // Which partition cluster does this key belong to?
        PartitionCluster pc = trie.get(DSBitSet.parse(key.hashCode()));

        //
        // Ask the partition cluster for the item and return it.
        Item item = pc.getItem(key);
        nw.stop();
        if(logger.isLoggable(Level.FINER)) {
            logger.finer(String.format("dsh gI took %.3f", nw.getTimeMillis()));
        }
        return item;
    }

    @Override
    public Collection<Item> getItems(Collection<String> keys) throws AuraException, RemoteException {
        NanoWatch nw = new NanoWatch();
        nw.start();
        //
        // Figure out which partitions we need to talk to.
        Map<PartitionCluster, List<String>> m = new HashMap();
        for(String key : keys) {
            PartitionCluster pc = trie.get(DSBitSet.parse(key.hashCode()));
            List<String> l = m.get(pc);
            if(l == null) {
                l = new ArrayList<String>();
                m.put(pc, l);
            }
            l.add(key);
        }

        Collection<Item> ret;

        if(m.size() == 1) {
            //
            // A single partition is typical when looking for a particular item
            Map.Entry<PartitionCluster, List<String>> e = m.entrySet().
                    iterator().next();
            ret = e.getKey().getItems(e.getValue());
        } else {


            //
            // Run the gets in seriallel (sigh)
            ret = new ArrayList<Item>();
            for(Map.Entry<PartitionCluster, List<String>> e : m.entrySet()) {
                ret.addAll(e.getKey().getItems(e.getValue()));
            }
        }

        nw.stop();
        if(logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("dsh gIs for %d took %.3f",
                    keys.size(),
                    nw.getTimeMillis()));
        }
        return ret;
    }
    
    

    private List<Scored<Item>> getScoredItems(List<Scored<String>> keys) throws RemoteException, AuraException {

        NanoWatch nw = new NanoWatch();
        nw.start();
        //List<Callable<List<Scored<Item>>>> callers =
        //        new ArrayList();
        
        //
        // Figure out which partitions we need to talk to.
        Map<PartitionCluster,List<Scored<String>>> m = new HashMap();
        for(Scored<String> key : keys) {
            PartitionCluster pc = trie.get(DSBitSet.parse(key.getItem().hashCode()));
            List<Scored<String>> l = m.get(pc);
            if(l == null) {
                l = new ArrayList();
                m.put(pc, l);
            }
            l.add(key);
        }
        
        List<Scored<Item>> ret;
        
        if(m.size() == 1) {
            //
            // A single partition is typical when looking for a particular item
            Map.Entry<PartitionCluster,List<Scored<String>>> e = m.entrySet().iterator().next();
            ret = e.getKey().getScoredItems(e.getValue());
        } else {

            //
            // Run the gets in parallel
//            for(Map.Entry<PartitionCluster, List<Scored<String>>> e : m.entrySet()) {
//                callers.add(new PCCaller(e.getKey(), e.getValue()) {
//
//                    public List<Scored<Item>> call()
//                            throws AuraException, RemoteException {
//                        List<Scored<Item>> ret = pc.getItems(keys);
//                        if(logger.isLoggable(Level.FINER)) {
//                            logger.finer(String.format("dsh pc %s gis return", pc.getPrefix().
//                                    toString()));
//                        }
//                        return ret;
//                    }
//                });
//            }
//
//            try {
//                List<Future<List<Scored<Item>>>> l = executor.invokeAll(callers);
//                ret = new ArrayList<Scored<Item>>();
//                for(Future<List<Scored<Item>>> f : l) {
//                    ret.addAll(f.get());
//                }
//                Collections.sort(ret, ReverseScoredComparator.COMPARATOR);
//
//            } catch(InterruptedException ie) {
//                throw new AuraException("Interrupted while getting items", ie);
//            } catch(ExecutionException ee) {
//                throw new AuraException("Error getting items", ee);
//            }
        
            //
            // Run the gets in seriallel (sigh)
            ret = new ArrayList<Scored<Item>>();
            for(Map.Entry<PartitionCluster, List<Scored<String>>> e : m.entrySet()) {
                ret.addAll(e.getKey().getScoredItems(e.getValue()));
            }
            Collections.sort(ret, ReverseScoredComparator.COMPARATOR);
        }
        
        nw.stop();
        if(logger.isLoggable(Level.FINER)) {
            logger.finer(String.format("dsh gSIs for %d took %.3f",
                    keys.size(),
                    nw.getTimeMillis()));
        }
        return ret;
    }

    public User getUser(String key) throws AuraException, RemoteException {
        //
        // Which partition cluster does this key belong to?
        PartitionCluster pc = trie.get(DSBitSet.parse(key.hashCode()));

        //
        // Ask the partition cluster for the user and return it.
        return pc.getUser(key);
    }

    /**
     * Gets a user based on the random string that is associated with that
     * user.
     * 
     * @param randStr the random string
     * @return the user associated with the string
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public User getUserForRandomString(String randStr)
            throws AuraException, RemoteException {
        //
        // The first 8 characters of the random string are the hash code of
        // the user.  To make things easy, the entire string is what we store.
        // We use 9 characters because the hash code may have a - (or be padded
        // with zero if it isn't)
        String hashHex = randStr.substring(0, 9);
        PartitionCluster pc =
                trie.get(DSBitSet.parse(Util.hexToInt(hashHex)));
        return pc.getUserForRandomString(randStr);
    }

    public Item putItem(Item item) throws AuraException, RemoteException {
        //
        // Which partition cluster does this item belong to?
        PartitionCluster pc = trie.get(DSBitSet.parse(item.hashCode()));

        //
        // Ask the partition cluster to store the item and return it.
        return pc.putItem(item);
    }

    public User putUser(User user) throws AuraException, RemoteException {
        //
        // Which partition cluster does this item belong to?
        PartitionCluster pc = trie.get(DSBitSet.parse(user.hashCode()));

        //
        // Ask the partition cluster to store the user and return it.
        return pc.putUser(user);
    }

    public void deleteItem(final String itemKey)
            throws AuraException, RemoteException {
        //
        // Delete the item, then any attention that had the item as a target.
        PartitionCluster pc = trie.get(DSBitSet.parse(itemKey.hashCode()));
        pc.deleteItem(itemKey);

        //
        // Now tell everybody to delete the attention associated with that
        // key
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<Object>> callers = new HashSet<Callable<Object>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public Object call() throws AuraException, RemoteException {
                    pc.removeAttention(itemKey);
                    return null;
                }
            });
        }

        //
        // Run all the deletes
        try {
            List<Future<Object>> results = executor.invokeAll(callers);
            for (Future<Object> future: results) {
                future.get();
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
    }

    public void deleteUser(String itemKey)
            throws AuraException, RemoteException {
        deleteItem(itemKey);
    }

    public DBIterator<Item> getItemsAddedSince(final ItemType type,
                                               final Date timeStamp)
            throws AuraException, RemoteException {
        //
        // We need to ask all the partition clusters to perform this query,
        // then we need to orchestrate the results into a list and provide
        // an iterator across the whole thing.  First, create the callables
        // to do the job:
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<DBIterator<Item>>> callers =
                new HashSet<Callable<DBIterator<Item>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {

                public DBIterator<Item> call()
                        throws AuraException, RemoteException {
                    return pc.getItemsAddedSince(type, timeStamp);
                }
            });
        }

        //
        // Try to run the whole thing and get a set of DBIterators out
        Set<DBIterator<Item>> iterators = new HashSet<DBIterator<Item>>();
        try {
            List<Future<DBIterator<Item>>> results =
                    executor.invokeAll(callers);
            for (Future<DBIterator<Item>> future : results) {
                iterators.add(future.get());
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }

        //
        // Now throw all the DBIterators together into a list so we can
        // iterate over all of them.  Since no particular ordering is
        // promised by this method, we'll use a simple composite iterator.
        MultiDBIterator<Item> mdbi = new MultiDBIterator<Item>(iterators);
        return (DBIterator<Item>) cm.getRemote(mdbi);
    }

    public List<Item> getItems(final User user,
                              final Attention.Type attnType,
                              final ItemType itemType)
            throws AuraException, RemoteException {
        //
        // Make this call across all partitions, then combine the results
        // into a set.  The set has no particular ordering semantics at this
        // time.  We need to do this in two steps since the targets of the
        // attentions don't necessarily live in the same partition as the
        // attentions themselves.  First get all the relevant attention, then
        // get all the target items of the right type.
        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(user.getKey());
        ac.setType(attnType);
        List<Attention> attns = getAttention(ac);
        Set<String> targets = new HashSet<String>();
        for (Attention a : attns) {
            targets.add(a.getTargetKey());
        }

        //
        // Get all the items, checking their types.  This could be optimized
        // by sorting the item keys by partition, then asking each partition
        // to return a subset of items that match the given type
        List<Item> ret = new ArrayList<Item>();
        for (String target : targets) {
            Item i = getItem(target);
            if ((i != null) && (i.getType() == itemType)) {
                ret.add(i);
            }
        }
        return ret;
    }


    public List<Attention> getAttention(final AttentionConfig ac)
            throws AuraException, RemoteException {
        //
        // Make sure that some criteria was specified
        if (Util.isEmpty(ac)) {
            throw new AuraException("At least one constraint must be set " +
                    "before calling getAttention(AttentionConfig)");
        }

        //
        // Ask all the partitions to gather up their attention for this item.
        // Attentions are stored evenly across all partitions.
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<List<Attention>>> callers =
                new HashSet<Callable<List<Attention>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public List<Attention> call()
                        throws AuraException, RemoteException {
                    return pc.getAttention(ac);
                }
            });
        }

        return assembleAttentionList(executor, callers);
    }

    public DBIterator<Attention> getAttentionIterator(final AttentionConfig ac)
            throws AuraException, RemoteException {
        //
        // Make sure that some criteria was specified
        if (Util.isEmpty(ac)) {
            throw new AuraException("At least one constraint must be set " +
                    "before calling getAttention(AttentionConfig)");
        }

        //
        // Ask all the partitions to gather up their attention for this item.
        // Attentions are stored evenly across all partitions.
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<DBIterator<Attention>>> callers =
                new HashSet<Callable<DBIterator<Attention>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public DBIterator<Attention> call()
                        throws AuraException, RemoteException {
                    return pc.getAttentionIterator(ac);
                }
            });
        }

        return assembleAttentionIterator(executor, callers);
    }

    public Long getAttentionCount(final AttentionConfig ac)
            throws AuraException, RemoteException {
        //
        // Ask all the partitions to gather up their attention for this item.
        // Attentions are stored evenly across all partitions.
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<Long>> callers = new HashSet<Callable<Long>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {

                public Long call() throws AuraException, RemoteException {
                    return pc.getAttentionCount(ac);
                }
            });
        }

        //
        // Tally up the counts and return
        long count = 0;
        try {
            List<Future<Long>> results = executor.invokeAll(callers);
            for (Future<Long> future : results) {
                count += future.get();
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
        return count;
    }

    public List<Attention> getAttentionSince(final AttentionConfig ac,
                                             final Date timeStamp)
            throws AuraException, RemoteException {
        //
        // Make sure that some criteria was specified
        if (Util.isEmpty(ac)) {
            throw new AuraException("At least one constraint must be set " +
                    "before calling getAttentionSince(AttentionConfig)");
        }

        //
        // Ask all the partitions to gather up their attention for this item.
        // Attentions are stored evenly across all partitions.
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<List<Attention>>> callers =
                new HashSet<Callable<List<Attention>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public List<Attention> call()
                        throws AuraException, RemoteException {
                    return pc.getAttentionSince(ac, timeStamp);
                }
            });
        }

        return assembleAttentionList(executor, callers);
    }

    public DBIterator<Attention> getAttentionSinceIterator(
            final AttentionConfig ac,
            final Date timeStamp)
            throws AuraException, RemoteException {
        //
        // Make sure that some criteria was specified
        if (Util.isEmpty(ac)) {
            throw new AuraException("At least one constraint must be set " +
                    "before calling getAttentionSince(AttentionConfig)");
        }

        //
        // Ask all the partitions to gather up their attention for this item.
        // Attentions are stored evenly across all partitions.
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<DBIterator<Attention>>> callers =
                new HashSet<Callable<DBIterator<Attention>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {

                public DBIterator<Attention> call()
                        throws AuraException, RemoteException {
                    return pc.getAttentionSinceIterator(ac, timeStamp);
                }
            });
        }

        return assembleAttentionIterator(executor, callers);
    }

    public Long getAttentionSinceCount(final AttentionConfig ac,
                                       final Date timeStamp)
            throws AuraException, RemoteException {
        //
        // Ask all the partitions to gather up their attention for this item.
        // Attentions are stored evenly across all partitions.
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<Long>> callers = new HashSet<Callable<Long>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public Long call() throws AuraException, RemoteException {
                    return pc.getAttentionSinceCount(ac, timeStamp);
                }
            });
        }

        //
        // Tally up the counts and return
        long count = 0;
        try {
            List<Future<Long>> results = executor.invokeAll(callers);
            for (Future<Long> future : results) {
                count += future.get();
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
        return count;
    }

    public List<Attention> getLastAttention(final AttentionConfig ac,
                                            final int count)
            throws AuraException, RemoteException {
        //
        // Make sure that some criteria was specified
        if (Util.isEmpty(ac)) {
            throw new AuraException("At least one constraint must be set " +
                    "before calling getAttentionSince(AttentionConfig)");
        }

        //
        // Ask all the partitions to gather up their attention for this item.
        // Attentions are stored evenly across all partitions.
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<List<Attention>>> callers =
                new HashSet<Callable<List<Attention>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public List<Attention> call()
                        throws AuraException, RemoteException {
                    return pc.getLastAttention(ac, count);
                }
            });
        }

        try {
            List<Future<List<Attention>>> results =
                    executor.invokeAll(callers);
            return sortAttention(results, count);
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
        return new ArrayList<Attention>();
    }

    public Attention attend(Attention att)
            throws AuraException, RemoteException {
        //
        // Store the attention -- first figure out where it is supposed to go,
        // then store it in the right place.
        PartitionCluster pc = trie.get(DSBitSet.parse(att.hashCode()));

        return pc.attend(att);
    }

    public List<Attention> attend(List<Attention> attns)
            throws AuraException, RemoteException {
        //
        // Batch up the attentions to go to their respective partitions in
        // groups
        Map<PartitionCluster,List<Attention>> map =
                new HashMap<PartitionCluster,List<Attention>>();
        for (Attention att : attns) {
            PartitionCluster pc = trie.get(DSBitSet.parse(att.hashCode()));
            List dest = map.get(pc);
            if (dest == null) {
                dest = new ArrayList<Attention>();
                map.put(pc, dest);
            }
            dest.add(att);
        }

        //
        // Send each batch off to each required partition and gather up the
        // results.  For more efficiency, we should do this all in parallel.
        List<Attention> results = new ArrayList<Attention>();
        for (Map.Entry<PartitionCluster,List<Attention>> ent : map.entrySet()) {
            PartitionCluster pc = ent.getKey();
            List<Attention> a = ent.getValue();
            results.addAll(pc.attend(a));
        }
        return results;
    }

    public void removeAttention(String srcKey, String targetKey,
                                Attention.Type type)
            throws AuraException, RemoteException {
        Attention a = StoreFactory.newAttention(srcKey, targetKey, type);
        PartitionCluster pc = trie.get(DSBitSet.parse(a.hashCode()));
        pc.removeAttention(srcKey, targetKey, type);
    }

    public void addItemListener(final ItemType itemType,
                                final ItemListener listener)
            throws AuraException, RemoteException {
        //
        // There isn't anything to return here, but we do want to make sure
        // that we didn't throw an exception, so we'll still use the Futures.
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<Object>> callers = new HashSet<Callable<Object>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public Object call() throws AuraException, RemoteException {
                    pc.addItemListener(itemType, listener);
                    return null;
                }
            });
        }

        try {
            List<Future<Object>> results = executor.invokeAll(callers);
            for (Future<Object> future : results) {
                //
                // We call get, because that gives the executor a chance to
                // throw an exception if there was one.
                future.get();
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
    }

    public void removeItemListener(final ItemType itemType,
                                   final ItemListener listener)
            throws AuraException, RemoteException {
        //
        // Instruct all partition clusters to remove this listener
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<Object>> callers = new HashSet<Callable<Object>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public Object call() throws AuraException, RemoteException {
                    pc.removeItemListener(itemType, listener);
                    return null;
                }
            });
        }

        try {
            List<Future<Object>> results = executor.invokeAll(callers);
            for (Future<Object> future : results) {
                //
                // Call get to see if there were any exceptions thrown
                future.get();
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
    }

    public long getItemCount(final ItemType itemType)
            throws AuraException, RemoteException {
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<Long>> callers = new HashSet<Callable<Long>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public Long call() throws AuraException, RemoteException {
                    return pc.getItemCount(itemType);
                }
            });
        }

        //
        // Tally up the counts and return
        long count = 0;
        try {
            List<Future<Long>> results = executor.invokeAll(callers);
            for (Future<Long> future : results) {
                count += future.get();
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
        return count;
    }

    /* **********
     * 
     *  Search methods
     *
     */
    
    public List<FieldFrequency> getTopValues(
            final String field,
            final int n,
            final boolean ignoreCase) throws RemoteException, AuraException {
        Set<PartitionCluster> clusters = trie.getAll();
        List<Callable<List<FieldFrequency>>> callers =
                new ArrayList<Callable<List<FieldFrequency>>>();
        //
        // Here's our list of callers to find similar.
        for(PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {

                public List<FieldFrequency> call()
                        throws AuraException, RemoteException {
                    return pc.getTopValues(field, n, ignoreCase);
                }
            });
        }

        //
        // Run the computation, sort the results and return.
        try {
            Map<Object, FieldFrequency> m =
                    new HashMap<Object, FieldFrequency>();
            for(Future<List<FieldFrequency>> f : executor.invokeAll(callers)) {
                for(FieldFrequency ff : f.get()) {
                    FieldFrequency c = m.get(ff.getVal());
                    if(c == null) {
                        c = new FieldFrequency(ff.getVal(), 0);
                        m.put(ff.getVal(), c);
                    }
                    c.setFreq(c.getFreq() + ff.getFreq());
                }
            }

            List<FieldFrequency> all = new ArrayList<FieldFrequency>(m.values());
            Collections.sort(all);
            Collections.reverse(all);
            List<FieldFrequency> ret = new ArrayList<FieldFrequency>();
            for(int i = 0; i < n && i < all.size(); i++) {
                ret.add(all.get(i));
            }
            return ret;
        } catch(ExecutionException ex) {
            checkAndThrow(ex);
            return new ArrayList<FieldFrequency>();
        } catch(InterruptedException ie) {
            throw new AuraException("getTopValues interrupted", ie);
        }


    }

    public DocumentVector getDocumentVector(String key, String field) throws AuraException, RemoteException {
        PartitionCluster pc = trie.get(DSBitSet.parse(key.hashCode()));
        return pc.getDocumentVector(key, new SimilarityConfig(field));
    }

    public List<Scored<Item>> findSimilar(String key, SimilarityConfig config)
            throws AuraException, RemoteException {
        PartitionCluster pc = trie.get(DSBitSet.parse(key.hashCode()));
        DocumentVector dv = pc.getDocumentVector(key, config);
        return findSimilar(dv, config);
    }

    public List<Scored<Item>> findSimilar(List<String> keys,
            SimilarityConfig config)
            throws AuraException, RemoteException {
        List<DocumentVector> dvs = new ArrayList<DocumentVector>();
        for(String key : keys) {
            PartitionCluster pc = trie.get(DSBitSet.parse(key.hashCode()));
            dvs.add(pc.getDocumentVector(key, config));
        }
        MultiDocumentVectorImpl mdvi = new MultiDocumentVectorImpl(dvs);
        return findSimilar(mdvi, config);
    }

    public List<Scored<Item>> findSimilar(WordCloud cloud,
            SimilarityConfig config)
            throws AuraException, RemoteException {
        //
        // Make sure we exclude terms here, since we may not be able to do it at
        // the other end of the wire.
        cloud.getExcluded(config);
        
        // we just want any one partition cluster essentially at random:
        PartitionCluster pc = trie.get(DSBitSet.parse(cloud.hashCode()));
        return findSimilar(pc.getDocumentVector(cloud, config), config);
    }

    private List<Scored<Item>> findSimilar(
            DocumentVector dv,
            final SimilarityConfig config)
            throws AuraException, RemoteException {


        int numClusters = Math.min((int) Math.ceil(trie.size() *
                config.getReportPercent()), trie.size());

        final PCLatch latch = new PCLatch(numClusters, config.getTimeout());

        //
        // What if the key didn't exist?
        if(dv == null) {
            return new ArrayList<Scored<Item>>();
        }

        if(logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("dsh fs start"));
        }

        Set<PartitionCluster> clusters = trie.getAll();
        List<Callable<List<Scored<String>>>> callers =
                new ArrayList<Callable<List<Scored<String>>>>();
        //
        // Here's our list of callers to find similar.  Each one will be given
        // a handle to a countdown latch to watch when they finish.
        for(PartitionCluster p : clusters) {
            callers.add(new PCCaller(p, dv, config.getFilter()) {

                public List<Scored<String>> call()
                        throws AuraException, RemoteException {
                    List<Scored<String>> ret = pc.findSimilar(dv, config);
                    if(logger.isLoggable(Level.FINER)) {
                        logger.finer(String.format("dsh pc %s fs return", pc.getPrefix().toString()));
                    }
                    latch.countDown();
                    return ret;
                }
            });
        }

        //
        // Run the computation, sort the results and return.
        try {
            NanoWatch allw = new NanoWatch();
            NanoWatch fsw = new NanoWatch();

            allw.start();
            fsw.start();
            List<Future<List<Scored<String>>>> futures =
                    new ArrayList<Future<List<Scored<String>>>>();
            for(Callable c : callers) {
                futures.add(executor.submit(c));
            }
            List<Scored<String>> keys =
                    sortScored(futures, config.getN(), latch);
            fsw.stop();
            List<Scored<Item>> ret = keysToItems(keys);
            allw.stop();
            if(logger.isLoggable(Level.FINER)) {
                logger.finer(String.format(
                        "dsh fs %s took %.3f actual fs: %.3f", dv.getKey(),
                        allw.getTimeMillis(), fsw.getTimeMillis()));
            } else if(logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("dsh fs %s took %.3f", dv.getKey(),
                        allw.getTimeMillis()));
            }
            return ret;
        } catch(ExecutionException ex) {
            checkAndThrow(ex);
            return new ArrayList<Scored<Item>>();
        } catch(InterruptedException ie) {
            throw new AuraException("findSimilar interrupted", ie);
        }

    }

    public WordCloud getTopTerms(String key,
            String field, int n)
            throws AuraException, RemoteException {
        PartitionCluster pc = trie.get(DSBitSet.parse(key.hashCode()));
        return pc.getTopTerms(key, field, n);
    }

    public List<Scored<String>> getExplanation(String key, String autoTag, int n)
            throws AuraException, RemoteException {
        PartitionCluster pc = trie.get(DSBitSet.parse(key.hashCode()));
        return pc.getExplanation(key, autoTag, n);
    }

    public List<Scored<String>> explainSimilarity(String key1, String key2,
            SimilarityConfig config)
            throws AuraException, RemoteException {
        PartitionCluster pc1 = trie.get(DSBitSet.parse(key1.hashCode()));
        PartitionCluster pc2 = trie.get(DSBitSet.parse(key2.hashCode()));
        DocumentVector dv1 = pc1.getDocumentVector(key1, config);
        DocumentVector dv2 = pc2.getDocumentVector(key2, config);
        return explainSimilarity(dv1, dv2, config.getN());
    }

    public List<Scored<String>> explainSimilarity(WordCloud cloud, String key,
            SimilarityConfig config)
            throws AuraException, RemoteException {
        PartitionCluster pc1 = trie.get(DSBitSet.parse(cloud.hashCode()));
        PartitionCluster pc2 = trie.get(DSBitSet.parse(key.hashCode()));
        DocumentVector dv1 = pc1.getDocumentVector(cloud, config);
        DocumentVector dv2 = pc2.getDocumentVector(key, config);
        return explainSimilarity(dv1, dv2, config.getN());
    }

    public List<Scored<String>> explainSimilarity(WordCloud cloud1,
            WordCloud cloud2, SimilarityConfig config)
            throws AuraException, RemoteException {
        PartitionCluster pc = trie.get(DSBitSet.parse(cloud1.hashCode()));
        DocumentVector dv1 = pc.getDocumentVector(cloud1, config);
        DocumentVector dv2 = pc.getDocumentVector(cloud2, config);
        return explainSimilarity(dv1, dv2, config.getN());
    }

    private List<Scored<String>> explainSimilarity(DocumentVector dv1,
            DocumentVector dv2, int n)
            throws AuraException, RemoteException {
        if(dv1 == null || dv2 == null) {
            return new ArrayList<Scored<String>>();
        }
        Map<String, Float> sm = dv1.getSimilarityTerms(dv2);
        PriorityQueue<Scored<String>> h = new PriorityQueue<Scored<String>>(n,
                ScoredComparator.COMPARATOR);
        for(Map.Entry<String, Float> e : sm.entrySet()) {
            if(h.size() < n) {
                h.offer(new Scored<String>(e.getKey(), e.getValue()));
            } else {
                Scored<String> top = h.peek();
                if(e.getValue() > top.getScore()) {
                    h.poll();
                    h.offer(new Scored<String>(e.getKey(), e.getValue()));
                }
            }
        }
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        while(h.size() > 0) {
            ret.add(h.poll());
        }
        Collections.reverse(ret);
        return ret;
    }

    public List<Scored<Item>> getAutotagged(final String autotag, final int n)
            throws AuraException, RemoteException {
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<List<Scored<String>>>> callers =
                new HashSet<Callable<List<Scored<String>>>>();
        final PCLatch latch = new PCLatch(clusters.size());
        for(PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {

                public List<Scored<String>> call()
                        throws AuraException, RemoteException {
                    List<Scored<String>> ret = pc.getAutotagged(autotag, n);
                    latch.countDown();
                    return ret;
                }
            });
        }
        try {
            NanoWatch sw = new NanoWatch();
            sw.start();
            List<Scored<Item>> res = keysToItems(sortScored(executor.invokeAll(
                    callers),
                    n, latch));
            sw.stop();
            if(logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("dsh at %s took %.3f", autotag, sw.
                        getTimeMillis()));
            }
            return res;
        } catch(ExecutionException ex) {
            checkAndThrow(ex);
            return new ArrayList<Scored<Item>>();
        } catch(InterruptedException e) {
            throw new AuraException("Query interrupted", e);
        }
    }

    public List<Scored<String>> getTopAutotagTerms(String autotag, int n)
            throws AuraException, RemoteException {
        Set<PartitionCluster> clusters = trie.getAll();
        for(PartitionCluster pc : clusters) {
            return pc.getTopAutotagTerms(autotag, n);
        }
        return new ArrayList<Scored<String>>();
    }

    public List<Scored<String>> findSimilarAutotags(String autotag, int n)
            throws AuraException, RemoteException {
        Set<PartitionCluster> clusters = trie.getAll();
        for(PartitionCluster pc : clusters) {
            return pc.findSimilarAutotags(autotag, n);
        }
        return new ArrayList<Scored<String>>();
    }

    public List<Scored<String>> explainSimilarAutotags(String a1, String a2,
            int n)
            throws AuraException, RemoteException {
        Set<PartitionCluster> clusters = trie.getAll();
        for(PartitionCluster pc : clusters) {
            return pc.explainSimilarAutotags(a1, a2, n);
        }
        return new ArrayList<Scored<String>>();
    }

    public synchronized void close() throws AuraException, RemoteException {
        if (!closed) {
            //
            // Inform all partition clusters that they should close down
            Set<PartitionCluster> clusters = trie.getAll();
            Set<Callable<Object>> callers = new HashSet<Callable<Object>>();
            for (PartitionCluster p : clusters) {
                callers.add(new PCCaller(p) {
                    public Object call() throws AuraException, RemoteException {
                        pc.close();
                        return null;
                    }
                });
            }

            try {
                List<Future<Object>> results = executor.invokeAll(callers);
                for (Future<Object> future : results) {
                    future.get();
                }
            } catch (InterruptedException e) {
                throw new AuraException("Execution was interrupted", e);
            } catch (ExecutionException e) {
                checkAndThrow(e);
            }
            closed = true;
        }
    }

    /* **************
     * 
     *  Utility and configuration methods
     * 
     */
    private List<Scored<String>> sortScored(
            List<Future<List<Scored<String>>>> results,
            int n,
            PCLatch latch)
            throws InterruptedException, ExecutionException {

        PriorityQueue<Scored<String>> sorter =
                new PriorityQueue<Scored<String>>(n, ScoredComparator.COMPARATOR);
        //
        // Wait for some, or all, or some time limit for execution to
        // finish.
        latch.await();
        for(Future<List<Scored<String>>> future : results) {
            if(future.isDone() || (!latch.allowPartialResults())) {
                List<Scored<String>> curr = future.get();
                if(curr != null) {
                    for(Scored<String> item : curr) {
                        if(sorter.size() < n) {
                            sorter.offer(item);
                        } else {
                            if(item.compareTo(sorter.peek()) > 0) {
                                sorter.poll();
                                sorter.offer(item);
                            }
                        }
                    }
                }
            }
        }

        //
        // Get the top n.
        List<Scored<String>> ret = new ArrayList<Scored<String>>(sorter.size());
        while(sorter.size() > 0) {
            ret.add(sorter.poll());
        }
        Collections.reverse(ret);
        return ret;
    }

    private List<Attention> sortAttention(List<Future<List<Attention>>> results,
            int n)
            throws InterruptedException, ExecutionException {
        PriorityQueue<Attention> sorter = new PriorityQueue<Attention>(n);
        for(Future<List<Attention>> future : results) {
            List<Attention> curr = future.get();
            if(curr != null) {
                for(Attention attn : curr) {
                    if(sorter.size() < n) {
                        sorter.offer(attn);
                    } else {
                        if(attn.compareTo(sorter.peek()) > 0) {
                            sorter.poll();
                            sorter.offer(attn);
                        }
                    }
                }
            }
        }

        //
        // Get the top n.
        List<Attention> ret = new ArrayList<Attention>(sorter.size());
        while(sorter.size() > 0) {
            ret.add(sorter.poll());
        }
        Collections.reverse(ret);
        return ret;

    }

    private List<Scored<Item>> keysToItems(List<Scored<String>> l) throws AuraException, RemoteException {
        NanoWatch nw = new NanoWatch();
        nw.start();
        List<Scored<Item>> ret;
        if(parallelGet) {
            ret = getScoredItems(l);
        } else {
            ret = new ArrayList<Scored<Item>>(l.size());
            for(Scored<String> ss : l) {
                Item i = getItem(ss.getItem());
                ret.add(new Scored<Item>(i, ss));
            }
        }
        nw.stop();
        if(logger.isLoggable(Level.FINER)) {
            logger.finer(String.format("dsh kTI for %d took %.3f", l.size(), nw.getTimeMillis()));
        }
        return ret;
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        cm = ps.getConfigurationManager();
        logger = ps.getLogger();
        stop = (StopWords) ps.getComponent(PROP_STOPWORDS);
        parallelGet = ps.getBoolean(PROP_PARALLEL_GET);
    }

    public boolean ready() throws RemoteException {
        //
        // We're ready if the trie has all of its nodes.
        return trie.isComplete();
    }

    public void registerPartitionCluster(PartitionCluster pc)
            throws RemoteException {
        logger.info("Adding partition cluster: " + pc.getPrefix());
        trie.add(pc, pc.getPrefix());
    }
    
    public void registerPartitionSplit(PartitionCluster zeroChild,
                                       PartitionCluster oneChild)
            throws RemoteException {
        logger.info("Adding split partitions: " + zeroChild.getPrefix() +
                " and " + oneChild.getPrefix());
        trie.addPair(zeroChild, zeroChild.getPrefix(),
                     oneChild, oneChild.getPrefix());
    }
    
    public Replicant getReplicant(String prefix) throws RemoteException {
        PartitionCluster pc = trie.get(DSBitSet.parse(prefix));
        if(pc != null) {
            return pc.getReplicant();
        }
        return null;
    }

    public List<String> getPrefixes() throws RemoteException {
        List<String> ret = new ArrayList<String>();
        for(PartitionCluster pc : trie.getAll()) {
            ret.add(pc.getPrefix().toString());
        }
        return ret;
    }

    protected abstract class PCCaller<V> implements Callable {

        protected PartitionCluster pc;

        protected List<Scored<String>> keys;

        protected DocumentVector dv;

        protected ResultsFilter rf;

        public PCCaller(PartitionCluster pc) {
            this.pc = pc;
        }

        public PCCaller(PartitionCluster pc, ResultsFilter rf) {
            this.pc = pc;
            this.rf = rf;
        }

        public PCCaller(PartitionCluster pc, DocumentVector dv, ResultsFilter rf) {
            this.pc = pc;
            this.rf = rf;
            //
            // Take a copy of the document vector, because we don't want the
            // same one handed to multiple threads!
            this.dv = dv.copy();
        }

        public PCCaller(PartitionCluster pc, List<Scored<String>> keys) {
            this.pc = pc;
            this.keys = keys;
        }

        public abstract V call() throws AuraException, RemoteException;
    }

    protected static class PCLatch extends CountDownLatch {

        protected long timeout;

        protected int initialCount;

        protected boolean allowPartialResults;

        /**
         * Construct a standard latch that waits for count elements to finish
         * before continuing.
         * @param count
         * @param partialResults true if partial results are allowed
         */
        public PCLatch(int count, boolean partialResults) {
            super(count);
            initialCount = count;
            timeout = 0;
            allowPartialResults = partialResults;
        }

        /**
         * Construct a standard latch that waits for count elements to finish
         * before continuing.
         * @param count
         */
        public PCLatch(int count) {
            this(count, false);
        }

        /**
         * Construct a latch for a PC call that will wait for some number of
         * partitions to return using a timeout period.
         * 
         * @param count the initial count of the latch
         * @param timeout the initial time to wait in milliseconds
         */
        public PCLatch(int count, long timeout) {
            super(count);
            initialCount = count;
            this.timeout = timeout;
            allowPartialResults = true;
        }

        /**
         * Determine if partial results are allowed by code controlled
         * by this latch
         * @return true if partial results are allowed
         */
        public boolean allowPartialResults() {
            return allowPartialResults;
        }

        public long getTimeout() {
            return timeout;
        }

        /**
         * Waits for the given number of completions to complete.  If a
         * timeout was specified in the constructor, await will return either
         * when the count has been satisfied, or the time has elapsed,
         * whichever comes first
         * @throws java.lang.InterruptedException
         */
        public void await() throws InterruptedException {
            if (getTimeout() > 0) {
                super.await(getTimeout(), TimeUnit.MILLISECONDS);
            } else {
                super.await();
            }
        }
    }

    protected class ItemCallable implements Callable<Scored<Item>> {

        private Scored<String> key;

        public ItemCallable(Scored<String> key) {
            this.key = key;
        }

        public Scored<Item> call() throws Exception {
            PartitionCluster pc = trie.get(DSBitSet.parse(key.getItem().hashCode()));
            
            //
            // Ask the partition cluster for the item and return it.
            return new Scored<Item>(pc.getItem(key.getItem()), key);
        }
    }

    /**
     * Handles an ExecutionException by throwing something more descriptive.
     * If the execution failed due to a known type of ecxeption (aura, remote)
     * then throw the cause exception.  If it failed for anything else, throw
     * an AuraException with the ExecutionException as its cause.
     * 
     * @param e the exception
     */
    protected void checkAndThrow(ExecutionException e)
            throws AuraException, RemoteException {

        if (e.getCause() instanceof AuraException) {
            throw (AuraException)e.getCause();
        } else if (e.getCause() instanceof RemoteException) {
            throw (RemoteException)e.getCause();
        } else {
            logger.log(Level.INFO, "Error?", e);
            throw new AuraException("Execution failed", e);
        }

    }

    /**
     * Executes a set of callers that return attention iterators and assembles
     * the results into a MultiDBIterator then returns that.
     * 
     * @param executor the executor to use
     * @param callers the callers to call
     * @return an iterator over the results
     */
    protected DBIterator<Attention> assembleAttentionIterator(
            ExecutorService executor,
            Set<Callable<DBIterator<Attention>>> callers)
            throws AuraException, RemoteException {

        Set<DBIterator<Attention>> ret = new HashSet<DBIterator<Attention>>();
        try {
            List<Future<DBIterator<Attention>>> results =
                    executor.invokeAll(callers);
            for (Future<DBIterator<Attention>> future : results) {
                ret.add(future.get());
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }

        //
        // Now throw all the DBIterators together into a list so we can
        // iterate over all of them.  Since no particular ordering is
        // promised by this method, we'll use a simple composite iterator.
        MultiDBIterator<Attention> mdbi = new MultiDBIterator<Attention>(ret);
        return (DBIterator<Attention>) cm.getRemote(mdbi);
    }

    protected List<Attention> assembleAttentionList(
            ExecutorService executor,
            Set<Callable<List<Attention>>> callers)
            throws AuraException, RemoteException {
        //
        // Run all the callables and collect up the results
        List<Attention> ret = new ArrayList<Attention>();
        try {
            List<Future<List<Attention>>> results = executor.invokeAll(callers);
            for (Future<List<Attention>> future : results) {
                ret.addAll(future.get());
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
        Collections.sort(ret, new ReverseAttentionTimeComparator());
        return ret;
    }

    public void start() {
    }

    public void stop() {
        try {
            logger.info("Shutting down dsHead");
            cm.shutdown();
            logger.info("CM shutdown");
//            close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to close DataStoreHead cleanly", e);
        }
    }

    public List<Scored<Item>> query(String query, int n, ResultsFilter rf)
            throws AuraException, RemoteException {
        return query(query, "-score", n, rf);
    }

    public List<Scored<Item>> query(final String query, final String sort,
            final int n, final ResultsFilter rf)
            throws AuraException, RemoteException {
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<List<Scored<String>>>> callers =
                new HashSet<Callable<List<Scored<String>>>>();
        final PCLatch latch = new PCLatch(clusters.size());
        for(PartitionCluster p : clusters) {
            callers.add(new PCCaller(p, rf) {

                public List<Scored<String>> call()
                        throws AuraException, RemoteException {
                    List<Scored<String>> ret = pc.query(query, sort, n, rf);
                    latch.countDown();
                    return ret;
                }
            });
        }
        try {
            NanoWatch sw = new NanoWatch();
            sw.start();
            List<Scored<Item>> res = keysToItems(sortScored(executor.invokeAll(
                    callers), n, latch));
            sw.stop();
            if(logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("dsh q %s took %.3f", query, sw.
                        getTimeMillis()));
            }
            return res;
        } catch(ExecutionException ex) {
            checkAndThrow(ex);
            return new ArrayList<Scored<Item>>();
        } catch (InterruptedException e) {
            throw new AuraException("Query interrupted", e);
        }

    }

    public StopWords getStopWords() {
        return stop;
    }

    public List<Cluster> cluster(List<String> keys, String field, int k) throws AuraException, RemoteException {
        KMeans km = new KMeans(keys, this, field, k, 200);
        km.cluster();
        return km.getClusters();
    }
}

