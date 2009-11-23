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

import com.sleepycat.collections.CurrentTransaction;
import com.sleepycat.persist.evolve.EvolveEvent;
import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import com.sleepycat.je.CursorConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentStats;
import com.sleepycat.je.LockConflictException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.je.rep.InsufficientAcksException;
import com.sleepycat.je.rep.InsufficientLogException;
import com.sleepycat.je.rep.InsufficientReplicasException;
import com.sleepycat.je.rep.NetworkRestore;
import com.sleepycat.je.rep.NetworkRestoreConfig;
import com.sleepycat.je.rep.QuorumPolicy;
import com.sleepycat.je.rep.ReplicaConsistencyException;
import com.sleepycat.je.rep.ReplicaWriteException;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.je.rep.ReplicationConfig;
import com.sleepycat.je.rep.TimeConsistencyPolicy;
import com.sleepycat.je.rep.UnknownMasterException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.ForwardCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.evolve.EvolveConfig;
import com.sleepycat.persist.evolve.EvolveListener;
import com.sleepycat.persist.evolve.EvolveStats;
import com.sleepycat.persist.evolve.Mutations;
import com.sleepycat.persist.model.AnnotationModel;
import com.sleepycat.persist.model.EntityModel;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.aura.datastore.impl.store.persist.IntAndTimeKey;
import com.sun.labs.aura.datastore.impl.store.persist.UserImpl;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.aura.datastore.impl.store.persist.StringAndTimeKey;
import com.sun.labs.aura.util.AuraReplicantWriteException;
import com.sun.labs.aura.util.Times;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a wrapper around all the berkeley DB iteractions to isolate it
 * from other logic in the item store.
 */
public class BerkeleyDataWrapper {

    /**
     * The max number of times to retry a deadlocked transaction before
     * admitting failure.
     */
    protected final static int MAX_RETRIES = 10;

    protected final static int MAX_OPEN_RETRIES = 10;

    /**
     * The actual database environment.  If we're replicated, this will
     * actually be a reference to a ReplicatedEnvironment.
     */
    protected Environment dbEnv;

    /**
     * The replicated database environment if we're replicated, otherwise null.
     * We keep this extra reference so that we don't have to cast later.
     */
    protected ReplicatedEnvironment repDbEnv;

    /**
     * The directory where our DB environment lives
     */
    protected File dbEnvDir;

    /**
     * What percent of memory should be used for cache
     */
    protected int cacheSizeMemPercentage;

    /**
     * Is this a replicated environment?
     */
    protected boolean replicated;

    /**
     * The name of the group to which this environment belongs
     */
    protected String groupName;

    /**
     * The logical name of the this node within the group
     */
    protected String nodeName;

    /**
     * The address (host and port) of this node's listen socket
     */
    protected String nodeHostPort;

    /**
     * A helper node to use to stitch into the group
     */
    protected String nodeHelper;

    /**
     * A mechanism for quiescing all activity in case we need to reopen
     * the environment.
     */
    protected ReentrantReadWriteLock quiesce;
    /**
     * The store inside the environment where all our indexes will live
     */
    protected EntityStore store;
    
    /**
     * The index of all field descriptions in the store, accessible by field
     * name.
     */
    protected PrimaryIndex<String, FieldDescription> fieldByName;

    /**
     * The index of all Items in the store, accessible by key
     */
    protected PrimaryIndex<String, ItemImpl> itemByKey;

    /**
     * The index of all Items in the store, accessible by type
     */
    protected SecondaryIndex<Integer, String, ItemImpl> itemByType;

    protected SecondaryIndex<IntAndTimeKey, String, ItemImpl> itemByTypeAndTime;

    /**
     * A subset of the item index that only indexes users
     */
    protected SecondaryIndex<Boolean, String, UserImpl> allUsers;

    /**
     * Only users, indexed by the random string associated with them
     */
    protected SecondaryIndex<String, String, UserImpl> usersByRandString;
    
    /**
     * The index of all Attention in the item store, accessible by ID
     */
    protected PrimaryIndex<Long, PersistentAttention> allAttn;

    /**
     * The index of all Attention in the item store, accessible by
     * the associated item
     */
    protected SecondaryIndex<String, Long, PersistentAttention> attnByTargetKey;

    /**
     * The index of all Attention in the item store, accessible by
     * the associated user
     */
    protected SecondaryIndex<String, Long, PersistentAttention> attnBySourceKey;

    /**
     * The index of all Attention in the item store, accessible by
     * the type of attention
     */
    protected SecondaryIndex<Integer, Long, PersistentAttention> attnByType;

    /**
     * The index of all Attention, accessible by the timestamp
     */
    protected SecondaryIndex<Long, Long, PersistentAttention> attnByTime;

    /**
     * The index of all Attention in the item store, accessible by
     * the composite key of source ID and timestamp.
     */
    protected SecondaryIndex<StringAndTimeKey, Long, PersistentAttention> attnBySourceAndTime;
    
    /**
     * The index of all Attention in the item store, accessible by
     * the composite key of target ID and timestamp
     */
    protected SecondaryIndex<StringAndTimeKey, Long, PersistentAttention> attnByTargetAndTime;
    
    /**
     * The index of all Attention in the item store, accessible by
     * the meta-data string provided by the user
     */
    protected SecondaryIndex<String, Long, PersistentAttention> attnByStringVal;
    
    /**
     * The index of all Attention in the item store, accessible by
     * the meta-data number provided by the user
     */
    protected SecondaryIndex<Long, Long, PersistentAttention> attnByNumberVal;
    
    protected Map<String,FieldDescription> fields;

    protected Logger log;

    /**
     * Constructs a database wrapper.
     * 
     * @param dbEnvDir the environment directory for the database
     * @param logger a logger to use for messages
     * @throws com.sleepycat.je.DatabaseException
     */
    public BerkeleyDataWrapper(String dbEnvDir, Logger logger)
            throws AuraException {
        this(dbEnvDir, logger, 60, false);
    }

