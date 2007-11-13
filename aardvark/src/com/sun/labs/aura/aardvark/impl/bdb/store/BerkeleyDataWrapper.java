package com.sun.labs.aura.aardvark.impl.bdb.store;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.EntryImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.FeedImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.ItemImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.UserImpl;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Provides a wrapper around all the berkeley DB iteractions to isolate from
 * other logic in the item store.
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
    
    protected Logger logger;
    
    public BerkeleyDataWrapper(String dbEnvDir, Logger logger)
            throws DatabaseException {
        this.logger = logger;
        
        EnvironmentConfig econf = new EnvironmentConfig();
        StoreConfig sconf = new StoreConfig();

        econf.setAllowCreate(true);
        sconf.setAllowCreate(true);

        dbEnv = new Environment(new File(dbEnvDir), econf);
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
                    users.add(u);
                }
            } finally {
                cur.close();
            }
        } catch (DatabaseException e) {
            logger.warning("Failed to retrieve users: " + e.toString());
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
                    entries.add(e);
                }
            } finally {
                cur.close();
            }
        } catch (DatabaseException e) {
            logger.warning("Failed to retrieve entries: " + e.toString());
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
                    feeds.add(f);
                }
            } finally {
                cur.close();
            }
        } catch (DatabaseException e) {
            logger.warning("Failed to retrieve entries: " + e.toString());
        }
        return feeds;
    }
    
    /**
     * Gets an item from the entry store
     * 
     * @param id the id of the item to fetch
     * @return the item or null if the id is unknown
     */
    public ItemImpl getItem(long id) {
        ItemImpl ret = null;
        try {
            ret = itemByID.get(id);
        } catch (DatabaseException e) {
            logger.warning("getItem() failed to retrieve item (id:" +
                           id + ")");
        }
        return ret;
    }
    
    /**
     * Gets an item from the entry store
     * @param key the key of the item to fetch
     * @return the item or null if the key is unknown
     */
    public ItemImpl getItem(String key) {
        ItemImpl ret = null;
        try {
            ret = itemByKey.get(key);
        } catch (DatabaseException e) {
            logger.warning("getItem() failed to retrieve item (key:" +
                           key + ") " + e.getMessage());
        }
        return ret;
    }
    
    /**
     * Puts an item into the entry store.  If the item already exists, it will
     * be replaced.
     * 
     * @param item the item to put
     */
    public ItemImpl putItem(ItemImpl item) {
        ItemImpl ret = null;
        try {
            ret = itemByID.put(item);
        } catch (DatabaseException e) {
            logger.warning("putItem() failed to put item (key:" +
                           item.getKey() + ") "+ e.getMessage());
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
        } catch (DatabaseException e) {
            logger.warning("getUser() failed to get user (id:" + id + ")");
        }
        return u;
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
            logger.warning("putAttn() failed for userID:" + pa.getUserID() +
                           " and itemID:" + pa.getItemID());
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
            logger.warning("getNumUsers failed: " + e.toString());
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
            logger.warning("getNumEntries failed: " + e.toString());
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
            logger.warning("getNumAttn failed: " + e.toString());
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
                logger.severe("Failed to close entity store: " + e.toString());
            }
        }
        
        if (dbEnv != null) {
            try {
                dbEnv.close();
            } catch (DatabaseException e) {
                logger.severe("Failed to close database environment: " +
                        e.toString());
            }
        }
        
    }

}