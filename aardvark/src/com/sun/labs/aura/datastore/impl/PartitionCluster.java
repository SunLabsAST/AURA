package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.impl.store.DBIterator;
import com.sun.labs.aura.datastore.impl.store.ItemStore;
import java.rmi.RemoteException;
import java.util.BitSet;
import java.util.Date;
import java.util.Set;

/**
 * A partition cluster stores one segment of the item store's data.  The
 * segment that it stores is determined by matching the prefix of each item's
 * key with the prefix for the cluster.
 */
public class PartitionCluster implements ItemStore {
    protected BitSet prefixCode;

    /**
     * Construct a PartitionCluster for use with a particular item prefix.
     * 
     * @param prefixCode the initial prefix that this cluster represents
     */
    public PartitionCluster(BitSet prefixCode) {
        this.prefixCode = prefixCode;
    }
    
    public BitSet getPrefix() {
        return prefixCode;
    }

    public Set<Item> getAll(ItemType itemType) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Item getItem(String key) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public User getUser(String key) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Item putItem(Item item) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public User putUser(User user) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DBIterator<Item> getItemsAddedSince(ItemType type, Date timeStamp) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<Item> getItems(User user, Type attnType, ItemType itemType) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Attention getAttention(long attnID) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<Attention> getAttentionForTarget(Item item) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Attention attend(Attention att) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DBIterator<Attention> getAttentionAddedSince(Date timeStamp) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addItemListener(ItemType itemType, ItemListener listener) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeItemListener(ItemType itemType, ItemListener listener) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getItemCount(ItemType itemType) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
