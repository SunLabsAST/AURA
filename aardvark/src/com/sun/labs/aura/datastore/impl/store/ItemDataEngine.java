package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.aardvark.impl.recommender.*;
import com.sun.kt.search.DocumentVector;
import com.sun.kt.search.FieldInfo;
import com.sun.kt.search.Log;
import com.sun.kt.search.Result;
import com.sun.kt.search.ResultSet;
import com.sun.kt.search.SearchEngine;
import com.sun.kt.search.SearchEngineException;
import com.sun.kt.search.SearchEngineFactory;
import com.sun.labs.aura.datastore.impl.store.Indexable;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A search engine for the data associated with items in the item store.
 * The engine will index all of the data in the map which is of the types that we 
 * know about (strings, dates, and numbers.)
 * 
 * <p>
 * 
 * All values from the map will be saved and therefore available for parametric
 * searching.  String values will also be indexed, tokenized, and vectored so 
 * that they can be used for simple searching as well as item similarity 
 * computations.
 * 
 * <p>
 * 
 * Data values in the map that implement the <code>Indexable</code> interface will
 * be changed into strings by calling the <code>toString</code> method on the object
 * before it is indexed.
 * 
 */
public class ItemDataEngine implements Configurable {

    private SearchEngine engine;

    private Logger log;

    private int entryBatchSize;

    private int engineLogLevel;

    private int entryCount = 0;

    private boolean shuttingDown;
    
    public void newProperties(PropertySheet ps) throws PropertyException {
        entryBatchSize = ps.getInt(PROP_ENTRY_BATCH_SIZE);
        engineLogLevel = ps.getInt(PROP_ENGINE_LOG_LEVEL);
        log = ps.getLogger();
        Log.setLogger(log);
        Log.setLevel(engineLogLevel);
        String indexDir = ps.getString(PROP_INDEX_DIR);
        String engineConfig = ps.getString(PROP_ENGINE_CONFIG_FILE);
        try {
            URL config = ItemDataEngine.class.getResource(engineConfig);

            //
            // Creates the search engine.  We'll use a full blown fields-and-all
            // engine because we need to be able to handle fielded doc vectors
            // and postings.
            engine = SearchEngineFactory.getSearchEngine(indexDir,
                                                         "search_engine",
                                                         config);
        } catch(SearchEngineException see) {
            log.log(Level.SEVERE, "error opening engine for: " + indexDir, see);
        }
    }

    /**
     * Indexes an item.  Note that the data indexed may not be available immediately
     * for searching, depending on the configuration of the indexer.
     * 
     * @param item the item to index
     * @return <code>true</code> if the item was added to the index, <code>false</code>
     * otherwise.
     */
    public boolean index(Item item) {
        if(shuttingDown) {
            return false;
        }

        try {

            //
            // Get the item's map, and make a map for ourselves of just the 
            // stuff that we want to index.
            Map<String,Serializable> dm = item.getMap();
            Map<String,Object> im = new HashMap<String,Object>();
            
            //
            // Add the data that we want in every map.
            im.put("aura-key", item.getKey());
            im.put("aura-name", item.getName());
            im.put("aura-type", item.getType().toString());
            for(Map.Entry<String,Serializable> e : dm.entrySet()) {
                Serializable val = e.getValue();
                
                //
                // OK, first up, make sure that we have an appropriately defined
                // field for this name.  We'll need to make sure that we're not
                // clobbering field types as we go.
                FieldInfo fi = engine.getFieldInfo(e.getKey());
                FieldInfo.Type type = getType(val);
                
                //
                // Ignore stuff we don't know how to handle.
                if(type == FieldInfo.Type.NONE) {
                    continue;
                }
                
                if(fi == null) {
                    //
                    // We haven't encountered this field name before, so define
                    // the field.
                    fi = new FieldInfo(e.getKey(), getAttributes(type), type);
                    engine.defineField(fi);
                } else {
                    //
                    // Make sure we're not clobbering something here.
                    if(fi.getType() != type) {
                        log.severe(String.format("Attempting to redefine field %s from %s to %s!",
                                e.getKey(), fi.getType(), type));
                        continue;
                    }
                }
                
                //
                // Now get a value to put in the index map.
                Object indexVal = val;
                if(val instanceof Indexable) {
                    indexVal = indexVal.toString();
                }
                im.put(e.getKey(), indexVal);
            }

            engine.index(item.getKey(), im);
            if(++entryCount % entryBatchSize == 0) {
                engine.flush();
                entryCount = 0;
            }
            return true;
        } catch(SearchEngineException ex) {
            log.log(Level.SEVERE, "Exception indexing " + item.getKey(), ex);
        }
        return false;
    }
    
