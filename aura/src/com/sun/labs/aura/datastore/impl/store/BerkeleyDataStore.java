package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.cluster.Cluster;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.FieldCapability;
import com.sun.labs.aura.datastore.Item.FieldType;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.impl.DataStoreHead;
import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Counted;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.query.Element;
import com.sun.labs.minion.retrieval.MultiDocumentVectorImpl;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of DataStore that just wraps the BerkeleyDB.
 */
public class BerkeleyDataStore implements DataStore, Configurable, AuraService {

    @ConfigComponent(type =
    com.sun.labs.aura.datastore.impl.store.BerkeleyItemStore.class)
    public static final String PROP_ITEM_STORE = "itemStore";

    private BerkeleyItemStore itemStore;

    private static Logger logger = Logger.getLogger(BerkeleyDataStore.class.getName());

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        try {
            itemStore.close();
        } catch(Exception e) {
            logger.log(Level.WARNING, "Failed to close item store cleanly", e);
        }
    }

    @Override
    public boolean ready() throws RemoteException {
        return true;
    }

    @Override
    public void registerPartitionCluster(PartitionCluster pc) throws
            RemoteException {
    }

    @Override
    public Replicant getReplicant(
            String prefix) throws RemoteException {
        return itemStore;
    }

    @Override
    public void registerPartitionSplit(PartitionCluster zeroChild,
                                       PartitionCluster oneChild) throws
            RemoteException {
    }

    @Override
    public List<String> getPrefixes() throws RemoteException {
        return new ArrayList<String>();
    }

    @Override
    public void defineField(String fieldName) throws AuraException,
            RemoteException {
        itemStore.defineField(fieldName);
    }

    @Override
    public void defineField(String fieldName, FieldType fieldType,
                            EnumSet<FieldCapability> caps) throws AuraException,
            RemoteException {
        itemStore.defineField(fieldName, fieldType, caps);
    }

    @Override
    public List<Item> getAll(ItemType itemType) throws AuraException,
            RemoteException {
        return itemStore.getAll(itemType);
    }

    @Override
    public DBIterator<Item> getAllIterator(ItemType itemType) throws
            AuraException,
            RemoteException {
        return itemStore.getAllIterator(itemType);
    }

    @Override
    public Item getItem(
            String key) throws AuraException, RemoteException {
        return itemStore.getItem(key);
    }

    @Override
    public Collection<Item> getItems(Collection<String> keys) throws
            AuraException,
            RemoteException {
        return itemStore.getItems(keys);
    }

    @Override
    public User getUser(
            String key) throws AuraException, RemoteException {
        return itemStore.getUser(key);
    }

    @Override
    public User getUserForRandomString(
            String randStr) throws AuraException,
            RemoteException {
        return itemStore.getUserForRandomString(randStr);
    }

    @Override
    public void deleteUser(String key) throws AuraException, RemoteException {
        itemStore.deleteUser(key);
    }

    @Override
    public void deleteItem(String key) throws AuraException, RemoteException {
        itemStore.deleteItem(key);
    }

    @Override
    public Item putItem(
            Item item) throws AuraException, RemoteException {
        return itemStore.putItem(item);
    }

    @Override
    public User putUser(
            User user) throws AuraException, RemoteException {
        return itemStore.putUser(user);
    }

    @Override
    public DBIterator<Item> getItemsAddedSince(ItemType type, Date timeStamp)
            throws AuraException, RemoteException {
        return itemStore.getItemsAddedSince(type, timeStamp);
    }

    @Override
    public List<Item> getItems(User user, Type attnType, ItemType itemType)
            throws AuraException, RemoteException {
        return itemStore.getItems(user, attnType, itemType);
    }

    @Override
    public List<Attention> getAttention(AttentionConfig ac) throws AuraException,
            RemoteException {
        return itemStore.getAttention(ac);
    }

    @Override
    public DBIterator<Attention> getAttentionIterator(AttentionConfig ac) throws
            AuraException,
            RemoteException {
        return itemStore.getAttentionIterator(ac);
    }

    @Override
    public Long getAttentionCount(
            AttentionConfig ac) throws AuraException,
            RemoteException {
        return itemStore.getAttentionCount(ac);
    }

    @Override
    public Object processAttention(
            AttentionConfig ac, String script,
            String language) throws AuraException,
            RemoteException {
        return itemStore.processAttention(ac, script, language);
    }

    @Override
    public List<Attention> getAttentionSince(AttentionConfig ac, Date timeStamp)
            throws AuraException, RemoteException {
        return itemStore.getAttentionSince(ac, timeStamp);
    }

    @Override
    public DBIterator<Attention> getAttentionSinceIterator(AttentionConfig ac,
                                                           Date timeStamp)
            throws AuraException, RemoteException {
        return itemStore.getAttentionSinceIterator(ac, timeStamp);
    }

    @Override
    public Long getAttentionSinceCount(
            AttentionConfig ac, Date timeStamp)
            throws AuraException, RemoteException {
        return itemStore.getAttentionSinceCount(ac, timeStamp);
    }

    @Override
    public List<Attention> getLastAttention(AttentionConfig ac, int count)
            throws AuraException, RemoteException {
        return itemStore.getLastAttention(ac, count);
    }

    @Override
    public Attention attend(
            Attention att) throws AuraException, RemoteException {
        return itemStore.attend(att);
    }

    @Override
    public List<Attention> attend(List<Attention> attns) throws AuraException,
            RemoteException {
        return itemStore.attend(attns);
    }

    @Override
    public void removeAttention(String srcKey, String targetKey, Type type)
            throws AuraException, RemoteException {
        itemStore.removeAttention(srcKey, targetKey, type);
    }

    @Override
    public void addItemListener(ItemType itemType, ItemListener listener) throws
            AuraException,
            RemoteException {
        itemStore.addItemListener(itemType, listener);
    }

    @Override
    public void removeItemListener(ItemType itemType, ItemListener listener)
            throws AuraException, RemoteException {
        itemStore.removeItemListener(itemType, listener);
    }

    @Override
    public long getItemCount(ItemType itemType) throws AuraException,
            RemoteException {
        return itemStore.getItemCount(itemType);
    }

    @Override
    public List<String> getSupportedScriptLanguages() throws AuraException,
            RemoteException {
        return itemStore.getSupportedScriptLanguages();
    }

    @Override
    public void close() throws AuraException, RemoteException {
        itemStore.close();
    }

    @Override
    public List<Cluster> cluster(List<String> keys, String field, int k) throws
            AuraException,
            RemoteException {
        return new ArrayList<Cluster>();
    }

    @Override
    public List<FieldFrequency> getTopValues(String field, int n,
                                             boolean ignoreCase) throws
            AuraException,
            RemoteException {
        return itemStore.getTopValues(field, n, ignoreCase);
    }

    @Override
    public List<Scored<Item>> findSimilar(String key, SimilarityConfig config)
            throws AuraException, RemoteException {
        try {
            DocumentVector dv = itemStore.getDocumentVector(key, config).get();
            return itemStore.getScoredItems(itemStore.findSimilar(dv, config));
        } catch(Exception ex) {
            throw new AuraException("Error getting document vector", ex);
        }

    }

    @Override
    public List<Scored<Item>> findSimilar(List<String> keys,
                                          SimilarityConfig config) throws
            AuraException,
            RemoteException {
        if(keys.size() == 1) {
            return findSimilar(keys.get(0), config);
        }

        List<DocumentVector> dvs = new ArrayList<DocumentVector>();
        try {
            for(String key : keys) {
                dvs.add(itemStore.getDocumentVector(key, config).get());
            }

            MultiDocumentVectorImpl mdvi = new MultiDocumentVectorImpl(dvs);
            return itemStore.getScoredItems(itemStore.findSimilar(mdvi, config));
        } catch(Exception ex) {
            throw new AuraException("Error marshalling document vectors", ex);
        }

    }

    @Override
    public List<Scored<Item>> findSimilar(WordCloud cloud,
                                          SimilarityConfig config) throws
            AuraException,
            RemoteException {
        //
        // Make sure we include and exclude terms here, since we may not be able to do it at
        // the other end of the wire.
        cloud.updateConfig(config);

        try {
            return itemStore.getScoredItems(itemStore.findSimilar(itemStore.
                    getDocumentVector(
                    cloud, config).get(), config));
        } catch(Exception ex) {
            throw new AuraException("Error finding similar", ex);
        }

    }

    @Override
    public List<Scored<Item>> query(String query, int n, ResultsFilter rf)
            throws AuraException, RemoteException {
        return itemStore.getScoredItems(itemStore.query(query, n, rf));
    }

    @Override
    public List<Scored<Item>> query(String query, String sort, int n,
                                    ResultsFilter rf) throws AuraException,
            RemoteException {
        return itemStore.getScoredItems(itemStore.query(query, sort, n, rf));
    }

    @Override
    public List<Scored<Item>> query(Element query, int n, ResultsFilter rf)
            throws AuraException, RemoteException {
        return itemStore.getScoredItems(itemStore.query(query, n, rf));
    }

    @Override
    public List<Scored<Item>> query(Element query, String sort, int n,
                                    ResultsFilter rf) throws AuraException,
            RemoteException {
        return itemStore.getScoredItems(itemStore.query(query, sort, n, rf));
    }

    @Override
    public WordCloud getTopTerms(
            String key, String field, int n) throws
            AuraException,
            RemoteException {
        return itemStore.getTopTerms(key, field, n);
    }

    @Override
    public List<Counted<String>> getTopTermCounts(String key, String field,
                                                  int n) throws AuraException,
            RemoteException {
        return itemStore.getTopTermCounts(key, field, n);
    }

    @Override
    public List<Counted<String>> getTermCounts(String term, String field, int n,
                                               ResultsFilter rf) throws
            AuraException,
            RemoteException {
        return itemStore.getTermCounts(term, field, n, rf);
    }

    @Override
    public List<Scored<String>> getExplanation(String key, String autoTag, int n)
            throws AuraException, RemoteException {
        return itemStore.getExplanation(key, autoTag, n);
    }

    @Override
    public List<Scored<String>> explainSimilarity(String key1, String key2,
                                                  SimilarityConfig config)
            throws AuraException, RemoteException {
        try {
            DocumentVector dv1 = itemStore.getDocumentVector(key1, config).get();
            DocumentVector dv2 = itemStore.getDocumentVector(key2, config).get();
            return DataStoreHead.explainSimilarity(dv1, dv2, config.getN());
        } catch(Exception ex) {
            throw new AuraException("Error explaining", ex);
        }

    }

    @Override
    public List<Scored<String>> explainSimilarity(WordCloud cloud, String key,
                                                  SimilarityConfig config)
            throws AuraException, RemoteException {
        try {
            DocumentVector dv1 =
                    itemStore.getDocumentVector(cloud, config).get();
            DocumentVector dv2 = itemStore.getDocumentVector(key, config).get();
            return DataStoreHead.explainSimilarity(dv1, dv2, config.getN());
        } catch(Exception ex) {
            throw new AuraException("Error explaining", ex);
        }

    }

    @Override
    public List<Scored<String>> explainSimilarity(WordCloud cloud1,
                                                  WordCloud cloud2,
                                                  SimilarityConfig config)
            throws AuraException, RemoteException {
        try {
            DocumentVector dv1 =
                    itemStore.getDocumentVector(cloud1, config).get();
            DocumentVector dv2 =
                    itemStore.getDocumentVector(cloud2, config).get();
            return DataStoreHead.explainSimilarity(dv1, dv2, config.getN());
        } catch(Exception ex) {
            throw new AuraException("Error explaining", ex);
        }

    }

    @Override
    public List<Scored<Item>> getAutotagged(String autotag, int n) throws
            AuraException,
            RemoteException {
        return itemStore.getScoredItems(itemStore.getAutotagged(autotag, n));
    }

    @Override
    public List<Scored<String>> getTopAutotagTerms(String autotag, int n) throws
            AuraException,
            RemoteException {
        return itemStore.getTopAutotagTerms(autotag, n);
    }

    @Override
    public List<Scored<String>> findSimilarAutotags(String autotag, int n)
            throws AuraException, RemoteException {
        return itemStore.findSimilarAutotags(autotag, n);
    }

    @Override
    public List<Scored<String>> explainSimilarAutotags(String a1, String a2,
                                                       int n) throws
            AuraException,
            RemoteException {
        return itemStore.explainSimilarAutotags(a1, a2, n);
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        itemStore = (BerkeleyItemStore) ps.getComponent(PROP_ITEM_STORE);
    }
}
