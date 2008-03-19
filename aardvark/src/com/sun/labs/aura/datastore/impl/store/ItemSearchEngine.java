package com.sun.labs.aura.datastore.impl.store;

import com.sun.kt.search.DocumentVector;
import com.sun.kt.search.FieldInfo;
import com.sun.kt.search.Log;
import com.sun.kt.search.Posting;
import com.sun.kt.search.Result;
import com.sun.kt.search.ResultSet;
import com.sun.kt.search.SearchEngine;
import com.sun.kt.search.SearchEngineException;
import com.sun.kt.search.SearchEngineFactory;
import com.sun.kt.search.WeightedField;
import com.sun.labs.aura.datastore.Indexable;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngnova.retrieval.FieldEvaluator;
import ngnova.retrieval.FieldTerm;
import ngnova.retrieval.ResultImpl;

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
public class ItemSearchEngine implements Configurable {

    private SearchEngine engine;

    private Logger log;

    private int engineLogLevel;

    private boolean shuttingDown;

    private long flushCheckInterval;

    private Timer flushTimer;

    public void newProperties(PropertySheet ps) throws PropertyException {

        //
        // Load up the search engine.
        engineLogLevel = ps.getInt(PROP_ENGINE_LOG_LEVEL);
        log = ps.getLogger();
        Log.setLogger(log);
        Log.setLevel(engineLogLevel);
        String indexDir = ps.getString(PROP_INDEX_DIR);
        String engineConfig = ps.getString(PROP_ENGINE_CONFIG_FILE);

        try {
            URL config = getClass().getResource(engineConfig);

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

        //
        // Set up for periodically flushing the data to disk.
        flushCheckInterval = ps.getInt(PROP_FLUSH_INTERVAL);
        flushTimer = new Timer("ItemSearchEngineFlushTimer");
        flushTimer.scheduleAtFixedRate(new FlushTimerTask(), flushCheckInterval,
                flushCheckInterval);
    }

    public SearchEngine getSearchEngine() {
        return engine;
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
            Map<String, Serializable> dm = item.getMap();
            Map<String, Object> im = new HashMap<String, Object>();

            //
            // Add the data that we want in every map.
            //im.put("aura-id", item.getID());
            im.put("aura-key", item.getKey());
            im.put("aura-name", item.getName());
            im.put("aura-type", item.getType().toString());
            if(dm != null) {
                for(Map.Entry<String, Serializable> e : dm.entrySet()) {
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
                        fi = new FieldInfo(e.getKey(), getAttributes(type),
                                type);
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
                    if(indexVal instanceof Map) {
                        indexVal = ((Map) indexVal).values();
                    } else if(val instanceof Indexable) {
                        indexVal = indexVal.toString();
                    }
                    im.put(e.getKey(), indexVal);
                }
            }

            engine.index(item.getKey(), im);
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
        if(val instanceof Indexable || val instanceof Indexable[] ||
                val instanceof Posting || val instanceof Posting[]) {
            return FieldInfo.Type.STRING;
        }

        if(val instanceof String || val instanceof String[]) {
            return FieldInfo.Type.STRING;
        }

        if(val instanceof Date || val instanceof Date[]) {
            return FieldInfo.Type.DATE;
        }

        if(val instanceof Integer || val instanceof Integer[] ||
                val instanceof Long || val instanceof Long[]) {
            return FieldInfo.Type.INTEGER;
        }

        if(val instanceof Float || val instanceof Float[] ||
                val instanceof Double || val instanceof Double[]) {
            return FieldInfo.Type.FLOAT;
        }
        
        //
        // The type of a map is the type of its values.  Arbitrary, but fun!
        if(val instanceof Map) {
            return getType(((Map) val).values());
        }
        
        //
        // Figure out an appropriate type for a collection.  We first want to 
        // make sure that all of the elements are of the same type.  This would
        // be a lot easier if we had real generic types, but there you go.  We'll
        // ignore zero length collections, because how would we know if it was 
        // indexed or not, eh?
        //
        // Once we figure out that everything is the same type, then we can
        // return a field type.  The underlying search engine can handle the
        // collection for itself.
        if(val instanceof Collection) {
            Collection c = (Collection) val;
            if(c.size() > 0) {
                Iterator i = c.iterator();
                Object o = i.next();
                FieldInfo.Type type = getType(o);
                if(type == FieldInfo.Type.NONE) {
                    return type;
                }
                while(i.hasNext()) {
                    Object o2 = i.next();
                    if(!o2.getClass().equals(o.getClass())) {
                        return FieldInfo.Type.NONE;
                    }
                }
                //
                // Return the type for the first object.
                return type;
            }
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
    public DocumentVector getDocumentVector(String key) {
        try {
            return engine.getDocumentVector(key);
        } catch(SearchEngineException ex) {
            log.log(Level.SEVERE, "Error searching for key " + key, ex);
            return null;
        }
    }

    public DocumentVector getDocumentVector(String key, String field) {
        return engine.getDocumentVector(key, field);
    }

    public DocumentVector getDocumentVector(String key, WeightedField[] fields) {
        return engine.getDocumentVector(key, fields);
    }

    /**
     * Finds the n most-similar items to the given item, based on the data in the 
     * provided field.
     * @param dv the document vector for the item of interest
     * @param n the number of similar items to return
     * @return the set of items most similar to the given item, based on the 
     * data indexed into the given field.  Note that the returned set may be
     * smaller than the number of items requested!
     * @see #getDocumentVector
     */
    public List<Scored<String>> findSimilar(DocumentVector dv, int n)
            throws AuraException {

        //
        // Recover from having been serialized.
        dv.setEngine(engine);
        ResultSet sim = dv.findSimilar("-score");
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        try {
            for(Result r : sim.getResults(0, n)) {
                ResultImpl ri = (ResultImpl) r;
                ret.add(new Scored<String>(ri.getKey(), 
                        ri.getScore(),
                        ri.getSortVals(),
                        ri.getDirections()));
            }
        } catch(SearchEngineException see) {
            throw new AuraException("Error getting similar items", see);
        }
        return ret;
    }

    /**
     * Gets a list of the keys for the items that have a field with a given value.
     * @param name the name of the field
     * @param val the value
     * @param n the number of keys to return
     * @return a list of the keys of the items whose fields have the given value
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Scored<String>> find(String name, String val, int n) throws AuraException, RemoteException {
        FieldEvaluator fe = new FieldEvaluator(name, FieldTerm.EQUAL, val);
        ResultSet rs = fe.eval(engine);
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        try {
            for(Result r : rs.getResults(0, n)) {
                ret.add(new Scored<String>(r.getKey(), r.getScore()));
            }
        } catch(SearchEngineException see) {
            throw new AuraException("Error finding items", see);
        }
        return ret;
    }

    public List<Scored<String>> query(String query, String sort, int n) throws AuraException, RemoteException {
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        try {
            for(Result r : engine.search(query, sort).getResults(0, n)) {
                ResultImpl ri = (ResultImpl) r;
                ret.add(new Scored<String>(ri.getKey(), 
                        ri.getScore(),
                        ri.getSortVals(), 
                        ri.getDirections()));
            }
        } catch(SearchEngineException see) {
            //
            // The search engine exception may be wrapping an exception that
            // we don't want to send across the wire, so we should see what if
            // there's one of these in there.
            Throwable ex = see.getCause();
            if(ex instanceof ngnova.retrieval.parser.ParseException ||
                    ex instanceof java.text.ParseException) {
                throw new AuraException("Error parsing query: " +
                        ex.getMessage());
            }
            throw new AuraException("Error finding items", see);
        }
        return ret;
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
     * A timer task for flushing the engine periodically.
     */
    class FlushTimerTask extends TimerTask {

        private long last = System.currentTimeMillis();

        @Override
        public void run() {
            try {
                long curr = System.currentTimeMillis();
                engine.flush();
                last = curr;
            } catch(SearchEngineException ex) {
                log.log(Level.SEVERE, "Error flushing engine data", ex);
            }
        }
    }
    /**
     * The resource to load for the engine configuration.  This gives us the
     * opportunity to use different configs as necessary (e.g., for testing).
     * Note that any resource named by this property must be accessible via
     * <code>Class.getResource()</code>!
     */
    @ConfigString(defaultValue = "itemSearchEngineConfig.xml")
    public static final String PROP_ENGINE_CONFIG_FILE = "engineConfigFile";

    /**
     * The default logging level for the search engine.  Paul likes things nice
     * and quiet.
     */
    @ConfigInteger(defaultValue = 1)
    public static final String PROP_ENGINE_LOG_LEVEL = "engineLogLevel";

    /**
     * The configurable index directory.
     */
    @ConfigString(defaultValue = "itemData.idx")
    public static final String PROP_INDEX_DIR = "indexDir";

    /**
     * The interval (in milliseconds) between index flushes.
     */
    @ConfigInteger(defaultValue = 3000, range = {1, 300000})
    public static final String PROP_FLUSH_INTERVAL = "flushInterval";

}
