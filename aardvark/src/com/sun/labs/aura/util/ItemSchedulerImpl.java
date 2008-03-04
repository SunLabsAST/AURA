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
        synchronized (itemQueue) {
            DelayedItem delayedItem = itemQueue.take();
            DelayedItem leasedItem = new DelayedItem(delayedItem.getItemKey(),
                    itemLeaseTime);
            outstandingQueue.add(leasedItem);
            return delayedItem.getItemKey();
        }
    }

    public void releaseItem(String itemKey, int secondsUntilNextScheduledProcessing) {
        if (secondsUntilNextScheduledProcessing <= 0) {
            secondsUntilNextScheduledProcessing = defaultPeriod;
        }
        addItem(itemKey, secondsUntilNextScheduledProcessing * 1000);
    }

    public void itemCreated(ItemEvent e) throws RemoteException {
        for (Item item : e.getItems()) {
            addItem(item.getKey(), 0);
        }
    }

    public void itemChanged(ItemEvent e) throws RemoteException {
    }

    public void itemDeleted(ItemEvent e) throws RemoteException {
        synchronized (itemQueue) {
            for (Item item : e.getItems()) {
                removeFromQueue(item.getKey(), outstandingQueue);
                removeFromQueue(item.getKey(), itemQueue);
            }
        }
    }

    private void processExpiredLeases() {
        logger.info("lease processing started");
        try {
            DelayedItem item = null;
            while ((item = outstandingQueue.take()) != null) {
                logger.warning("reclaimed item after lease expired");
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


        itemLeaseTime = ps.getInt(PROP_ITEM_LEASE_TIME) * 1000;

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
                    long initialDelay = 0L;
                    long delayIncrement = defaultPeriod * 1000 / items.size();

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
    private void addItem(String itemKey, long delay) {
        synchronized (itemQueue) {
            boolean inQueue = false;

            // first remove it from the outstanding queue

            removeFromQueue(itemKey, outstandingQueue);

            // check to make sure the item isn't already in the queue

            for (DelayedItem item : itemQueue) {
                if (itemKey.equals(item.getItemKey())) {
                    inQueue = true;
                    break;
                }
            }

            // it's not in the queue, so add it
            if (!inQueue) {
                itemQueue.add(new DelayedItem(itemKey, delay));
            }
        }
    }

    /**
     * Removed the given item from the given queue
     * @param id the id of the item to remove
     * @param queue the queue from which to remove the item
     */
    private void removeFromQueue(String key, DelayQueue<DelayedItem> queue) {
        DelayedItem queuedItem = null;
        for (DelayedItem item : queue) {
            if (key.equals(item.getItemKey())) {
                queuedItem = item;
                break;
            }
        }

        if (queuedItem != null) {
            queue.remove(queuedItem);
        }
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
    @ConfigInteger(defaultValue = 60, range = {1, 60 * 60})
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

    public DelayedItem(String itemKey, long deltaTimeInMilliseconds) {
        if (deltaTimeInMilliseconds < 0) {
            deltaTimeInMilliseconds = 0;
        }

        this.itemKey = itemKey;
        nextProcessingTime = System.currentTimeMillis() +
                deltaTimeInMilliseconds;
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

    public int compareTo(Delayed o) {
        long result = getDelay(TimeUnit.MILLISECONDS) -
                o.getDelay(TimeUnit.MILLISECONDS);
        return result < 0 ? -1 : result > 0 ? 1 : 0;
    }
}
    
