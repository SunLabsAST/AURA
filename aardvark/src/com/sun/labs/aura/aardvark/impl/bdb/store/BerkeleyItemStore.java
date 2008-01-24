package com.sun.labs.aura.aardvark.impl.bdb.store;

import com.sleepycat.je.DatabaseException;
import com.sun.labs.aura.aardvark.AardvarkService;
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
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This implementation of the ItemStore is backed by the Berkeley DB Java
 * Edition database.  It uses the Direct Persistence Layer to map its data
 * types into the database.
 */
public class BerkeleyItemStore implements ItemStore, Configurable, AardvarkService {

    /**
     * The location of the BDB/JE Database Environment
     */
    @ConfigString(defaultValue = "/tmp/aura")
    public final static String PROP_DB_ENV = "dbEnv";

    protected String dbEnvDir;
    @ConfigBoolean(defaultValue = false)
    public final static String PROP_OVERWRITE = "overwrite";

    protected boolean overwriteExisting;
    /**
     * The wrapper around all the BDB/JE implementation
     */
    protected BerkeleyDataWrapper bdb;
    /**
     * A map to store listeners for each type of item
     */
    protected Map<String, Set<ItemListener>> listenerMap;

    /**
     * A queue of change events that need to be sent
     */
    private ConcurrentLinkedQueue<ItemEvent> changeEvents;
    
