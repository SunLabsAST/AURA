package com.sun.labs.aura.aardvark.impl.bdb.store;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.ForwardCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.EntryImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.FeedImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.ItemImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.UserImpl;
import com.sun.labs.aura.aardvark.store.Attention;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a wrapper around all the berkeley DB iteractions to isolate it
 * from other logic in the item store.
 */
public class BerkeleyDataWrapper {
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
     * A sub-index across all entries in the item store
     */
    protected SecondaryIndex<Boolean,Long,EntryImpl> allEntries;
    
    /**
     * A sub-index across all feeds in the item store
     */
    protected SecondaryIndex<Boolean,Long,FeedImpl> allFeeds;
    
    /**
     * The index of all Attention in the item store, accessible by ID
     */
    protected PrimaryIndex<Long,PersistentAttention> attnByID;
    
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
        
        allEntries = store.getSubclassIndex(itemByID, EntryImpl.class,
                                            Boolean.class, "isEntry");
        
        allFeeds = store.getSubclassIndex(itemByID, FeedImpl.class,
                                          Boolean.class, "isFeed");
        
        attnByID = store.getPrimaryIndex(Long.class,
                                         PersistentAttention.class);
        
        attnByItemID = store.getSecondaryIndex(attnByID,
                                               Long.class,
                                               "itemID");
        
        attnByUserID = store.getSecondaryIndex(attnByID,
                                               Long.class,
                                               "userID");
       
        attnByType = store.getSecondaryIndex(attnByID,
                                             Integer.class,
                                             "type");
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
    public ItemImpl putItem(ItemImpl item) {
        ItemImpl ret = null;
        try {
            ret = itemByID.put(item);
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "putItem() failed to put item (key:" +
                           item.getKey() + ")", e);
        }
        return ret;
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
    
    public PersistentAttention getAttention(long id) {
        PersistentAttention pa = null;
        try {
            pa = attnByID.get(id);
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "Failed to get attention (id:" + id + ")",
                    e);
        }
        return pa;
    }
    
    public Set<PersistentAttention> getAttentionForUser(long userID,
                                                        Attention.Type type) {
        EntityJoin<Long,PersistentAttention> join = new EntityJoin(attnByID);
        join.addCondition(attnByUserID, userID);
        join.addCondition(attnByType, type.ordinal());
        
        Set<PersistentAttention> ret = new HashSet<PersistentAttention>();
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
     * Puts an attention into the entry store.  Attentions should never be
     * overwritten.
     * 
     * @param pa the attention
     */
    public void putAttention(PersistentAttention pa) {
        try {
            attnByID.putNoOverwrite(pa);
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "putAttn() failed for userID:" +
                    pa.getUserID() + " and itemID:" + pa.getItemID(), e);
        }
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
            count = attnByID.count();
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

}
