package com.sun.labs.aura.aardvark.impl.bdb.store;

import com.sleepycat.je.DatabaseException;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.EntryImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.FeedImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.ItemImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.UserImpl;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.DBIterator;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.ItemStoreStats;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.item.ItemEvent;
import com.sun.labs.aura.aardvark.store.item.ItemListener;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This implementation of the ItemStore is backed by the Berkeley DB Java
 * Edition database.  It uses the Direct Persistence Layer to map its data
 * types into the database.
 */
public class BerkeleyItemStore implements ItemStore {

    /**
     * The location of the BDB/JE Database Environment
     */
    @ConfigString(defaultValue="/tmp/aura")
    public final static String PROP_DB_ENV="dbEnv";
    protected String dbEnvDir;
    
    @ConfigBoolean(defaultValue=false)
    public final static String PROP_OVERWRITE="overwrite";
    protected boolean overwriteExisting;
    
    /**
     * The wrapper around all the BDB/JE implementation
     */
    protected BerkeleyDataWrapper bdb;
    
    /**
     * A map to store listeners for each type of item
     */
    protected Map<String,Set<ItemListener>> listenerMap;
    
    /**
     * Indicates if the item store has been closed.  Once the store is
     * closed, no more operators are permitted.
     */
    protected boolean closed = false;
    
    /**
     * A logger for messages/debug info
     */
    protected Logger logger;
    
    /**
     * The system-wide item store instance
     */
    protected static BerkeleyItemStore store;
    
    /**
     * Constructs an empty item store, ready to be configured.
     */
    public BerkeleyItemStore() {
        listenerMap = new HashMap<String,Set<ItemListener>>();
        listenerMap.put(User.ITEM_TYPE, new HashSet<ItemListener>());
        listenerMap.put(Feed.ITEM_TYPE, new HashSet<ItemListener>());
        listenerMap.put(Entry.ITEM_TYPE, new HashSet<ItemListener>());
    }
    
    /**
     * Gets the system-wide instance of the item store.  Right now this
     * just returns a static reference, but in the future this might
     * provide a means of looking up the service and generating an RMI proxy.
     * 
     * @return the item store
     */
    public static BerkeleyItemStore getItemStore() throws AuraException {
        if (store != null ) {
            return store;
        } else {
            throw new AuraException("Store has not yet been initialized");
        }
    }
    
    /**
     * Sets the properties for the item store, opening the database
     * environment and the entity store.  This must be called immediately
     * after the object is instantiated.
     * 
     * @param ps the set of properties
     * @throws com.sun.labs.util.props.PropertyException
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        
        //
        // Get the database environment
        dbEnvDir = ps.getString(PROP_DB_ENV);
        
        //
        // See if we should overwrite any existing database at that path
        overwriteExisting = ps.getBoolean(PROP_OVERWRITE);
        
        //
        // Configure and open the environment and entity store
        try {
            bdb = new BerkeleyDataWrapper(dbEnvDir, logger, overwriteExisting);
        } catch (DatabaseException e) {
            logger.severe("Failed to load the database environment at " +
                          dbEnvDir + ": " + e);
        }
        store = this;
    }

    /**
     * Close up the entity store and the database environment.
     */
    public void close() throws AuraException {
        closed = true;
        bdb.close();
    }
    
    /**
     * A factory method for creating new items.
     * 
     * @param itemType the type of item to create
     * @param key the key that will be assigned to that item
     * @return a new item instance
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public <T extends Item> T newItem(Class<T> itemType, String key)
            throws AuraException {
        if (closed) {
            throw new AuraException("ItemStore is closed");
        }
        T ret = null;
        if (itemType.equals(User.class)) {
            ret = (T)new UserImpl(key);
        } else if (itemType.equals(Feed.class)) {
            ret = (T)new FeedImpl(key);
        } else if (itemType.equals(Entry.class)) {
            ret = (T)new EntryImpl(key);
        } else {
            throw new AuraException("Unsupported item type");
        }
        return ret;
    }

    /*public long getID(String itemKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/

    /**
     * Get all the instances of a particular type of item from the store
     * 
     * @param itemType the type of item to fetch
     * @return all of those items
     */
    public <T extends Item> Set<T> getAll(Class<T> itemType)
            throws AuraException {
        if (closed) {
            throw new AuraException("ItemStore is closed");
        }

        if (itemType.equals(User.class)) {
            return (Set<T>)bdb.getAllUsers();
        } else if (itemType.equals(Feed.class)) {
            return (Set<T>)bdb.getAllFeeds();
        } else if (itemType.equals(Entry.class)) {
            return (Set<T>)bdb.getAllEntries();
        }
        return new HashSet<T>();
    }

    /**
     * Get an Item by its ID.
     * 
     * @param id the id of the Item
     * @return the Item or null if the item ID is unknown
     */
    public Item get(long id) {
        return bdb.getItem(id);
    }

