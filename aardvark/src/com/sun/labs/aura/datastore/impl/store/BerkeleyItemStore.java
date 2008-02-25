
package com.sun.labs.aura.datastore.impl.store;

import com.sleepycat.je.DatabaseException;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemEvent;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.impl.DSBitSet;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the item store using the berkeley database as a back
 * end.
 */
public class BerkeleyItemStore implements Replicant, Configurable, AuraService {
    /**
     * The location of the BDB/JE Database Environment
     */
    @ConfigString(defaultValue="/tmp/aura")
    public final static String PROP_DB_ENV="dbEnv";
    protected String dbEnvDir;
    
    @ConfigString
    public final static String PROP_PREFIX="prefix";
    protected DSBitSet prefixCode;
    
    @ConfigBoolean(defaultValue=false)
    public final static String PROP_OVERWRITE="overwrite";
    protected boolean overwriteExisting;
    
    /**
     * The search engine that will store item info
     */
    @ConfigComponent(type = ItemSearchEngine.class)
    public static final String PROP_SEARCH_ENGINE =
            "itemSearchEngine";
    protected ItemSearchEngine searchEngine;

    
    /**
     * ComponentRegistry will be non-null if we're running in a RMI environment
     */
    protected ConfigurationManager cm = null;
    
    /**
     * The wrapper around all the BDB/JE implementation
     */
    protected BerkeleyDataWrapper bdb;
    
    /**
     * A map to store listeners for each type of item
     */
    protected Map<ItemType,Set<ItemListener>> listenerMap;
    
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
        listenerMap = new HashMap<ItemType,Set<ItemListener>>();
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
        File f = new File(dbEnvDir);
        f.mkdirs();
        
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
        
        //
        // Get the search engine from the config system
        searchEngine = (ItemSearchEngine)ps.getComponent(PROP_SEARCH_ENGINE);
        
        //
        // Get the configuration manager, which we'll use to export things, if
        // necessary.
        cm = ps.getConfigurationManager();