    /**
     * Constructs a database wrapper.
     *
     * @param dbEnvDir the environment directory for the database
     * @param logger a logger to use for messages
     * @param cacheSizeMemPercentage amount of memory to use for cache
     * @throws com.sleepycat.je.DatabaseException
     */
    public BerkeleyDataWrapper(String dbEnvDir,
                               Logger logger,
                               int cacheSizeMemPercentage,
                               boolean replicated)
        throws AuraException {
        this(dbEnvDir, logger, cacheSizeMemPercentage, replicated, null, null, null, null);
    }
    /**
     * Constructs a database wrapper.
     * 
     * @param dbEnvDir the environment directory for the database
     * @param logger a logger to use for messages
     * @param cacheSizeMemPercentage amount of memory to use for cache
     * @param groupName name of the group to belong in if replicated.  Must be
     *                  null otherwise.
     * @param nodeName name of this node in the group
     * @param nodeHostPort a host name (for this host) and port to bind to
     * @param helperHost another host in this group, or its own nodeHostPort
     * @throws com.sleepycat.je.DatabaseException
     */
    public BerkeleyDataWrapper(String dbEnvDir,
                               Logger logger,
                               int cacheSizeMemPercentage,
                               boolean replicated,
                               String groupName,
                               String nodeName,
                               String nodeHost,
                               String helperHost)
            throws AuraException {
        this.log = logger;
        this.cacheSizeMemPercentage = cacheSizeMemPercentage;
        this.replicated = replicated;
        this.groupName = groupName;
        this.nodeName = nodeName;
        this.nodeHostPort = nodeHost;
        this.nodeHelper = helperHost;

        quiesce = new ReentrantReadWriteLock();

        //
        // See if our DB dir exists, or if we can create it
        this.dbEnvDir = new File(dbEnvDir);
        if(!this.dbEnvDir.exists()) {
            if (!this.dbEnvDir.mkdirs()) {
                log.severe("Failed to make DB home dir!");
            }
        }

        openBDB();

        //
        // Did we have our invalid attention item stored to keep all 'columns'
        // populated with at least one value?
        PersistentAttention bad = PersistentAttention.INVALID_ATTN;
        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(bad.getSourceKey());
        ac.setTargetKey(bad.getTargetKey());
        long cnt = getAttentionCount(ac);
        if (cnt < 1) {
            try {
                logger.fine("Storing the all-fields-filled attention");
                putAttention(bad);
            } catch (AuraException e) {
                logger.fine("Storing the-all-fields-filled attention failed!");
            }
        }

        //
        // Same thing for users - make sure we have at least one user that
        // has every field filled in before proceeding.
        UserImpl badUser = UserImpl.INVALID_USER;
        UserImpl storedUser = (UserImpl) getItem(badUser.getKey());
        if (storedUser == null) {
            try {
                logger.fine("Storing the all-fields-filled user");
                putItem(badUser);
            } catch (AuraException e) {
                logger.fine("Storing the all-fields-filled user failed!");
            }
        }

        //
        // Start evolving anything that needs evolving
        Runnable evolver = new Runnable() {
            @Override
            public void run() {
                log.info("Starting store evolution");
                try {
                    EvolveConfig evconf = new EvolveConfig();
                    evconf.addClassToEvolve(ItemImpl.class.getName());
                    //
                    // Set an evolve listener so that we get stats on the evolve
                    evconf.setEvolveListener(new EvolveListener() {
                        @Override
                        public boolean evolveProgress(EvolveEvent event) {
                            return true;
                        }
                    });
                    EvolveStats stats = store.evolve(evconf);
                    log.info("Read " + stats.getNRead() +
                            " and converted " + stats.getNConverted());
                } catch (DatabaseException e) {
                    log.log(Level.WARNING, "Evolving all objects failed", e);
                }
                log.info("Done evolving store.");
            }
        };
        Thread t = new Thread(evolver);
        t.start();
        log.info("BDB done loading");
    }