    /**
     * A queue of create events that need to be sent
     */
    private ConcurrentLinkedQueue<ItemImpl> createEventItems;
    
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
        listenerMap = new HashMap<String, Set<ItemListener>>();
        listenerMap.put(User.ITEM_TYPE, new HashSet<ItemListener>());
        listenerMap.put(Feed.ITEM_TYPE, new HashSet<ItemListener>());
        listenerMap.put(Entry.ITEM_TYPE, new HashSet<ItemListener>());
        changeEvents = new ConcurrentLinkedQueue<ItemEvent>();
        createEventItems = new ConcurrentLinkedQueue<ItemImpl>();
    }

    /**
     * Gets the system-wide instance of the item store.  Right now this
     * just returns a static reference, but in the future this might
     * provide a means of looking up the service and generating an RMI proxy.
     * 
     * @return the item store
     */
    public static BerkeleyItemStore getItemStore() throws AuraException {
        if (store != null) {
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
        } catch(DatabaseException e) {
            throw new PropertyException(e, ps.getInstanceName(), PROP_DB_ENV, "Failed to load the database environment at " +
                          dbEnvDir);
        }
        store = this;
    }

    /**
     * Close up the entity store and the database environment.
     */
    public void close() throws AuraException {
        //
        // If we're already closed, then we're done.
        if (closed) {
            return;
        }
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
            throws AuraException, RemoteException {
        if(closed) {
            throw new AuraException("ItemStore is closed");
        }
        T ret = null;
        if(itemType.equals(User.class)) {
            ret = (T) new UserImpl(key);
        } else if(itemType.equals(Feed.class)) {
            ret = (T) new FeedImpl(key);
        } else if(itemType.equals(Entry.class)) {
            ret = (T) new EntryImpl(key);
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
            throws AuraException, RemoteException {
        if(closed) {
            throw new AuraException("ItemStore is closed");
        }

        if(itemType.equals(User.class)) {
            return (Set<T>) bdb.getAllUsers();
        } else if(itemType.equals(Feed.class)) {
            return (Set<T>) bdb.getAllFeeds();
        } else if(itemType.equals(Entry.class)) {
            return (Set<T>) bdb.getAllEntries();
        }
        return new HashSet<T>();
    }

    /**
     * Get an Item by its ID.
     * 
     * @param id the id of the Item
     * @return the Item or null if the item ID is unknown
     */
    public Item get(long id) throws RemoteException {
        return bdb.getItem(id);
    }

    /**
     * Get an item by its key
     * 
     * @param key the key of the Item
     * @return the Item or null if the item key is unknown
     */
    public Item get(String key) throws RemoteException {
        return bdb.getItem(key);
    }
    
    public List<Attention> getAttentionData(Item item) throws AuraException, RemoteException {
         if(item instanceof ItemImpl) {
             Set<Long> ids = ((ItemImpl) item).getAttentionIDs();
             List<Attention> ret = new ArrayList<Attention>();
             for(Long aid : ids) {
                 ret.add(bdb.getAttention(aid));
             }
             return ret;
         } else {
             throw new AuraException("Unknown item type: " + item.getClass());
         }
       
    }

    public SortedSet<Attention> getLastAttention(User user, int count) throws RemoteException {
        return getLastAttention(user, null, count);
    }
    
    public SortedSet<Attention> getLastAttention(User user, Attention.Type type, int count) throws RemoteException {
        return bdb.getLastAttentionForUser(user.getID(), type, count);
        
    }
    
    public SortedSet<Entry> getEntries(Feed feed) throws RemoteException {
        return bdb.getAllEntriesForFeed(feed.getID());
    }
    
    public Set<Feed> getFeeds(User user, Attention.Type type) throws AuraException, RemoteException {
        if(user instanceof UserImpl) {
            Set<Feed> feeds = new HashSet<Feed>();

            Set<PersistentAttention> attns = bdb.getAttentionForUser(user.getID(),
                                                                     type);
            for(PersistentAttention attn : attns) {
                FeedImpl feed = (FeedImpl) bdb.getItem(attn.getItemID());
                feeds.add(feed);
            }
            return feeds;
        } else {
            throw new AuraException("Unknown user type: " + user.getClass());
        }
    }

    /**
     * Puts an item into the item store.  If the item has the same ID as
     * an existing item, then the existing item is overwritten.
     * 
     * @param item the item to put into the store
     * @throws AuraException if an invalid item is provided
     */
    public Item put(Item item) throws AuraException, RemoteException {
        boolean existed = false;
        if(item instanceof ItemImpl) {
            ItemImpl itemImpl = (ItemImpl) item;
            ItemImpl prev = bdb.putItem(itemImpl);
            if(prev != null) {
                existed = true;
            }
            //
            // Notify listeners that an item was added or updated
            Set<ItemListener> l = listenerMap.get(itemImpl.getTypeString());
            for(ItemListener il : l) {
                if(existed) {
                    try {
                        il.itemChanged(new ItemEvent(new Item[]{item},
                                                     ItemEvent.ChangeType.AURA));
                    } catch(RemoteException rx) {
                        logger.log(Level.WARNING,
                                   "Error sending change event to " + il, rx);
                    }
                } else {
                    try {
                        il.itemCreated(new ItemEvent(new Item[]{item}));
                    } catch(RemoteException rx) {
                        logger.log(Level.WARNING,
                                   "Error sending change event to " + il, rx);
                    }
                }
            }
            return item;
        } else {
            throw new AuraException("Unsupported Item type");
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
                                                              Date timeStamp)
            throws AuraException, RemoteException {
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
            throws AuraException, RemoteException {
        return bdb.getAttentionAddedSince(timeStamp.getTime());
    }

    /**
     * Store an attention record
     * 
     * @param att the attention to store
     * @throws AuraException if the attention is invalid
     */
    public void attend(Attention att) throws AuraException, RemoteException {
        //
        // Attention must be paid to an existing user and item.  First,
        // get the user and the item to make sure they exist.
        UserImpl user = bdb.getUser(att.getUserID());
        ItemImpl item = bdb.getItem(att.getItemID());
        if(user == null) {
            throw new AuraException("User " + att.getUserID() + " is invalid");
        }
        
        if(item == null) {
            throw new AuraException("Item " + att.getItemID() + " is invalid");
        }
        //
        // Create the attention and add references to it from the item and
        // the user. (bdb wrapper does this)
        PersistentAttention pa = new PersistentAttention(att);
        bdb.putAttention(pa, user, item);

        //
        // Finally, notify the listeners that the user and the item changed
        itemChanged(item, ItemEvent.ChangeType.ATTENTION);
        itemChanged(user, ItemEvent.ChangeType.ATTENTION);
    }

    /**
     * Add an item listener for a particular type of item, or all items
     * 
     * @param type the type of item to listen for, or null for all types
     * @param listener a listener to notify of events
     */
    public <T extends Item> void addItemListener(Class<T> type,
                                                  ItemListener listener)
            throws RemoteException {
        if(type == null) {
            for(String k : listenerMap.keySet()) {
                Set<ItemListener> l = listenerMap.get(k);
                l.add(listener);
            }
        } else {
            String key = "";
            if(type.equals(User.class)) {
                key = User.ITEM_TYPE;
            } else if(type.equals(Entry.class)) {
                key = Entry.ITEM_TYPE;
            } else if(type.equals(Feed.class)) {
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
                                                     ItemListener listener)
            throws RemoteException {
        if(type == null) {
            for(String k : listenerMap.keySet()) {
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

    /**
     * Internal method to handle sending/queueing item changed events.
     */
    private void itemChanged(ItemImpl item, ItemEvent.ChangeType ctype) {
        //
        // Queue the event for later delivery
        changeEvents.add(new ItemEvent(new Item[] {item},
                                       ctype));
        sendChangedEvents();
    }
    
    private void sendChangedEvents() {
        //
        // Get all the item events that we'll use.
        ArrayList events = new ArrayList();
        for (Iterator<ItemEvent> itemIt = changeEvents.iterator();
             itemIt.hasNext();) {
            ItemEvent ie = itemIt.next();
            events.add(ie);
            itemIt.remove();
        }
        
        //
        // For each type for which there is at least one listener:
        for (String itemType : listenerMap.keySet()) {
            //
            // Collect the items of type attention and of type aura
            ArrayList<Item> attnItems = new ArrayList<Item>();
            ArrayList<Item> auraItems = new ArrayList<Item>();
            
            //
            // For each item event with an item of this type, sort out the
            // items by change type in order to accumulate them all
            for (Iterator<ItemEvent> itemIt = events.iterator();
                 itemIt.hasNext();) {
                ItemEvent ie = itemIt.next();
                ItemImpl i = (ItemImpl) ie.getItems()[0];
                if (i.getTypeString().equals(itemType)) {
                    //
                    // The listener I'm going to send to is interested in
                    // items of this type.  Filter the items by change-type.
                    try {
                        switch (ie.getChangeType()) {
                            case AURA:
                                auraItems.add(i);
                                break;
                            case ATTENTION:
                                attnItems.add(i);
                                break;
                        }
                    } catch (AuraException e) {
                        //
                        // If no type was specified, send the update as an
                        // aura change.
                        auraItems.add(i);
                        logger.log(Level.INFO, "Sending change event that " +
                                   "had no change type");
                    }
                    
                    //
                    // We're handling this item, so remove it from the list
                    // of outstanding items
                    itemIt.remove();
                }
            }
            
            //
            // Now send events to all the listeners for items of this type
            // that were of change-type attention
            ItemEvent big = new ItemEvent(attnItems.toArray(new Item[0]),
                                          ItemEvent.ChangeType.ATTENTION);
            Set<ItemListener> l = listenerMap.get(itemType);
            for (ItemListener il : l) {
                il.itemChanged(big);
            }
            
            //
            // And finally, send events to all the listeners for items of this
            // type that were of change-type aura
            big = new ItemEvent(auraItems.toArray(new Item[0]),
                                          ItemEvent.ChangeType.AURA);
            for (ItemListener il : l) {
                il.itemChanged(big);
            }
        }
    }

    /**
     * Internal method to handle sending/queueing item created events.
     */
    private void itemCreated(ItemImpl item) {
        //
        // Queue up this item to be sent out
        createEventItems.add(item);
        sendCreatedEvents();
    }
    
    private void sendCreatedEvents() {
        //
        // Get all the item events that we'll use.
        ArrayList<ItemImpl> events = new ArrayList<ItemImpl>();
        for (Iterator<ItemImpl> itemIt = createEventItems.iterator();
             itemIt.hasNext();) {
            ItemImpl ie = itemIt.next();
            events.add(ie);
            itemIt.remove();
        }
        
        //
        // For each type of item for which there is a listener, batch up
        // the items of that type and send them off together.
        for (String itemType : listenerMap.keySet()) {
            ArrayList<Item> itemsToSend = new ArrayList<Item>();
            for (Iterator<ItemImpl> itemIt = events.iterator();
                 itemIt.hasNext();) {
                ItemImpl i = itemIt.next();
                if (i.getTypeString().equals(itemType)) {
                    itemsToSend.add(i);
                    itemIt.remove();
                }
            }
            
            // Send the events
            ItemEvent big = new ItemEvent(itemsToSend.toArray(new Item[0]));
            Set<ItemListener> l = listenerMap.get(itemType);
            for (ItemListener il : l) {
                il.itemCreated(big);
            }
        }
    }

    public void start() {
    }

    public void stop() {
        try {
            close();
        } catch (AuraException ae) {
            logger.log(Level.WARNING, "Error closing item store", ae);
        }
    }
}
