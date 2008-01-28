
package com.sun.labs.aura.aardvark.impl.store.bdb;

import com.sleepycat.je.DatabaseException;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.Attention.Type;
import com.sun.labs.aura.aardvark.store.DBIterator;
import com.sun.labs.aura.aardvark.store.ItemStoreStats;
import com.sun.labs.aura.aardvark.store.SimpleItemStore;
import com.sun.labs.aura.aardvark.store.item.SimpleItemEvent;
import com.sun.labs.aura.aardvark.store.item.SimpleItemListener;
import com.sun.labs.aura.aardvark.store.item.SimpleItem;
import com.sun.labs.aura.aardvark.store.item.SimpleItem.ItemType;
import com.sun.labs.aura.aardvark.store.item.SimpleUser;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the item store using the berkeley database as a back
 * end.
 */
public class BerkeleyItemStore implements SimpleItemStore, Configurable {
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
    protected Map<ItemType,Set<SimpleItemListener>> listenerMap;
    
    /**
     * A queue of change events that need to be sent
     */
    private ConcurrentLinkedQueue<SimpleItemEvent> changeEvents;
    
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
        listenerMap = new HashMap<ItemType,Set<SimpleItemListener>>();
        changeEvents = new ConcurrentLinkedQueue<SimpleItemEvent>();
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
     * Get all the instances of a particular type of item from the store
     * 
     * @param itemType the type of item to fetch
     * @return all of those items
     */
    public Set<SimpleItem> getAll(ItemType itemType) throws AuraException {
        return bdb.getAll(itemType);
    }

    public SimpleItem getItem(long id) throws AuraException {
        return bdb.getItem(id);
    }

    public SimpleItem getItem(String key) throws AuraException {
        return bdb.getItem(key);
    }

    public SimpleUser getUser(long id) throws AuraException {
        return (SimpleUser)bdb.getItem(id);
    }

    public SimpleUser getUser(String key) throws AuraException {
        return (SimpleUser)bdb.getItem(key);
    }