        prefixCode = DSBitSet.parse(ps.getString(PROP_PREFIX));
        store = this;
    }

    public DSBitSet getPrefix() {
        return prefixCode;
    }
    
    /**
     * Close up the entity store and the database environment.
     */
    public void close() throws AuraException {
        closed = true;
        bdb.close();
    }

    /**
     * Get all the instances of a particular type of item from the store
     * 
     * @param itemType the type of item to fetch
     * @return all of those items
     */
    public Set<Item> getAll(ItemType itemType) throws AuraException {
        return bdb.getAll(itemType);
    }

    public Item getItem(String key) throws AuraException {
        return bdb.getItem(key);
    }

    public User getUser(String key) throws AuraException {
        return (User)bdb.getItem(key);
    }

    public Item putItem(Item item) throws AuraException {
        boolean existed = false;
        if (item instanceof ItemImpl) {
            ItemImpl itemImpl = (ItemImpl)item;
            //
            // If this was a remote object, its transient map will be null
            // and storeMap will be a no-op.  If it was a local object then
            // storeMap will serialize the map (if there is one).
            itemImpl.storeMap();
            ItemImpl prev = bdb.putItem(itemImpl);
            if (prev != null) {
                existed = true;
            }
        
            //
            // The item was modified and/or created, so tell the indexer
            // about it
            searchEngine.index(item);
            
            //
            // Finally, send out relevant events.
            if (existed) {
                itemChanged(itemImpl, ItemEvent.ChangeType.AURA);
            } else {
                itemCreated(itemImpl);
            }
            return itemImpl;
        } else {
            throw new AuraException ("Unsupported Item type");
        }

    }
    
    public User putUser(User user) throws AuraException {
        return (User)putItem(user);
    }
    
    public Set<Item> getItems(User user, Type attnType,
                                    ItemType itemType)
            throws AuraException {
        return bdb.getItems(user.getKey(), attnType, itemType);
    }

    public DBIterator<Item> getItemsAddedSince(ItemType type,
                                               Date timeStamp)
            throws AuraException {
        DBIterator<Item> res =
                bdb.getItemsAddedSince(type, timeStamp.getTime());
        
        return (DBIterator<Item>) cm.getRemote(res, this);
    }
    
    public Set<Attention> getAttentionForTarget(String itemKey)
            throws AuraException {
        return bdb.getAttentionForTarget(itemKey);
    }
    
    
    public Attention attend(Attention att) throws AuraException {
        //
        // Create the attention and add references to it from the item and
        // the user. (bdb wrapper does this)
        PersistentAttention pa = new PersistentAttention(att);
        bdb.putAttention(pa);
        return pa;
    }

    public DBIterator<Attention> getAttentionAddedSince(Date timeStamp)
            throws AuraException {
        DBIterator<Attention> res = 
                bdb.getAttentionAddedSince(timeStamp.getTime());
        
        return (DBIterator<Attention>) cm.getRemote(res, this);
    }

    public SortedSet<Attention> getLastAttentionForSource(String srcKey,
                                                          int count)
            throws AuraException, RemoteException {
        return getLastAttentionForSource(srcKey, null, count);
    }

    public SortedSet<Attention> getLastAttentionForSource(String srcKey,
                                                          Type type,
                                                          int count)
            throws AuraException, RemoteException {
        return bdb.getLastAttentionForUser(srcKey, type, count);
    }

    public void addItemListener(ItemType itemType, ItemListener listener)
            throws AuraException {
        //
        // Find the set of listeners for this type and add it, adding a set to
        // track these listeners if there isn't one.
        synchronized(listenerMap) {
            Set<ItemListener> l = listenerMap.get(itemType);
            if (l == null) {
                l = new HashSet<ItemListener>();
                listenerMap.put(itemType, l);
            }
            l.add(listener);
        }
    }

    public void removeItemListener(ItemType itemType, ItemListener listener) throws AuraException {
        //
        // If we were givn a null item type, remove from all types
        synchronized(listenerMap) {
            if (itemType == null) {
                for (ItemType t : listenerMap.keySet()) {
                    Set<ItemListener> l = listenerMap.get(t);
                    l.remove(listener);
                }
            } else {
                Set<ItemListener> l = listenerMap.get(itemType);
                l.remove(listener);
            }
        }
    }

    public long getItemCount(ItemType type) {
        return bdb.getItemCount(type);
    }

    public long getAttentionCount() {
        return bdb.getAttentionCount();
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
    
    /**
     * Send any queued up change events
     */
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
        for (ItemType itemType : listenerMap.keySet()) {
            //
            // Collect the items of type aura change
            ArrayList<Item> auraItems = new ArrayList<Item>();
            
            //
            // For each item event with an item of this type, sort out the
            // items by change type in order to accumulate them all
            for (Iterator<ItemEvent> itemIt = events.iterator();
                 itemIt.hasNext();) {
                ItemEvent ie = itemIt.next();
                ItemImpl i = (ItemImpl) ie.getItems()[0];
                if (i.getType() == itemType) {
                    //
                    // The listener I'm going to send to is interested in
                    // items of this type.  Filter the items by change-type.
                    try {
                        switch (ie.getChangeType()) {
                            case AURA:
                                auraItems.add(i);
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
            // And finally, send events to all the listeners for items of this
            // type that were of change-type aura
            ItemEvent big = new ItemEvent(
                    auraItems.toArray(new Item[0]),
                                          ItemEvent.ChangeType.AURA);
            Set<ItemListener> l = listenerMap.get(itemType);
            //
            // Also send to all listeners for the "null" (all) item list
            Set<ItemListener> nulls = listenerMap.get(null);
            if (nulls != null) {
                l.addAll(nulls);
            }
            for (ItemListener il : l) {
                try {
                    il.itemChanged(big);
                } catch (RemoteException e)  {
                    logger.log(Level.WARNING,
                            "Error sending change event to " + il, e);
                }
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
    
    /**
     * Send any queued up create events
     */
    private void sendCreatedEvents() {
        //
        // Get all the item events that we'll use.
        ArrayList<ItemImpl> newItems = new ArrayList<ItemImpl>();
        for (Iterator<ItemImpl> itemIt = createEventItems.iterator();
             itemIt.hasNext();) {
            ItemImpl ie = itemIt.next();
            newItems.add(ie);
            itemIt.remove();
        }
        
        //
        // For each type of item for which there is a listener, batch up
        // the items of that type and send them off together.
        for (ItemType itemType : listenerMap.keySet()) {
            ArrayList<Item> itemsToSend = new ArrayList<Item>();
            for (Iterator<ItemImpl> itemIt = newItems.iterator();
                 itemIt.hasNext();) {
                ItemImpl i = itemIt.next();
                if (i.getType() == itemType) {
                    itemsToSend.add(i);
                    itemIt.remove();
                }
            }
            
            // Send the events
            ItemEvent big = new ItemEvent(
                    itemsToSend.toArray(new Item[0]));
            Set<ItemListener> l = listenerMap.get(itemType);
            //
            // Also send to the "null" (all) item listeners
            Set<ItemListener> nulls = listenerMap.get(itemType);
            if (nulls != null) {
                l.addAll(nulls);
            }
            //
            // Finally, sned the event out
            for (ItemListener il : l) {
                try {
                    il.itemCreated(big);
                } catch (RemoteException e)  {
                    logger.log(Level.WARNING,
                            "Error sending create event to " + il, e);
                }
            }
        }
    }

    public void start() {
    }

    public void stop() {
        try {
            cm.shutdown();
            close();
        } catch (AuraException ae) {
            logger.log(Level.WARNING, "Error closing item store", ae);
        }
    }
    /**
     * Never call this.
     */
    public void closeAndDestroy() throws AuraException {
        close();
        File f = new File(dbEnvDir);
        File[] content = f.listFiles();
        for (File c : content) {
            c.delete();
        }
        f.delete();
    }
}