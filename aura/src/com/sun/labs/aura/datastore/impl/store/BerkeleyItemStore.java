package com.sun.labs.aura.datastore.impl.store;

import com.sleepycat.je.DatabaseException;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemEvent;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.impl.DSBitSet;
import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.StatService;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.IndexListener;
import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.SearchEngine;
import com.sun.labs.minion.util.DirCopier;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ComponentListener;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.ConfigStringList;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the item store using the berkeley database as a back
 * end.
 */
public class BerkeleyItemStore implements Replicant, Configurable, ComponentListener, AuraService,
        IndexListener {
    
    /**
     * A directory that we should copy into /tmp/${prefix} to start out with.
     */
    @ConfigBoolean(defaultValue=false)
    public static final String PROP_COPY_DIR = "copyDir";

    /**
     * The location of the BDB/JE Database Environment
     */
    @ConfigString(defaultValue = "/tmp/aura")
    public final static String PROP_DB_ENV = "dbEnv";

    protected String dbEnvDir;

    /**
     * The prefix that we're storing data for.
     */
    @ConfigString
    public static final String PROP_PREFIX = "prefix";

    private DSBitSet prefixCode;
    
    private String prefixString;

    /**
     * The search engine that will store item info
     */
    @ConfigComponent(type = ItemSearchEngine.class)
    public static final String PROP_SEARCH_ENGINE =
            "itemSearchEngine";

    protected ItemSearchEngine searchEngine;

    @ConfigInteger(defaultValue = 60)
    public final static String PROP_CACHE_SIZE_MEM_PERCENTAGE = "cacheSizeMemPercentage";
    private int cacheSizeMemPercentage;

    /**
     * Our partition cluster.
     */
    @ConfigComponent(type =
    com.sun.labs.aura.datastore.impl.PartitionCluster.class)
    public static final String PROP_PARTITION_CLUSTER = "partitionCluster";

    private PartitionCluster partitionCluster;

    @ConfigComponent(type = com.sun.labs.aura.util.StatService.class)
    public static final String PROP_STAT_SERVICE = "statService";
    protected StatService statService;

    @ConfigInteger(defaultValue = 500)
    public final static String PROP_STAT_BATCH_SIZE = "statBatchSize";
    protected int statBatchSize;
    
    @ConfigStringList(mandatory = false)
    public static final String PROP_LOG_METHODS = "logMethods";
    protected EnumSet<StatName> toLog;
    
    /**
     * ComponentRegistry will be non-null if we're running in a RMI environment
     */
    protected ConfigurationManager cm = null;

    /**
     * The wrapper around all the BDB/JE implementation
     */
    protected BerkeleyDataWrapper bdb;

    /**
     * A map to store listeners for each type of item
     */
    protected Map<ItemType, Set<ItemListener>> listenerMap;
    
    /**
     * A queue of change events that need to be sent
     */
    private ConcurrentLinkedQueue<ChangeEvent> changeEvents;

    /**
     * A queue of create events that need to be sent
     */
    private ConcurrentLinkedQueue<ItemImpl> createEvents;

    /**
     * Indicates if the item store has been closed.  Once the store is
     * closed, no more operators are permitted.
     */
    protected boolean closed;
    
    protected Timer timer;
    
    /**
     * A logger for messages/debug info
     */
    protected Logger logger;

    protected AtomicInteger[] statInvocationCounts;
    
    /**
     * Constructs an empty item store, ready to be configured.
     */
    public BerkeleyItemStore() {
        listenerMap = new HashMap<ItemType, Set<ItemListener>>();
        changeEvents = new ConcurrentLinkedQueue<ChangeEvent>();
        createEvents = new ConcurrentLinkedQueue<ItemImpl>();
        
        //
        // Set up the array of stat invocation to have one atomic integer per
        // stat
        statInvocationCounts = new AtomicInteger[StatName.values().length];
        for (StatName name : StatName.values()) {
            statInvocationCounts[name.ordinal()] = new AtomicInteger(0);
        }

        timer = new Timer("StatScheduler", true);
        timer.schedule(new StatSender(), 0, 10 * 1000);
    }

    /**
     * Sets the properties for the item store, opening the database
     * environment and the entity store.  This must be called immediately
     * after the object is instantiated.
     * 
     * @param ps the set of properties
     * @throws com.sun.labs.util.props.PropertyException
     */
    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        
        logger = ps.getLogger();
        prefixString = ps.getString(PROP_PREFIX);
        prefixCode = DSBitSet.parse(prefixString);
        boolean copyDir = ps.getBoolean(PROP_COPY_DIR);

        //
        // See if there are specific methods we need to log
        List<String> logs = ps.getStringList(PROP_LOG_METHODS);
        if (logs != null && !logs.isEmpty()) {
            toLog = EnumSet.noneOf(StatName.class);
            for (String val : logs) {
                toLog.add(StatName.valueOf(val));
            }
        } else {
            toLog = EnumSet.allOf(StatName.class);
        }
        
        //
        // Get the database environment, copying it if necessary.
        dbEnvDir = ps.getString(PROP_DB_ENV);
        File f = new File(dbEnvDir);
        
        //
        // Since we've changed the default dir, see if we need to relocate
        // an existing db already on disk.  This code should get cleaned out
        // once it is run once on our live system.
        if (!f.exists()) {
            if (!dbEnvDir.contains(prefixString)) {
                String oldPath =
                        dbEnvDir.replaceFirst("db$", prefixString + "/db");
                if (!oldPath.equals(dbEnvDir)) {
                    File of = new File(oldPath);
                    if (of.exists()) {
                        if (!of.renameTo(f)) {
                            logger.info("Failed to move "
                                    + oldPath + " to " + dbEnvDir);
                        }
                    }
                    oldPath = dbEnvDir.replaceFirst("db$",
                            prefixString + "/itemIndex.idx");
                    of = new File(oldPath);
                    File nf = new File(
                            dbEnvDir.replaceFirst("db$", "itemIndex.idx"));
                    if (of.exists()) {
                        if (!of.renameTo(nf)) {
                            logger.info("Failed to move "
                                    + of.getPath() + " to " + nf.getPath());
                        }
                    }
                }
            }
        }
        // end of code for patching directory structure
        
        if(!f.exists() && !f.mkdirs()) {
            throw new PropertyException(ps.getInstanceName(), PROP_DB_ENV,
                    "Unable to create new directory for db");
        }

        //
        // If we want to copy the data into temp storage, do it now.
        if(copyDir) {
            String tds = String.format(
                    System.getProperty("java.io.tmpdir") +
                    "/replicant-%s/db/", ps.getString(PROP_PREFIX));
            File td = new File(tds);
            if(!td.mkdirs() && !td.exists()) {
                throw new PropertyException(ps.getInstanceName(),
                        PROP_COPY_DIR,
                        "Unable to make temporary directory for db");
            }
            try {
                logger.info("Copying BDB to temp directory: " + tds);
                DirCopier dc = new DirCopier(f, td);
                dc.copy();
                logger.info("Copy to temp directory completed");
                dbEnvDir = tds;
            } catch(IOException ex) {
                throw new PropertyException(ex, ps.getInstanceName(),
                        PROP_COPY_DIR,
                        "Unable to copy DBD to directory: " + tds);
            }
        }


        // get the cache size memory percentage
        cacheSizeMemPercentage = ps.getInt(PROP_CACHE_SIZE_MEM_PERCENTAGE);

        //
        // Configure and open the environment and entity store
        try {
            logger.info("Opening BerkeleyDataWrapper: " + dbEnvDir);
            bdb = new BerkeleyDataWrapper(dbEnvDir, logger,
                    cacheSizeMemPercentage);
            logger.info("Finished opening BerkeleyDataWrapper");
        } catch(DatabaseException e) {
            logger.severe("Failed to load the database environment at " +
                    dbEnvDir + ": " + e);
        }

        //
        // Get the search engine from the config system
        searchEngine =
                (ItemSearchEngine) ps.getComponent(PROP_SEARCH_ENGINE);
        searchEngine.getSearchEngine().addIndexListener(this);


        //
        // Get the configuration manager, which we'll use to export things, if
        // necessary.
        cm = ps.getConfigurationManager();

        //
        // Fetch the partition cluster with this prefix.
        partitionCluster = (PartitionCluster) ps.getComponent(PROP_PARTITION_CLUSTER, this);
        register(partitionCluster);

        //
        // Get a handle to the stat service if we got one
        statService = (StatService) ps.getComponent(PROP_STAT_SERVICE, this);
        statBatchSize = ps.getInt(PROP_STAT_BATCH_SIZE);
    }

    @Override
    public void componentAdded(Component c) {
        if(c instanceof StatService) {
            statService = (StatService) c;
        } else if(c instanceof PartitionCluster) {
            register((PartitionCluster) c);
        }
    }

    private void register(PartitionCluster pc) {
        try {
            logger.info("Registering with partition: " + pc.getPrefix());
            pc.addReplicant((Replicant) cm.getRemote(this, pc));
        } catch (RemoteException rx) {
            throw new PropertyException(null, PROP_PARTITION_CLUSTER, "Unable to add " +
                    "replicant to partition cluster.");
        }
    }
    
    @Override
    public void componentRemoved(Component c) {
        logger.info("Removed: " + c);
        if(c instanceof StatService) {
            statService = null;
        }
    }

    @Override
    public DSBitSet getPrefix() {
        return prefixCode;
    }

    /**
     * Close up the entity store and the database environment.
     */
    @Override
    public synchronized void close() throws AuraException {
        if (!closed) {
            closed = true;
            logger.info("Closing BDB for prefix: " + prefixString);
            bdb.close();
            logger.info("Shutting down search engine");
            searchEngine.getSearchEngine().removeIndexListener(this);
            searchEngine.shutdown();
            logger.info("Finished closing search engine");
        }
    }

    @Override
    public void defineField(ItemType itemType, String field)
            throws AuraException, RemoteException {
        defineField(itemType, field, null, null);
    }

    @Override
    public void defineField(ItemType itemType, String field, EnumSet<Item.FieldCapability> caps, 
            Item.FieldType fieldType) throws AuraException, RemoteException {
        bdb.defineField(itemType, field, caps, fieldType);
        
        //
        // If this field is going to be dealt with by the search engine, then
        // send it there.
        if(caps != null && caps.size() > 0) {
            searchEngine.defineField(itemType, field, caps, fieldType);
        }
        
    }

    @Override
    public Map<String,FieldDescription> getFieldDescriptions()
            throws RemoteException {
        return bdb.getFieldDescriptions();
    }
    
    /**
     * Get all the instances of a particular type of item from the store
     * 
     * @param itemType the type of item to fetch
     * @return all of those items
     */
    @Override
    public List<Item> getAll(ItemType itemType) throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_ALL, state);
        
        List<Item> ret = bdb.getAll(itemType);
        
        exit(state);
        return ret;
    }

    @Override
    public DBIterator<Item> getAllIterator(ItemType itemType)
            throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_ALL_ITR, state);
        
        DBIterator<Item> dbit = bdb.getAllIterator(itemType);
        dbit = (DBIterator<Item>)cm.getRemote(dbit);
        
        exit(state);
        return dbit;
    }
    
    @Override
    public Item getItem(String key) throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_ITEM, state);
        
        Item ret = bdb.getItem(key);
        
        exit(state, ": " + key);
        return ret;
    }

    @Override
    public Collection<Item> getItems(Collection<String> keys) throws AuraException, RemoteException {
        StatState state = new StatState();
        enter(StatName.GET_ITEMS, state, " for " + keys.size());
        
        List<Item> ret = new ArrayList();
        for(String key : keys) {
            Item i = getItem(key);
            if(i != null) {
                ret.add(i);
            }
        }

        state.count = ret.size();
        exit(state);
        return ret;
    }
    
    @Override
    public List<Scored<Item>> getScoredItems(List<Scored<String>> keys) throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_SCORED_ITEMS, state, " for " + keys.size());

        List<Scored<Item>> ret = new ArrayList();
        for(Scored<String> key : keys) {
            Item item = getItem(key.getItem());
            if(item != null) {
                ret.add(new Scored<Item>(item, key));
            }
        }

        exit(state);
        if(ret.size() > 0) {
            ret.get(0).time = state.timer.getTimeMillis();
        }
        return ret;
    }

    @Override
    public User getUser(String key) throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_USER, state);
        
        User ret = (User) bdb.getItem(key);
        
        exit(state);
        return ret;
    }

    @Override
    public User getUserForRandomString(String randStr) throws AuraException {
        return bdb.getUserForRandomString(randStr);
    }
    
    @Override
    public Item putItem(Item item) throws AuraException {
        StatState state = new StatState();
        enter(StatName.PUT_ITEM, state);
        
        boolean existed = false;
        if(item instanceof ItemImpl) {
            ItemImpl itemImpl = (ItemImpl) item;
            
            //
            // Walk the fields, make sure they're defined, and figure out whether
            // we need to re-index this item.
            boolean mustIndex = false;
            Set<String> setFields = itemImpl.getModifiedFields();
            for(Map.Entry<String,Serializable> e : itemImpl) {
                FieldDescription fd;
                try {
                    fd = bdb.fieldByName.get(e.getKey());
                } catch(DatabaseException ex) {
                    throw new AuraException("Error getting field description ", ex);
                }
                if(fd == null) {
                    throw new AuraException("Item " + item.getKey() + 
                            " contains unknown field " + e.getKey());
                }
                if(fd.mustIndex() && setFields.contains(e.getKey())) {
                    mustIndex = true;
                }
            }
            
            
            //
            // If this was a remote object, its transient map will be null
            // and storeMap will be a no-op.  If it was a local object then
            // storeMap will serialize the map (if there is one).
            itemImpl.storeMap();
            ItemImpl prev = bdb.putItem(itemImpl);
            if(prev != null) {
                existed = true;
            }

            if(mustIndex || !existed) {
                //
                // The item was modified in a way that requires indexing.
                searchEngine.index(itemImpl);
            }

            //
            // Finally, send out relevant events.
            if(existed) {
                statInvocationCounts[StatName.UPDATE_ITEM.ordinal()]
                        .getAndIncrement();
                itemChanged(itemImpl, ItemEvent.ChangeType.AURA);
            } else {
                statInvocationCounts[StatName.NEW_ITEM.ordinal()]
                        .getAndIncrement();
                itemCreated(itemImpl);
            }
            
            exit(state);
            return itemImpl;
        } else {
            throw new AuraException("Unsupported Item type");
        }

    }

    @Override
    public User putUser(User user) throws AuraException {
        return (User) putItem(user);
    }

    /**
     * Deletes just a user from the item store, not touching the attention.
     */
    @Override
    public void deleteUser(String userKey) throws AuraException {
        deleteItem(userKey);
    }
    
    /**
     * Deletes just an item from the item store, not touching the attention.
     */
    @Override
    public void deleteItem(String itemKey) throws AuraException {
        searchEngine.delete(itemKey);
        bdb.deleteItem(itemKey);
    }

    @Override
    public void deleteAttention(List<Long> ids) throws AuraException {
        bdb.deleteAttention(ids);
    }
    
    @Deprecated
    @Override
    public List<Item> getItems(User user, Type attnType,
            ItemType itemType)
            throws AuraException {
        return bdb.getItems(user.getKey(), attnType, itemType);
    }

    @Override
    public DBIterator<Item> getItemsAddedSince(ItemType type,
            Date timeStamp)
            throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_ITEMS_SINCE, state);
        
        DBIterator<Item> res =
                bdb.getItemsAddedSince(type, timeStamp.getTime());
        res = (DBIterator<Item>)cm.getRemote(res);
        
        exit(state);
        return res;
    }

    @Override
    public List<Attention> getAttention(AttentionConfig ac)
            throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_ATTN, state);
        
        DBIterator<Attention> attn = bdb.getAttentionIterator(ac);
        List<Attention> res = new ArrayList<Attention>();
        try {
            while (attn.hasNext()) {
                res.add(attn.next());
            }
            attn.close();
        } catch (RemoteException e) {
            throw new AuraException("Remote exception on local object!!", e);
        }
        
        exit(state);
        return res;
    }

    @Override
    public DBIterator<Attention> getAttentionIterator(AttentionConfig ac)
            throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_ALL_ITR, state);
        
        DBIterator<Attention> res = bdb.getAttentionIterator(ac);
        res = (DBIterator<Attention>)cm.getRemote(res);
        
        exit(state);
        return res;
    }

    
    @Override
    public Long getAttentionCount(AttentionConfig ac) {
        StatState state = new StatState();
        enter(StatName.GET_ATTN_CNT, state);
        
        Long ret = bdb.getAttentionCount(ac);
        
        exit(state);
        return ret;
    }
    
    @Override
    public List<Attention> getAttentionSince(AttentionConfig ac,
                                             Date timeStamp)
            throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_ATTN_SINCE, state);
        
        DBIterator<Attention> attn =
                bdb.getAttentionSinceIterator(ac, timeStamp);
        List<Attention> res = new ArrayList<Attention>();
        try {
            while (attn.hasNext()) {
                res.add(attn.next());
            }
            attn.close();
        } catch (RemoteException e) {
            throw new AuraException("Remote exception on local object!!", e);
        }
        
        exit(state);
        return res;
    }

    @Override
    public DBIterator<Attention> getAttentionSinceIterator(AttentionConfig ac,
                                                           Date timeStamp)
            throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_ATTN_SINCE_ITR, state);
        
        DBIterator<Attention> res =
                bdb.getAttentionSinceIterator(ac, timeStamp);
        res = (DBIterator<Attention>)cm.getRemote(res);
        
        exit(state);
        return res;
    }

    
    @Override
    public Long getAttentionSinceCount(AttentionConfig ac, Date timeStamp)
            throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_ATTN_SINCE_CNT, state);
        
        Long ret = bdb.getAttentionSinceCount(ac, timeStamp);
        
        exit(state);
        return ret;
    }
    
    @Override
    public List<Attention> getLastAttention(AttentionConfig ac, int count)
            throws AuraException {
        StatState state = new StatState();
        enter(StatName.GET_LAST_ATTN, state);
        
        List<Attention> attn = getAttention(ac);
        if (attn.isEmpty()) {
            return attn;
        }
        
        int numReturned = Math.min(count, attn.size());
        Collections.sort(attn, new ReverseAttentionTimeComparator());
        
        exit(state);
        return new ArrayList(attn.subList(0, numReturned));
    }
    
    @Override
    public Attention attend(Attention att) throws AuraException {
        StatState state = new StatState();
        enter(StatName.ATTEND, state);
        
        //
        // Make a persistent attention and give it to the BDB
        Attention ret = null;
        if (att instanceof PersistentAttention) {
            bdb.putAttention((PersistentAttention)att);
            ret = att;
        } else {
            PersistentAttention pa = new PersistentAttention(att);
            bdb.putAttention(pa);
            ret = pa;
        }
        exit(state);
        return ret;
    }

    @Override
    public List<Attention> attend(List<Attention> attns) throws AuraException {
        StatState state = new StatState();
        enter(StatName.ATTEND, state, " with " + attns.size());
        state.count = attns.size();
        
        //
        // Make persistent attentions and feed them to the BDB
        List<PersistentAttention> pas = new ArrayList<PersistentAttention>(attns.size());
        for (Attention a : attns) {
            if (a instanceof PersistentAttention) {
                pas.add((PersistentAttention)a);
            } else {
                pas.add(new PersistentAttention(a));
            }
        }
        bdb.putAttention(pas);
        
        exit(state);
        return new ArrayList<Attention>(pas);
    }

    @Override
    public void removeAttention(String srcKey, String targetKey,
                                Attention.Type type)
            throws AuraException {
        bdb.removeAttention(srcKey, targetKey, type);
    }

    @Override
    public void removeAttention(String itemKey)
            throws AuraException, RemoteException {
        bdb.removeAttention(itemKey);
    }

    @Override
    public List<Scored<String>> query(String query, int n, ResultsFilter rf)
            throws AuraException, RemoteException {
        return query(query, "-score", n, rf);
    }

    @Override
    public List<Scored<String>> query(String query, String sort, int n, ResultsFilter rf)
            throws AuraException, RemoteException {
        StatState state = new StatState();
        enter(StatName.QUERY, state);
        
        List<Scored<String>> res = searchEngine.query(query, sort, n, rf);

        exit(state, ": " + query);
        return res;
    }

    @Override
    public List<Scored<String>> getAutotagged(String autotag, int n)
            throws AuraException, RemoteException {
        StatState state = new StatState();
        enter(StatName.GET_AUTOTAGGED, state);

        List<Scored<String>> res = searchEngine.getAutotagged(autotag, n);

        exit(state, ": " + autotag);
        return res;
    }
    
    @Override
    public List<Scored<String>> getTopAutotagTerms(String autotag, int n)
            throws AuraException, RemoteException {
        StatState state = new StatState();
        enter(StatName.GET_TOP_AUTOTAG_TERMS, state);
        
        List<Scored<String>> ret = searchEngine.getTopFeatures(autotag, n);
        
        exit(state);
        return ret;
    }
    
    @Override
    public List<Scored<String>> findSimilarAutotags(String autotag, int n)
            throws AuraException, RemoteException {
        StatState state = new StatState();
        enter(StatName.FIND_SIM_AUTOTAGS, state);
        
        List<Scored<String>> ret = searchEngine.findSimilarAutotags(autotag, n);
        
        exit(state);
        return ret;
    }
 
    @Override
    public List<Scored<String>> explainSimilarAutotags(String a1, String a2,
            int n)
            throws AuraException, RemoteException {
        StatState state = new StatState();
        enter(StatName.EXPLAIN_SIM, state);
        
        List<Scored<String>> ret = searchEngine.explainSimilarAutotags(a1, a2, n);
        
        exit(state);
        return ret;
    }
    
    @Override
    public DocumentVector getDocumentVector(String key, SimilarityConfig config) {
        StatState state = new StatState();
        enter(StatName.GET_DV_KEY, state);
        
        DocumentVector dv = searchEngine.getDocumentVector(key, config);
        
        exit(state);
        return dv;
    }
    
    @Override
    public DocumentVector getDocumentVector(WordCloud cloud, SimilarityConfig config) {
        StatState state = new StatState();
        enter(StatName.GET_DV_CLOUD, state);
        
        DocumentVector dv = searchEngine.getDocumentVector(cloud, config);
        
        exit(state);
        return dv;
    }
    
    @Override
    public List<FieldFrequency> getTopValues(String field, int n,
            boolean ignoreCase) throws AuraException, RemoteException {
        StatState state = new StatState();
        enter(StatName.GET_TOP_VALUES, state);
        
        List<FieldFrequency> ret =
                searchEngine.getSearchEngine().getTopFieldValues(field, n, ignoreCase);
        
        exit(state);
        return ret;
    }
    
    /**
     * Finds a the n most similar items to the given item.
     * @param key the item that we want to find similar items for
     * @param n the number of similar items to return
     * @return the set of items most similar to the given item, ordered by 
     * similarity to the given item.  The similarity of the items is based on 
     * all of the indexed text associated with the item in the data store.
     */
    @Override
    public List<Scored<String>> findSimilar(DocumentVector dv, SimilarityConfig config)
            throws AuraException, RemoteException {
        StatState state = new StatState();
        enter(StatName.FIND_SIM, state);
        
        List<Scored<String>> fsr = searchEngine.findSimilar(dv, config);

        exit(state, ": " + dv.getKey());
        return fsr;
    }

    @Override
    public WordCloud getTopTerms(String key, String field, int n)
            throws AuraException, RemoteException {
        StatState state = new StatState();
        enter(StatName.GET_TOP_TERMS, state);
        
        WordCloud ret = searchEngine.getTopTerms(key, field, n);
        
        exit(state);
        return ret;
    }

    @Override
    public List<Scored<String>> getExplanation(String key, String autoTag,
            int n)
            throws AuraException, RemoteException {
        StatState state = new StatState();
        enter(StatName.GET_EXPLAIN, state);
        
        List<Scored<String>> ret = searchEngine.getExplanation(key, autoTag, n);
        
        exit(state);
        return ret;
    }

    /**
     * Transforms a list of scored keys (as from the search engine) to a list of scored
     * items. 
     * @param s the list of keys to transform
     * @return a set of items, organized by score
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    private List<Scored<Item>> keysToItems(List<Scored<String>> s)
            throws AuraException, RemoteException {
        List<Scored<Item>> ret = new ArrayList<Scored<Item>>();
        for(Scored<String> ss : s) {
            Item item = getItem(ss.getItem());
            if(item == null) {
                logger.info("Null item for key: " + ss.getItem());
                continue;
            }
            ret.add(new Scored<Item>(item, ss));
        }
        return ret;
    }

    @Override
    public void addItemListener(ItemType itemType, ItemListener listener)
            throws AuraException {
        //
        // Find the set of listeners for this type and add it, adding a set to
        // track these listeners if there isn't one.
        synchronized(listenerMap) {
            Set<ItemListener> l = listenerMap.get(itemType);
            if(l == null) {
                l = new HashSet<ItemListener>();
                listenerMap.put(itemType, l);
            }
            l.add(listener);
        }
    }

    @Override
    public void removeItemListener(ItemType itemType, ItemListener listener)
            throws AuraException {
        //
        // If we were givn a null item type, remove from all types
        synchronized(listenerMap) {
            if(itemType == null) {
                for(ItemType t : listenerMap.keySet()) {
                    Set<ItemListener> l = listenerMap.get(t);
                    l.remove(listener);
                }
            } else {
                Set<ItemListener> l = listenerMap.get(itemType);
                l.remove(listener);
            }
        }
    }

    @Override
    public long getItemCount(ItemType type) {
        return bdb.getItemCount(type);
    }

    public long getAttentionCount() {
        return bdb.getAttentionCount();
    }

    @Override
    public long getDBSize() {
        return bdb.getSize();
    }
    
    @Override
    public long getIndexSize() {
        return searchEngine.getSize();
    }

    
    /**
     * Internal method to handle sending/queueing item changed events.
     */
    private void itemChanged(ItemImpl item, ItemEvent.ChangeType ctype) {
        //
        // Queue the event for later delivery
        changeEvents.offer(new ChangeEvent(item, ctype));
    }

    /**
     * Adds an item to a itemType->changeType->item map of maps.
     */
    private void addItem(ChangeEvent ce,
            Map<ItemType, Map<ItemEvent.ChangeType, List<ItemImpl>>> eventsByType,
            ItemType type) {

        //
        // Our per-item type map.
        Map<ItemEvent.ChangeType, List<ItemImpl>> eventMap =
                eventsByType.get(type);
        if(eventMap == null) {
            eventMap = new HashMap<ItemEvent.ChangeType, List<ItemImpl>>();
            eventsByType.put(type, eventMap);
        }

        //
        // Our per-change type map and list of items.
        List<ItemImpl> l = eventMap.get(ce.type);
        if(l == null) {
            l = new ArrayList<ItemImpl>();
            eventMap.put(ce.type, l);
        }
        l.add(ce.item);
    }

    /**
     * Sends any queued up change events when the changed data has been flushed.
     * 
     * @param keys the set of keys that are in a partition that has just been
     * dumped to disk.  These are really strings.
     */
    private synchronized void sendChangedEvents(Set<Object> keys) {

        //
        // OK, this is a bit tricky:  we want to send events by item type and
        // then by change type, so we need a map to a map.  We also want to send
        // events of all types, so we'll have a separate map from change type
        // to item for that.
        Map<ItemType, Map<ItemEvent.ChangeType, List<ItemImpl>>> eventsByType =
                new HashMap<ItemType, Map<ItemEvent.ChangeType, List<ItemImpl>>>();

        //
        // Process our stored change events against the keys we were given.
        // We'll try to process however many elements are in the queue at 
        // this point.
        int n = changeEvents.size();
        for(int i = 0; i < n; i++) {
            ChangeEvent ce = changeEvents.poll();

            //
            // We probably shouldn't get null, but just in case.
            if(ce == null) {
                break;
            }

            //
            // If this item is in our set, then process it.
            if(keys.contains(ce.item.getKey())) {

                //
                // Add the autotags.
                List<Scored<String>> autotags =
                        searchEngine.getAutoTags(ce.item.getKey());
                if(autotags != null) {
                    ce.item.setField("autotag", (Serializable) autotags);
                    try {
                        ce.item.storeMap();
                        bdb.putItem(ce.item);
                    } catch(AuraException ae) {
                        logger.log(Level.SEVERE, "Error adding autotags to " +
                                ce.item.getKey(), ae);
                    }
                }
                //
                // Add this item to the all events type map and the per-events
                // type map.
                addItem(ce, eventsByType, null);
                addItem(ce, eventsByType, ce.item.getType());
            } else {
                //
                // Put it back on the queue for the next guy.
                changeEvents.offer(ce);
            }
        }

        //
        // For each type for which there is at least one listener:
        for(ItemType itemType : listenerMap.keySet()) {

            Map<ItemEvent.ChangeType, List<ItemImpl>> te =
                    eventsByType.get(itemType);
            if(te == null) {
                continue;
            }

            //
            // Send the events of each change type to each of the listeners.
            for(Map.Entry<ItemEvent.ChangeType, List<ItemImpl>> e : te.entrySet()) {
                for(ItemListener il : listenerMap.get(itemType)) {
                    try {
                        il.itemChanged(new ItemEvent(e.getValue().
                                toArray(new ItemImpl[0]), e.getKey()));
                    } catch(RemoteException ex) {
                        logger.log(Level.SEVERE, "Error sending change events",
                                ex);
                    }
                }
            }
        }
    }

    /**
     * Internal method to handle sending/queueing item created events.
     */
    private void itemCreated(ItemImpl item) {
        //
        // Queue up this item to be sent out
        createEvents.offer(item);
    }

    private void addItem(ItemImpl item, Map<ItemType, List<ItemImpl>> m,
            ItemType type) {
        List<ItemImpl> l = m.get(type);
        if(l == null) {
            l = new ArrayList<ItemImpl>();
            m.put(type, l);
        }
        l.add(item);
    }

    /**
     * Sends any queued up create events
     */
    private synchronized void sendCreatedEvents(Set<Object> keys) {

        Map<ItemType, List<ItemImpl>> newItems =
                new HashMap<ItemType, List<ItemImpl>>();

        //
        // Process the new items we've accumulated, sending events for those
        // that are in our set of keys.  We'll process however much stuff is 
        // on the queue when we get here.
        int n = createEvents.size();
        for(int i = 0; i < n; i++) {
            ItemImpl ie = createEvents.poll();
            if(ie == null) {
                break;
            }
            if(keys.contains(ie.getKey())) {
                //
                // Add the autotags.
                List<Scored<String>> autotags = searchEngine.getAutoTags(ie.getKey());
                if(autotags != null) {
                    ie.setField("autotag", (Serializable) autotags);
                    ie.storeMap();
                    try {
                        bdb.putItem(ie);
                    } catch(AuraException ae) {
                        logger.log(Level.SEVERE, "Error adding autotags to " +
                                ie.getKey(), ae);
                    }
                }
                addItem(ie, newItems, null);
                addItem(ie, newItems, ie.getType());
            } else {
                createEvents.offer(ie);
            }
        }

        //
        // For each type of item for which there is a listener, batch up
        // the items of that type and send them off together.
        for(ItemType itemType : listenerMap.keySet()) {

            List<ItemImpl> l = newItems.get(itemType);
            if(l == null) {
                continue;
            }

            for(Iterator it = listenerMap.get(itemType).iterator(); it.hasNext(); ) {
                ItemListener il = (ItemListener)it.next();
                try {
                    il.itemCreated(new ItemEvent(l.toArray(new ItemImpl[0])));
                } catch(RemoteException ex) {
                    logger.log(Level.SEVERE,"Error sending new item events " +
                            "from BIS. Removing listener.\n(" +
                            ex.getMessage() + ")");
                    it.remove();
                }
            }
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        try {
            cm.shutdown();
            logger.info("Closing BerkeleyItemStore for prefix: " + prefixString);
            close();
            logger.info("Finished closing BerkeleyItemStore");
        } catch(AuraException ae) {
            logger.severe("Error closing item store" + ae);
            ae.printStackTrace();
        }
    }

    public void partitionAdded(SearchEngine e, Set<Object> keys) {
        sendCreatedEvents(keys);
        sendChangedEvents(keys);
    }
    
    /**
     * Track data upon entering a method
     * @param name the name of the stat associated with the method
     * @param state the state needed for tracking
     */
    protected void enter(StatName name, StatState state) {
        enter(name, state, "");
    }
    
    /**
     * Track data upon entering a method
     * @param name the name of the stat associated with the method
     * @param state the state needed for tracking
     * @param extra further string data to append to the log line
     */
    protected void enter(StatName name, StatState state, String extra) {
        state.timer.start();
        state.name = name;
        if(logger.isLoggable(Level.FINER) && toLog.contains(name)) {
            logger.finer(String.format("rep %s T%s enter %s %s",
                                       prefixString,
                                       Thread.currentThread().getId(),
                                       name,
                                       extra));
        }
    }
    
    /**
     * Track data upon exiting from a method
     * @param state the state needed for tracking
     */
    protected void exit(StatState state) {
        exit(state, "");
    }
    
    /**
     * Track data upon exiting from a method
     * @param state the state needed for tracking
     * @param extra further string data to append to the log line
     */
    protected void exit(StatState state, String extra) {
        state.timer.stop();
        if (logger.isLoggable(Level.FINE) && toLog.contains(state.name)) {
            logger.fine(String.format("rep %s T%s exit %s after %.3f %s",
                                      prefixString,
                                      Thread.currentThread().getId(),
                                      state.name,
                                      state.timer.getTimeMillis(),
                                      extra));
        }
        statInvocationCounts[state.name.ordinal()].addAndGet(state.count);
    }
    
    protected class StatState {
        NanoWatch timer = new NanoWatch();
        int count = 1;
        StatName name;
    }
    
    
    class StatSender extends TimerTask {
        @Override
        public void run() {
            //
            // Send each stat
            for (StatName name : StatName.values()) {
                int num = statInvocationCounts[name.ordinal()].getAndSet(0);
                sendStat(name.toString(), num, num);
            }
        }
        
        /**
         * Internal method to send an updated stat to the stat server.
         * Only sends if n > 0.
         */
        private void sendStat(String statName, int incr, int n) {
            if (n <= 0) {
                return;
            }
            
            if (statService != null) {
                try {
                    statService.incr("Rep-" + getPrefix() + "-" + statName,
                                     incr, n);
                } catch (RemoteException e) {
                    logger.finer("Failed to notify stat server for stat "
                                 + statName);
                }
            }
        }
    }
    
}
