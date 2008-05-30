package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.datastore.Indexable;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.minion.CompositeResultsFilter;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldInfo;
import com.sun.labs.minion.FieldValue;
import com.sun.labs.minion.IndexableString;
import com.sun.labs.minion.Log;
import com.sun.labs.minion.Posting;
import com.sun.labs.minion.Result;
import com.sun.labs.minion.ResultSet;
import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.SearchEngine;
import com.sun.labs.minion.SearchEngineException;
import com.sun.labs.minion.SearchEngineFactory;
import com.sun.labs.minion.WeightedField;
import com.sun.labs.minion.classification.ClassifierModel;
import com.sun.labs.minion.classification.ExplainableClassifierModel;
import com.sun.labs.minion.classification.FeatureCluster;
import com.sun.labs.minion.classification.WeightedFeature;
import com.sun.labs.minion.engine.SearchEngineImpl;
import com.sun.labs.minion.indexer.entry.DocKeyEntry;
import com.sun.labs.minion.indexer.partition.InvFileDiskPartition;
import com.sun.labs.minion.retrieval.DocumentVectorImpl;
import com.sun.labs.minion.retrieval.FieldEvaluator;
import com.sun.labs.minion.retrieval.FieldTerm;
import com.sun.labs.minion.retrieval.ResultImpl;
import com.sun.labs.minion.retrieval.ResultSetImpl;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.minion.util.Util;
import com.sun.labs.util.props.ConfigDouble;
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
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;
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
public class ItemSearchEngine implements Configurable {

    private SearchEngine engine;

    private Logger log;

    private int engineLogLevel;

    private boolean shuttingDown;

    private long flushCheckInterval;
    
    private double skimPercentage;

    private Timer flushTimer;
    
    public ItemSearchEngine() {
        
    }
    
    /**
     * Creates an item search engine pointed at a particular index directory.
     * @param indexDir
     * @param config
     */
    public ItemSearchEngine(String indexDir, String config) {
        log = Logger.getLogger(getClass().getName());
        try {
            URL cu = getClass().getResource(config);

            //
            // Creates the search engine.  We'll use a full blown fields-and-all
            // engine because we need to be able to handle fielded doc vectors
            // and postings.
            engine = SearchEngineFactory.getSearchEngine(indexDir,
                    "aardvark_search_engine",
                    cu);
        } catch(SearchEngineException see) {
            log.log(Level.SEVERE, "error opening engine for: " + indexDir, see);
        }
        
    }

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
                    "aardvark_search_engine",
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
        