    public void openBDB() throws AuraException {
        try {
            quiesce.writeLock().lock();

            if (dbEnv != null) {
                close();
            }

            EnvironmentConfig econf = new EnvironmentConfig();

            econf.setAllowCreate(true);
            econf.setTransactional(true);
            econf.setCachePercent(cacheSizeMemPercentage);
            econf.setConfigParam(EnvironmentConfig.TXN_DUMP_LOCKS, "true");


            log.info("BDB opening DB Env...");
            if (replicated) {
                //
                // Configure our environment
                ReplicationConfig rconf = new ReplicationConfig();

                if (groupName != null) {
                    //
                    // If we specified a group name, we're starting fresh.
                    // Otherwise, we assume the following values are already set.
                    log.info(String.format("Opening replicated environment " +
                                           "with:\nGroupName: %s\nNodeName: " +
                                           "%s\nNodeHostPort: %s\nNodeHelper: %s",
                                           groupName, nodeName, nodeHostPort,
                                           nodeHelper));

                    rconf.setGroupName(groupName);
                    rconf.setNodeName(nodeName);
                    rconf.setNodeHostPort(nodeHostPort);
                    rconf.setHelperHosts(nodeHelper);
                }

                //
                // Open up the replicated environment.
                for (int i = 0; i < MAX_OPEN_RETRIES; i++) {
                    try {
                        //
                        // HMMM - What should go in the time consistency policy???
                        repDbEnv = new ReplicatedEnvironment(dbEnvDir, rconf, econf,
                                new TimeConsistencyPolicy(2, TimeUnit.SECONDS,
                                                          5, TimeUnit.SECONDS),
                                QuorumPolicy.SIMPLE_MAJORITY);
                        dbEnv = repDbEnv;
                        break;
                    } catch (UnknownMasterException e) {
                        //
                        // Just wait a little and try again, assuming a master
                        // will show up before long.
                        log.info("Unknown Master, waiting for one to appear (" +
                                e.getMessage() + ")");
                        try {
                            Thread.sleep(3 * 1000);
                        } catch (InterruptedException ex) {
                        }
                        continue;
                    } catch (InsufficientLogException e) {
                        NetworkRestore netRest = new NetworkRestore();
                        NetworkRestoreConfig nrconf = new NetworkRestoreConfig();
                        nrconf.setRetainLogFiles(false);
                        netRest.execute(e, nrconf);
                        continue;
                    }
                }
                //
                // If we still couldn't make an environment, throw an exception
                if (dbEnv == null) {
                    throw new AuraException("Failed to open replicated environment");
                }
            } else {
                //
                // Stand-alone mode
                dbEnv = new Environment(dbEnvDir, econf);
            }

            StoreConfig sconf = new StoreConfig();
            //
            // Set up any mutations -- object version changes
            Mutations mutations = new Mutations();
            ItemImpl.addMutations(mutations);
            sconf.setMutations(mutations);
            sconf.setAllowCreate(true);
            sconf.setTransactional(true);

            //
            // Code from Mark Hayes to register persist subclasses -- this is
            // a potential work-around to our corruption issue.
            EntityModel model = new AnnotationModel();
            // register all entity subclasses
            model.registerClass(UserImpl.class);
            // set the model and create the store
            sconf.setModel(model);

            log.info("BDB opening Store...");
            store = new EntityStore(dbEnv, "Aura", sconf);

            //
            // Load the indexes that we'll use during regular operation

            log.fine("Opening fieldByName");
            fieldByName = store.getPrimaryIndex(String.class, FieldDescription.class);

            log.fine("Opening itemByKey");
            itemByKey = store.getPrimaryIndex(String.class, ItemImpl.class);

            log.fine("Opening itemByType");
            itemByType = store.getSecondaryIndex(itemByKey,
                    Integer.class,
                    "itemType");

            log.fine("Opening itemByTypeAndTime");
            itemByTypeAndTime = store.getSecondaryIndex(itemByKey,
                    IntAndTimeKey.class,
                    "typeAndTimeAdded");

            log.fine("Opening allUsers");
            allUsers = store.getSubclassIndex(itemByKey, UserImpl.class,
                    Boolean.class, "isUser");

            log.fine("Opening usersByRandString");
            usersByRandString = store.getSubclassIndex(itemByKey, UserImpl.class,
                    String.class, "randStr");

            log.fine("Opening allAttn");
                allAttn = store.getPrimaryIndex(Long.class,
                        PersistentAttention.class);

            log.fine("Opening attnByTargetKey");
            attnByTargetKey = store.getSecondaryIndex(allAttn,
                    String.class,
                    "targetKey");

            log.fine("Opening attnBySourceKey");
            attnBySourceKey = store.getSecondaryIndex(allAttn,
                    String.class,
                    "sourceKey");

            log.fine("Opening attnByType");
            attnByType = store.getSecondaryIndex(allAttn,
                    Integer.class,
                    "type");

            log.fine("Opening attnByTime");
            attnByTime = store.getSecondaryIndex(allAttn, Long.class, "timeStamp");

            log.fine("Opening attnBySourceAndTime");
            attnBySourceAndTime = store.getSecondaryIndex(allAttn,
                    StringAndTimeKey.class,
                    "sourceAndTime");

            log.fine("Opening attnByTargetAndTime");
            attnByTargetAndTime = store.getSecondaryIndex(allAttn,
                    StringAndTimeKey.class,
                    "targetAndTime");

            log.fine("Opening attnByStringVal");
            attnByStringVal = store.getSecondaryIndex(allAttn,
                    String.class,
                    "metaString");

            log.fine("Opening attnByNumberVal");
            attnByNumberVal = store.getSecondaryIndex(allAttn,
                    Long.class,
                    "metaLong");
        } finally {
            quiesce.writeLock().unlock();
        }
    }

    public void defineField(final String fieldName,
            final Item.FieldType fieldType,
            final EnumSet<Item.FieldCapability> caps) throws AuraException {
        DBCommand<Void> cmd = new DBCommand<Void>() {
            @Override
            public Void run(Transaction txn) throws AuraException {
                FieldDescription fd =
                        new FieldDescription(fieldName, fieldType, caps);
                FieldDescription prev = fieldByName.get(txn, fieldName, LockMode.READ_UNCOMMITTED);
                if(prev != null) {
                    if(!prev.equals(fd)) {
                        throw new AuraException("Attempt to redefined field " + fieldName +
                                " using different capabilities or type prev: " +
                                prev.getCapabilities() + " " + prev.getType() +
                                " new: " + fd.getCapabilities() + " " + fd.getType());
                    }
                } else {
                    fieldByName.put(txn, fd);
                }
                return null;
            }

            @Override
            public String getDescription() {
                return "defineField(" + fieldName + ")";
            }

        };
        invokeCommand(cmd);
    }
    
    public Map<String,FieldDescription> getFieldDescriptions()
            throws AuraException {
        DBCommand<Map<String,FieldDescription>> cmd =
                new DBCommand<Map<String,FieldDescription>>() {

            @Override
            public Map<String, FieldDescription> run(Transaction txn) throws AuraException {
                HashMap<String, FieldDescription> ret =
                        new HashMap<String, FieldDescription>();

                return new HashMap(fieldByName.map());
            }

            @Override
            public String getDescription() {
                return "getFieldDescriptions()";
            }

            public TransactionConfig getTransactionConfig() {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                return conf;
            }
        };
        return invokeCommandCurrentTxn(cmd);
    }
    
    /**
     * Gets the set of all items of a particular type.  This could be a large
     * set.
     * 
     * @return all users in the item store
     */
    public List<Item> getAll(final Item.ItemType type)
            throws AuraException {
        //
        // If there is a type, get a subindex to iterator through
        EntityIndex subIndex = null;
        if (type != null) {
            subIndex = getItemSubIndex(type);
        } else {
            subIndex = itemByType;
        }

        final EntityIndex index = subIndex;
        DBCommand<List<Item>> cmd = new DBCommand<List<Item>>() {

            @Override
            public List<Item> run(Transaction txn) throws AuraException {
                List<Item> items = new ArrayList<Item>();
                EntityCursor<ItemImpl> cur = index.entities(
                        txn, CursorConfig.READ_UNCOMMITTED);
                try {
                    for(ItemImpl i : cur) {
                        items.add(i);
                    }
                } finally {
                    cur.close();
                }
                return items;
            }

            @Override
            public String getDescription() {
                if (type != null) {
                    return "getAll(" + type.toString() + ")";
                } else {
                    return "getAll(null)";
                }
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                return conf;
            }

        };
        return invokeCommand(cmd);
    }