    /**
     * Gets the field type appropriate for an object.
     * @param val the value that we want the appropriate type for
     * @return the appropriate type, or <code>FieldInfo.Type.NONE</code> if there
     * is no appropriate type.
     */
    private FieldInfo.Type getType(Object val) {
        if(val instanceof Indexable) {
            return FieldInfo.Type.STRING;
        }
        
        if(val instanceof String) {
            return FieldInfo.Type.STRING;
        }
        
        if(val instanceof Date) {
            return FieldInfo.Type.DATE;
        }
        
        if(val instanceof Integer || val instanceof Long) {
            return FieldInfo.Type.INTEGER;
        }
        
        if(val instanceof Float || val instanceof Double) {
            return FieldInfo.Type.FLOAT;
        }
        
        return FieldInfo.Type.NONE;
        
    }
    
    /**
     * Gets a set of attributes suitable for a given field type.
     */
    private EnumSet<FieldInfo.Attribute> getAttributes(FieldInfo.Type type) {
        //
        // Everything's saved.
        EnumSet<FieldInfo.Attribute> ret = EnumSet.of(FieldInfo.Attribute.SAVED);
        //
        // Strings get indexed.
        if(type == FieldInfo.Type.STRING) {
            ret.addAll(FieldInfo.getIndexedAttributes());
        }
        return ret;
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
            ResultSet rs = engine.search(String.format("id = %d", id));
            if(rs.size() == 0) {
                return null;
            }
            if(rs.size() > 1) {
                log.warning("Multiple entries for ID: " + id);
            }
            Result r = rs.getResults(0, 1).get(0);
            return r.getDocumentVector();
        } catch(SearchEngineException ex) {
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
        } catch(SearchEngineException ex) {
            log.log(Level.SEVERE, "Error searching for key " + key, ex);
            return null;
        }
    }
    

    public synchronized void shutdown() {
        try {
            //
            // Stop listening for things and shut down the engine.
            shuttingDown = true;
            log.log(Level.INFO, "Shutting down search engine");
            engine.close();
        } catch(SearchEngineException ex) {
            log.log(Level.WARNING, "Error closing index data engine", ex);
        }
    }
    
    /**
     * The resource to load for the engine configuration.  This gives us the
     * opportunity to use different configs as necessary (e.g., for testing).
     * Note that any resource named by this property must be accessible via
     * <code>Class.getResource()</code>!
     */
    @ConfigString(defaultValue = "entryEngineConfig.xml")
    public static final String PROP_ENGINE_CONFIG_FILE = "engineConfigFile";

    /**
     * The default logging level for the search engine.  Paul likes things nice
     * and quiet.
     */
    @ConfigInteger(defaultValue = 2)
    public static final String PROP_ENGINE_LOG_LEVEL = "engineLogLevel";

    /**
     * The configurable item store.  We'll listen to this item store for new
     * entries and generate recommendations for the entries in this store.
     */
    @ConfigComponent(type = DataStore.class)
    public static final String PROP_ITEM_STORE =
            "dataStore";

    /**
     * The configurable index directory.
     */
    @ConfigString(defaultValue = "itemData.idx")
    public static final String PROP_INDEX_DIR =
            "indexDir";

    /**
     * Number of entries to batch up before indexing
     */
    @ConfigInteger(defaultValue = 20, range = {1, 8192})
    public static final String PROP_ENTRY_BATCH_SIZE = "entryBatchSize";

}