    public SimpleItem putItem(SimpleItem item) throws AuraException {
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
        
            if (existed) {
                itemChanged(itemImpl, SimpleItemEvent.ChangeType.AURA);
            } else {
                itemCreated(itemImpl);
            }
            return itemImpl;
        } else {
            throw new AuraException ("Unsupported Item type");
        }

    }
    
    public SimpleUser putUser(SimpleUser user) throws AuraException {
        return (SimpleUser)putItem(user);
    }
    
    public Set<SimpleItem> getItems(SimpleUser user, Type attnType, ItemType itemType) {
        return bdb.getItems(user.getID(), attnType, itemType);
    }

    public DBIterator<SimpleItem> getItemsAddedSince(ItemType type, Date timeStamp) throws AuraException {
        return bdb.getItemsAddedSince(type, timeStamp.getTime());
    }
    
    public Attention getAttention(long attnID) throws AuraException {
        return bdb.getAttention(attnID);
    }
    
    public Set<Attention> getAttention(SimpleItem item) throws AuraException {
        HashSet<Attention> result = new HashSet<Attention>();
        ItemImpl i = (ItemImpl) item;
        for (Long attnID : i.getAttentionIDs()) {
            result.add(bdb.getAttention(attnID));
        }
        return result;
    }
    
    
    public Attention attend(Attention att) throws AuraException {
        //
        // Attention must be paid to an existing user and item.  First,
        // get the user and the item to make sure they exist.
        UserImpl user = (UserImpl)bdb.getItem(att.getUserID());
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
        itemChanged(item, SimpleItemEvent.ChangeType.ATTENTION);
        itemChanged(user, SimpleItemEvent.ChangeType.ATTENTION);
        return pa;
    }

    public DBIterator<Attention> getAttentionAddedSince(Date timeStamp) throws AuraException {
        return bdb.getAttentionAddedSince(timeStamp.getTime());
    }

    public void addItemListener(ItemType itemType, SimpleItemListener listener) throws AuraException {
        //
        // Find the set of listeners for this type and add it, adding a set to
        // track these listeners if there isn't one.
        synchronized(listenerMap) {
            Set<SimpleItemListener> l = listenerMap.get(itemType);
            if (l == null) {
                l = new HashSet<SimpleItemListener>();
                listenerMap.put(itemType, l);
            }
            l.add(listener);
        }
    }

    public void removeItemListener(ItemType itemType, SimpleItemListener listener) throws AuraException {
        //
        // If we were givn a null item type, remove from all types
        synchronized(listenerMap) {
            if (itemType == null) {
                for (ItemType t : listenerMap.keySet()) {
                    Set<SimpleItemListener> l = listenerMap.get(t);
                    l.remove(listener);
                }
            } else {
                Set<SimpleItemListener> l = listenerMap.get(itemType);
                l.remove(listener);
            }
        }
    }

    public long getItemCount(ItemType type) {
        return 0;
    }
    
    public ItemStoreStats getStats() throws AuraException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Internal method to handle sending/queueing item changed events.
     */
    private void itemChanged(ItemImpl item, SimpleItemEvent.ChangeType ctype) {
        //
        // Queue the event for later delivery
        changeEvents.add(new SimpleItemEvent(new SimpleItem[] {item},
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
        for (Iterator<SimpleItemEvent> itemIt = changeEvents.iterator();
             itemIt.hasNext();) {
            SimpleItemEvent ie = itemIt.next();
            events.add(ie);
            itemIt.remove();
        }
        
        //
        // For each type for which there is at least one listener:
        for (ItemType itemType : listenerMap.keySet()) {
            //
            // Collect the items of type attention and of type aura
            ArrayList<SimpleItem> attnItems = new ArrayList<SimpleItem>();
            ArrayList<SimpleItem> auraItems = new ArrayList<SimpleItem>();
            
            //
            // For each item event with an item of this type, sort out the
            // items by change type in order to accumulate them all
            for (Iterator<SimpleItemEvent> itemIt = events.iterator();
                 itemIt.hasNext();) {
                SimpleItemEvent ie = itemIt.next();
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
            SimpleItemEvent big = new SimpleItemEvent(
                    attnItems.toArray(new SimpleItem[0]),
                    SimpleItemEvent.ChangeType.ATTENTION);
            Set<SimpleItemListener> l = listenerMap.get(itemType);
            //
            // Also send to all listeners for the "null" (all) item list
            Set<SimpleItemListener> nulls = listenerMap.get(null);
            if (nulls != null) {
                l.addAll(nulls);
            }
            for (SimpleItemListener il : l) {
                try {
                    il.itemChanged(big);
                } catch (RemoteException e)  {
                    logger.log(Level.WARNING,
                            "Error sending change event to " + il, e);
                }
            }
            
            //
            // And finally, send events to all the listeners for items of this
            // type that were of change-type aura
            big = new SimpleItemEvent(auraItems.toArray(new SimpleItem[0]),
                                          SimpleItemEvent.ChangeType.AURA);
            for (SimpleItemListener il : l) {
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
            ArrayList<SimpleItem> itemsToSend = new ArrayList<SimpleItem>();
            for (Iterator<ItemImpl> itemIt = newItems.iterator();
                 itemIt.hasNext();) {
                ItemImpl i = itemIt.next();
                if (i.getType() == itemType) {
                    itemsToSend.add(i);
                    itemIt.remove();
                }
            }
            
            // Send the events
            SimpleItemEvent big = new SimpleItemEvent(
                    itemsToSend.toArray(new SimpleItem[0]));
            Set<SimpleItemListener> l = listenerMap.get(itemType);
            //
            // Also send to the "null" (all) item listeners
            Set<SimpleItemListener> nulls = listenerMap.get(itemType);
            if (nulls != null) {
                l.addAll(nulls);
            }
            //
            // Finally, sned the event out
            for (SimpleItemListener il : l) {
                try {
                    il.itemCreated(big);
                } catch (RemoteException e)  {
                    logger.log(Level.WARNING,
                            "Error sending create event to " + il, e);
                }
            }
        }
    }



}
