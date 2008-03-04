package com.sun.labs.aura.datastore.impl.store;

import com.sleepycat.je.CursorConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DeadlockException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.ForwardCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.aardvark.util.Times;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.aura.datastore.impl.store.persist.IntAndTimeKey;
import com.sun.labs.aura.datastore.impl.store.persist.UserImpl;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.aura.datastore.impl.store.persist.StringAndTimeKey;
import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
     * the composite key of user ID and timestamp.
     */
    protected SecondaryIndex<StringAndTimeKey, Long, PersistentAttention> attnBySourceAndTime;

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
        this(dbEnvDir, logger, false);
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
            boolean overwrite)
            throws DatabaseException {
        this.log = logger;

        EnvironmentConfig econf = new EnvironmentConfig();
        StoreConfig sconf = new StoreConfig();

        econf.setAllowCreate(true);
        econf.setTransactional(true);
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

        dbEnv = new Environment(dir, econf);
        store = new EntityStore(dbEnv, "Aura", sconf);

        //
        // Load the indexes that we'll use during regular operation
        //itemByID = store.getPrimaryIndex(Long.class, ItemImpl.class);

        //itemByKey = store.getSecondaryIndex(itemByID, String.class, "key");
        itemByKey = store.getPrimaryIndex(String.class, ItemImpl.class);

        itemByType = store.getSecondaryIndex(itemByKey,
                Integer.class,
                "itemType");
        itemByTypeAndTime = store.getSecondaryIndex(itemByKey,
                IntAndTimeKey.class,
                "typeAndTimeAdded");

        allUsers = store.getSubclassIndex(itemByKey, UserImpl.class,
                Boolean.class, "isUser");

        allAttn = store.getPrimaryIndex(Long.class,
                PersistentAttention.class);

        attnByTargetKey = store.getSecondaryIndex(allAttn,
                String.class,
                "targetKey");

        attnBySourceKey = store.getSecondaryIndex(allAttn,
                String.class,
                "sourceKey");

        attnByType = store.getSecondaryIndex(allAttn,
                Integer.class,
                "type");

        attnByTime = store.getSecondaryIndex(allAttn, Long.class, "timeStamp");

        attnBySourceAndTime = store.getSecondaryIndex(allAttn,
                StringAndTimeKey.class,
                "sourceAndTime");
    }

    /**
     * Gets the set of all items of a particular type.  This could be a large
     * set.
     * 
     * @return all users in the item store
     */
    public Set<Item> getAll(Item.ItemType type) {
        Set<Item> items = new HashSet<Item>();
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
     * Puts an attention into the entry store.  Attentions should never be
     * overwritten.  Since Users and Items have links to their attentions by
     * ID, we need to update that table too.
     * 
     * @param pa the attention
     */
    public void putAttention(PersistentAttention pa) throws AuraException {
        int numRetries = 0;
        while(numRetries < MAX_DEADLOCK_RETRIES) {
            Transaction txn = null;
            try {
                txn = dbEnv.beginTransaction(null, null);
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
                    txn.abort();
                } catch(DatabaseException ex) {
                }
                throw new AuraException("Transaction failed", e);
            }
        }
        throw new AuraException("putAttn failed for " + pa + " after " +
                numRetries + " retries");
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
            txn = dbEnv.beginTransaction(null, null);
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
            try {
                if(cursor != null) {
                    cursor.close();
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
    public Set<Item> getItems(
            String userKey,
            Attention.Type attnType,
            ItemType itemType) {

        Set<Item> result = new HashSet<Item>();
        //
        // First get all the attention of the particular type with the
        // particular user
        Set<PersistentAttention> attns = getAttentionForUser(userKey, attnType);

        //
        // Now do the in-memory join, looking up each item as we go
        for(PersistentAttention attn : attns) {
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
            txn = dbEnv.beginTransaction(null, null);
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

    public Set<Attention> getAttentionForSource(String key) {
        return getAttentionFor(key, true);
    }

    public Set<Attention> getAttentionForTarget(String key) {
        return getAttentionFor(key, false);
    }

    protected Set<Attention> getAttentionFor(String key,
            boolean isSrc) {
        HashSet<Attention> res = new HashSet<Attention>();

        try {
            EntityIndex<Long, PersistentAttention> attns = null;
            if(isSrc) {
                attns = attnBySourceKey.subIndex(key);
            } else {
                attns = attnByTargetKey.subIndex(key);
            }
            EntityCursor<PersistentAttention> c = attns.entities();
            try {
                for(PersistentAttention a : c) {
                    res.add(a);
                }
            } finally {
                if(c != null) {
                    c.close();
                }
            }
        } catch(DatabaseException ex) {
            log.log(Level.WARNING, "Failed to close cursor", ex);
        }
        return res;
    }

    public SortedSet<PersistentAttention> getAttentionForUser(String userKey,
            Attention.Type type) {
        EntityJoin<Long, PersistentAttention> join = new EntityJoin(allAttn);
        join.addCondition(attnBySourceKey, userKey);
        join.addCondition(attnByType, type.ordinal());

        TreeSet<PersistentAttention> ret = new TreeSet<PersistentAttention>(
                new RevAttnTimeComparator());
        try {
            ForwardCursor<PersistentAttention> cur = null;
            try {
                cur = join.entities();
                for(PersistentAttention attn : cur) {
                    ret.add(attn);
                }
            } finally {
                if(cur != null) {
                    cur.close();
                }
            }
        } catch(DatabaseException e) {
            log.log(Level.WARNING, "Failed to get attention type " + type +
                    " for user " + userKey, e);
        }
        return ret;
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
    public SortedSet<Attention> getLastAttentionForUser(String srcKey,
            Attention.Type type, int count) {
        //
        // Start querying for attention for this user based on time, expanding
        // the time range until we have enough attention.
        TreeSet<Attention> results = new TreeSet<Attention>(
                new RevAttnTimeComparator());
        long recent = System.currentTimeMillis();

        // Try one hour first
        SortedSet<Attention> curr =
                getUserAttnForTimePeriod(srcKey, type, recent,
                Times.ONE_HOUR, count);

        count -= curr.size();
        recent -= Times.ONE_HOUR;
        results.addAll(curr);
        if(count <= 0) {
            return results;
        }

        //
        // Now add in from one hour ago to one day ago
        curr = getUserAttnForTimePeriod(srcKey, type, recent,
                Times.ONE_DAY, count);
        count -= curr.size();
        recent -= Times.ONE_DAY;
        results.addAll(curr);
        if(count <= 0) {
            return results;
        }

        //
        // Now add in from one day ago to one week ago
        curr = getUserAttnForTimePeriod(srcKey, type, recent,
                Times.ONE_WEEK, count);
        count -= curr.size();
        recent -= Times.ONE_WEEK;
        results.addAll(curr);
        if(count <= 0) {
            return results;
        }

        //
        // Now add in from one week ago to one month ago
        curr = getUserAttnForTimePeriod(srcKey, type, recent,
                Times.ONE_MONTH, count);
        count -= curr.size();
        recent -= Times.ONE_MONTH;
        results.addAll(curr);
        if(count <= 0) {
            return results;
        }

        //
        // Finally, expand out to one year.
        curr = getUserAttnForTimePeriod(srcKey, type, recent,
                Times.ONE_YEAR, count);
        //
        // Take whatever we got and return it.  We won't search back more than
        // one year.
        results.addAll(curr);
        return results;
    }

    private SortedSet<Attention> getUserAttnForTimePeriod(
            String srcKey,
            Attention.Type type,
            long recentTime,
            long interval,
            int count) {
        TreeSet<Attention> result = new TreeSet<Attention>(
                new RevAttnTimeComparator());
        //
        // Set the begin and end times chronologically
        StringAndTimeKey begin = new StringAndTimeKey(srcKey, recentTime -
                interval);
        StringAndTimeKey end = new StringAndTimeKey(srcKey + 1, recentTime);
        EntityCursor<PersistentAttention> cursor = null;
        try {
            try {
                //
                // Examine each item in the cursor in reverse order (newest
                // first) to see if it matches our requirements.  If so, add
                // it to our return set.
                cursor = attnBySourceAndTime.entities(begin, true, end, false);
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
            EntityIndex idx = itemByType.subIndex(type.ordinal());
            return idx.count();
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
                store.close();
            } catch(DatabaseException e) {
                log.log(Level.SEVERE, "Failed to close entity store", e);
            }
        }

        if(dbEnv != null) {
            try {
                dbEnv.close();
            } catch(DatabaseException e) {
                log.log(Level.SEVERE, "Failed to close database environment",
                        e);
            }
        }

    }

    /**
     * Compares attention objects to sort in reverse chronological order
     */
    class RevAttnTimeComparator implements Comparator<Attention> {

        public int compare(Attention o1, Attention o2) {
            if(o1.getTimeStamp() - o2.getTimeStamp() < 0) {
                return 1;
            } else if(o1.getTimeStamp() == o2.getTimeStamp()) {
                return 0;
            } else {
                return -1;
            }

        }
    }
    /**
     * Compares item objects to sort in reverse chronological order
     */
    /*
    class RevItemTimeComparator implements Comparator<Item> {
    public int compare(Item o1, Item o2) {
    if (o1.getTimeStamp() - o2.getTimeStamp() < 0) {
    return 1;
    } else if (o1.getTimeStamp() == o2.getTimeStamp()) {
    return 0;
    } else {
    return -1;
    }
    }
    }
     */
}
