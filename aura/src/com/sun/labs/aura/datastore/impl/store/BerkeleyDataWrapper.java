package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import com.sleepycat.je.CursorConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DeadlockException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentStats;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.ForwardCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.impl.Util;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.aura.datastore.impl.store.persist.IntAndTimeKey;
import com.sun.labs.aura.datastore.impl.store.persist.UserImpl;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.aura.datastore.impl.store.persist.StringAndTimeKey;
import com.sun.labs.aura.util.Times;
import com.sun.labs.minion.util.StopWatch;
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
    protected final static int MAX_DEADLOCK_RETRIES = 10;

    /**
     * The actual database environment.
     */
    protected Environment dbEnv;

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
            throws DatabaseException {
        this(dbEnvDir, logger, false, 60);
    }

    /**
     * Constructs a database wrapper.
     * 
     * @param dbEnvDir the environment directory for the database
     * @param logger a logger to use for messages
     * @param overwrite true if an existing database should be overwritten
     * @throws com.sleepycat.je.DatabaseException
     */
    public BerkeleyDataWrapper(String dbEnvDir,
            Logger logger,
            boolean overwrite, int cacheSizeMemPercentage)
            throws DatabaseException {
        this.log = logger;

        EnvironmentConfig econf = new EnvironmentConfig();
        StoreConfig sconf = new StoreConfig();

        econf.setAllowCreate(true);
        econf.setTransactional(true);
        econf.setCachePercent(cacheSizeMemPercentage);
        
        sconf.setAllowCreate(true);
        sconf.setTransactional(true);

        //econf.setConfigParam("je.txn.dumpLocks", "true");

        File dir = new File(dbEnvDir);
        if(!dir.exists()) {
            dir.mkdirs();
        } else if(overwrite) {
            for(File f : dir.listFiles()) {
                f.delete();
            }
            dir.delete();
            dir.mkdir();
        }

        log.info("BDB opening DB Env...");
        dbEnv = new Environment(dir, econf);
        log.info("BDB opening Store...");
        store = new EntityStore(dbEnv, "Aura", sconf);

        //
        // Load the indexes that we'll use during regular operation
        //itemByID = store.getPrimaryIndex(Long.class, ItemImpl.class);
        
        logger.fine("Opening fieldByName");
        fieldByName = store.getPrimaryIndex(String.class, FieldDescription.class);

        //itemByKey = store.getSecondaryIndex(itemByID, String.class, "key");
        logger.fine("Opening itemByKey");
        itemByKey = store.getPrimaryIndex(String.class, ItemImpl.class);

        logger.fine("Opening itemByType");
        itemByType = store.getSecondaryIndex(itemByKey,
                Integer.class,
                "itemType");

        logger.fine("Opening itemByTypeAndTime");
        itemByTypeAndTime = store.getSecondaryIndex(itemByKey,
                IntAndTimeKey.class,
                "typeAndTimeAdded");

        logger.fine("Opening allUsers");
        allUsers = store.getSubclassIndex(itemByKey, UserImpl.class,
                Boolean.class, "isUser");

        logger.fine("Opening usersByRandString");
        usersByRandString = store.getSubclassIndex(itemByKey, UserImpl.class,
                String.class, "randStr");
        
        logger.fine("Opening allAttn");
        allAttn = store.getPrimaryIndex(Long.class,
                PersistentAttention.class);

        logger.fine("Opening attnByTargetKey");
        attnByTargetKey = store.getSecondaryIndex(allAttn,
                String.class,
                "targetKey");

        logger.fine("Opening attnBySourceKey");
        attnBySourceKey = store.getSecondaryIndex(allAttn,
                String.class,
                "sourceKey");

        logger.fine("Opening attnByType");
        attnByType = store.getSecondaryIndex(allAttn,
                Integer.class,
                "type");

        logger.fine("Opening attnByTime");
        attnByTime = store.getSecondaryIndex(allAttn, Long.class, "timeStamp");

        logger.fine("Opening attnBySourceAndTime");
        attnBySourceAndTime = store.getSecondaryIndex(allAttn,
                StringAndTimeKey.class,
                "sourceAndTime");
        
        logger.fine("Opening attnByTargetAndTime");
        attnByTargetAndTime = store.getSecondaryIndex(allAttn,
                StringAndTimeKey.class,
                "targetAndTime");
        
        logger.fine("Opening attnByStringVal");
        attnByStringVal = store.getSecondaryIndex(allAttn,
                String.class,
                "metaString");
        
        logger.fine("Opening attnByNumberVal");
        attnByNumberVal = store.getSecondaryIndex(allAttn,
                Long.class,
                "metaLong");
        
        log.info("BDB done loading");
    }

    public void defineField(ItemType itemType, String field, EnumSet<Item.FieldCapability> caps, 
            Item.FieldType fieldType) throws AuraException {
        try {
            FieldDescription fd =
                    new FieldDescription(field, caps, fieldType);
            FieldDescription prev = fieldByName.get(field);
            if(prev != null) {
                if(!prev.equals(fd)) {
                    throw new AuraException("Attempt to redefined field " + field +
                            " using different capabilities or type prev: " +
                            prev.getCapabilities() + " " + prev.getType() +
                            " new: " + fd.getCapabilities() + " " + fd.getType());
                }
            } else {
                int numRetries = 0;
                while(numRetries < MAX_DEADLOCK_RETRIES) {
                    Transaction txn = null;
                    try {
                        txn = dbEnv.beginTransaction(null, null);
                        fieldByName.put(txn, fd);
                        txn.commit();
                        return;
                    } catch(DeadlockException e) {
                        try {
                            txn.abort();
                            numRetries++;
                        } catch(DatabaseException ex) {
                            throw new AuraException("Txn abort failed", ex);
                        }
                    } catch(Exception e) {
                        try {
                            if(txn != null) {
                                txn.abort();
                            }
                        } catch(DatabaseException ex) {
                        }
                        throw new AuraException("putItem transaction failed", e);
                    }
                }
                throw new AuraException("defineField failed for " + field);
            }
        } catch(DatabaseException ex) {
            throw new AuraException("defineField failed getting field description", ex);
        }
    }
    
    public Map<String,FieldDescription> getFieldDescriptions() {
        return new HashMap(fieldByName.map());
    }
    
    /**
     * Gets the set of all items of a particular type.  This could be a large
     * set.
     * 
     * @return all users in the item store
     */
    public List<Item> getAll(Item.ItemType type) {
        List<Item> items = new ArrayList<Item>();
        try {
            EntityIndex index = itemByType.subIndex(type.ordinal());
            EntityCursor<ItemImpl> cur = index.entities();
            try {
                for(ItemImpl i : cur) {
                    items.add(i);
                }
            } finally {
                cur.close();
            }
        } catch(DatabaseException e) {
            log.log(Level.WARNING, "Failed to retrieve users", e);
        }
        return items;
    }

    public DBIterator<Item> getAllIterator(Item.ItemType type)
            throws AuraException {
        EntityCursor cur = null;
        Transaction txn = null;
        try {
            TransactionConfig conf = new TransactionConfig();
            conf.setReadUncommitted(true);
            txn = dbEnv.beginTransaction(null, conf);
            txn.setTxnTimeout(0);
            if (type != null) {
                EntityIndex index = itemByType.subIndex(type.ordinal());
                cur = index.entities(txn, CursorConfig.READ_UNCOMMITTED);
            } else {
                cur = itemByKey.entities(txn, CursorConfig.READ_UNCOMMITTED);
            }
        } catch(DatabaseException e) {
            handleCursorException(cur, txn, e);
        }
        DBIterator<Item> dbIt = new EntityIterator<Item>(cur, txn);
        return dbIt;
    }
    
    /**
     * Gets an item from the entity store
     * @param key the key of the item to fetch
     * @return the item or null if the key is unknown
     */
    public ItemImpl getItem(String key) {
        ItemImpl ret = null;
        try {
            ret = itemByKey.get(null, key, LockMode.READ_UNCOMMITTED);
        } catch(DatabaseException e) {
            log.log(Level.WARNING, "getItem() failed to retrieve item (key:" +
                    key + ")", e);
        }
        return ret;
    }

    /**
     * Puts an item into the entity store.  If the item already exists, it will
     * be replaced.
     * 
     * @param item the item to put
     * @return the existing entity that was updated or null of the item was
     * inserted
     */
    public ItemImpl putItem(ItemImpl item) throws AuraException {
        ItemImpl ret = null;
        int numRetries = 0;
        while(numRetries < MAX_DEADLOCK_RETRIES) {
            Transaction txn = null;
            try {
                txn = dbEnv.beginTransaction(null, null);
                ret = itemByKey.put(txn, item);
                txn.commit();
                return ret;
            } catch(DeadlockException e) {
                try {
                    txn.abort();
                    numRetries++;
                } catch(DatabaseException ex) {
                    throw new AuraException("Txn abort failed", ex);
                }
            } catch(Exception e) {
                try {
                    if(txn != null) {
                        txn.abort();
                    }
                } catch(DatabaseException ex) {
                }
                throw new AuraException("putItem transaction failed", e);
            }
        }
        throw new AuraException("putItem failed for " +
                item.getType().toString() + ":" + item.getKey() +
                " after " + numRetries + " retries");
    }

    /**
     * Deletes an item from the database.
     * 
     * @param itemKey the key of the item to delete
     * @throws com.sun.labs.aura.util.AuraException
     */
    public void deleteItem(String itemKey) throws AuraException {
        int numRetries = 0;
        while (numRetries < MAX_DEADLOCK_RETRIES) {
            Transaction txn = null;
            try {
                txn = dbEnv.beginTransaction(null, null);
                itemByKey.delete(itemKey);
                txn.commit();
                return;
            } catch (DeadlockException e) {
                try {
                    txn.abort();
                    numRetries++;
                } catch (DatabaseException ex) {
                    throw new AuraException("Txn abort failed", ex);
                }
            } catch (Exception e) {
                try {
                    if (txn != null) {
                        txn.abort();
                    }
                } catch (DatabaseException ex) {
                }
                throw new AuraException("deleteItem transaction failed", e);
            }
        }
        throw new AuraException("deleteItem failed for " +
                itemKey + " after " + numRetries + " retries");
    }
    
    public void deleteAttention(List<Long> ids) throws AuraException {
        int numRetries = 0;
        while (numRetries < MAX_DEADLOCK_RETRIES) {
            Transaction txn = null;
            try {
                txn = dbEnv.beginTransaction(null, null);
                for (Long id : ids) {
                    allAttn.delete(id);
                }
                txn.commit();
                return;
            } catch (DeadlockException e) {
                try {
                    txn.abort();
                    numRetries++;
                } catch (DatabaseException ex) {
                    throw new AuraException("Txn abort failed", ex);
                }
            } catch (Exception e) {
                try {
                    if (txn != null) {
                        txn.abort();
                    }
                } catch (DatabaseException ex) {
                }
                throw new AuraException("deleteItem transaction failed", e);
            }
        }
        throw new AuraException("deleteAttention failed after " +
                numRetries + " retries");
    }
    
    public DBIterator<ItemImpl> getItemIterator() throws AuraException {
        EntityCursor c = null;
        DBIterator<ItemImpl> i = null;
        Transaction txn = null;
        try {
            TransactionConfig conf = new TransactionConfig();
            conf.setReadUncommitted(true);
            txn = dbEnv.beginTransaction(null, conf);
            c = itemByKey.entities(txn, CursorConfig.READ_UNCOMMITTED);
            i = new EntityIterator<ItemImpl>(c, txn);
        } catch (DatabaseException e) {
            handleCursorException(c, txn, e);
        }
        return i;
    }
    
    public UserImpl getUserForRandomString(String randStr) throws AuraException {
        UserImpl ret = null;
        try {
            ret = usersByRandString.get(null, randStr, LockMode.READ_UNCOMMITTED);
        } catch(DatabaseException e) {
            log.log(Level.WARNING, "getUserForRandomString() failed (randStr:" +
                    randStr + ")", e);
        }
        return ret;
    }
    
    /**
     * Puts an attention into the entry store.  Attentions should never be
     * overwritten.
     * 
     * @param pa the attention
     */
    public void putAttention(PersistentAttention pa) throws AuraException {
        int numRetries = 0;
        while(numRetries < MAX_DEADLOCK_RETRIES) {
            Transaction txn = null;
            try {
                TransactionConfig txConf = new TransactionConfig();
                txConf.setWriteNoSync(true);
                txn = dbEnv.beginTransaction(null, txConf);
                allAttn.putNoOverwrite(txn, pa);
                txn.commit();
                return;
            } catch(DeadlockException e) {
                try {
                    txn.abort();
                    numRetries++;
                } catch(DatabaseException ex) {
                    throw new AuraException("Txn abort failed", ex);
                }
            } catch(DatabaseException e) {
                try {
                    if (txn != null) {
                        txn.abort();
                    }
                } catch(DatabaseException ex) {
                }
                throw new AuraException("Transaction failed", e);
            }
        }
        throw new AuraException("putAttn failed for " + pa + " after " +
                numRetries + " retries");
    }

    /**
     * Puts attention into the entry store.  Attentions should never be
     * overwritten.
     * 
     * @param pa the attention
     */
    public void putAttention(List<PersistentAttention> pas) throws AuraException {
        int numRetries = 0;
        while(numRetries < MAX_DEADLOCK_RETRIES) {
            Transaction txn = null;
            try {
                TransactionConfig txConf = new TransactionConfig();
                txConf.setWriteNoSync(true);
                txn = dbEnv.beginTransaction(null, txConf);
                for (PersistentAttention pa : pas) {
                    allAttn.putNoOverwrite(txn, pa);
                }
                txn.commit();
                return;
            } catch(DeadlockException e) {
                try {
                    txn.abort();
                    numRetries++;
                } catch(DatabaseException ex) {
                    throw new AuraException("Txn abort failed", ex);
                }
            } catch(DatabaseException e) {
                try {
                    if (txn != null) {
                        txn.abort();
                    }
                } catch(DatabaseException ex) {
                }
                throw new AuraException("Transaction failed", e);
            }
        }
        throw new AuraException("putAttns failed for <list> after " +
                numRetries + " retries");
    }

    /**
     * Delete all the attention that has as a source or target the given item.
     * This should be used to clean up after an item was deleted.
     * 
     * @param itemKey the key of the item
     * @throws com.sun.labs.aura.util.AuraException
     */
    public void removeAttention(String itemKey)
            throws AuraException {
        int numRetries = 0;
        while(numRetries < MAX_DEADLOCK_RETRIES) {
            //
            // Get all the attentions for which this item was a source and delete
            Transaction txn = null;
            try {
                //
                // Get all the attentions for which this item was a source
                // and delete them
                EntityIndex<Long, PersistentAttention> attns =
                        attnBySourceKey.subIndex(itemKey);
                txn = dbEnv.beginTransaction(null, null);
                EntityCursor<PersistentAttention> c = attns.entities(txn, new CursorConfig());
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
                attns = attnByTargetKey.subIndex(itemKey);
                c = attns.entities();
                try {
                    for(PersistentAttention a : c) {
                        c.delete();
                    }
                } finally {
                    if(c != null) {
                        c.close();
                    }
                }
                txn.commit();
                return;
            } catch (DeadlockException ex) {
                try {
                    txn.abort();
                    numRetries++;
                } catch(DatabaseException dex) {
                    throw new AuraException("Txn abort failed", dex);
                }

            } catch(DatabaseException ex) {
                log.log(Level.WARNING, "Failed to delete attention related to "
                        + itemKey, ex);
                try {
                    if (txn != null) {
                        txn.abort();
                    }
                } catch(DatabaseException dex) {
                }
                throw new AuraException("Transaction failed", ex);
            }
        }
        throw new AuraException("deleteAttn failed for item " + itemKey
                + " after " + numRetries + " retries");
    }
    
    public void removeAttention(String srcKey, String targetKey,
                                Attention.Type type)
                throws AuraException {
        //
        // Get all matching attention and remove it
        EntityJoin<Long, PersistentAttention> join = new EntityJoin(allAttn);
        join.addCondition(attnBySourceKey, srcKey);
        join.addCondition(attnByTargetKey, targetKey);
        join.addCondition(attnByType, type.ordinal());

        int numRetries = 0;
        while(numRetries < MAX_DEADLOCK_RETRIES) {
            Transaction txn = null;
            try {
                //
                // Get all the attention IDs that match the criteria
                List<Long> attnIDs = new ArrayList();
                ForwardCursor<PersistentAttention> cur = null;
                try {
                    cur = join.entities();
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
                try {
                    txn = dbEnv.beginTransaction(null, null);
                    for (Long id : attnIDs) {
                        allAttn.delete(txn, id);
                    }
                    txn.commit();
                    return;
                } catch (DeadlockException e) {
                    try {
                        numRetries++;
                        txn.abort();
                    } catch (DatabaseException ex) {
                        throw new AuraException("Txn abort failed", ex);
                    }
                }
                
            } catch (DatabaseException e) {
                log.log(Level.WARNING, "Failed to remove Attention", e);
                try {
                    if (txn != null) {
                        txn.abort();
                    }
                } catch (DatabaseException ex) {
                }
                throw new AuraException("Remove attention failed", e);
            }
        }
        throw new AuraException("removeAttn failed for src " + srcKey +
                " and tgt " + targetKey + " after " + numRetries + " retries");
    }
    
    /**
     * Gets all the items of a particular type that have been added since a
     * particular time.  Returns an iterator over those items that must be
     * closed when reading is done.
     * 
     * @param itemType the type of item to retrieve
     * @param timeStamp the time from which to search (to the present time
     * @return an iterator over the added items
     * @throws com.sun.labs.aura.aardvark.util.AuraException 
     */
    public DBIterator<Item> getItemsAddedSince(ItemType itemType,
            long timeStamp) throws AuraException {
        //
        // We need to get a cursor based on the long & time key that stores
        // the item type and the time that it was added.  We'll get a cursor
        // for only that type and the range of times from the provided
        // timestamp to the current time.
        IntAndTimeKey begin = new IntAndTimeKey(itemType.ordinal(), timeStamp);
        IntAndTimeKey end = new IntAndTimeKey(itemType.ordinal(),
                System.currentTimeMillis());

        EntityCursor cursor = null;
        Transaction txn = null;
        try {
            TransactionConfig conf = new TransactionConfig();
            conf.setReadUncommitted(true);
            txn = dbEnv.beginTransaction(null, conf);
            //
            // This transaction is read-only and it is up to the developer
            // to release it.  Don't time out the transaction.
            txn.setTxnTimeout(0);

            //
            // Set Read Committed behavior - this ensures the stability of
            // the current item being read (puts a read lock on it) but allows
            // previously read items to change (releases the read lock after
            // reading).
            CursorConfig cc = new CursorConfig();
            cc.setReadCommitted(true);
            cursor = itemByTypeAndTime.entities(txn,
                    begin, true,
                    end, true,
                    cc);
        } catch(DatabaseException e) {
            handleCursorException(cursor, txn, e);
        }

        DBIterator<Item> dbIt = new EntityIterator<Item>(cursor, txn);
        return dbIt;
    }

    /**
     * Get items with a particular user id, attn type, and item type (answers
     * the query: Get me all the items of this type that this user has paid
     * this kind of attention to)
     * 
     * @param userID the id of the user
     * @param attnType the type of attention
     * @param itemType the type of the item
     * @return the set of matching items
     */
    @Deprecated
    public List<Item> getItems(
            String userKey,
            Attention.Type attnType,
            ItemType itemType) {

        List<Item> result = new ArrayList<Item>();
        //
        // First get all the attention of the particular type with the
        // particular user
        List<Attention> attns = getAttentionForSource(userKey, attnType);

        //
        // Now do the in-memory join, looking up each item as we go
        for(Attention attn : attns) {
            ItemImpl item = getItem(attn.getTargetKey());
            if(item.getType() == itemType) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Gets all the attention that has been added to the store since a
     * particular date.  Returns an iterator over the attention that must be
     * closed when reading is done.
     * 
     * @param timeStamp the time to search back to
     * @return the Attentions added since that time
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public DBIterator<Attention> getAttentionAddedSince(long timeStamp)
            throws AuraException {
        EntityCursor c = null;
        Transaction txn = null;
        try {
            TransactionConfig conf = new TransactionConfig();
            conf.setReadUncommitted(true);
            txn = dbEnv.beginTransaction(null, conf);
            //
            // This transaction is read-only and it is up to the developer
            // to release it.  Don't time out the transaction.
            txn.setTxnTimeout(0);

            //
            // Set Read Committed behavior - this ensures the stability of
            // the current item being read (puts a read lock on it) but allows
            // previously read items to change (releases the read lock after
            // reading).
            CursorConfig cc = new CursorConfig();
            cc.setReadCommitted(true);
            c = attnByTime.entities(txn, timeStamp, true,
                    System.currentTimeMillis(), true, cc);
        } catch(DatabaseException e) {
            try {
                if(c != null) {
                    c.close();
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
            throw new AuraException("getAttentionAddedSince failed", e);
        }
        DBIterator<Attention> dbIt = new EntityIterator<Attention>(c, txn);
        return dbIt;
    }

    public List<Attention> getAttentionForSource(String key) {
        return getAttentionFor(key, true);
    }

    public List<Attention> getAttentionForTarget(String key) {
        return getAttentionFor(key, false);
    }

    protected List<Attention> getAttentionFor(String key,
            boolean isSrc) {
        List<Attention> res = new ArrayList<Attention>();

        try {
            EntityIndex<Long, PersistentAttention> attns = null;
            if(isSrc) {
                attns = attnBySourceKey.subIndex(key);
            } else {
                attns = attnByTargetKey.subIndex(key);
            }
            EntityCursor<PersistentAttention> c = null;
            Transaction txn = null;
            try {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                txn = dbEnv.beginTransaction(null, conf);
                CursorConfig cc = new CursorConfig();
                cc.setReadCommitted(true);
                c = attns.entities(txn, cc);
                for(PersistentAttention a : c) {
                    res.add(a);
                }
            } finally {
                if(c != null) {
                    c.close();
                }
                if (txn != null) {
                    txn.commitNoSync();
                }
            }
        } catch(DatabaseException ex) {
            log.log(Level.WARNING, "Failed to read attention", ex);
        }
        Collections.sort(res, new ReverseAttentionTimeComparator());
        return res;
    }

    public List<Attention> getAttentionForSource(String userKey,
            Attention.Type type) {
        EntityJoin<Long, PersistentAttention> join = new EntityJoin(allAttn);
        join.addCondition(attnBySourceKey, userKey);
        join.addCondition(attnByType, type.ordinal());

        List<Attention> ret = new ArrayList<Attention>();

        try {
            ForwardCursor<PersistentAttention> cur = null;
            Transaction txn = null;
            try {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                txn = dbEnv.beginTransaction(null, conf);
                cur = join.entities(txn, CursorConfig.READ_UNCOMMITTED);
                for(PersistentAttention attn : cur) {
                    ret.add(attn);
                }
            } finally {
                if(cur != null) {
                    cur.close();
                }
                if (txn != null) {
                    txn.commitNoSync();
                }
            }
        } catch(DatabaseException e) {
            log.log(Level.WARNING, "Failed to get attention type " + type +
                    " for user " + userKey, e);
        }
        return ret;
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
    
    public DBIterator<Attention> getAttentionIterator(AttentionConfig ac)
            throws AuraException {
        EntityJoin<Long,PersistentAttention> join = null;
        if (!Util.isEmpty(ac)) {
            join = getAttentionJoin(ac);
        }

        ForwardCursor cur = null;
        Transaction txn = null;
        try {
            TransactionConfig conf = new TransactionConfig();
            conf.setReadUncommitted(true);
            txn = dbEnv.beginTransaction(null, conf);
            txn.setTxnTimeout(0);
            if (!Util.isEmpty(ac)) {
                cur = join.entities(txn, CursorConfig.READ_UNCOMMITTED);
            } else {
                cur = allAttn.entities(txn, CursorConfig.READ_UNCOMMITTED);
            }
        } catch(DatabaseException e) {
            handleCursorException(cur, txn, e);
        }
        DBIterator<Attention> dbIt = new EntityIterator<Attention>(cur, txn);
        return dbIt;
    }
    
    /**
     * Returns a count of attention meeting the given criteria
     * 
     * @param ac
     * @return
     */
    public Long getAttentionCount(AttentionConfig ac) {
        if (Util.isEmpty(ac)) {
            try {
                return allAttn.count();
            } catch(DatabaseException e) {
                log.log(Level.WARNING, "Failed to get count of all attentions",
                        e);
            }
            return new Long(0);
        }
        
        //
        // We need to iterate over all the attentions since the DB can't
        // tell us the count.  If calling this with a single constraint
        // is common (for example, only a source key, or only a target key)
        // we can probably special case to answer faster by instantiating
        // a subIndex for the specific value and calling count() on it.
        EntityJoin<Long,PersistentAttention> join = getAttentionJoin(ac);
        long ret = 0;
        try {
            ForwardCursor<PersistentAttention> cur = null;
            Transaction txn = null;
            try {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                txn = dbEnv.beginTransaction(null, conf);
                cur = join.entities(txn, CursorConfig.READ_UNCOMMITTED);
                for(PersistentAttention attn : cur) {
                    ret++;
                }
            } finally {
                if(cur != null) {
                    cur.close();
                }
                if (txn != null) {
                    txn.commitNoSync();
                }
            }
        } catch(DatabaseException e) {
            log.log(Level.WARNING, "Failed to read attention ", e);
        }
        return ret;

    }
    
    public DBIterator<Attention> getAttentionSinceIterator(AttentionConfig ac,
                                                           Date timeStamp)
            throws AuraException {
        if (Util.isEmpty(ac)) {
            throw new AuraException("At least one constraint must be " +
                "specified before calling getAttentionSince(AttentionConfig)");
        }
        //
        // We'll do the join in the DB, then filter the time on memory
        EntityJoin<Long,PersistentAttention> join = getAttentionJoin(ac);

        ForwardCursor cur = null;
        Transaction txn = null;
        try {
            TransactionConfig conf = new TransactionConfig();
            conf.setReadUncommitted(true);
            txn = dbEnv.beginTransaction(null, conf);
            txn.setTxnTimeout(0);
            cur = join.entities(txn, CursorConfig.READ_UNCOMMITTED);
        } catch(DatabaseException e) {
            handleCursorException(cur, txn, e);
        }
        DateFilterEntityIterator dbIt =
                new DateFilterEntityIterator(cur, txn, timeStamp);
        return dbIt;
    }

    /**
     * Returns a count of attention meeting the given criteria, after the given
     * date
     * 
     * @param ac
     * @return
     */
    public Long getAttentionSinceCount(AttentionConfig ac, Date timeStamp)
            throws AuraException {
        if (Util.isEmpty(ac)) {
            throw new AuraException("At least one constraint must be " +
                "specified before calling getAttentionSince(AttentionConfig)");
        }
        
        //
        // We need to iterate over all the attentions since the DB can't
        // tell us the count.  If calling this with a single constraint
        // is common (for example, only a source key, or only a target key)
        // we can probably special case to answer faster by instantiating
        // a subIndex for the specific value and calling count() on it.
        EntityJoin<Long,PersistentAttention> join = getAttentionJoin(ac);
        long ret = 0;
        try {
            ForwardCursor<PersistentAttention> cur = null;
            Transaction txn = null;
            try {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                txn = dbEnv.beginTransaction(null, conf);
                cur = join.entities(txn, CursorConfig.READ_UNCOMMITTED);
                for(PersistentAttention attn : cur) {
                    //
                    // Post process the date filter in memory
                    if (attn.getTimeStamp() >= timeStamp.getTime()) {
                        ret++;
                    }
                }
            } finally {
                if(cur != null) {
                    cur.close();
                }
                if (txn != null) {
                    txn.commitNoSync();
                }
            }
        } catch(DatabaseException e) {
            log.log(Level.WARNING, "Failed to read attention ", e);
        }
        return ret;

    }

    public DBIterator<Attention> getAttentionForSourceSince(String key,
            long timeStamp) throws AuraException {
        return getAttentionForKeySince(key, true, timeStamp);
    }
    
    public DBIterator<Attention> getAttentionForTargetSince(String key,
            long timeStamp) throws AuraException {
        return getAttentionForKeySince(key, false, timeStamp);
    }
    
    protected DBIterator<Attention> getAttentionForKeySince(String key,
            boolean isSrc, long timeStamp) throws AuraException {
        //
        // Set the begin and end times chronologically
        StringAndTimeKey begin = new StringAndTimeKey(key, timeStamp);
        StringAndTimeKey end = new StringAndTimeKey(key + '\0',
                System.currentTimeMillis());
        EntityCursor c = null;
        Transaction txn = null;
        try {
            TransactionConfig conf = new TransactionConfig();
            conf.setReadUncommitted(true);
            txn = dbEnv.beginTransaction(null, conf);
            //
            // This transaction is read-only and it is up to the developer
            // to release it.  Don't time out the transaction.
            txn.setTxnTimeout(0);

            //
            // Set Read Committed behavior - this ensures the stability of
            // the current item being read (puts a read lock on it) but allows
            // previously read items to change (releases the read lock after
            // reading).
            CursorConfig cc = new CursorConfig();
            cc.setReadCommitted(true);
            if (isSrc) {
                c = attnBySourceAndTime.entities(
                        txn, begin, true, end, false, cc);
            } else {
                c = attnByTargetAndTime.entities(
                        txn, begin, true, end, false, cc);
            }
        } catch(DatabaseException e) {
            try {
                if(c != null) {
                    c.close();
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
            throw new AuraException("getAttentionForKeySince failed", e);
        }
        DBIterator<Attention> dbIt = new EntityIterator<Attention>(c, txn);
        return dbIt;
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
    public List<Attention> getLastAttentionForUser(String srcKey,
            Attention.Type type, int count) {
        //
        // Start querying for attention for this user based on time, expanding
        // the time range until we have enough attention.
        Set<Attention> results = new HashSet<Attention>();
        long recent = System.currentTimeMillis();

        // Try one hour first
        List<Attention> curr =
                getUserAttnForTimePeriod(srcKey, type, recent,
                Times.ONE_HOUR, count);

        count -= curr.size();
        recent -= Times.ONE_HOUR;
        results.addAll(curr);
        if(count <= 0) {
            List<Attention> temp = new ArrayList<Attention>(results);
            Collections.sort(temp, new ReverseAttentionTimeComparator());
            return temp;
        }

        //
        // Now add in from one hour ago to one day ago
        curr = getUserAttnForTimePeriod(srcKey, type, recent,
                Times.ONE_DAY, count);
        count -= curr.size();
        recent -= Times.ONE_DAY;
        results.addAll(curr);
        if(count <= 0) {
            List<Attention> temp = new ArrayList<Attention>(results);
            Collections.sort(temp, new ReverseAttentionTimeComparator());
            return temp;
        }

        //
        // Now add in from one day ago to one week ago
        curr = getUserAttnForTimePeriod(srcKey, type, recent,
                Times.ONE_WEEK, count);
        count -= curr.size();
        recent -= Times.ONE_WEEK;
        results.addAll(curr);
        if(count <= 0) {
            List<Attention> temp = new ArrayList<Attention>(results);
            Collections.sort(temp, new ReverseAttentionTimeComparator());
            return temp;
        }

        //
        // Now add in from one week ago to one month ago
        curr = getUserAttnForTimePeriod(srcKey, type, recent,
                Times.ONE_MONTH, count);
        count -= curr.size();
        recent -= Times.ONE_MONTH;
        results.addAll(curr);
        if(count <= 0) {
            List<Attention> temp = new ArrayList<Attention>(results);
            Collections.sort(temp, new ReverseAttentionTimeComparator());
            return temp;
        }

        //
        // Finally, expand out to one year.
        curr = getUserAttnForTimePeriod(srcKey, type, recent,
                Times.ONE_YEAR, count);
        //
        // Take whatever we got and return it.  We won't search back more than
        // one year.
        results.addAll(curr);
        List<Attention> temp = new ArrayList<Attention>(results);
        Collections.sort(temp, new ReverseAttentionTimeComparator());
        return temp;
    }

    private List<Attention> getUserAttnForTimePeriod(
            String srcKey,
            Attention.Type type,
            long recentTime,
            long interval,
            int count) {
        List<Attention> result = new ArrayList<Attention>();
        //
        // Set the begin and end times chronologically
        StringAndTimeKey begin = new StringAndTimeKey(srcKey, recentTime -
                interval);
        StringAndTimeKey end = new StringAndTimeKey(srcKey + 1, recentTime);
        EntityCursor<PersistentAttention> cursor = null;
        Transaction txn = null;
        try {
            try {
                //
                // Examine each item in the cursor in reverse order (newest
                // first) to see if it matches our requirements.  If so, add
                // it to our return set.
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                txn = dbEnv.beginTransaction(null, conf);
                cursor = attnBySourceAndTime.entities(txn, begin, true, end, false, CursorConfig.READ_UNCOMMITTED);
                PersistentAttention curr = cursor.last();
                while(curr != null && count > 0) {
                    if((type == null) || (curr.getType().equals(type))) {
                        result.add(curr);
                        count--;
                    }
                    curr = cursor.prev();
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }
                if (txn != null) {
                    txn.commitNoSync();
                }
            }
        } catch(DatabaseException e) {
            log.log(Level.WARNING, "Failed while retrieving " + count +
                    " recent attentions for user " + srcKey, e);
        }
        return result;
    }

    /**
     * Gets the number of items of a particular type that are in the index.
     * 
     * @param type the type of item to count
     * @return the number of instances of that item type in the index
     */
    public long getItemCount(ItemType type) {
        try {
            if (type == null) {
                return itemByKey.count();
            } else {
                EntityIndex idx = itemByType.subIndex(type.ordinal());
                return idx.count();
            }
        } catch(DatabaseException e) {
            log.log(Level.WARNING, "Failed to get count for items of type " +
                    type);
        }
        return 0;
    }

    /**
     * Gets the number of attentions that are in the attention index
     * 
     * @return the number of instances of attention in the index
     */
    public long getAttentionCount() {
        try {
            return allAttn.count();
        } catch(DatabaseException e) {
            log.log(Level.WARNING, "Failed to get count of attentions");
        }
        return 0;
    }

    /**
     * Get the number of user entities in the entity store
     * 
     * @return the number of users or -1 if there was an error
     */
    public long getNumUsers() {
        long count = -1;
        try {
            count = allUsers.count();
        } catch(DatabaseException e) {
            log.log(Level.WARNING, "getNumUsers failed", e);
        }
        return count;
    }

    /**
     * Close up the entity store and the database environment.
     */
    public void close() {
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

    }
    
    /**
     * Gets the size of the database in bytes
     * 
     * @return the size in bytes
     */
    public long getSize() {
        try {
            EnvironmentStats stats = dbEnv.getStats(null);
            return stats.getTotalLogSize();
        } catch (DatabaseException e) {
            log.warning("Failed to get DB stats: " + e.getMessage());
        }
        return 0;
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

}
