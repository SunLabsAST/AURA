
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
import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.aura.util.Counted;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.query.Element;
import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ComponentListener;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigComponentList;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurableMXBean;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A partition cluster stores one segment of the item store's data.  The
 * segment that it stores is determined by matching the prefix of each item's
 * key with the prefix for the cluster.
 */
public class PartitionClusterImpl implements PartitionCluster,
                                             Configurable, ConfigurableMXBean,
                                             ComponentListener, AuraService {
    
    @ConfigString
    public static final String PROP_PREFIX = "prefix";
    
    @ConfigComponentList(type=com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE_HEADS = "dataStoreHeads";
    
    @ConfigComponent(type=com.sun.labs.aura.datastore.impl.ProcessManager.class)
    public static final String PROP_PROC_MGR = "processManager";
    
    @ConfigBoolean(defaultValue=true)
    public static final String PROP_REGISTER = "register";
    
    @ConfigString(defaultValue="")
    public static final String PROP_OWNER = "owner";
    
    /**
     * The prefix of the owner of this partition, used when splitting
     */
    private String owner;
    
    private boolean register;
    
    private Set<DataStore> dataStoreHeads;
    
    private ProcessManager processManager;
    
    private ConfigurationManager cm;
    
    protected DSBitSet prefixCode;
        
    protected boolean closed = false;

    //protected List<BerkeleyItemStore> replicants;
    protected Replicant replicant;
        
    protected PCStrategy strategy;
    
    protected Logger logger;

    private String[] properties;
    
    protected AtomicBoolean splitting = new AtomicBoolean(false);
    
    protected boolean migrateItemsSucceeded = false;
    
    protected boolean migrateAttnSucceeded = false;
    
    /**
     * Construct a PartitionClusterImpl for use with a particular item prefix.
     * 
     * @param prefixCode the initial prefix that this cluster represents
     */
    public PartitionClusterImpl() {
        dataStoreHeads = new HashSet();
    }
    
    public DSBitSet getPrefix() {
        return prefixCode;
    }
    
    public void defineField(String fieldName)
            throws AuraException, RemoteException {
        defineField(fieldName, null, null);
    }

    public void defineField(String fieldName,
            Item.FieldType fieldType, EnumSet<Item.FieldCapability> caps) throws AuraException, RemoteException {
        strategy.defineField(fieldName, fieldType, caps);
    }
    
    public Map<String,FieldDescription> getFieldDescriptions()
            throws RemoteException {
        return replicant.getFieldDescriptions();
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
    
    public List<Scored<Item>> getScoredItems(List<Scored<String>> keys) throws AuraException, RemoteException {
        enter("gSIs");
        List<Scored<Item>> ret = strategy.getScoredItems(keys);
        exit("gSIs");
        return ret;
    }

    public Collection<Item> getItems(Collection<String> keys) throws AuraException, RemoteException {
        enter("gIs");
        Collection<Item> ret = strategy.getItems(keys);
        exit("gIs");
        return ret;
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

    public Object processAttention(AttentionConfig ac, String script, String language)
            throws AuraException, RemoteException {
        return strategy.processAttention(ac, script, language);
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
    
    public List<FieldFrequency> getTopValues(String field, int n, boolean ignoreCase) throws AuraException, RemoteException {
        return strategy.getTopValues(field, n, ignoreCase);
    }

    public List<Scored<String>> query(String query, int n, ResultsFilter rf) 
            throws AuraException, RemoteException {
        return strategy.query(query, "-score", n, rf);
    }

    public List<Scored<String>> query(String query, String sort, int n, ResultsFilter rf) 
            throws AuraException, RemoteException {
        enter("query");
        List<Scored<String>> ret = strategy.query(query, sort, n, rf);
        exit("query", query);
        return ret;
    }
    
    public List<Scored<String>> query(Element query, int n, ResultsFilter rf)
            throws AuraException, RemoteException {
        return query(query, "-score", n, rf);
    }

    public List<Scored<String>> query(Element query, String sort, int n, ResultsFilter rf)
            throws AuraException, RemoteException {
        enter("query");
        List<Scored<String>> ret = strategy.query(query, sort, n, rf);
        exit("query", query.toString());
        return ret;
    }

    public List<Scored<String>> getAutotagged(String autotag, int n)
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

    public List<Counted<String>> getTopTermCounts(String key, String field,
                                                  int n)
            throws AuraException, RemoteException {
        return strategy.getTopTermCounts(key, field, n);
    }
    public List<Scored<String>> getExplanation(String key, String autoTag,
            int n)
            throws AuraException, RemoteException {
        return strategy.getExplanation(key, autoTag, n);
    }

    public MarshalledObject<DocumentVector> getDocumentVector(String key, SimilarityConfig config) throws RemoteException, AuraException {
        return strategy.getDocumentVector(key, config);
    }

    public MarshalledObject<DocumentVector> getDocumentVector(WordCloud cloud, SimilarityConfig config) throws RemoteException, AuraException {
        return strategy.getDocumentVector(cloud, config);
    }

    public MarshalledObject<List<Scored<String>>> findSimilar(MarshalledObject<DocumentVector> dv, 
            MarshalledObject<SimilarityConfig> config) throws AuraException, RemoteException {
        enter("fs");
        MarshalledObject<List<Scored<String>>> ret = strategy.findSimilar(dv, config);
        exit("fs");
        return ret;
    }

    public List<String> getSupportedScriptLanguages()
            throws AuraException, RemoteException {
        return strategy.getSupportedScriptLanguages();
    }

    public synchronized void close() throws AuraException, RemoteException {
        if (!closed) {
            //
            // do something
            strategy.close();
        }
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        properties = new String[] {"logLevel"};
        logger = ps.getLogger();
        prefixCode = DSBitSet.parse(ps.getString(PROP_PREFIX));
        processManager = (ProcessManager)ps.getComponent(PROP_PROC_MGR, this);
        if (processManager == null) {
            logger.info("No ProcessManager was found, splitting will fail");
        }
        register = ps.getBoolean(PROP_REGISTER);
        cm = ps.getConfigurationManager();
        if (register) {
            List<DataStore> heads = (List<DataStore>) ps.getComponentList(PROP_DATA_STORE_HEADS, this);
            for(DataStore head : heads) {
                register(head);
            }
        }
        owner = ps.getString(PROP_OWNER);
    }
    
    @Override
    public String[] getProperties() {
        return properties.clone();
    }

    @Override
    public String getValue(String property) {
        logger.info(String.format("getValue: %s", property));
        if(property.equals("logLevel")) {
            logger.info("level: " + logger.getLevel());
            return logger.getLevel().toString();
        }
        return null;
    }

    @Override
    public String[] getValues(String arg0) {
        return null;
    }

    @Override
    public boolean setValue(String property, String value) {
        if(property.equals("logLevel")) {
            try {
                Level l = Level.parse(value);
                logger.setLevel(l);
            } catch(IllegalArgumentException ex) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean setValues(String property, String[] values) {
        return false;
    }

    public void componentAdded(Component c) {
        if(c instanceof DataStore && register) {
            register((DataStore) c);
        } else if(c instanceof ProcessManager) {
            if(processManager == null) {
                processManager = (ProcessManager) c;
            }
        }
    }

    public void componentRemoved(Component c) {
        if(c instanceof DataStore && register) {
            dataStoreHeads.remove((DataStore) c);
        } else if(c instanceof ProcessManager) {
            processManager = null;
        }
    }

    private void register(DataStore ds) {
        PartitionCluster exported = (PartitionCluster) cm.getRemote(this, ds);

        try {
            logger.info("Registering partition cluster: " + exported.getPrefix());
            ds.registerPartitionCluster(exported);
            dataStoreHeads.add(ds);
        } catch(RemoteException rx) {
            throw new PropertyException(rx, null,
                    PROP_DATA_STORE_HEADS,
                    "Unable to add partition cluster to data store");
        }
        
    }

    public void addReplicant(Replicant replicant) throws RemoteException {
        logger.log(Level.INFO, "Adding replicant with prefix " + replicant.getPrefix());
        if (replicant.getPrefix().equals(prefixCode)) {
            this.replicant = replicant;
            strategy = new PCDefaultStrategy(replicant);
            synchronized(this) {
                notifyAll();
            }
        } else {
            logger.log(Level.SEVERE, "Adding replicant with wrong prefix our prefix: " +
                    prefixCode + " prefix added: " + replicant.getPrefix());
        }
    }
    
    public Replicant getReplicant() throws RemoteException {
        return replicant;
    }
    
    public boolean isReady() {
        if (replicant != null && strategy != null) {
            return true;
        }
        return false;
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
        logger.info("Starting to split " + prefixCode + " into "
                    + localPrefix + " and " + remotePrefix);

        //
        // Use the Process Manager to get a new partition cluster for the "1"
        // sub prefix.  Pass in the current prefix as the owner of the partition.
        PartitionCluster remote =
                processManager.createPartitionCluster(remotePrefix, prefixCode);
        if (remote == null) {
            throw new AuraException(
                    "Failed to find remote partition cluster for "
                    + remotePrefix);
        }
        logger.info("Got new partition cluster for " + remote.getPrefix());
        innerSplit(localPrefix, remotePrefix, remote);
    }

    public void resumeSplit(PartitionCluster remote) throws AuraException, RemoteException {
        if (!splitting.compareAndSet(false, true)) {
            //
            // We must already be splitting.
            throw new AuraException("A split is already in progress for " + prefixCode);
        }

        //
        // This may be called before we are totally set up (before we have a
        // replicant).  Wait until we do have one.
        synchronized(this) {
            while (!isReady()) {
                try {
                    wait(5000);
                } catch (InterruptedException e) {
                    // doesn't matter
                }
            }
        }
        DSBitSet localPrefix = (DSBitSet)prefixCode.clone();
        localPrefix.addBit(false);
        logger.info("Resuming split from " + prefixCode + " into "
                    + localPrefix + " and " + remote.getPrefix());
        innerSplit(localPrefix, remote.getPrefix(), remote);
    }
    
    protected void innerSplit(DSBitSet localPrefix, DSBitSet remotePrefix, PartitionCluster remote)
            throws AuraException, RemoteException {
        try {
            //
            // Define all fields in the new partition
            Map<String,FieldDescription> fields = getFieldDescriptions();
            for (FieldDescription fd : fields.values()) {
                remote.defineField(fd.getName(), fd.getType(), fd.getCapabilities());
            }

            logger.info("Fields defined in " + remote.getPrefix());

            PCSplitStrategy splitStrat =
                    new PCSplitStrategy(strategy, localPrefix,
                                        remote, remotePrefix);


            //
            // Start a thread that will read every item from this partition and
            // migrate if necessary.  Ditto for attentions.
            List<Thread> threads = new ArrayList<Thread>();
            Thread it = new Thread(new MigrateItems(strategy, localPrefix, remote, remotePrefix));
            it.setDaemon(true);
            it.start();
            threads.add(it);
            Thread at = new Thread(new MigrateAttentions(strategy, localPrefix, remote, remotePrefix));
            at.setDaemon(true);
            at.start();
            threads.add(at);
            Thread ender = new Thread(new SplitMonitor(threads, localPrefix, remote));
            ender.setDaemon(true);
            ender.start();
            logger.info("Started split threads");

            //
            // Install the split strategy for all requests going forward
            strategy = splitStrat;
        } catch (AuraException ax) {
            logger.log(Level.SEVERE, "Aura exception splitting", ax);
            throw(ax);
        } catch (RemoteException rx) {
            logger.log(Level.SEVERE, "Remote exception splitting", rx);
            throw(rx);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Other exception splitting", ex);
        }
    }

    protected void endSplit(DSBitSet newLocalPrefix, PartitionCluster remote) {
        //
        // Notify the data store heads that the partition clusters have
        // changed.  Once that has happened, switch back to a default
        // strategy and our new prefix
        try {
            processManager.finishSplit(prefixCode, newLocalPrefix, remote.
                    getPrefix());
            replicant.setPrefix(prefixCode);
        } catch(RemoteException rx) {
            logger.log(Level.SEVERE, "Error finishing split", rx);
        } catch(AuraException ax) {
            logger.log(Level.SEVERE, "Error finishing split", ax);
        }            
        
        logger.info("Split finished, registering new partitions");
        prefixCode = newLocalPrefix;
        for(DataStore ds : dataStoreHeads) {
            PartitionCluster exported =
                    (PartitionCluster) cm.getRemote(this, ds);
            try {
                ds.registerPartitionSplit(exported, remote);
            } catch(RemoteException e) {
                logger.log(Level.WARNING, "Failed to register partition split",
                        e);
            }
        }
        splitting.set(false);
        strategy = new PCDefaultStrategy(replicant);
    }
    
    public void start() {
        if (owner != null && !owner.isEmpty()) {
            //
            // Look up the owner partition and call resume split on it.
            try {
                //
                // Get all partition clusters
                List<Component> pcs = cm.lookupAll(PartitionCluster.class, this);
                for (Component c : pcs) {
                    PartitionCluster pc = (PartitionCluster)c;
                    if (pc.getPrefix().toString().equals(owner)) {
                        //
                        // This is the one
                        synchronized(this) {
                            while (!isReady()) {
                                try {
                                    wait(5000);
                                } catch (InterruptedException e) {
                                    // doesn't matter
                                }
                            }
                        }
                        pc.resumeSplit((PartitionCluster)cm.getRemote(this));
                        break;
                    }
                }
            } catch (RemoteException e) {
                throw new PropertyException(e, null, PROP_OWNER,
                        "Failed to invoke resumeSplit on owner partition");
            } catch (AuraException e) {
                throw new PropertyException(e, null, PROP_OWNER,
                        "Failed to resume split with partition " + owner);
            }
        }
    }

    public void stop() {
        try {
            long t = System.currentTimeMillis();
            System.out.println(String.format("[%tD %tT:%tL] %s",
                                       t, 
                                       t, 
                                       t,
                                       "Partition cluster " + prefixCode + " shutting down."));
            close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to close properly", e);
        }
    }

    protected void enter(String name) {
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, String.format("pc %s T%s enter %s", prefixCode, Thread.currentThread().getId(), name));
        }
    }

    protected void exit(String name) {
        exit(name, "");
    }

    protected void exit(String name, String extra) {
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, String.format(" pc %s T%s exit  %s: %s", prefixCode, Thread.currentThread().getId(), name, extra));
        }
    }

    class MigrateItems implements Runnable {
        protected PartitionCluster remote;
        protected DSBitSet remotePrefix;
        /** the local strategy so we're only reading from this partition */
        protected PCStrategy local;
        protected DSBitSet newPrefix;
        
        public MigrateItems(PCStrategy local, DSBitSet newPrefix,
                            PartitionCluster remote, DSBitSet remotePrefix) {
            this.local = local;
            this.newPrefix = newPrefix;
            this.remote = remote;
            this.remotePrefix = remotePrefix;
        }
        
        public void run() {
            //
            // Start reading items and writing them as necessary.
            DBIterator<Item> items = null;
            long numProcessed = 0;
            try {
                items = local.getAllIterator(null);
                while (items.hasNext()) {
                    Item i = items.next();
                    if (!Util.keyIsLocal(i.hashCode(),
                                        newPrefix,
                                        remotePrefix)) {
                        remote.putItem(i);
                        local.deleteItem(i.getKey());
                        numProcessed++;
                    }
                }
                migrateItemsSucceeded = true;
            } catch (AuraException e) {
                logger.log(Level.SEVERE, "Split/migrate items failed", e);
            } catch (RemoteException ex) {
                logger.log(Level.SEVERE, "Split/migrate items failed", ex);
            } finally {
                try {
                    if (items != null) {
                        items.close();
                        logger.info("Migrated a total of "
                                    + numProcessed + " items");
                    }
                } catch (RemoteException e) {
                }
            }
        }
    }
    
    class MigrateAttentions implements Runnable {
        /** the local strategy so we're only reading from this partition */
        protected PCStrategy local;
        protected DSBitSet newPrefix;
        protected PartitionCluster remote;
        protected DSBitSet remotePrefix;
        
        public MigrateAttentions(PCStrategy local,
                                 DSBitSet newPrefix,
                                 PartitionCluster remote,
                                 DSBitSet remotePrefix) {
            this.local = local;
            this.newPrefix = newPrefix;
            this.remote = remote;
            this.remotePrefix = remotePrefix;
        }
        
        public void run() {
            //
            // Start reading attentions and writing them as necessary
            DBIterator<Attention> attns = null;
            //
            // We expect to see a single invalid attention go across here
            // (the invalid one won't necessarily have the right hashcode
            // to belong here).  Allow one, but truly fail if there is another
            long invalid = 0;
            long numProcessed = 0;
            try {
                List<Attention> migrate = new ArrayList<Attention>();
                List<Long> ids = new ArrayList<Long>();
                attns = local.getAttentionIterator(new AttentionConfig());
                while (attns.hasNext()) {
                    Attention a = attns.next();
                    boolean isRemote = false;
                    try {
                        isRemote = !Util.keyIsLocal(a.hashCode(),
                                                    newPrefix,
                                                    remotePrefix);
                    } catch (AuraException ae) {
                        invalid++;
                        if (invalid > 1) {
                            throw ae;
                        }
                    }
                    if (isRemote) {
                        migrate.add(a);
                        ids.add(((PersistentAttention)a).getID());
                    }
                    if (migrate.size() >= 2000) {
                        remote.attend(migrate);
                        local.deleteAttention(ids);
                        numProcessed += migrate.size();
                        migrate.clear();
                        ids.clear();
                    }
                }
                //
                // If there are any more we didn't get to, finish them now
                if (!migrate.isEmpty()) {
                    remote.attend(migrate);
                    local.deleteAttention(ids);
                    numProcessed += migrate.size();
                    migrate.clear();
                    ids.clear();
                }
                migrateAttnSucceeded = true;
            } catch (AuraException e) {
                logger.log(Level.SEVERE, "Attention Migration failed", e);
            } catch (RemoteException ex) {
                logger.log(Level.SEVERE, "Attention Migration failed", ex);
            } finally {
                try {
                    if (attns != null) {
                        attns.close();
                        logger.info("Migrated a total of "
                                    + numProcessed + " attentions");
                    }
                } catch (RemoteException e) {
                }
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
                if (migrateItemsSucceeded && migrateAttnSucceeded) {
                    //
                    // Once all our threads finished, we can signal that
                    // migration is done
                    endSplit(localPrefix, remote);
                    migrateItemsSucceeded = false;
                    migrateAttnSucceeded = false;
                } else {
                    logger.severe("Migrate threads finished but did not succeeded.  Staying in split state.");
                }
            } catch (InterruptedException e) {
                logger.severe("Migration was interrupted");
            }
        }
    }

}
