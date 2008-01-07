package com.sun.labs.aura.aardvark.impl.bdb.store;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DeadlockException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.ForwardCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.EntryImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.FeedImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.IDTimeKey;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.ItemImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.UserImpl;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.DBIterator;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.aura.aardvark.util.Times;
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
     * The index of all Items in the store, accessible by ID
     */
    protected PrimaryIndex<Long,ItemImpl> itemByID;
    
    /**
     * The index of all Items in the store, accessible by key
     */
    protected SecondaryIndex<String,Long,ItemImpl> itemByKey;
    
    /**
     * A sub-index across all users in the item store
     */
    protected SecondaryIndex<Boolean,Long,UserImpl> allUsers;
    
    /**
     * A sub-index of all users, accessible by the time they were added
     */
    protected SecondaryIndex<Long,Long,UserImpl> usersByAddedTime;
    
    /**
     * A sub-index across all entries in the item store
     */
    protected SecondaryIndex<Boolean,Long,EntryImpl> allEntries;
    
    /**
     * A sub-index across all feeds in the item store
     */
    protected SecondaryIndex<Boolean,Long,FeedImpl> allFeeds;
    
    /**
     * An index of entries, accessible by feed ID
     */
    protected SecondaryIndex<Long,Long,EntryImpl> entriesByFeedID;
    
    /**
     * A sub-index of all entries, accessible by the time they were added
     */
    protected SecondaryIndex<Long,Long,EntryImpl> entriesByAddedTime;
    
    /**
     * A sub-index of all feeds, accessible by the time they were added
     */
    protected SecondaryIndex <Long,Long,FeedImpl> feedsByAddedTime;
    
    /**
     * The index of all Attention in the item store, accessible by ID
     */
    protected PrimaryIndex<Long,PersistentAttention> allAttn;
    
    /**
     * The index of all Attention in the item store, accessible by
     * the associated item
     */
    protected SecondaryIndex<Long,Long,PersistentAttention> attnByItemID;
    
    /**
     * The index of all Attention in the item store, accessible by
     * the associated user
     */
    protected SecondaryIndex<Long,Long,PersistentAttention> attnByUserID;
    
    /**
     * The index of all Attention in the item store, accessible by
     * the type of attention
     */
    protected SecondaryIndex<Integer,Long,PersistentAttention> attnByType;
    
    /**
     * The index of all Attention, accessible by the timestamp
     */
    protected SecondaryIndex<Long,Long,PersistentAttention> attnByTime;
    
    /**
     * The index of all Attention in the item store, accessible by
     * the composite key of user ID and timestamp.
     */
    protected SecondaryIndex<IDTimeKey,Long,PersistentAttention>
            attnByUserAndTime;
    
    
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
        econf.setLockTimeout(10000000L); // PBL test - set the lock timeout to 10 seconds
        sconf.setAllowCreate(true);
        sconf.setTransactional(true);
        
        econf.setConfigParam("je.txn.dumpLocks", "true");
        
        File dir = new File(dbEnvDir);
        if (!dir.exists()) {
            dir.mkdirs();
        } else if (overwrite) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
            dir.delete();
            dir.mkdir();
        }
        
        dbEnv = new Environment(dir, econf);
        store = new EntityStore(dbEnv, "Aardvark", sconf);
        
        //
        // Load the indexes that we'll use during regular operation
        itemByID = store.getPrimaryIndex(Long.class, ItemImpl.class);
        
        itemByKey = store.getSecondaryIndex(itemByID, String.class, "key");
        
        allUsers = store.getSubclassIndex(itemByID, UserImpl.class,
                                          Boolean.class, "isUser");
        
        usersByAddedTime = store.getSubclassIndex(itemByID, UserImpl.class,
                                                  Long.class, "userAddedTime");
        
        allEntries = store.getSubclassIndex(itemByID, EntryImpl.class,
                                            Boolean.class, "isEntry");
        
        entriesByFeedID = store.getSubclassIndex(itemByID, EntryImpl.class,
                                                 Long.class, "parentFeedID");
        
        entriesByAddedTime = store.getSubclassIndex(itemByID, EntryImpl.class,
                                                  Long.class, "entryAddedTime");
        
        allFeeds = store.getSubclassIndex(itemByID, FeedImpl.class,
                                          Boolean.class, "isFeed");
        
        feedsByAddedTime = store.getSubclassIndex(itemByID, FeedImpl.class,
                                                  Long.class, "feedAddedTime");
        
        allAttn = store.getPrimaryIndex(Long.class,
                                         PersistentAttention.class);
        
        attnByItemID = store.getSecondaryIndex(allAttn,
                                               Long.class,
                                               "itemID");
        
        attnByUserID = store.getSecondaryIndex(allAttn,
                                               Long.class,
                                               "userID");
       
        attnByType = store.getSecondaryIndex(allAttn,
                                             Integer.class,
                                             "type");
        
        attnByTime = store.getSecondaryIndex(allAttn, Long.class, "timeStamp");
        
        attnByUserAndTime = store.getSecondaryIndex(allAttn,
                                                    IDTimeKey.class,
                                                    "userAndTime");
    }
    
    /**
     * Gets the set of all users known to the store.  This could be a large
     * set.
     * 
     * @return all users in the item store
     */
    public Set<UserImpl> getAllUsers() {
        Set<UserImpl> users = new HashSet<UserImpl>();
        try {
            EntityCursor<UserImpl> cur = allUsers.entities();
            try {
                for (UserImpl u : cur) {
                    u.setBerkeleyDataWrapper(this);
                    users.add(u);
                }
            } finally {
                cur.close();
            }
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "Failed to retrieve users", e);
        }
        return users;
    }

    /**
     * Gets the set of all entries known to the store.  This could be a large
     * set.
     * 
     * @return all entries in the item store
     */
    public Set<EntryImpl> getAllEntries() {
        Set<EntryImpl> entries = new HashSet<EntryImpl>();
        try {
            EntityCursor<EntryImpl> cur = allEntries.entities();
            try {
                for (EntryImpl e : cur) {
                    e.setBerkeleyDataWrapper(this);
                    entries.add(e);
                }
            } finally {
                cur.close();
            }
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "Failed to retrieve entries", e);
        }
        return entries;
    }
    
    /**
     * Gets the set of all feeds known to the store.  This could be a large
     * set.
     * 
     * @return all feeds in the item store
     */
    public Set<FeedImpl> getAllFeeds() {
        Set<FeedImpl> feeds = new HashSet<FeedImpl>();
        try {
            EntityCursor<FeedImpl> cur = allFeeds.entities();
            try {
                for (FeedImpl f : cur) {
                    f.setBerkeleyDataWrapper(this);
                    feeds.add(f);
                }
            } finally {
                cur.close();
            }
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "Failed to retrieve entries", e);
        }
        return feeds;
    }
    
    /**
     * Gets an item from the entity store
     * 
     * @param id the id of the item to fetch
     * @return the item or null if the id is unknown
     */
    public ItemImpl getItem(long id) {
        ItemImpl ret = null;
        try {
            ret = itemByID.get(id);
            if (ret != null) {
                ret.setBerkeleyDataWrapper(this);
            }
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "getItem() failed to retrieve item (id:" +
                           id + ")", e);
        }
        return ret;
    }
    
    /**
     * Gets an item from the entity store
     * @param key the key of the item to fetch
     * @return the item or null if the key is unknown
     */
    public ItemImpl getItem(String key) {
        ItemImpl ret = null;
        try {
            ret = itemByKey.get(key);
            if (ret != null) {
                ret.setBerkeleyDataWrapper(this);
            }
        } catch (DatabaseException e) {
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
        while (numRetries < MAX_DEADLOCK_RETRIES) {
            Transaction txn = null;
            try {
                txn = dbEnv.beginTransaction(null, null);
                ret = itemByID.put(txn, item);
                txn.commit();
                return ret;
            } catch (DeadlockException e) {
                try {
                    txn.abort();
                    numRetries++;
                } catch (DatabaseException ex) {
                    throw new AuraException("Txn abort failed", ex);
                }
            } catch (DatabaseException e) {
                try {
                    txn.abort();
                } catch (DatabaseException ex) {
                }
                throw new AuraException("Transaction failed", e);
            }
        }
        //log.log(Level.WARNING, "putItem() failed to put item (key:" +
        //               item.getKey() + ") after " + numRetries +
        //               " retries");
        throw new AuraException("putItem failed for " + item.getTypeString() +
                ":" + item.getKey() + " after " + numRetries + " retries");
    }

    /**
     * Puts an attention into the entry store.  Attentions should never be
     * overwritten.
     * 
     * @param pa the attention
     */
    public void putAttention(PersistentAttention pa) throws AuraException {
        int numRetries = 0;
        while (numRetries < MAX_DEADLOCK_RETRIES) {
            Transaction txn = null;
            try {
                txn = dbEnv.beginTransaction(null, null);
                allAttn.putNoOverwrite(pa);
                txn.commit();
                return;
            } catch (DeadlockException e) {
                try {
                    txn.abort();
                    numRetries++;
                } catch (DatabaseException ex) {
                    throw new AuraException("Txn abort failed", ex);
                }
            } catch (DatabaseException e) {
                try {
                    txn.abort();
                } catch (DatabaseException ex) {
                }
                throw new AuraException("Transaction failed", e);
            }
        }
        throw new AuraException("putAttn failed for userID:" + pa.getUserID() +
                " and itemID:" + pa.getItemID() + " after " + numRetries +
                " retries");
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
    public <T extends Item> DBIterator<T> getItemsAddedSince(Class<T> itemType,
            long timeStamp) throws AuraException {

        EntityCursor c = null;
        try {
            if (itemType.equals(User.class)) {
                c = usersByAddedTime.entities(timeStamp,true,
                        System.currentTimeMillis(), true);
            } else if (itemType.equals(Entry.class)) {
                c = entriesByAddedTime.entities(timeStamp, true,
                        System.currentTimeMillis(), true);
            } else if (itemType.equals(Feed.class)) {
                c = feedsByAddedTime.entities(timeStamp, true,
                        System.currentTimeMillis(), true);
            } else {
                throw new AuraException("Unsupported item type");
            }
        } catch (DatabaseException e) {
            if (c != null) {
                try {
                    c.close();
                } catch (DatabaseException ex) {
                    log.log(Level.WARNING, "Failed to close cursor", ex);
                }
            }
            throw new AuraException("getItemsAddedSince failed", e);
        }
        DBIterator<T> dbIt = new EntityIterator<T>(c);
        return dbIt;
    }
    
    /**
     * Convenience method to get a user from the user subclass index
     * 
     * @param id the id of a user
     * @return the user
     */
    public UserImpl getUser(long id) {
        UserImpl u = null;
        try {
            PrimaryIndex<Long,UserImpl> pi = allUsers.getPrimaryIndex();
            u = pi.get(id);
            if (u != null) {
                u.setBerkeleyDataWrapper(this);
            }
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "getUser() failed to get user (id:" + id
                    + ")", e);
        }
        return u;
    }
    
    public SortedSet<Entry> getAllEntriesForFeed(long feedID) {
        TreeSet<Entry> entries = new TreeSet<Entry>(
                new RevItemTimeComparator());
        try {
            EntityIndex<Long,EntryImpl> subIndex =
                    entriesByFeedID.subIndex(feedID);
            EntityCursor<EntryImpl> cursor = subIndex.entities();
            try {
                for (EntryImpl ent : cursor) {
                    entries.add(ent);
                }
            } finally {
                cursor.close();
            }
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "getAllEntriesForFeed() failed to get " +
                    "entries for feed (id: " + feedID + ")");
        }
        return entries;
        
    }
    
    public PersistentAttention getAttention(long id) {
        PersistentAttention pa = null;
        try {
            pa = allAttn.get(id);
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "Failed to get attention (id:" + id + ")",
                    e);
        }
        return pa;
    }
    public SortedSet<PersistentAttention> getAttentionForUser(long userID,
                                                        Attention.Type type) {
        EntityJoin<Long,PersistentAttention> join = new EntityJoin(allAttn);
        join.addCondition(attnByUserID, userID);
        join.addCondition(attnByType, type.ordinal());
        
        TreeSet<PersistentAttention> ret = new TreeSet<PersistentAttention>(
                new RevAttnTimeComparator());
        try {
            ForwardCursor<PersistentAttention> cur = null;
            try {
                cur = join.entities();
                for (PersistentAttention attn : cur) {
                    ret.add(attn);
                }
            } finally {
                if (cur != null) {
                    cur.close();
                }
            }
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "Failed to get attention type " + type +
                    " for user " + userID, e);
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
     * @param userID the ID of the user to query for
     * @param type the type of attention to limit to, or null for all attentions
     * @param count the desired number of attentions to return
     * @return a set of attentions, sorted by date
     */
    public SortedSet<Attention> getLastAttentionForUser(long userID,
            Attention.Type type, int count) {
        //
        // Start querying for attention for this user based on time, expanding
        // the time range until we have enough attention.
        TreeSet<Attention> results = new TreeSet<Attention>(
            new RevAttnTimeComparator());
        long recent = System.currentTimeMillis();

        // Try one hour first
        SortedSet<Attention> curr =
                getUserAttnForTimePeriod(userID, type, recent,
                Times.ONE_HOUR, count);
        
        count -= curr.size();
        recent -= Times.ONE_HOUR;
        results.addAll(curr);
        if (count <= 0) {
            return results;
        }

        //
        // Now add in from one hour ago to one day ago
        curr = getUserAttnForTimePeriod(userID, type, recent,
               Times.ONE_DAY, count);
        count -= curr.size();
        recent -= Times.ONE_DAY;
        results.addAll(curr);
        if (count <= 0) {
            return results;
        }
        
        //
        // Now add in from one day ago to one week ago
        curr = getUserAttnForTimePeriod(userID, type, recent,
               Times.ONE_WEEK, count);
        count -= curr.size();
        recent -= Times.ONE_WEEK;
        results.addAll(curr);
        if (count <= 0) {
            return results;
        }
        
        //
        // Now add in from one week ago to one month ago
        curr = getUserAttnForTimePeriod(userID, type, recent,
               Times.ONE_MONTH, count);
        count -= curr.size();
        recent -= Times.ONE_MONTH;
        results.addAll(curr);
        if (count <= 0) {
            return results;
        }
        
        //
        // Finally, expand out to one year.
        curr = getUserAttnForTimePeriod(userID, type, recent,
               Times.ONE_YEAR, count);
        //
        // Take whatever we got and return it.  We won't search back more than
        // one year.
        results.addAll(curr);
        return results;
    }
    
    private SortedSet<Attention> getUserAttnForTimePeriod(
            long userID,
            Attention.Type type,
            long recentTime,
            long interval,
            int count) {
        TreeSet<Attention> result = new TreeSet<Attention>(
                new RevAttnTimeComparator());
        //
        // Set the begin and end times chronologically
        IDTimeKey begin = new IDTimeKey(userID, recentTime - interval);
        IDTimeKey end = new IDTimeKey(userID + 1, recentTime);
        EntityCursor<PersistentAttention> cursor = null;
        try {
            try {
                //
                // Examine each item in the cursor in reverse order (newest
                // first) to see if it matches our requirements.  If so, add
                // it to our return set.
                cursor = attnByUserAndTime.entities(begin, true, end, false);
                PersistentAttention curr = cursor.last();
                while (curr != null && count > 0) {
                    if ((type == null) || (curr.getType().equals(type))) {
                        result.add(curr);
                        count--;
                    }
                    curr = cursor.prev();
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "Failed while retrieving " + count +
                    " recent attentions for user " + userID, e);
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
        try {
            c = attnByTime.entities(timeStamp, true,
                    System.currentTimeMillis(), true);
        } catch (DatabaseException e) {
            if (c != null) {
                try {
                    c.close();
                } catch (DatabaseException ex) {
                    log.log(Level.WARNING, "Failed to close cursor", ex);
                }
            }
            throw new AuraException("getAttentionAddedSince failed", e);
        }
        DBIterator<Attention> dbIt = new EntityIterator<Attention>(c);
        return dbIt;
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
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "getNumUsers failed", e);
        }
        return count;
    }
    
    /**
     * Gets the number of entry entities in the entity store
     * 
     * @return the number of entries or -1 if there was an error
     */
    public long getNumEntries() {
        long count = -1;
        try {
            count = allEntries.count();
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "getNumEntries failed", e);
        }
        return count;
    }
    
    /**
     * Gets the number of attention entities in the entity store
     * 
     * @return the number of attentions or -1 if there was an error
     */
    public long getNumAttention() {
        long count = -1;
        try {
            count = allAttn.count();
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "getNumAttn failed", e);
        }
        return count;
    }
    
    /**
     * Close up the entity store and the database environment.
     */
    public void close() {
        if (store != null) {
            try {
                store.close();
            } catch (DatabaseException e) {
                log.log(Level.SEVERE, "Failed to close entity store", e);
            }
        }
        
        if (dbEnv != null) {
            try {
                dbEnv.close();
            } catch (DatabaseException e) {
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
            if (o1.getTimeStamp() - o2.getTimeStamp() < 0) {
                return 1;
            } else if (o1.getTimeStamp() == o2.getTimeStamp()) {
                return 0;
            } else {
                return -1;
            }

        }
    }
    
    /**
     * Compares item objects to sort in reverse chronological order
     */
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
}
