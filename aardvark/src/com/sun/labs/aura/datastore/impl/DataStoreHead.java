
package com.sun.labs.aura.datastore.impl;

import com.sun.kt.search.WeightedField;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
    
    protected static Logger logger = Logger.getLogger("");
    
    public DataStoreHead() {
        trie = new BinaryTrie<PartitionCluster>();
        executor = Executors.newCachedThreadPool();
    }
    
    public Set<Item> getAll(final ItemType itemType)
            throws AuraException, RemoteException {
        //
        // Get all the items of this type from all the partitions and combine
        // the sets.  No particular ordering is guaranteed so the merge is
        // simple.  First, set up the infrastructure to call all the clusters:
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<Set<Item>>> callers = new HashSet<Callable<Set<Item>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller<Set<Item>>(p) {
                public Set<Item> call() throws AuraException, RemoteException {
                    return pc.getAll(itemType);
                }
            });
        }
        
        Set<Item> ret = new HashSet<Item>();
        
        //
        // Now issue the call and get the answers
        try {
            List<Future<Set<Item>>> results = executor.invokeAll(callers);
            for (Future<Set<Item>> future : results) {
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


    public Item getItem(String key) throws AuraException, RemoteException {
        //
        // Which partition cluster does this key belong to?
        PartitionCluster pc = trie.get(DSBitSet.parse(key.hashCode()));
        
        //
        // Ask the partition cluster for the item and return it.
        return pc.getItem(key);
    }

    public User getUser(String key) throws AuraException, RemoteException {
        //
        // Which partition cluster does this key belong to?
        PartitionCluster pc = trie.get(DSBitSet.parse(key.hashCode()));
        
        //
        // Ask the partition cluster for the user and return it.
        return pc.getUser(key);
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
        return (MultiDBIterator<Item>) cm.getRemote(mdbi, this);
    }

    public Set<Item> getItems(final User user,
                              final Attention.Type attnType,
                              final ItemType itemType)
            throws AuraException, RemoteException {
        //
        // Make this call across all partitions, then combine the results
        // into a set.  The set has no particular ordering semantics at this
        // time.
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<Set<Item>>> callers = new HashSet<Callable<Set<Item>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public Set<Item> call() throws AuraException, RemoteException {
                    return pc.getItems(user, attnType, itemType);
                }
            });
        }
        
        //
        // Run them all and collect up the results
        Set<Item> ret = new HashSet<Item>();
        try {
            List<Future<Set<Item>>> results = executor.invokeAll(callers);
            for (Future<Set<Item>> future : results) {
                ret.addAll(future.get());
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
        return ret;
    }

    public Set<Attention> getAttentionFor(final String itemKey,
                                          final boolean isSrc)
            throws AuraException, RemoteException {
        //
        // Ask all the partitions to gather up their attention for this item.
        // Attentions are stored evenly across all partitions.
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<Set<Attention>>> callers =
                new HashSet<Callable<Set<Attention>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public Set<Attention> call()
                        throws AuraException, RemoteException {
                    if (isSrc) {
                        return pc.getAttentionForSource(itemKey);
                    } else {
                        return pc.getAttentionForTarget(itemKey);
                    }
                }
            });
        }
        
        //
        // Run all the callables and collect up the results
        Set<Attention> ret = new HashSet<Attention>();
        try {
            List<Future<Set<Attention>>> results = executor.invokeAll(callers);
            for (Future<Set<Attention>> future : results) {
                ret.addAll(future.get());
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
        return ret;
    }

    public Set<Attention> getAttentionForSource(String srcKey)
            throws AuraException, RemoteException {
        return getAttentionFor(srcKey, true);
    }

    public Set<Attention> getAttentionForTarget(String itemKey)
            throws AuraException, RemoteException {
        return getAttentionFor(itemKey, false);
    }

    public Attention attend(Attention att)
            throws AuraException, RemoteException {
        //
        // Store the attention -- first figure out where it is supposed to go,
        // then store it in the right place.
        PartitionCluster pc = trie.get(DSBitSet.parse(att.hashCode()));
        
        return pc.attend(att);
    }

    public DBIterator<Attention> getAttentionAddedSince(final Date timeStamp)
            throws AuraException, RemoteException {
        Set<PartitionCluster> cluster = trie.getAll();
        Set<Callable<DBIterator<Attention>>> callers =
                new HashSet<Callable<DBIterator<Attention>>>();
        for (PartitionCluster p : cluster) {
            callers.add(new PCCaller(p) {
               public DBIterator<Attention> call()
                       throws AuraException, RemoteException {
                   return pc.getAttentionAddedSince(timeStamp);
               } 
            });
        }
        
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
        return (MultiDBIterator<Attention>) cm.getRemote(mdbi, this);

    }

    public SortedSet<Attention> getLastAttentionForSource(String srcKey,
                                                         int count)
            throws AuraException, RemoteException {
        return getLastAttentionForSource(srcKey, null, count);
    }

    public SortedSet<Attention> getLastAttentionForSource(final String srcKey,
                                                          final Type type,
                                                          final int count)
            throws AuraException, RemoteException {
        //
        // Call out to all the clusters to search for attention for this user,
        // type, and no more than count.  Then we'll combine all the results
        // and only take the first 'count' of those.
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<SortedSet<Attention>>> callers =
                new HashSet<Callable<SortedSet<Attention>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
               public SortedSet<Attention> call()
                       throws AuraException, RemoteException {
                   return pc.getLastAttentionForSource(srcKey, type, count);
               }
            });
        }
        
        SortedSet<Attention> ret = null;
        try {
            List<Future<SortedSet<Attention>>> results =
                    executor.invokeAll(callers);
            for (Future<SortedSet<Attention>> future : results) {
                SortedSet<Attention> curr = future.get();
                if (curr != null) {
                    if (ret == null) {
                        //
                        // Make a new set based on the existing one, including
                        // the comparator
                        ret = new TreeSet<Attention>(curr);
                    } else {
                        ret.addAll(curr);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
        
        //
        // Now put together a set with the right count of attentions in it
        SortedSet<Attention> retCnted = new TreeSet(ret.comparator());
        Iterator<Attention> it = ret.iterator();
        for (int i = 0; i < count; i++) {
            if (it.hasNext()) {
                retCnted.add(it.next());
            } else {
                break;
            }
        }
        
        return retCnted;
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

    public long getAttentionCount() throws AuraException, RemoteException {
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<Long>> callers = new HashSet<Callable<Long>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public Long call() throws AuraException, RemoteException {
                    return pc.getAttentionCount();
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
    public SortedSet<Item> findSimilar(String key, int n)
            throws AuraException, RemoteException {
        return findSimilar(key, (String)null, n);
    }

    public SortedSet<Item> findSimilar(String key, final String field, int n)
            throws AuraException, RemoteException {
        WeightedField wf = new WeightedField(field, 1);
        return findSimilar(key, new WeightedField[]{wf}, n);
    }

    public SortedSet<Item> findSimilar(final String key,
                                       final WeightedField[] fields,
                                       final int n)
            throws AuraException, RemoteException {
        Set<PartitionCluster> clusters = trie.getAll();
        Set<Callable<SortedSet<Item>>> callers =
                new HashSet<Callable<SortedSet<Item>>>();
        for (PartitionCluster p : clusters) {
            callers.add(new PCCaller(p) {
                public SortedSet<Item> call()
                        throws AuraException, RemoteException {
                    if (fields == null) {
                        return pc.findSimilar(key, n);
                    } else if (fields.length == 1) {
                        return pc.findSimilar(key, fields[0].getFieldName(), n);
                    } else {
                        return pc.findSimilar(key, fields, n);
                    }
                }
            });
        }
        
        //
        // Combine the results, then return only the top n
        SortedSet<Item> ret = null;
        try {
            List<Future<SortedSet<Item>>> results = executor.invokeAll(callers);
            for (Future<SortedSet<Item>> future : results) {
                SortedSet<Item> curr = future.get();
                if (curr != null) {
                    if (ret == null) {
                        //
                        // Make a new set with the contents and comparator from
                        // curr
                        ret = new TreeSet<Item>(curr);
                    } else {
                        ret.addAll(curr);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new AuraException("Execution was interrupted", e);
        } catch (ExecutionException e) {
            checkAndThrow(e);
        }
        
        if (ret == null) {
            return new TreeSet<Item>();
        }
        
        //
        // Make a set of the top n to return
        SortedSet<Item> retCnted = new TreeSet<Item>(ret.comparator());
        Iterator<Item> it = ret.iterator();
        for (int i = 0; i < n; i++) {
            if (it.hasNext()) {
                retCnted.add(it.next());
            } else {
                break;
            }
        }
        return retCnted;
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
    
    public void newProperties(PropertySheet ps) throws PropertyException {
        cm = ps.getConfigurationManager();
    }
    
    public void registerPartitionCluster(PartitionCluster pc)
            throws RemoteException {
        trie.add(pc, pc.getPrefix());
    }

    protected abstract class PCCaller<V> implements Callable {
        protected PartitionCluster pc;
        public PCCaller(PartitionCluster pc) {
            this.pc = pc;
        }
        
        public abstract V call() throws AuraException, RemoteException;
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
            throw new AuraException("Execution failed", e);
        }

    }

    public void start() {
    }

    public void stop() {
        try {
            cm.shutdown();
            close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to close DataStoreHead cleanly", e);
        }
    }

}
