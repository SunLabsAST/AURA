/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.util;

import com.sun.labs.aura.aardvark.impl.*;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DBIterator;
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
import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the ItemScheduler interface
 * @author plamere
 */
public class ItemSchedulerImpl implements ItemScheduler, Configurable,
        ItemListener, AuraService {

    private DelayQueue<DelayedItem> itemQueue;
    private ItemListener exportedItemListener;
    private AtomicInteger waiters = new AtomicInteger();

    public String getNextItemKey() throws InterruptedException {
        long start = System.currentTimeMillis();
        waiters.incrementAndGet();

        if (logger.isLoggable(Level.FINE)) {
            DelayedItem next = itemQueue.peek();
            if (next != null) {
                logger.fine("in waiters: " + waiters.get() + ", waiting " + next.getDelay(TimeUnit.SECONDS) + " secs, items: " + size());
            } else {
                logger.fine("in waiters: " + waiters.get() + ", waiting, items: " +  size());
            }
        }

        DelayedItem delayedItem = itemQueue.take();


        long lateSeconds = -delayedItem.getDelay(TimeUnit.SECONDS);
        if (lateSeconds > 0) {

            // if the delay time is negative, then, we are late at getting to this
            // item to process it.  If we are later than lateTime, issue a warning
            // so we will know to add more resources to the scheduling of these items

            if (lateSeconds > lateTime) {
                logger.warning("schedule of " + delayedItem.getItemKey() + " was " + lateSeconds + " seconds late.");
            }
            logger.fine("getting " + delayedItem.getItemKey() + " was late by " +
                    -delayedItem.getDelay(TimeUnit.SECONDS) + " secs");
        }

        waiters.decrementAndGet();

        long wait = System.currentTimeMillis() - start;
        logger.fine("out waiters: " + waiters.get() + ", waited " + wait + " msecs, items: " + size()
                + " who: " + Thread.currentThread().getName());

        logger.info(" Scheduled " + delayedItem.getItemKey());
        return delayedItem.getItemKey();
    }

    public void releaseItem(String itemKey, int secondsUntilNextScheduledProcessing) {
        if (secondsUntilNextScheduledProcessing <= 0) {
            secondsUntilNextScheduledProcessing = defaultPeriod;
        }
        addItem(itemKey, secondsUntilNextScheduledProcessing);
        logger.fine("released item " + itemKey + " next time is " + secondsUntilNextScheduledProcessing + " secs, cur size " + size());
    }

    public void itemCreated(ItemEvent e) throws RemoteException {

        int pullTime = 0;
        int headTime = 0;

        // add new items to the head of the queue, so peek at the current head
        //  and add the new items with a lower delay than the head of the queue
        DelayedItem head = itemQueue.peek();
        if (head != null) {
            headTime  = (int) head.getDelay(TimeUnit.SECONDS);
            if (headTime <= 0) {
                pullTime = headTime;
            }
         }
        
        for (Item item : e.getItems()) {
            addItem(item.getKey(), --pullTime);
        }
        // we want new items to get to cut to the head of the queue, so
        // we use newItemTime to schedule newly created items earlier then 
        // anyone else.
        logger.fine("Added " + e.getItems().length + " items " + " total size is " + size());
    }

    public void itemChanged(ItemEvent e) throws RemoteException {
    }

    public void itemDeleted(ItemEvent e) throws RemoteException {
        for (Item item : e.getItems()) {
            deleteItemByKeyFromQueues(item.getKey());
        }
        logger.info("removed " + e.getItems().length + " items " + " total size is " + size());
    }

    private void deleteItemByKeyFromQueues(String itemKey) {
        DelayedItem itemToDelete = new DelayedItem(itemKey);
        itemQueue.remove(itemToDelete);
    }

    synchronized public void newProperties(final PropertySheet ps) throws PropertyException {
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


        itemQueue.clear();

        defaultPeriod = ps.getInt(PROP_DEFAULT_PERIOD);
        lateTime = ps.getInt(PROP_LATE_TIME);

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
                throw new PropertyException(ex, ps.getInstanceName(),
                        PROP_DATA_STORE, "aura exception " + ex.getMessage());
            } catch (RemoteException ex) {
                throw new PropertyException(ps.getInstanceName(),
                        PROP_DATA_STORE, "remote exception " + ex.getMessage());
            }


            dataStore = newItemStore;
            itemType = newItemType;

            Thread t = new Thread() {

                public void run() {
                    collectItems(ps.getInstanceName(), dataStore, itemType);
                }
            };
            t.start();
        }
    }

    private void collectItems(String name, DataStore ds, Item.ItemType type) {
        float initialDelay = 0;
        float delayIncrement = .2f;
        try {
            // collect all of the items of our item type and add them to the
            // itemQueue.  Stagger the period over the default period
            DBIterator<Item> iter = ds.getItemsAddedSince(type, new Date(0));
            try {
                while (iter.hasNext()) {
                    Item item = iter.next();
                    addItem(item.getKey(), (int) initialDelay);
                    initialDelay += delayIncrement;
                }
            } finally {
                iter.close();
            }
        } catch (AuraException ex) {
            logger.severe("Can't get items from the store " + ex.getMessage());
        } catch (RemoteException ex) {
            logger.severe("Can't get items from the store " + ex.getMessage());
        }
        
    }

    /**
     * Adds the item to the item queue. Ensure that it is no longer on the 
     * outstanding queue, make sure that no duplicates of the item are on the item queue
     * 
     * @param itemID the item to add
     * @param delay the delay, in seconds, until the item should be made
     * available for processing
     */
    private void addItem(String itemKey, int delay) {
        itemQueue.add(new DelayedItem(itemKey, delay));
        logger.fine("Added item " + itemKey + " delay " + delay);
    }
    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    private DataStore dataStore;
    /**
     * the confieurable property for type of item to be managed
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
    /**
     * the configurable property for late processing notification tim (in seconds)
     */
    @ConfigInteger(defaultValue = 60, range = {1, 60 * 60 * 24 * 365})
    public final static String PROP_LATE_TIME = "lateTime";
    private int lateTime;

    public void start() {
    }

    public void stop() {
    }

    public int size() {
        return itemQueue.size();
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