    public DBIterator<Item> getAllIterator(final Item.ItemType type)
            throws AuraException {
        //
        // If there is a type, get a subindex to iterator through
        EntityIndex subIndex = null;
        if (type != null) {
            subIndex = getItemSubIndex(type);
        }

        final EntityIndex index = subIndex;
        DBCommand<DBIterator<Item>> cmd = new DBCommand<DBIterator<Item>>() {

            @Override
            public DBIterator<Item> run(Transaction txn) throws AuraException {
                EntityCursor cur = null;
                txn.setTxnTimeout(0, null);
                try {
                    if (type != null) {
                        cur = index.entities(txn, CursorConfig.READ_UNCOMMITTED);
                    } else {
                        cur = itemByKey.entities(txn, CursorConfig.READ_UNCOMMITTED);
                    }
                } catch (DatabaseException e) {
                    handleCursorException(cur, txn, e);
                }
                DBIterator<Item> dbIt = new EntityIterator<Item>(cur, txn);
                return dbIt;
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                return conf;
            }

            @Override
            public String getDescription() {
                if (type != null) {
                    return "getAllIterator(" + type.toString() + ")";
                } else {
                    return "getAllIterator(null)";
                }
            }

        };
        return invokeCommand(cmd, false, false);
    }
    
    /**
     * Gets an item from the entity store
     * @param key the key of the item to fetch
     * @return the item or null if the key is unknown
     */
    public ItemImpl getItem(final String key) throws AuraException {
        DBCommand<ItemImpl> cmd = new DBCommand<ItemImpl>() {

            @Override
            public ItemImpl run(Transaction txn) throws AuraException {
                return itemByKey.get(null, key, LockMode.READ_UNCOMMITTED);
            }

            @Override
            public String getDescription() {
                return "getItem(" + key + ")";
            }

        };
        return invokeCommand(cmd);
    }

    /**
     * Puts an item into the entity store.  If the item already exists, it will
     * be replaced.
     * 
     * @param item the item to put
     * @return the existing entity that was updated or null of the item was
     * inserted
     */
    public ItemImpl putItem(final ItemImpl item) throws AuraException {
        DBCommand<ItemImpl> cmd = new DBCommand<ItemImpl>() {

            @Override
            public ItemImpl run(Transaction txn) throws AuraException {
                return itemByKey.put(txn, item);
            }

            @Override
            public String getDescription() {
                return "putItem(" + item.getType().toString() + ":" + item.getKey() + ")";
            }

        };
        return invokeCommand(cmd);
    }

    /**
     * Puts a list of items into the entity store.  If an item already exists, it will
     * be replaced.
     *
     * @param items the items to put in the store.
     */
    public void putItems(final List<ItemImpl> items) throws AuraException {
        DBCommand<Void> cmd = new DBCommand<Void>() {
            @Override
            public Void run(Transaction txn) throws AuraException {
                for(ItemImpl item : items) {
                    itemByKey.putNoReturn(txn, item);
                }
                return null;
            }

            @Override
            public String getDescription() {
                if (items != null) {
                    return "putItems(" + items.size() + " items)";
                }
                return "putItems{null)";
            }

        };
        invokeCommand(cmd);
    }

    /**
     * Deletes an item from the database.
     * 
     * @param itemKey the key of the item to delete
     * @throws com.sun.labs.aura.util.AuraException
     */
    public void deleteItem(final String itemKey) throws AuraException {
        DBCommand<Void> cmd = new DBCommand<Void>() {

            @Override
            public Void run(Transaction txn) throws AuraException {
                itemByKey.delete(itemKey);
                return null;
            }

            @Override
            public String getDescription() {
                return "deleteItem(" + itemKey + ")";
            }

        };
        invokeCommand(cmd);
    }
    
    public void deleteAttention(final List<Long> ids) throws AuraException {
        DBCommand<Void> cmd = new DBCommand<Void>() {

            @Override
            public Void run(Transaction txn) throws AuraException {
                for (Long id : ids) {
                    allAttn.delete(txn, id);
                }
                return null;
            }

            @Override
            public String getDescription() {
                return "deleteAttention(" + ids.size() + " attns)";
            }

        };
        invokeCommand(cmd);
    }
    
    public UserImpl getUserForRandomString(final String randStr)
            throws AuraException {
        DBCommand<UserImpl> cmd = new DBCommand<UserImpl>() {

            @Override
            public UserImpl run(Transaction txn) throws AuraException {
                return usersByRandString.get(null,
                                             randStr,
                                             LockMode.READ_UNCOMMITTED);
            }

            @Override
            public String getDescription() {
                return "getUserForRndStr(" + randStr + ")";
            }

        };
        return invokeCommand(cmd);
    }
    
    /**
     * Puts an attention into the entry store.  Attentions should never be
     * overwritten.
     * 
     * @param pa the attention
     */
    public void putAttention(final PersistentAttention pa) throws AuraException {
        DBCommand<Void> cmd = new DBCommand<Void>() {

            @Override
            public Void run(Transaction txn) throws AuraException {
                long prevID = pa.getID();
                if (!allAttn.putNoOverwrite(txn, pa)) {
                    log.warning("Failed to insert attention since primary key already exists: " + pa);
                    try {
                        throw new AuraException("Failed to insert attention.  PrevID was " + prevID);
                    } catch (AuraException e) {
                        log.log(Level.WARNING, "", e);
                    }
                }
                return null;
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                //
                // We want putting attentions to be fast and not necessarily
                // durable or consistent across replicas.
                TransactionConfig txConf = new TransactionConfig();
                Durability dur = new Durability(
                        Durability.SyncPolicy.WRITE_NO_SYNC,
                        Durability.SyncPolicy.WRITE_NO_SYNC,
                        Durability.ReplicaAckPolicy.NONE
                        );
                txConf.setDurability(dur);
                return txConf;
            }

            @Override
            public String getDescription() {
                return "putAttention(" + pa + ")";
            }

        };
        invokeCommand(cmd);
    }

    /**
     * Puts attention into the entry store.  Attentions should never be
     * overwritten.
     * 
     * @param pa the attention
     */
    public void putAttention(final List<PersistentAttention> pas) throws AuraException {
        DBCommand<Void> cmd = new DBCommand<Void>() {

            @Override
            public Void run(Transaction txn) throws AuraException {
                for (PersistentAttention pa : pas) {
                    allAttn.putNoOverwrite(txn, pa);
                }
                return null;
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                //
                // We want putting attentions to be fast and not necessarily
                // durable or consistent across replicas.
                TransactionConfig txConf = new TransactionConfig();
                Durability dur = new Durability(
                        Durability.SyncPolicy.WRITE_NO_SYNC,
                        Durability.SyncPolicy.WRITE_NO_SYNC,
                        Durability.ReplicaAckPolicy.NONE
                        );
                txConf.setDurability(dur);
                return txConf;
            }

            @Override
            public String getDescription() {
                return "putAttention(" + pas.size() + " attns)";
            }

        };
        invokeCommand(cmd);
    }

