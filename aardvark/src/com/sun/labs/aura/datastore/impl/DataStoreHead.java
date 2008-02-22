
package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.aardvark.AardvarkService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.impl.store.DBIterator;
import com.sun.labs.util.props.ComponentRegistry;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class DataStoreHead implements DataStore, Configurable, AardvarkService {
    
    protected BinaryTrie<PartitionCluster> trie = null;

    protected ExecutorService executor;
    
    protected ComponentRegistry compReg = null;
    
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
        PartitionCluster pc = trie.get(Util.intToBitSet(key.hashCode()));
        
        //
        // Ask the partition cluster for the item and return it.
        return pc.getItem(key);
    }

    public User getUser(String key) throws AuraException, RemoteException {
        //
        // Which partition cluster does this key belong to?
        PartitionCluster pc = trie.get(Util.intToBitSet(key.hashCode()));
        
        //
        // Ask the partition cluster for the user and return it.
        return pc.getUser(key);
    }

    public Item putItem(Item item) throws AuraException, RemoteException {
        //
        // Which partition cluster does this item belong to?
        PartitionCluster pc = trie.get(Util.intToBitSet(item.hashCode()));
        
        //
        // Ask the partition cluster to store the item and return it.
        return pc.putItem(item);
    }

    public User putUser(User user) throws AuraException, RemoteException {
        //
        // Which partition cluster does this item belong to?
        PartitionCluster pc = trie.get(Util.intToBitSet(user.hashCode()));
        
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
        if (compReg != null) {
            mdbi = (MultiDBIterator<Item>) compReg.getRemote(mdbi);
        }
        return mdbi;
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

    public Set<Attention> getAttentionForTarget(final Item item)
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
                    return pc.getAttentionForTarget(item);
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

    public Attention attend(Attention att)
            throws AuraException, RemoteException {
        //
        // Store the attention -- first figure out where it is supposed to go,
        // then store it in the right place.
        PartitionCluster pc = trie.get(Util.intToBitSet(att.hashCode()));
        
        return pc.attend(att);
    }

    public DBIterator<Attention> getAttentionAddedSince(Date timeStamp)
            throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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

    public void newProperties(PropertySheet arg0) throws PropertyException {
        compReg = arg0.getConfigurationManager().getComponentRegistry();
        throw new UnsupportedOperationException("Not supported yet.");
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
            close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to close DataStoreHead cleanly", e);
        }
    }
}
