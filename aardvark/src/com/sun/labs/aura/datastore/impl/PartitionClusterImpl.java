package com.sun.labs.aura.datastore.impl;

import com.sun.kt.search.DocumentVector;
import com.sun.kt.search.ResultsFilter;
import com.sun.kt.search.WeightedField;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
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
public class PartitionClusterImpl implements PartitionCluster,
                                             Configurable, AuraService {
    
    @ConfigString
    public static final String PROP_PREFIX = "prefix";
    
    @ConfigComponent(type=com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE_HEAD = "dataStoreHead";
    
    private DataStore dataStoreHead;
    
    protected DSBitSet prefixCode;
    
    protected boolean closed = false;

    //protected List<BerkeleyItemStore> replicants;
    protected Replicant replicant;
    
    protected Logger logger;
    
    /**
     * Construct a PartitionClusterImpl for use with a particular item prefix.
     * 
     * @param prefixCode the initial prefix that this cluster represents
     */
    public PartitionClusterImpl() {

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

    public Set<Attention> getAttentionForSource(String srcKey)
            throws AuraException, RemoteException {
        return replicant.getAttentionForSource(srcKey);
    }
    
    public Set<Attention> getAttentionForSource(String srcKey,
                                                Attention.Type type)
            throws AuraException, RemoteException {
        return replicant.getAttentionForSource(srcKey, type);
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
        replicant.removeItemListener(itemType, listener);
    }

    public long getItemCount(ItemType itemType)
            throws AuraException, RemoteException {
        return replicant.getItemCount(itemType);
    }

    public long getAttentionCount() throws AuraException, RemoteException {
        return replicant.getAttentionCount();
    }

    public List<Scored<Item>> findSimilar(String key, int n, ResultsFilter rf)
            throws AuraException, RemoteException {
        DocumentVector dv = replicant.getDocumentVector(key);
        return replicant.findSimilar(dv, n, rf);
    }

    public List<Scored<Item>> findSimilar(String key, String field, int n, ResultsFilter rf)
            throws AuraException, RemoteException {
        DocumentVector dv = replicant.getDocumentVector(key, field);
        return replicant.findSimilar(dv, n, rf);
    }

    public List<Scored<Item>> findSimilar(String key,
                                       WeightedField[] fields,
                                       int n, ResultsFilter rf)
            throws AuraException, RemoteException {
        DocumentVector dv = replicant.getDocumentVector(key, fields);
        return replicant.findSimilar(dv, n, rf);
    }
    
    public List<Scored<Item>> query(String query, int n, ResultsFilter rf) 
            throws AuraException, RemoteException {
        return replicant.query(query, n, rf);
    }


    public List<Scored<Item>> query(String query, String sort, int n, ResultsFilter rf) 
            throws AuraException, RemoteException {
        return replicant.query(query, sort, n, rf);
    }


    public synchronized void close() throws AuraException, RemoteException {
        if (!closed) {
            //
            // do something
            closed = true;
        }
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        prefixCode = DSBitSet.parse(ps.getString(PROP_PREFIX));
        logger = ps.getLogger();
        dataStoreHead = (DataStore) ps.getComponent(PROP_DATA_STORE_HEAD);
        PartitionCluster exported = (PartitionCluster) ps.getConfigurationManager().getRemote(this, dataStoreHead);
        try {
            dataStoreHead.registerPartitionCluster(exported);
        } catch (RemoteException rx) {
            throw new PropertyException(ps.getInstanceName(), PROP_DATA_STORE_HEAD, 
                    "Unable to add partition cluster to data store");
        }
    }
    
    public void addReplicant(Replicant replicant) throws RemoteException {
        if (replicant.getPrefix().equals(prefixCode)) {
            this.replicant = replicant;
        } else {
            logger.log(Level.SEVERE, "Adding replicant with wrong prefix our prefix: " +
                    prefixCode + " prefix added: " + replicant.getPrefix());
        }
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

    public DocumentVector getDocumentVector(String key) throws RemoteException, AuraException {
        return replicant.getDocumentVector(key);
    }

    public DocumentVector getDocumentVector(String key, String field) throws RemoteException, AuraException {
        return replicant.getDocumentVector(key, field);
    }

    public DocumentVector getDocumentVector(String key, WeightedField[] fields)
            throws RemoteException, AuraException {
        return replicant.getDocumentVector(key, fields);
    }

    public List<Scored<Item>> findSimilar(DocumentVector dv, int n, ResultsFilter rf) throws AuraException, RemoteException {
        return replicant.findSimilar(dv, n, rf);
    }
}