    /**
     * Delete all the attention that has as a source or target the given item.
     * This should be used to clean up after an item was deleted.
     * 
     * @param itemKey the key of the item
     * @throws com.sun.labs.aura.util.AuraException
     */
    public void removeAttention(final String itemKey)
            throws AuraException {
        DBCommand<Void> cmd = new DBCommand<Void>() {

            @Override
            public Void run(Transaction txn) throws AuraException {
                //
                // Get all the attentions for which this item was a source
                // and delete them
                EntityIndex<Long, PersistentAttention> attns =
                        getStringSubIndex(attnBySourceKey, itemKey);
                EntityCursor<PersistentAttention> c =
                        attns.entities(txn, CursorConfig.READ_COMMITTED);
                try {
                    for(PersistentAttention a : c) {
                        c.delete();
                    }
                } finally {
                    if(c != null) {
                        c.close();
                    }
                }

                //
                // Now do the same, but for attention where itemKey was the target
                attns = getStringSubIndex(attnByTargetKey, itemKey);
                c = attns.entities(txn, new CursorConfig());
                try {
                    for(PersistentAttention a : c) {
                        c.delete();
                    }
                } finally {
                    if(c != null) {
                        c.close();
                    }
                }
                return null;
            }

            @Override
            public String getDescription() {
                return "removeAttn(" + itemKey + ")";
            }

        };
        invokeCommand(cmd);
    }
    
    public void removeAttention(final String srcKey,
                                final String targetKey,
                                final Attention.Type type)
                throws AuraException {
        //
        // Get all matching attention and remove it
        DBCommand<Void> cmd = new DBCommand<Void>() {

            @Override
            public Void run(Transaction txn) throws AuraException {
                EntityJoin<Long, PersistentAttention> join =
                        new EntityJoin(allAttn);
                join.addCondition(attnBySourceKey, srcKey);
                join.addCondition(attnByTargetKey, targetKey);
                join.addCondition(attnByType, type.ordinal());

                //
                // Get all the attention IDs that match the criteria
                List<Long> attnIDs = new ArrayList();
                ForwardCursor<PersistentAttention> cur = null;
                try {
                    cur = join.entities(txn, CursorConfig.READ_COMMITTED);
                    for(PersistentAttention attn : cur) {
                        attnIDs.add(attn.getID());
                    }
                } finally {
                    if(cur != null) {
                        cur.close();
                    }
                }

                //
                // And delete them
                for (Long id : attnIDs) {
                    allAttn.delete(txn, id);
                }
                return null;
            }

            @Override
            public String getDescription() {
                return String.format("removeAttn(%s/%s %s)",
                        srcKey, targetKey, type.toString());
            }

        };
        invokeCommand(cmd);
    }
    
    /**
     * Gets all the items of a particular type that have been added since a
     * particular time.  Returns an iterator over those items that must be
     * closed when reading is done.
     * 
     * @param itemType the type of item to retrieve
     * @param timeStamp the time from which to search (to the present time
     * @return an iterator over the added items
     * @throws com.sun.labs.aura.util.AuraException 
     */
    public DBIterator<Item> getItemsAddedSince(final ItemType itemType,
                                               final long timeStamp)
            throws AuraException {
        DBCommand<DBIterator<Item>> cmd = new DBCommand<DBIterator<Item>>() {

            @Override
            public DBIterator<Item> run(Transaction txn) throws AuraException {
                //
                // We need to get a cursor based on the long & time key that stores
                // the item type and the time that it was added.  We'll get a cursor
                // for only that type and the range of times from the provided
                // timestamp to the current time.
                IntAndTimeKey begin = new IntAndTimeKey(itemType.ordinal(), timeStamp);
                IntAndTimeKey end = new IntAndTimeKey(itemType.ordinal(),
                        System.currentTimeMillis());

                EntityCursor cursor = null;
                txn.setTxnTimeout(0, null);
                
                //
                // Set Read Committed behavior - this ensures the stability of
                // the current item being read (puts a read lock on it) but allows
                // previously read items to change (releases the read lock after
                // reading).
                try {
                    cursor = itemByTypeAndTime.entities(txn,
                            begin, true,
                            end, true,
                            CursorConfig.READ_UNCOMMITTED);
                } catch (DatabaseException e) {
                    handleCursorException(cursor, txn, e);
                }
                
                DBIterator<Item> dbIt = new EntityIterator<Item>(cursor, txn);
                return dbIt;
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                return conf;
            }

            @Override
            public String getDescription() {
                Date d = new Date(timeStamp);
                return String.format("getItemsAddedSince(%s, %s)",
                                     itemType.toString(), d.toString());
            }

        };
        return invokeCommand(cmd, false, false);
    }

    /**
     * Gets all the attention that has been added to the store since a
     * particular date.  Returns an iterator over the attention that must be
     * closed when reading is done.
     * 
     * @param timeStamp the time to search back to
     * @return the Attentions added since that time
     * @throws com.sun.labs.aura.util.AuraException
     */
    @SuppressWarnings(value="RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE",
                      justification="Future-proofing isn't bad")
    public DBIterator<Attention> getAttentionAddedSince(final long timeStamp)
            throws AuraException {
        DBCommand<DBIterator<Attention>> cmd =
                new DBCommand<DBIterator<Attention>>() {

            @Override
            public DBIterator<Attention> run(Transaction txn) throws AuraException {
                //
                // This transaction is read-only and it is up to the developer
                // to release it.  Don't time out the transaction.
                txn.setTxnTimeout(0, null);

                //
                // Set Read Committed behavior - this ensures the stability of
                // the current item being read (puts a read lock on it) but allows
                // previously read items to change (releases the read lock after
                // reading).
                EntityCursor c = null;
                try {
                    c = attnByTime.entities(txn, timeStamp, true,
                            System.currentTimeMillis(), true,
                            CursorConfig.READ_UNCOMMITTED);
                } catch (DatabaseException e) {
                    handleCursorException(c, txn, e);
                }
                DBIterator<Attention> dbIt = new EntityIterator<Attention>(c, txn);
                return dbIt;
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                TransactionConfig tconf = new TransactionConfig();
                tconf.setReadUncommitted(true);
                return tconf;
            }

            @Override
            public String getDescription() {
                return "getAttentionAddedSince(" + new Date(timeStamp).toString() + ")";
            }

        };
        return invokeCommand(cmd, false, false);
    }
    