    /**
     * Get an item by its key
     * 
     * @param key the key of the Item
     * @return the Item or null if the item key is unknown
     */
    public Item get(String key) {
        return bdb.getItem(key);
    }

    /**
     * Puts an item into the item store.  If the item has the same ID as
     * an existing item, then the existing item is overwritten.
     * 
     * @param item the item to put into the store
     * @throws AuraException if an invalid item is provided
     */
    public void put(Item item) throws AuraException {
        boolean existed = false;
        if (item instanceof ItemImpl) {
            ItemImpl itemImpl = (ItemImpl)item;
            ItemImpl prev = bdb.putItem(itemImpl);
            if (prev != null) {
                existed = true;
            }
        
            //
            // Notify listeners that an item was added or updated
            Set<ItemListener> l = listenerMap.get(itemImpl.getTypeString());
            for (ItemListener il : l) {
                if (existed) {
                    il.itemChanged(new ItemEvent(new Item[] {item},
                                                 ItemEvent.ChangeType.AURA));
                } else {
                    il.itemCreated(new ItemEvent(new Item[] {item}));
                }
            }
        } else {
            throw new AuraException ("Unsupported Item type");
        }

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
            Date timeStamp) throws AuraException {
        return bdb.getItemsAddedSince(itemType, timeStamp.getTime());
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
    public DBIterator<Attention> getAttentionAddedSince(Date timeStamp)
            throws AuraException {
        return bdb.getAttentionAddedSince(timeStamp.getTime());
    }
    
    /**
     * Store an attention record
     * 
     * @param att the attention to store
     * @throws AuraException if the attention is invalid
     */
    public void attend(Attention att) throws AuraException {
        //
        // Attention must be paid to an existing user and item.  First,
        // get the user and the item to make sure they exist.
        UserImpl user = bdb.getUser(att.getUserID());
        ItemImpl item = bdb.getItem(att.getItemID());
        
        if ((user == null) || (item == null)) {
            throw new AuraException("User or Item is invalid");
        }
        
        //
        // Create the attention and add references to it from the item and
        // the user. (bdb wrapper does this)
        PersistentAttention pa = new PersistentAttention(att);
        bdb.putAttention(pa, user, item);
        
        //
        // Finally, notify the listeners that the user and the item changed
        Set<ItemListener> l = listenerMap.get(item.getTypeString());
        for (ItemListener il : l) {
            il.itemChanged(new ItemEvent(new Item[] {item},
                                         ItemEvent.ChangeType.ATTENTION));
        }

        l = listenerMap.get(user.getTypeString());
        for (ItemListener il : l) {
            il.itemChanged(new ItemEvent(new Item[] {user},
                                         ItemEvent.ChangeType.ATTENTION));
        }

    }

    /**
     * Add an item listener for a particular type of item, or all items
     * 
     * @param type the type of item to listen for, or null for all types
     * @param listener a listener to notify of events
     */
    public <T extends Item> void addItemListener(Class<T> type,
                                                 ItemListener listener) {
        if (type == null) {
            for (String k : listenerMap.keySet()) {
                Set<ItemListener> l = listenerMap.get(k);
                l.add(listener);
            }
        } else {
            String key = "";
            if (type.equals(User.class)) {
                key = User.ITEM_TYPE;
            } else if (type.equals(Entry.class)) {
                key = Entry.ITEM_TYPE;
            } else if (type.equals(Feed.class)) {
                key = Feed.ITEM_TYPE;
            }
            Set<ItemListener> l = listenerMap.get(key);
            l.add(listener);
        }
    }

    /**
     * Remove an item listener for a particular type, or for all types
     * 
     * @param type the specific type for this listener, or null for all types
     * @param listener the listener
     */
    public <T extends Item> void removeItemListener(Class<T> type,
                                                    ItemListener listener) {
        if (type == null) {
            for (String k : listenerMap.keySet()) {
                Set l = listenerMap.get(k);
                l.remove(listener);
            }
        } else {
            String key = "";
            if (type.equals(User.class)) {
                key = User.ITEM_TYPE;
            } else if (type.equals(Entry.class)) {
                key = Entry.ITEM_TYPE;
            } else if (type.equals(Feed.class)) {
                key = Feed.ITEM_TYPE;
            }
            Set l = listenerMap.get(key);
            l.remove(listener);
        }
    }

    /**
     * Get statistics about this item store
     * 
     * @return item stats
     */
    public ItemStoreStats getStats() {
        long numUsers = bdb.getNumUsers();
        long numEntries = bdb.getNumEntries();
        long numAttn = bdb.getNumAttention();
        long numFeeds = bdb.getNumFeeds();
        return new ItemStoreStats(numUsers, numEntries, numAttn, numFeeds);
    }

}
