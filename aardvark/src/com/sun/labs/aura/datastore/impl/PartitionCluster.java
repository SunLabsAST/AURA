package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.impl.store.BerkeleyItemStore;
import com.sun.labs.aura.datastore.impl.store.DBIterator;
import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A partition cluster stores one segment of the item store's data.  The
 * segment that it stores is determined by matching the prefix of each item's
 * key with the prefix for the cluster.
 */
public class PartitionCluster implements ItemStore, Configurable, AuraService {
    protected DSBitSet prefixCode;
    
    protected boolean closed = false;

    //protected List<BerkeleyItemStore> replicants;
    protected BerkeleyItemStore replicant;
    
    protected Logger logger = Logger.getLogger("");
    
    /**
     * Construct a PartitionCluster for use with a particular item prefix.
     * 
     * @param prefixCode the initial prefix that this cluster represents
     */
    public PartitionCluster() {

    }
    
    public DSBitSet getPrefix() {
        return prefixCode;
    }

    public Set<Item> getAll(ItemType itemType)
            throws AuraException, RemoteException {
        return replicant.getAll(itemType);
    }

    public Item getItem(String key) throws AuraException, RemoteException {
        return replicant.getItem(key);
    }

    public User getUser(String key) throws AuraException, RemoteException {
        return replicant.getUser(key);
    }

    public Item putItem(Item item) throws AuraException, RemoteException {
        return replicant.putItem(item);
    }

    public User putUser(User user) throws AuraException, RemoteException {
        return replicant.putUser(user);
    }

    public DBIterator<Item> getItemsAddedSince(ItemType type, Date timeStamp)
            throws AuraException, RemoteException {
        return replicant.getItemsAddedSince(type, timeStamp);
    }

    public Set<Item> getItems(User user, Type attnType, ItemType itemType)
            throws AuraException, RemoteException {
        return replicant.getItems(user, attnType, itemType);
    }

    public Attention getAttention(long attnID)
            throws AuraException, RemoteException {
        return replicant.getAttention(attnID);
    }

    public Set<Attention> getAttentionForTarget(String itemKey)
            throws AuraException, RemoteException {
        return replicant.getAttentionForTarget(itemKey);
    }

    public Attention attend(Attention att)
            throws AuraException, RemoteException {
        return replicant.attend(att);
    }

    public DBIterator<Attention> getAttentionAddedSince(Date timeStamp)
            throws AuraException, RemoteException {
        return replicant.getAttentionAddedSince(timeStamp);
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
        return replicant.getLastAttentionForSource(srcKey, type, count);
    }

    public void addItemListener(ItemType itemType, ItemListener listener)
            throws AuraException, RemoteException {
        //
        // Should the listener only go down to the partition cluster level?
        replicant.addItemListener(itemType, listener);
    }

    public void removeItemListener(ItemType itemType, ItemListener listener)
            throws AuraException, RemoteException {
        //
        // Should the listener only go down to the partition cluster level?
        replicant.addItemListener(itemType, listener);
    }

    public long getItemCount(ItemType itemType)
            throws AuraException, RemoteException {
        return replicant.getItemCount(itemType);
    }

    public long getAttentionCount() throws AuraException, RemoteException {
        return replicant.getAttentionCount();
    }

    
    public synchronized void close() throws AuraException, RemoteException {
        if (!closed) {
            //
            // do something
            closed = true;
        }
    }

    public void newProperties(PropertySheet arg0) throws PropertyException {
        prefixCode = null;
        
        //
        // Get replicas?
    }

    public void start() {
    }

    public void stop() {
        try {
            close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to close properly", e);
        }
    }

}