    protected EntityJoin<Long, PersistentAttention> getAttentionJoin(
            AttentionConfig ac) {
        EntityJoin<Long, PersistentAttention> join = new EntityJoin(allAttn);
        if (ac.getSourceKey() != null) {
            join.addCondition(attnBySourceKey, ac.getSourceKey());
        }
        if (ac.getTargetKey() != null) {
            join.addCondition(attnByTargetKey, ac.getTargetKey());
        }
        if (ac.getType() != null) {
            join.addCondition(attnByType, ac.getType().ordinal());
        }
        if (ac.getStringVal() != null) {
            join.addCondition(attnByStringVal, ac.getStringVal());
        }
        if (ac.getNumberVal() != null) {
            join.addCondition(attnByNumberVal, ac.getNumberVal());
        }
        return join;
    }
    
    public DBIterator<Attention> getAttentionIterator(final AttentionConfig ac)
            throws AuraException {
        DBCommand<DBIterator<Attention>> cmd =
                new DBCommand<DBIterator<Attention>>() {

            @Override
            public DBIterator<Attention> run(Transaction txn) throws AuraException {
                txn.setTxnTimeout(0, null);
                EntityJoin<Long,PersistentAttention> join = null;
                ForwardCursor cur = null;
                try {
                    if (!ac.isEmpty()) {
                        join = getAttentionJoin(ac);
                        cur = join.entities(txn, CursorConfig.READ_UNCOMMITTED);
                    } else {
                        cur = allAttn.entities(txn, CursorConfig.READ_UNCOMMITTED);
                    }
                } catch (DatabaseException e) {
                    handleCursorException(cur, txn, e);
                }
                DBIterator<Attention> dbIt = new EntityIterator<Attention>(cur, txn);
                return dbIt;
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                return conf;
            }

            @Override
            public String getDescription() {
                return "getAttentionIterator(AttnConf)";
            }

        };
        return invokeCommand(cmd, false, false);
    }
    
    /**
     * Returns a count of attention meeting the given criteria
     * 
     * @param ac
     * @return
     */
    public Long getAttentionCount(final AttentionConfig ac)
            throws AuraException {
        DBCommand<Long> cmd = new DBCommand<Long>() {

            @Override
            public Long run(Transaction txn) throws AuraException {
                if (ac == null || ac.isEmpty()) {
                    try {
                        return allAttn.count();
                    } catch(DatabaseException e) {
                        log.log(Level.WARNING, "Failed to get count of all attentions",
                                e);
                    }
                    return 0L;
                }

                //
                // We need to iterate over all the attentions since the DB can't
                // tell us the count.  If calling this with a single constraint
                // is common (for example, only a source key, or only a target key)
                // we can probably special case to answer faster by instantiating
                // a subIndex for the specific value and calling count() on it.
                EntityJoin<Long,PersistentAttention> join = getAttentionJoin(ac);
                long ret = 0;
                ForwardCursor<PersistentAttention> cur = null;
                try {
                    cur = join.entities(txn, CursorConfig.READ_UNCOMMITTED);
                    for(PersistentAttention attn : cur) {
                        ret++;
                    }
                } finally {
                    if(cur != null) {
                        cur.close();
                    }
                }
                return ret;
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                return conf;
            }

            @Override
            public String getDescription() {
                return "getAttentionCount(AttnConf)";
            }
        };

        return invokeCommand(cmd);
    }
    
    public DBIterator<Attention> getAttentionSinceIterator(
                                            final AttentionConfig ac,
                                            final Date timeStamp)
            throws AuraException {
        if (ac.isEmpty()) {
            throw new AuraException("At least one constraint must be " +
                "specified before calling getAttentionSince(AttentionConfig)");
        }

        DBCommand<DBIterator<Attention>> cmd =
                new DBCommand<DBIterator<Attention>>() {

            @Override
            public DBIterator<Attention> run(Transaction txn) throws AuraException {
                txn.setTxnTimeout(0, null);
                //
                // We'll do the join in the DB, then filter the time on memory
                EntityJoin<Long,PersistentAttention> join = getAttentionJoin(ac);

                ForwardCursor cur = null;
                try {
                    cur = join.entities(txn, CursorConfig.READ_UNCOMMITTED);
                } catch(DatabaseException e) {
                    handleCursorException(cur, txn, e);
                }
                DateFilterEntityIterator dbIt =
                        new DateFilterEntityIterator(cur, txn, timeStamp);
                return dbIt;
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                return conf;
            }

            @Override
            public String getDescription() {
                return "getAttnSinceIt(AttnConf," + timeStamp.toString() + ")";
            }
            
        };
        return invokeCommand(cmd, false, false);
    }

    /**
     * Returns a count of attention meeting the given criteria, after the given
     * date
     * 
     * @param ac
     * @return
     */
    public Long getAttentionSinceCount(final AttentionConfig ac,
                                       final Date timeStamp)
            throws AuraException {
        if (ac.isEmpty()) {
            throw new AuraException("At least one constraint must be " +
                "specified before calling getAttentionSince(AttentionConfig)");
        }

        DBCommand<Long> cmd = new DBCommand<Long>() {

            @Override
            public Long run(Transaction txn) throws AuraException {
                //
                // We need to iterate over all the attentions since the DB can't
                // tell us the count.  If calling this with a single constraint
                // is common (for example, only a source key, or only a target key)
                // we can probably special case to answer faster by instantiating
                // a subIndex for the specific value and calling count() on it.
                EntityJoin<Long,PersistentAttention> join = getAttentionJoin(ac);
                long ret = 0;
                ForwardCursor<PersistentAttention> cur = null;
                try {
                    cur = join.entities(txn, CursorConfig.READ_UNCOMMITTED);
                    for(PersistentAttention attn : cur) {
                        //
                        // Post process the date filter in memory
                        if (attn.getTimeStamp() >= timeStamp.getTime()) {
                            ret++;
                        }
                    }

                } finally {
                    if (cur != null) {
                        cur.close();
                    }
                }
                return ret;
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                return conf;
            }

            @Override
            public String getDescription() {
                return "getAttnSinceCnt(AttnConf, "
                        + timeStamp.toString() + ")";
            }

        };
        return invokeCommand(cmd);
    }

