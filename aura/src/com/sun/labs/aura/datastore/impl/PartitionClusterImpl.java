package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A partition cluster stores one segment of the item store's data.  The
 * segment that it stores is determined by matching the prefix of each item's
 * key with the prefix for the cluster.
 */
public class PartitionClusterImpl implements PartitionCluster,
                                             Configurable, AuraService {
    
    @ConfigString
    public static final String PROP_PREFIX = "prefix";
    
    @ConfigComponent(type=com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE_HEAD = "dataStoreHead";
    
    private DataStore dataStoreHead;
    
    protected DSBitSet prefixCode;
    
    protected boolean closed = false;

    //protected List<BerkeleyItemStore> replicants;
    protected Replicant replicant;
    
    protected PCStrategy strategy;
    
    protected Logger logger;
    
    protected AtomicBoolean splitting = new AtomicBoolean(false);
    
    /**
     * Construct a PartitionClusterImpl for use with a particular item prefix.
     * 
     * @param prefixCode the initial prefix that this cluster represents
     */
    public PartitionClusterImpl() {

    }
    
    public DSBitSet getPrefix() {
        return prefixCode;
    }
    
    public void defineField(ItemType itemType, String field)
            throws AuraException, RemoteException {
        defineField(itemType, field, null, null);
    }

    public void defineField(ItemType itemType, String field, EnumSet<Item.FieldCapability> caps, 
            Item.FieldType fieldType) throws AuraException, RemoteException {
        strategy.defineField(itemType, field, caps, fieldType);
    }
    
    public List<Item> getAll(ItemType itemType)
            throws AuraException, RemoteException {
        return strategy.getAll(itemType);
    }
    
    public DBIterator<Item> getAllIterator(ItemType itemType)
            throws AuraException, RemoteException {
        return strategy.getAllIterator(itemType);
    }

    public Item getItem(String key) throws AuraException, RemoteException {
        return strategy.getItem(key);
    }

    public User getUser(String key) throws AuraException, RemoteException {
        return (User)strategy.getItem(key);
    }

    public User getUserForRandomString(String randStr)
            throws AuraException, RemoteException {
        return strategy.getUserForRandomString(randStr);
    }
    
    public Item putItem(Item item) throws AuraException, RemoteException {
        return strategy.putItem(item);
    }

    public User putUser(User user) throws AuraException, RemoteException {
        return (User)strategy.putItem(user);
    }

    /**
     * Deletes just an item from the item store, not touching the attention.
     */
    public void deleteItem(String itemKey) throws AuraException, RemoteException {
        strategy.deleteItem(itemKey);
    }
    
    /**
     * Deletes just a user from the item store, not touching the attention.
     */
    public void deleteUser(String userKey) throws AuraException, RemoteException {
        deleteItem(userKey);
    }
    
    public void deleteLocalAttention(List<Long> ids) throws AuraException, RemoteException {
        strategy.deleteLocalAttention(ids);
    }
    
    public DBIterator<Item> getItemsAddedSince(ItemType type, Date timeStamp)
            throws AuraException, RemoteException {
        return strategy.getItemsAddedSince(type, timeStamp);
    }

    public List<Item> getItems(User user, Type attnType, ItemType itemType)
            throws AuraException, RemoteException {
        return strategy.getItems(user, attnType, itemType);
    }

    public List<Attention> getAttention(AttentionConfig ac)
            throws AuraException, RemoteException {
        return strategy.getAttention(ac);
    }

    public DBIterator<Attention> getAttentionIterator(AttentionConfig ac)
            throws AuraException, RemoteException {
        return strategy.getAttentionIterator(ac);
    }

    public Long getAttentionCount(AttentionConfig ac)
            throws AuraException, RemoteException {
        return strategy.getAttentionCount(ac);
    }
    
    public List<Attention> getAttentionSince(AttentionConfig ac,
                                             Date timeStamp)
            throws AuraException, RemoteException {
        return strategy.getAttentionSince(ac, timeStamp);
    }

    public DBIterator<Attention> getAttentionSinceIterator(AttentionConfig ac,
                                                           Date timeStamp)
            throws AuraException, RemoteException {
        return strategy.getAttentionSinceIterator(ac, timeStamp);
    }

    public Long getAttentionSinceCount(AttentionConfig ac,
                                       Date timeStamp)
            throws AuraException, RemoteException {
        return strategy.getAttentionSinceCount(ac, timeStamp);
    }
    
    public List<Attention> getLastAttention(AttentionConfig ac,
                                            int count)
            throws AuraException, RemoteException {
        return strategy.getLastAttention(ac, count);
    }
    
    public List<Attention> getAttentionForSource(String srcKey)
            throws AuraException, RemoteException {
        throw new UnsupportedOperationException("getAttentionForSource no longer supported");
    }
    
    public List<Attention> getAttentionForSource(String srcKey,
                                                Attention.Type type)
            throws AuraException, RemoteException {
        throw new UnsupportedOperationException("getAttentionForSource no longer supported");
    }
    
    public List<Attention> getAttentionForTarget(String itemKey)
            throws AuraException, RemoteException {
        throw new UnsupportedOperationException("getAttentionForTarget no longer supported");
    }

    public Attention attend(Attention att)
            throws AuraException, RemoteException {
        return strategy.attend(att);
    }

    public List<Attention> attend(List<Attention> attns)
            throws AuraException, RemoteException {
        return strategy.attend(attns);
    }

    public void removeAttention(String srcKey, String targetKey,
                                Attention.Type type)
            throws AuraException, RemoteException {
        strategy.removeAttention(srcKey, targetKey, type);
    }
    
    public void removeAttention(String itemKey)
            throws AuraException, RemoteException {
        strategy.removeAttention(itemKey);
    }
    
    public DBIterator<Attention> getAttentionSince(Date timeStamp)
            throws AuraException, RemoteException {
        throw new UnsupportedOperationException("getAttentionSince no longer supported");
    }
    public DBIterator<Attention> getAttentionForSourceSince(String sourceKey,
            Date timeStamp) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("getAttentionForSourceSince no longer supported");
    }
    
    public DBIterator<Attention> getAttentionForTargetSince(String targetKey,
            Date timeStamp) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("getAttentionForTargetSince no longer supported");
    }

    public List<Attention> getLastAttentionForSource(String srcKey,
                                                          int count)
            throws AuraException, RemoteException {
        return getLastAttentionForSource(srcKey, null, count);
    }

    public List<Attention> getLastAttentionForSource(String srcKey,
                                                          Type type,
                                                          int count)
            throws AuraException, RemoteException {
        return strategy.getLastAttentionForSource(srcKey, type, count);
    }

    public void addItemListener(ItemType itemType, ItemListener listener)
            throws AuraException, RemoteException {
        //
        // Should the listener only go down to the partition cluster level?
        strategy.addItemListener(itemType, listener);
    }

    public void removeItemListener(ItemType itemType, ItemListener listener)
            throws AuraException, RemoteException {
        //
        // Should the listener only go down to the partition cluster level?
        strategy.removeItemListener(itemType, listener);
    }

    public long getItemCount(ItemType itemType)
            throws AuraException, RemoteException {
        return strategy.getItemCount(itemType);
    }

    public long getAttentionCount() throws AuraException, RemoteException {
        throw new UnsupportedOperationException("getAttentionCount() no longer supported");
    }
    
    public List<FieldFrequency> getTopValues(String field, int n, boolean ignoreCase) throws AuraException, RemoteException {
        return strategy.getTopValues(field, n, ignoreCase);
    }

    public List<Scored<Item>> query(String query, int n, ResultsFilter rf) 
            throws AuraException, RemoteException {
        return strategy.query(query, "-score", n, rf);
    }

    public List<Scored<Item>> query(String query, String sort, int n, ResultsFilter rf) 
            throws AuraException, RemoteException {
        return strategy.query(query, sort, n, rf);
    }
    
    public List<Scored<Item>> getAutotagged(String autotag, int n)
            throws AuraException, RemoteException {
        return strategy.getAutotagged(autotag, n);
    }

    public List<Scored<String>> getTopAutotagTerms(String autotag, int n)
            throws AuraException, RemoteException {
        return strategy.getTopAutotagTerms(autotag, n);
    }

    public List<Scored<String>> findSimilarAutotags(String autotag, int n)
            throws AuraException, RemoteException {
        return strategy.findSimilarAutotags(autotag, n);
    }

    public List<Scored<String>> explainSimilarAutotags(String a1, String a2,
            int n)
            throws AuraException, RemoteException {
        return strategy.explainSimilarAutotags(a1, a2, n);
    }

    public WordCloud getTopTerms(String key, String field, int n)
            throws AuraException, RemoteException {
        return strategy.getTopTerms(key, field, n);
    }

    public List<Scored<String>> getExplanation(String key, String autoTag,
            int n)
            throws AuraException, RemoteException {
        return strategy.getExplanation(key, autoTag, n);
    }

    public DocumentVector getDocumentVector(String key, SimilarityConfig config) throws RemoteException, AuraException {
        return strategy.getDocumentVector(key, config);
    }

    public DocumentVector getDocumentVector(WordCloud cloud, SimilarityConfig config) throws RemoteException, AuraException {
        return strategy.getDocumentVector(cloud, config);
    }

    public List<Scored<Item>> findSimilar(DocumentVector dv, SimilarityConfig config) throws AuraException, RemoteException {
        return strategy.findSimilar(dv, config);
    }

    public synchronized void close() throws AuraException, RemoteException {
        if (!closed) {
            //
            // do something
            closed = true;
        }
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        prefixCode = DSBitSet.parse(ps.getString(PROP_PREFIX));
        dataStoreHead = (DataStore) ps.getComponent(PROP_DATA_STORE_HEAD);
        PartitionCluster exported = (PartitionCluster) ps.getConfigurationManager().getRemote(this, dataStoreHead);
        try {
            logger.info("Registering partition cluster: " + exported.getPrefix());
            dataStoreHead.registerPartitionCluster(exported);
        } catch (RemoteException rx) {
            throw new PropertyException(ps.getInstanceName(), PROP_DATA_STORE_HEAD, 
                    "Unable to add partition cluster to data store");
        }
    }
    
    public void addReplicant(Replicant replicant) throws RemoteException {
        logger.log(Level.INFO, "Adding replicant with prefix " + replicant.getPrefix());
        if (replicant.getPrefix().equals(prefixCode)) {
            this.replicant = replicant;
            strategy = new PCDefaultStrategy(replicant);
        } else {
            logger.log(Level.SEVERE, "Adding replicant with wrong prefix our prefix: " +
                    prefixCode + " prefix added: " + replicant.getPrefix());
        }
    }
    
    public Replicant getReplicant() throws RemoteException {
        return replicant;
    }

    public void split() throws AuraException, RemoteException {
        if (!splitting.compareAndSet(false, true)) {
            //
            // We must already be splitting.
            throw new AuraException("A split is already in progress for " + prefixCode);
        }
        //
        // Determine what our new sub prefixes will be
        DSBitSet localPrefix = (DSBitSet)prefixCode.clone();
        localPrefix.addBit(false);
        DSBitSet remotePrefix = (DSBitSet)prefixCode.clone();
        remotePrefix.addBit(true);

        //
        // Use the Process Manager to get a new partition cluster for the "1"
        // sub prefix
        ProcessManager pMgr = null;
        PartitionCluster remote = pMgr.createPartitionCluster(remotePrefix);
        PCSplitStrategy splitStrat =
                new PCSplitStrategy(strategy, localPrefix,
                                    remote, remotePrefix);
        
        //
        // Install the split strategy for all requests going forward
        strategy = splitStrat;
        
        //
        // Start a thread that will read every item from this partition and
        // migrate if necessary.  Ditto for attentions.
        List<Thread> threads = new ArrayList<Thread>();
        Thread it = new Thread(new MigrateItems(remote, remotePrefix));
        it.start();
        threads.add(it);
        Thread at = new Thread(new MigrateAttentions(remote, remotePrefix));
        at.start();
        threads.add(at);
        Thread ender = new Thread(new SplitMonitor(threads, localPrefix, remote));
        ender.start();
    }

    protected void endSplit(DSBitSet newLocalPrefix, PartitionCluster remote) {
        //
        // Notify the data store heads that the partition clusters have
        // changed.  Once that has happened, switch back to a default
        // strategy and our new prefix
        try {
            dataStoreHead.registerPartitionSplit(this, remote);
            prefixCode = newLocalPrefix;
        } catch (RemoteException e) {
            logger.log(Level.WARNING, "Failed to register partition split", e);
        }
        
        strategy = new PCDefaultStrategy(replicant);
    }
    
    public void start() {
    }

    public void stop() {
        try {
            close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to close properly", e);
        }
    }

    class MigrateItems implements Runnable {
        protected PartitionCluster remote;
        protected DSBitSet remotePrefix;
        
        public MigrateItems(PartitionCluster remote,
                                  DSBitSet remotePrefix) {
            this.remote = remote;
            this.remotePrefix = remotePrefix;
        }
        
        public void run() {
            //
            // Start reading items and writing them as necessary.
            try {
                DBIterator<Item> items = getAllIterator(null);
                while (items.hasNext()) {
                    Item i = items.next();
                    if (!Util.keyIsLocal(i.hashCode(),
                                        prefixCode,
                                        remotePrefix)) {
                        remote.putItem(i);
                        deleteItem(i.getKey());
                    }
                }
            } catch (AuraException e) {
                logger.log(Level.SEVERE, "Split/migrate items failed", e);
            } catch (RemoteException ex) {
                logger.log(Level.SEVERE, "Split/migrate items failed", ex);
            }
        }
    }
    
    class MigrateAttentions implements Runnable {
        protected PartitionCluster remote;
        protected DSBitSet remotePrefix;
        
        public MigrateAttentions(PartitionCluster remote, DSBitSet remotePrefix) {
            this.remote = remote;
            this.remotePrefix = remotePrefix;
        }
        
        public void run() {
            //
            // Start reading attentions and writing them as necessary
            try {
                List<Attention> migrate = new ArrayList<Attention>();
                List<Long> ids = new ArrayList<Long>();
                DBIterator<Attention> attns =
                        getAttentionIterator(new AttentionConfig());
                while (attns.hasNext()) {
                    Attention a = attns.next();
                    if (!Util.keyIsLocal(a.hashCode(),
                                         prefixCode,
                                         remotePrefix)) {
                        migrate.add(a);
                        ids.add(((PersistentAttention)a).getID());
                    }
                    if (migrate.size() >= 100) {
                        remote.attend(migrate);
                        deleteLocalAttention(ids);
                        migrate.clear();
                        ids.clear();
                    }
                }
            } catch (AuraException e) {
                logger.log(Level.SEVERE, "Attention Migration failed", e);
            } catch (RemoteException ex) {
                logger.log(Level.SEVERE, "Attention Migration failed", ex);
            }
        }
    }
    
    class SplitMonitor implements Runnable {
        protected List<Thread> migrateThreads;
        protected DSBitSet localPrefix;
        protected PartitionCluster remote;
        
        public SplitMonitor(List<Thread> migrateThreads,
                            DSBitSet newLocalPrefix,
                            PartitionCluster remote) {
            this.migrateThreads = migrateThreads;
            this.localPrefix = newLocalPrefix;
            this.remote = remote;
        }
        
        public void run() {
            try {
                for (Thread t : migrateThreads) {
                    t.join();
                }
                //
                // Once all our threads finished, we can signal that
                // migration is done
                endSplit(localPrefix, remote);
            } catch (InterruptedException e) {
                logger.severe("Migration was interrupted");
            }
        }
    }
    
}
