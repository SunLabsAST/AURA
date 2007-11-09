package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.kt.search.DocumentVector;
import com.sun.kt.search.Log;
import com.sun.kt.search.Result;
import com.sun.kt.search.ResultSet;
import com.sun.kt.search.SearchEngine;
import com.sun.kt.search.SearchEngineException;
import com.sun.kt.search.SearchEngineFactory;
import com.sun.kt.search.SimpleIndexer;
import com.sun.labs.aura.aardvark.recommender.Recommender;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.SimpleAttention;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.item.ItemEvent;
import com.sun.labs.aura.aardvark.store.item.ItemListener;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A search engine instance for the indexed data for feed entries.  Note that this
 * index will only store the entry ids, keys, and content.  A more complicated 
 * version could handle all of the fields set in the item by 
 * {@link com.sun.labs.aura.aardvark.store.item.Item#setField}
 * 
 */
public class EntryContentEngine implements Configurable, Recommender, ItemListener {

    private SearchEngine engine;
    private ItemStore itemStore;
    private Logger log;
    private SimpleIndexer simpleIndexer;
    private int entryBatchSize;
    private int entryCount = 0;

    public void newProperties(PropertySheet ps) throws PropertyException {
        entryBatchSize = ps.getInt(PROP_ENTRY_BATCH_SIZE);
        log = ps.getLogger();
        Log.setLogger(log);
        Log.setLevel(3);
        String indexDir = ps.getString(PROP_INDEX_DIR);
        String engineConfig = ps.getString(PROP_ENGINE_CONFIG_FILE);
        try {
            URL config = EntryContentEngine.class.getResource(engineConfig);

            //
            // Creates the search engine, using a simple doc and freq unfielded
            // postings type, since we want things to be fast.
            engine = SearchEngineFactory.getSearchEngine(indexDir,
                    "simple_search_engine", config);
            itemStore = (ItemStore) ps.getComponent(PROP_ITEM_STORE);
            simpleIndexer = engine.getSimpleIndexer();

            //
            // Catch up to what's in the item store already, if we need to.  This
            // is going to be waaaaaaaay too slow once we get lots of stuff.  I'm 
            // just sayin'.
            //
            // Note that this won't account for entries whose text have changed while
            // the engine was not active, just the entries that have never been indexed.
            if (ps.getBoolean(PROP_INDEX_AT_STARTUP)) {
                for (Item i : itemStore.getAll(Entry.class)) {
                    if (!engine.isIndexed(i.getKey())) {
                        index((Entry) i);
                    }
                }
            }

            //
        // Listen for new things.
            itemStore.addItemListener(Entry.class, this);
        } catch (SearchEngineException see) {
            log.log(Level.SEVERE, "error opening engine for: " + indexDir, see);
        }
    }

    /**
     * Indexes an entry.  The data will be indexed and written to disk before 
     * control is returned to the caller.
     * 
     * @param e the entry to index.
     */
    public void index(Entry e) {
        index(simpleIndexer, e);
        if (++entryCount % entryBatchSize == 0) {
            simpleIndexer.finish();
            simpleIndexer = engine.getSimpleIndexer();
        }
    }

    /**
     * Indexes an entry with the given indexer.  This can be used for bulk
     * indexing at engine startup time.
     * 
     * @param si a simple indexer to use to index the entry
     * @param e the entry to index
     */
    private void index(SimpleIndexer si, Entry e) {
        si.startDocument(e.getKey());
        si.addField("id", e.getID());
        si.addField(null, e.getContent());
        si.endDocument();
    }

    /**
     * Gets the document vector associated with the entry with a given ID.
     * @param id the ID of the entry that we want the document vector for
     * @return the document vector associated with the given ID.  If no entry in
     * the index has that ID or if an error occurs during the search for the ID,
     * then <code>null<code> is returned.
     */
    public DocumentVector getDocument(long id) {
        try {
            ResultSet rs = engine.search(String.format("id = %d", id), "-score", null);
            if (rs.size() == 0) {
                return null;
            }
            if (rs.size() > 1) {
                log.warning("Multiple entries for ID: " + id);
            }
            Result r = rs.getResults(0, 1).get(0);
            return r.getDocumentVector();
        } catch (SearchEngineException ex) {
            log.log(Level.SEVERE, "Error searching for ID " + id, ex);
            return null;
        }
    }

    /**
     * Gets the document associated with a given key.
     * @param key the key of the entry that we want the document vector for
     * @return the document vector associated with the key.  If the entry with this
     * key has not been indexed or if an error occurs while fetching the docuemnt,
     * then <code>null</code> will be returned.
     */
    public DocumentVector getDocument(String key) {
        try {
            return engine.getDocumentVector(key);
        } catch (SearchEngineException ex) {
            log.log(Level.SEVERE, "Error searching for key " + key, ex);
            return null;
        }
    }

    public List<Entry> getRecommendations(User user) {
        int numResults = 15;
        try {
            List<Attention> a =  getUserStarredAttentionData(user);

            if (a.size() == 0) {
                return new ArrayList<Entry>();
            }

            Random rand = new Random();
            Attention att = a.get(rand.nextInt(a.size()));
            DocumentVector dv = getDocument(att.getItemID());
            if (dv == null) {
                return new ArrayList<Entry>();
            }

            Set<Long> userItems = getUserItems(user);
            List<Entry> ret = new ArrayList<Entry>();
            ResultSet rs = dv.findSimilar();

            for (int i = 0; ret.size() < numResults && i < rs.size(); i += numResults) {
                for (Result r : rs.getResults(i, numResults)) {
                    Entry entry = (Entry) itemStore.get((Long) r.getSingleFieldValue("id"));
                    if (entry != null) {
                        if (!userItems.contains(entry.getID())) {
                            userItems.add(entry.getID());   // to avoid dups
                            attend(user, entry, Attention.Type.VIEWED);
                            ret.add(entry);
                            if (ret.size() >= numResults) {
                                break;
                            }
                        }
                    }
                }
            }
            return ret;
        } catch (SearchEngineException ex) {
            log.log(Level.SEVERE, "Error finding most similar documents for " +
                    user.getKey(), ex);
            return new ArrayList<Entry>();
        } catch (AuraException ex) {
            log.log(Level.SEVERE, "Exception while attending to items", ex);
            return new ArrayList<Entry>();
        }
    }
    
    /**
     * Add new attention data to the item store
     * @param user the user
     * @param item the item
     * @param type the type of attention
     */
    private void attend(User user, Item item, Attention.Type type) throws AuraException {
        Attention attention = new SimpleAttention(user.getID(), item.getID(), Attention.Type.VIEWED);
        itemStore.attend(attention);
    }

    /** 
     * Gets a set of the IDs for the items that the given user has
     * paid attention to.
     * @param user the use rof interest
     * @return the set of items
     */
    private Set<Long> getUserItems(User user) {
        Set<Long> itemSet = new HashSet<Long>();
        List<Attention> attentionData = user.getAttentionData();
        for (Attention attention : attentionData) {
            itemSet.add(attention.getItemID());
        }
        return itemSet;
    }

    /**
     * Returns a list of the starred items for a suer
     * @param user the user of interest
     * @return the list of starred items for a user
     */
    private List<Attention> getUserStarredAttentionData(User user) {
        List<Attention> starredAttentionData = new ArrayList<Attention>();
        
        for (Attention a: user.getAttentionData()) {
            if (a.getType() == Attention.Type.STARRED) {
                starredAttentionData.add(a);
            }
        }
        return starredAttentionData;
    }

    public void itemCreated(ItemEvent e) {

        //
        // Index the new items.
        for (Item i : e.getItems()) {
            index((Entry) i);
        }
    }

    public void itemChanged(ItemEvent e) {
        try {
            //
            // If the aura was changed, then re-index.
            if (e.getChangeType() == ItemEvent.ChangeType.AURA) {
                for (Item i : e.getItems()) {
                    index((Entry) i);
                }
            }
        } catch (AuraException ex) {
            log.log(Level.SEVERE, "Error handling changed items", ex);
        }
    }

    public void itemDeleted(ItemEvent e) {
        try {
            //
            // Get the list of keys and delete them all at once.
            List<String> keys = new ArrayList<String>();
            for (Item i : e.getItems()) {
                keys.add(i.getKey());
            }
            engine.delete(keys);
        } catch (SearchEngineException ex) {
            log.log(Level.SEVERE, "Error removing deleted items", ex);
        }
    }

    public void shutdown() {
        try {
            log.log(Level.INFO, "Shutting down search engine");
            simpleIndexer.finish();
            engine.close();
        } catch (SearchEngineException ex) {
            log.log(Level.WARNING, "Error closing entry content engine", ex);
        }
    }
    
    /**
     * The resource to load for the engine configuration.  This gives us the
     * opportunity to use different configs as necessary (e.g., for testing).
     * Note that any resource named by this property must be accessible via
     * <code>Class.getResource()</code>!
     */
    @ConfigString(defaultValue="entryEngineConfig.xml")
    public static final String PROP_ENGINE_CONFIG_FILE = "engineConfigFile";

    /**
     * The configurable item store.  We'll listen to this item store for new
     * entries and generate recommendations for the entries in this store.
     */
    @ConfigComponent(type = com.sun.labs.aura.aardvark.store.ItemStore.class)
    public static final String PROP_ITEM_STORE =
            "itemStore";
    /**
     * The configurable index directory.
     */
    @ConfigString(defaultValue = "entryContent.idx")
    public static final String PROP_INDEX_DIR =
            "indexDir";

    /**
     * Whether to index all entries in the item store at startup.
     */
    @ConfigBoolean(defaultValue = true)
    public static final String PROP_INDEX_AT_STARTUP = "indexAtStartup";

    /**
     * Number of entries to batch up before indexing
     */
    @ConfigInteger(defaultValue = 20, range = {1, 8192})
    public static final String PROP_ENTRY_BATCH_SIZE = "entryBatchSize";
}