    /**
     * Gets the most recent N attentions of a specified type) that
     * a user has recorded.  This will potentially perform a series of time
     * based queries, expanding the query range until enough attentions have
     * been found to satisfy the count, or until all attentions within the last
     * year have been considered.
     * 
     * @param srcKey the ID of the attention source to query for
     * @param type the type of attention to limit to, or null for all attentions
     * @param count the desired number of attentions to return
     * @return a set of attentions, sorted by date
     */
    public List<Attention> getLastAttentionForUser(final String srcKey,
                                                   final Attention.Type type,
                                                   final int count)
            throws AuraException {
        DBCommand<List<Attention>> cmd = new DBCommand<List<Attention>>() {

            @Override
            public List<Attention> run(Transaction txn) throws AuraException {
                int remaining = count;

                //
                // Start querying for attention for this user based on time, expanding
                // the time range until we have enough attention.
                Set<Attention> results = new HashSet<Attention>();
                long recent = System.currentTimeMillis();

                // Try one hour first
                List<Attention> curr =
                        getUserAttnForTimePeriod(txn, srcKey, type, recent,
                        Times.ONE_HOUR, count);

                remaining -= curr.size();
                recent -= Times.ONE_HOUR;
                results.addAll(curr);
                if(count <= 0) {
                    List<Attention> temp = new ArrayList<Attention>(results);
                    Collections.sort(temp, new ReverseAttentionTimeComparator());
                    return temp;
                }

                //
                // Now add in from one hour ago to one day ago
                curr = getUserAttnForTimePeriod(txn, srcKey, type, recent,
                        Times.ONE_DAY, count);
                remaining -= curr.size();
                recent -= Times.ONE_DAY;
                results.addAll(curr);
                if(count <= 0) {
                    List<Attention> temp = new ArrayList<Attention>(results);
                    Collections.sort(temp, new ReverseAttentionTimeComparator());
                    return temp;
                }

                //
                // Now add in from one day ago to one week ago
                curr = getUserAttnForTimePeriod(txn, srcKey, type, recent,
                        Times.ONE_WEEK, count);
                remaining -= curr.size();
                recent -= Times.ONE_WEEK;
                results.addAll(curr);
                if(count <= 0) {
                    List<Attention> temp = new ArrayList<Attention>(results);
                    Collections.sort(temp, new ReverseAttentionTimeComparator());
                    return temp;
                }

                //
                // Now add in from one week ago to one month ago
                curr = getUserAttnForTimePeriod(txn, srcKey, type, recent,
                        Times.ONE_MONTH, count);
                remaining -= curr.size();
                recent -= Times.ONE_MONTH;
                results.addAll(curr);
                if(count <= 0) {
                    List<Attention> temp = new ArrayList<Attention>(results);
                    Collections.sort(temp, new ReverseAttentionTimeComparator());
                    return temp;
                }

                //
                // Finally, expand out to one year.
                curr = getUserAttnForTimePeriod(txn, srcKey, type, recent,
                        Times.ONE_YEAR, count);
                //
                // Take whatever we got and return it.  We won't search back more than
                // one year.
                results.addAll(curr);
                List<Attention> temp = new ArrayList<Attention>(results);
                Collections.sort(temp, new ReverseAttentionTimeComparator());
                return temp;
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                return conf;
            }

            @Override
            public String getDescription() {
                return String.format("getLastAttnForUser(%s %s %d)",
                                     type.toString(), srcKey, count);
            }

        };
        return invokeCommand(cmd);
    }

    private List<Attention> getUserAttnForTimePeriod(
            Transaction txn,
            String srcKey,
            Attention.Type type,
            long recentTime,
            long interval,
            int count)
            throws AuraException {
        List<Attention> result = new ArrayList<Attention>();
        //
        // Set the begin and end times chronologically
        StringAndTimeKey begin = new StringAndTimeKey(srcKey, recentTime -
                interval);
        StringAndTimeKey end = new StringAndTimeKey(srcKey + 1, recentTime);
        EntityCursor<PersistentAttention> cursor = null;
        try {
            //
            // Examine each item in the cursor in reverse order (newest
            // first) to see if it matches our requirements.  If so, add
            // it to our return set.
            cursor = attnBySourceAndTime.entities(txn, begin, true, end, false, CursorConfig.READ_UNCOMMITTED);
            PersistentAttention curr = cursor.last();
            while(curr != null && count > 0) {
                if((type == null) || (curr.getType().equals(type))) {
                    result.add(curr);
                    count--;
                }
                curr = cursor.prev();
            }
        } catch(DatabaseException e) {
            handleCursorException(cursor, txn, e);
        }
        return result;
    }

    public boolean isEmpty() throws AuraException {
        DBCommand<Boolean> cmd = new DBCommand<Boolean>() {

            @Override
            public Boolean run(Transaction txn) throws AuraException {
                return itemByKey.count() + allAttn.count() + allUsers.count() == 0;
            }

            @Override
            public String getDescription() {
                return "isEmpty()";
            }

        };
        return invokeCommand(cmd);
    }

    /**
     * Gets the number of items of a particular type that are in the index.
     * 
     * @param type the type of item to count
     * @return the number of instances of that item type in the index
     */
    public long getItemCount(final ItemType type) throws AuraException {
        DBCommand<Long> cmd = new DBCommand<Long>() {

            @Override
            public Long run(Transaction txn) throws AuraException {
                if (type == null) {
                    return itemByKey.count();
                } else {
                    EntityIndex idx = itemByType.subIndex(type.ordinal());
                    return idx.count();
                }
            }

            @Override
            public String getDescription() {
                if (type != null) {
                    return "getItemCount(" + type.toString() + ")";
                } else {
                    return "getItemCount(null)";
                }
            }

            @Override
            public TransactionConfig getTransactionConfig() {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                return conf;
            }
        };
        return invokeCommandCurrentTxn(cmd);
    }

    /**
     * Get the number of user entities in the entity store
     * 
     * @return the number of users or -1 if there was an error
     */
    public long getNumUsers() throws AuraException {
        DBCommand<Long> cmd = new DBCommand<Long>() {

            @Override
            public Long run(Transaction txn) throws AuraException {
                long count = allUsers.count();
                return count;
            }

            @Override
            public String getDescription() {
                return "getNumUsers()";
            }

        };
        return invokeCommand(cmd);
    }