        skimPercentage = ps.getDouble(PROP_SKIM_PERCENTAGE);
        
    }

    public SearchEngine getSearchEngine() {
        return engine;
    }

    public void defineField(ItemType itemType, String field, EnumSet<Item.FieldCapability> caps, 
            Item.FieldType fieldType) throws AuraException {
        EnumSet<FieldInfo.Attribute> attr = EnumSet.noneOf(FieldInfo.Attribute.class);
        for(Item.FieldCapability fc : caps) {
            switch(fc) {
                case FILTER:
                    attr.add(FieldInfo.Attribute.SAVED);
                    break;
                case MATCH:
                    attr.add(FieldInfo.Attribute.SAVED);
                    break;
                case SEARCH:
                    attr.add(FieldInfo.Attribute.INDEXED);
                    attr.add(FieldInfo.Attribute.TOKENIZED);
                    break;
                case SIMILARITY:
                    attr.add(FieldInfo.Attribute.TOKENIZED);
                    attr.add(FieldInfo.Attribute.VECTORED);
                    break;
                case SORT:
                    attr.add(FieldInfo.Attribute.SAVED);
                    break;
            }
        }
        
        FieldInfo.Type type = fieldType == null ? FieldInfo.Type.NONE : FieldInfo.Type.valueOf(fieldType.toString());
        try {
            engine.defineField(new FieldInfo(field, attr, type));
        } catch(SearchEngineException ex) {
            throw new AuraException("Error defining field " + field, ex);
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
            Map<String, Object> im = new HashMap<String, Object>();

            //
            // Add the data that we want in every map.
            //im.put("aura-id", item.getID());
            im.put("aura-key", item.getKey());
            im.put("aura-name", item.getName());
            im.put("aura-type", item.getType().toString());
            
            //
            // Index the elements of the map that require indexing.
            for(Map.Entry<String, Serializable> e : item) {
                Serializable val = e.getValue();

                //
                // We need to make sure that if an item changes, it doesn't
                // get an ever-growing set of autotags, so we won't add any
                // autotags when indexing.
                if(e.getKey().equalsIgnoreCase("autotag")) {
                    continue;
                }

                //
                // OK, first up, make sure that we have an appropriately defined
                // field for this name.  We'll need to make sure that we're not
                // clobbering field types as we go.
                FieldInfo fi = engine.getFieldInfo(e.getKey());
                
                if(fi == null) {
                    //
                    // We should have had this field defined, so we can skip this
                    // one.
                    continue;
                }
                
                FieldInfo.Type type = getType(val);

                //
                // Ignore stuff we don't know how to handle.
                if(type == FieldInfo.Type.NONE) {
                    continue;
                }

                //
                // Now get a value to put in the index map.
                Object indexVal = val;
                if(indexVal instanceof Map) {
                    indexVal = ((Map) indexVal).values();
                } else if(val instanceof Indexable ||
                        val instanceof String) {
                    //
                    // The content might contain XML or HTML, so let's get
                    // rid of that stuff.
                    indexVal = new IndexableString(indexVal.toString(),
                            IndexableString.Type.HTML);
                }
                im.put(e.getKey(), indexVal);
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
     * @param rf a (possibly <code>null</code>) filter to apply when getting 
     * the top results from the findSimilar
     * @return the set of items most similar to the given item, based on the
     * data indexed into the given field.  Note that the returned set may be
     * smaller than the number of items requested!
     * @see #getDocumentVector
     */
    public List<Scored<String>> findSimilar(DocumentVector dv, int n, ResultsFilter rf)
            throws AuraException {

        //
        // Recover from having been serialized.
        dv.setEngine(engine);
        ResultSet sim = dv.findSimilar("-score", skimPercentage);
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        NanoWatch nw = new NanoWatch();
        nw.start();
        try {
            for(Result r : sim.getResults(0, n, rf)) {
                ResultImpl ri = (ResultImpl) r;
                ret.add(new Scored<String>(ri.getKey(), 
                        ri.getScore(),
                        ri.getSortVals(),
                        ri.getDirections()));
            }
        } catch(SearchEngineException see) {
            throw new AuraException("Error getting similar items", see);
        }
        nw.stop();
        int nt = 0;
        int np = 0;
        if(rf instanceof CompositeResultsFilter) {
            nt = ((CompositeResultsFilter) rf).getTested();
            np = ((CompositeResultsFilter) rf).getPassed();
        }
        log.info(String.format("fsgr %s docs: %d test: %d pass: %d gr: %.2f", 
                dv.getKey(), 
                sim.size(),
                nt, np, 
                nw.getTimeMillis()));
        return ret;
    }

    public List<Scored<String>> getTopTerms(String key, String field, int n)
            throws AuraException, RemoteException {
        DocumentVectorImpl dv = (DocumentVectorImpl) getDocumentVector(key, field);
        if(dv == null) {
            return new ArrayList<Scored<String>>();
        }
        WeightedFeature[] wf = dv.getFeatures();
        Util.sort(wf, WeightedFeature.getInverseWeightComparator());
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        for(int i = 0; i < wf.length && i < n; i++) {
            ret.add(new Scored<String>(wf[i].getName(), wf[i].getWeight()));
        }
        return ret;
    }
    
    public List<Scored<String>> getTopFeatures(String autotag, int n) {
        ClassifierModel cm = ((SearchEngineImpl) engine).getClassifier(autotag);
        if(cm == null) {
            return new ArrayList<Scored<String>>();
        }
        PriorityQueue<FeatureCluster> q = new PriorityQueue<FeatureCluster>(n, FeatureCluster.weightComparator);
        for(FeatureCluster fc : cm.getFeatures()) {
            if(q.size() < n) {
                q.offer(fc);
            } else {
                FeatureCluster top = q.peek();
                if(fc.getWeight() > top.getWeight()) {
                    q.poll();
                    q.offer(fc);
                }
            }
        }
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        while(q.size() > 0) {
            FeatureCluster fc = q.poll();
            ret.add(new Scored<String>(fc.getHumanReadableName(), fc.getWeight()));
        }
        Collections.reverse(ret);
        return ret;
    }

    public List<Scored<String>> findSimilarAutotags(String autotag, int n)
            throws AuraException, RemoteException {
        List<FieldValue> l = ((SearchEngineImpl) engine).getSimilarClassifiers(autotag, n);
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        for(FieldValue fv : l) {
            ret.add(new Scored<String>(fv.getValue(), fv.getScore()));
        }
        return ret;
    }
    
    public List<Scored<String>> explainSimilarAutotags(String a1, String a2, int n)
            throws AuraException, RemoteException {
        List<WeightedFeature> l = ((SearchEngineImpl) engine).getSimilarClassifierTerms(a1, a2, n);
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        for(WeightedFeature wf : l) {
            ret.add(new Scored<String>(wf.getName(), wf.getWeight()));
        }
        return ret;
    }

    /**
     * Gets an explanation as to why a given autotag would be applied to 
     * a given document.
     * 
     * @param key the key of th item for which we want an explanation
     * @param autoTag the autotag that we want to explain
     * @param n the number of terms to return
     * @return a list of the terms that contribute the most towards the
     * autotagging.  The score associated with a term is the proportion of 
     * contribution towards the autotagging.
     */
    public List<Scored<String>> getExplanation(String key, String autoTag,
            int n) 
            throws AuraException, RemoteException {
        ClassifierModel cm = ((SearchEngineImpl) engine).getClassifierManager().getClassifier(autoTag);
        if(cm == null || !(cm instanceof ExplainableClassifierModel)) {
            log.warning("Not an explainable classifier: " + autoTag);
            return new ArrayList<Scored<String>>();
        }
        
        List<WeightedFeature> wf = ((ExplainableClassifierModel) cm).explain(key);
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        for(Iterator<WeightedFeature> i = wf.iterator(); i.hasNext() && ret.size() < n;) {
            WeightedFeature f = i.next();
            ret.add(new Scored<String>(f.getName(), f.getWeight()));
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

    public List<Scored<String>> query(String query, String sort, int n, ResultsFilter rf) throws AuraException, RemoteException {
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        try {
            for(Result r : engine.search(query, sort).getResults(0, n, rf)) {
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
            if(ex instanceof com.sun.labs.minion.retrieval.parser.ParseException ||
                    ex instanceof java.text.ParseException) {
                throw new AuraException("Error parsing query: " +
                        ex.getMessage());
            }
            throw new AuraException("Error finding items", see);
        }
        return ret;
    }
    
    /**
     * Gets the items that have had a given autotag applied to them.
     * @param autotag the tag that we want items to have been assigned
     * @param n the number of items that we want
     * @return a list of the item keys that have had a given autotag applied.  The
     * list is ordered by the confidence of the tag assignment
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Scored<String>> getAutotagged(String autotag, int n)
            throws AuraException, RemoteException {
        try {

            List<Scored<String>> ret = new ArrayList<Scored<String>>();
            
            ResultSetImpl rs = (ResultSetImpl) engine.search(String.format("autotag = \"%s\"", autotag));
            for(Result r : rs.getResultsForScoredField(0, n, "autotag", autotag, "autotag-score")) {
                ret.add(new Scored<String>(r.getKey(), r.getScore()));
            }
            return ret;
                    
            
        } catch(SearchEngineException ex) {
            throw new AuraException("Error searching for autotag " + autotag, ex);
        }
    }
   
    
    /**
     * Gets a list of scored strings consisting of the autotags assigned to
     * an item and their associated classifier scores.  This requires rather
     * deeper knowledge of the field store than I am comfortable with, but using
     * the document abstraction would require fetching all of the field values.
     * 
     * <p>
     * 
     * We should probably fix the document abstraction so that it fetches on
     * demand, but not before the open house, eh?
     * 
     * <p>
     * 
     * autotagfix
     * 
     * @param key the key for the document whose autotags we want
     * @return a list of scored strings where the item is the autotag and the score
     * is the classifier score associated with the autotag.
     */
    public List<Scored<String>> getAutoTags(String key) {
        DocKeyEntry dke = ((SearchEngineImpl) engine).getDocumentTerm(key);
        if(dke == null) {
            //
            // No document by that name here...
            return null;
        }
        List<String> autotags = (List<String>) ((InvFileDiskPartition) dke.getPartition()).getFieldStore().getSavedFieldData("autotag", dke.getID(), true);
        if(autotags.size() == 0) {
            
            //
            // No tags.
            return null;
        }
        List<Double> autotagScores = (List<Double>) ((InvFileDiskPartition) dke.getPartition()).getFieldStore().getSavedFieldData("autotag-score", dke.getID(), true);
        if(autotags.size() != autotagScores.size()) {
            log.warning("Mismatched autotags and scores: " + autotags + " " + autotagScores);
        }
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        int lim = Math.min(autotags.size(), autotagScores.size());
        for(int i = 0; i < lim; i++) {
            ret.add(new Scored<String>(autotags.get(i), autotagScores.get(i)));
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
    @ConfigInteger(defaultValue = 3000, range = {1, 3000000})
    public static final String PROP_FLUSH_INTERVAL = "flushInterval";
    
    /**
     * The skim percentage to use for findSimilar.
     */
    @ConfigDouble(defaultValue=0.25)
    public static final String PROP_SKIM_PERCENTAGE = "skimPercentage";

}
