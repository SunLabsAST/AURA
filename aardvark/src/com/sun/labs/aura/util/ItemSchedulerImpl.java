/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.util;

import com.sun.labs.aura.aardvark.impl.*;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.ItemEvent;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigEnum;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * An implementation of the ItemScheduler interface
 * @author plamere
 */
public class ItemSchedulerImpl implements ItemScheduler, Configurable,
        ItemListener, AuraService {

    private DelayQueue<DelayedItem> itemQueue;
    private DelayQueue<DelayedItem> outstandingQueue;
    private ItemListener exportedItemListener;
    private Thread leaseReclaimer = null;

    public String getNextItemKey() throws InterruptedException {
        DelayedItem delayedItem = itemQueue.take();
        DelayedItem leasedItem = new DelayedItem(delayedItem.getItemKey(), itemLeaseTime);
        outstandingQueue.add(leasedItem);
        return delayedItem.getItemKey();
    }

    public void releaseItem(String itemKey, int secondsUntilNextScheduledProcessing) {
        if (secondsUntilNextScheduledProcessing <= 0) {
            secondsUntilNextScheduledProcessing = defaultPeriod;
        }
        addItem(itemKey, secondsUntilNextScheduledProcessing);
    }

    public void itemCreated(ItemEvent e) throws RemoteException {
        for (Item item : e.getItems()) {
            addItem(item.getKey(), 0);
        }
    }

    public void itemChanged(ItemEvent e) throws RemoteException {
    }

    public void itemDeleted(ItemEvent e) throws RemoteException {
        for (Item item : e.getItems()) {
            deleteItemByKeyFromQueues(item.getKey());
        }
    }


    private void deleteItemByKeyFromQueues(String itemKey) {
        DelayedItem itemToDelete = new DelayedItem(itemKey);
        outstandingQueue.remove(itemToDelete);
        itemQueue.remove(itemToDelete);
    }

    private void processExpiredLeases() {
        logger.info("lease processing started");
        try {
            DelayedItem item = null;
            while ((item = outstandingQueue.take()) != null) {
                logger.warning("reclaimed item after lease expired: " + item.getItemKey());
                addItem(item.getItemKey(), 0);
            }
        } catch (InterruptedException ex) {
        }
        logger.info("lease processing finished");
    }

    synchronized public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();

        DataStore oldStore = dataStore;
        Item.ItemType oldItemType = itemType;

        DataStore newItemStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        Item.ItemType newItemType = (Item.ItemType) ps.getEnum(PROP_ITEM_TYPE);


        // everything is connected OK, so lets create our item queues
        // but don't create it if it already exists, since we may already
        // have some clients pending on it.
        if (itemQueue == null) {
            itemQueue = new DelayQueue<DelayedItem>();
        }

        if (outstandingQueue == null) {
            outstandingQueue = new DelayQueue<DelayedItem>();
        }

        itemQueue.clear();
        outstandingQueue.clear();


        if (leaseReclaimer == null) {
            leaseReclaimer = new Thread() {

                @Override
                public void run() {
                    try {
                        processExpiredLeases();
                    } finally {
                        leaseReclaimer = null;
                    }
                }
            };
            leaseReclaimer.start();
        }


        itemLeaseTime = ps.getInt(PROP_ITEM_LEASE_TIME);
        defaultPeriod = ps.getInt(PROP_DEFAULT_PERIOD);

        // if we have a new item store, or a new item type, disconnect from 
        // the old item store, and connect up to the new store.

        if (newItemStore != oldStore || newItemType != oldItemType) {
            if (oldStore != null && exportedItemListener != null) {
                try {
                    oldStore.removeItemListener(oldItemType,
                            exportedItemListener);
                } catch (AuraException ex) {
                    logger.severe("can't disconnect to old itemstore " +
                            ex.getMessage());
                } catch (RemoteException ex) {
                    logger.severe("can't disconnect to old itemstore " +
                            ex.getMessage());
                }
            }

            try {
                exportedItemListener = (ItemListener) ps.getConfigurationManager().
                        getRemote(this, newItemStore);
                newItemStore.addItemListener(newItemType, exportedItemListener);
            } catch (AuraException ex) {
                throw new PropertyException(ps.getInstanceName(),
                        PROP_DATA_STORE, "aura exception " + ex.getMessage());
            } catch (RemoteException ex) {
                throw new PropertyException(ps.getInstanceName(),
                        PROP_DATA_STORE, "remote exception " + ex.getMessage());
            }


            try {


                // collect all of the items of our item type and add them to the
                // itemQueue.  Stagger the period over the default period

                Set<Item> items = newItemStore.getAll(newItemType);
                if (items.size() > 0) {
                    int initialDelay = 0;
                    int delayIncrement = defaultPeriod / items.size();

                    for (Item item : items) {
                        addItem(item.getKey(), initialDelay);
                        initialDelay += delayIncrement;
                    }
                }

                dataStore = newItemStore;
                itemType = newItemType;

            } catch (AuraException ex) {
                throw new PropertyException(ps.getInstanceName(),
                        PROP_DATA_STORE,
                        "Can't get items from the store " + ex.getMessage());
            } catch (RemoteException ex) {
                throw new PropertyException(ps.getInstanceName(),
                        PROP_DATA_STORE,
                        "Can't get items from the store " + ex.getMessage());
            }
        }
    }

    /**
     * Adds the item to the item queue. Ensure that it is no longer on the 
     * outstanding queue, make sure that no duplicates of the item are on the item queue
     * 
     * @param itemID the item to add
     * @param delay the delay, in milliseconds, until the item should be made
     * available for processing
     */
    private void addItem(String itemKey, int delay) {
        deleteItemByKeyFromQueues(itemKey);
        itemQueue.add(new DelayedItem(itemKey, delay));
    }

    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    private DataStore dataStore;
    /**
     * the configurable property for maximum item lease time in seconds
     */
    @ConfigInteger(defaultValue = 60 * 30, range = {1, 60 * 60 * 24})
    public final static String PROP_ITEM_LEASE_TIME = "itemLeaseTime";
    private int itemLeaseTime;
    /**
     * the configurable property for type of item to be managed
     */
    @ConfigEnum(type = com.sun.labs.aura.datastore.Item.ItemType.class, defaultValue =
    "")
    public final static String PROP_ITEM_TYPE = "itemType";
    private Item.ItemType itemType;
    private Logger logger;
    /**
     * the configurable property for default processing period (in seconds)
     */
    @ConfigInteger(defaultValue = 60 * 60, range = {1, 60 * 60 * 24 * 365})
    public final static String PROP_DEFAULT_PERIOD = "defaultPeriod";
   private int defaultPeriod;

    public void start() {
    }

    public void stop() {
    }
}

/**
 * Represents an item and its delay time, suitable for use with a DelayQueue
 * @author plamere
 */
class DelayedItem implements Delayed {

    private String itemKey;
    private long nextProcessingTime;

    public DelayedItem(String itemKey, int seconds) {
        if (seconds < 0) {
            seconds = 0;
        }

        this.itemKey = itemKey;
        nextProcessingTime = System.currentTimeMillis() + seconds * 1000;
    }

    public DelayedItem(String itemKey) {
        this(itemKey, 0);
    }

    /**
     * Gets the feed represented by this DelayedFeed
     * @return the feed
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public String getItemKey() {
        return itemKey;
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(nextProcessingTime - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DelayedItem other = (DelayedItem) obj;
        if (this.itemKey != other.itemKey && (this.itemKey == null || !this.itemKey.equals(other.itemKey))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.itemKey != null ? this.itemKey.hashCode() : 0);
        return hash;
    }

    

    public int compareTo(Delayed o) {
        long result = getDelay(TimeUnit.MILLISECONDS) -
                o.getDelay(TimeUnit.MILLISECONDS);
        return result < 0 ? -1 : result > 0 ? 1 : 0;
    }
}
    