    /**
     * Close up the entity store and the database environment.
     */
    public void close() {
        try {
            quiesce.writeLock().lock();
            if(store != null) {
                try {
                    System.out.println("BDB closing store");
                    store.close();
                } catch(DatabaseException e) {
                    System.out.println("Failed to close entity store" + e);
                    e.printStackTrace();
                }
            }

            if(dbEnv != null) {
                try {
                    System.out.println("BDB closing dbEnv");
                    dbEnv.close();
                } catch(DatabaseException e) {
                    System.out.println("Failed to close database environment" + e);
                    e.printStackTrace();
                }
            }
        } finally {
            quiesce.writeLock().unlock();
        }
    }
    
    /**
     * Gets the size of the database in bytes
     * 
     * @return the size in bytes
     */
    public long getSize() {
        try {
            quiesce.readLock().lock();
            EnvironmentStats stats = dbEnv.getStats(null);
            return stats.getTotalLogSize();
        } catch (DatabaseException e) {
            log.warning("Failed to get DB stats: " + e.getMessage());
        } finally {
            quiesce.readLock().unlock();
        }
        return 0;
    }

    protected EntityIndex getItemSubIndex(final Item.ItemType type)
            throws AuraException {
        DBCommand<EntityIndex> cmd = new DBCommand<EntityIndex>() {

            @Override
            public EntityIndex run(Transaction txn) throws AuraException {
                return itemByType.subIndex(type.ordinal());
            }

            @Override
            public String getDescription() {
                return "getItemSubIndex(" + type.toString() + ")";
            }

        };
        return invokeCommandCurrentTxn(cmd);
    }

    protected EntityIndex getStringSubIndex(
                                    final SecondaryIndex idx,
                                    final String str)
            throws AuraException {
        DBCommand<EntityIndex> cmd = new DBCommand<EntityIndex>() {

            @Override
            public EntityIndex run(Transaction txn) throws AuraException {
                return idx.subIndex(str);
            }

            @Override
            public String getDescription() {
                return "getStringSubIndex(" + str + ")";
            }

        };
        return invokeCommandCurrentTxn(cmd);
    }


    protected void handleCursorException(ForwardCursor cur, Transaction txn, Exception cause)
            throws AuraException {
        try {
            if(cur != null) {
                cur.close();
            }
        } catch(DatabaseException ex) {
            log.log(Level.WARNING, "Failed to close cursor", ex);
        }
        try {
            if(txn != null) {
                txn.abort();
            }
        } catch(DatabaseException ex) {
            log.log(Level.WARNING, "Failed to abort cursor txn", ex);
        }
        throw new AuraException("Cursor failed", cause);
    }

    //
    // NOTE: should we have an invoke command that doesn't use a transaction?
    // Some methods don't need them (like some of the count methods), and it
    // may be extra overhead to craete and commit unused transactions.

    protected <R> R invokeCommand(DBCommand<R> cmd) throws AuraException {
        return invokeCommand(cmd, true, false);
    }

    protected <R> R invokeCommandCurrentTxn(DBCommand<R> cmd)
            throws AuraException {
        return invokeCommand(cmd, true, true);
    }

    protected <R> R invokeCommand(DBCommand<R> cmd,
                                  boolean commit,
                                  boolean useCurrentTxn)
            throws AuraException {
        int numRetries = 0;
        int sleepTime = 0;
        while(numRetries < MAX_RETRIES) {
            Transaction txn = null;
            CurrentTransaction currTxn = null;
            try {
                quiesce.readLock().lock();
                TransactionConfig tconf = cmd.getTransactionConfig();
                if (useCurrentTxn) {
                    currTxn = CurrentTransaction.getInstance(dbEnv);
                    txn = currTxn.beginTransaction(tconf);
                } else {
                    txn = dbEnv.beginTransaction(null, tconf);
                }
                R result = cmd.run(txn);
                if (commit) {
                    if (useCurrentTxn) {
                        currTxn.commitTransaction();
                    } else {
                        txn.commit();
                    }
                }
                return result;
            } catch (InsufficientReplicasException e) {
                //
                // In the event of a write operation that couldn't be sent
                // to a quorum of replicas, wait a bit and try again
                sleepTime = 2 * 1000;
            } catch (InsufficientAcksException e) {
                //
                // We didn't get confirmation from other replicas that the
                // write was accepted.  This likely happens when a replica
                // is going down (and when we are requiring acks).  For us,
                // this is okay.
            } catch (ReplicaWriteException e) {
                //
                // We tried to write to this node, but this node is a replica.
                throw new AuraReplicantWriteException(
                        "Cannot modify a replica: " + cmd.getDescription());
            } catch (ReplicaConsistencyException e) {
                //
                // We require a higher level of consistency that is currently
                // available on this replica.  Wait a bit and try again.
                sleepTime = 1000;
            } catch (LockConflictException e) {
                try {
                    if (useCurrentTxn) {
                        currTxn.abortTransaction();
                    } else {
                        txn.abort();
                    }
                    log.finest("Deadlock detected in command " +
                            cmd.getDescription() + ": " + e.getMessage());
                    numRetries++;
                } catch (DatabaseException ex) {
                    throw new AuraException("Txn abort failed", ex);
                }
            } catch (Throwable t) {
                try {
                    if(txn != null) {
                        if (useCurrentTxn) {
                            currTxn.abortTransaction();
                        } else {
                            txn.abort();
                        }
                    }
                } catch (DatabaseException ex) {
                    //
                    // Not much that can be done at this point
                }
                throw new AuraException("Command failed: " +
                        cmd.getDescription(), t);
            } finally {
                quiesce.readLock().unlock();
            }

            //
            // Do we need to sleep before trying again?
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    // Nothing we can do about it.
                }
            }
        }
        throw new AuraException(String.format(
                "Command failed after %d retries: %s",
                numRetries, cmd.getDescription()));
    }

    public abstract class DBCommand<R> {
        /**
         * Returns a configuration for the transaction that should be
         * used when running this command.
         *
         * @return null by default
         */
        public TransactionConfig getTransactionConfig() {
            return null;
        }

        /**
         * Runs the command within the given transaction.  The transaction is
         * committed by the invoker method so commit should not be called here.
         * @param txn the transaction within which the code should be run
         * @return the result of the command
         */
        public abstract R run(Transaction txn) throws AuraException;

        /**
         * Gets a message that should be included upon failure that should
         * include the command name and any extra data (item key, etc)
         * 
         * @return the status message
         */
        public abstract String getDescription();
    }
}
