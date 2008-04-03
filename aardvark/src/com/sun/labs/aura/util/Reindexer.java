package com.sun.labs.aura.util;

import com.sun.kt.search.IndexListener;
import com.sun.kt.search.Log;
import com.sun.kt.search.SearchEngine;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.impl.store.BerkeleyDataWrapper;
import com.sun.labs.aura.datastore.impl.store.ItemSearchEngine;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.util.SimpleLabsLogFormatter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngnova.engine.SearchEngineImpl;
import ngnova.indexer.entry.DocKeyEntry;
import ngnova.indexer.partition.InvFileDiskPartition;
import ngnova.util.Getopt;

/**
 * Indexes the data in a number of BDBs into a single search index.
 */
public class Reindexer implements IndexListener {

    private ItemSearchEngine engine;

    private Logger logger;

    private BerkeleyDataWrapper bdw;

    public Reindexer(ItemSearchEngine engine) {
        this.engine = engine;
        this.engine.getSearchEngine().addIndexListener(this);
        logger = Logger.getLogger("");
    }

    public void reindex(String db) throws Exception {
        bdw = new BerkeleyDataWrapper(db, logger);
        logger.info("Opened: " + db);
        for(ItemType type : ItemType.values()) {
            int count = 0;
            long total = bdw.getItemCount(type);
            DBIterator<Item> iter = bdw.getItemsAddedSince(type, 0);
            while(iter.hasNext()) {
                Item item = iter.next();
                engine.index(item);
                count++;
                if(count % 5000 == 0) {
                    logger.info(String.format("Indexed %d/%d %s from %s",
                            count, total, type, db));
                }
            }
            iter.close();
            if(count % 5000 != 0) {
                logger.info(String.format("Indexed %d/%d %s from %s", count,
                        total, type, db));
            }

        }
        engine.getSearchEngine().flush();  
        bdw.close();
    }

    public void partitionAdded(SearchEngine e,
            Set<Object> keys) {
        
        int done = 0;
        for(Object o : keys) {

            String key = o.toString();
            List<Scored<String>> at = getAutoTags(key);
            if(at != null) {
                ItemImpl item = bdw.getItem(key);
                item.getMap().put("autotag", (Serializable) at);
                item.storeMap();
                try {
                    bdw.putItem(item);
                } catch(AuraException ae) {
                    logger.log(Level.SEVERE, "Error adding tags to " + key, ae);
                }
            }
            done++;
            if(done % 5000 == 0) {
                logger.info(String.format("Autotagged %d/%d", done, keys.size()));
            }
        }
        if(done % 5000 != 0) {
            logger.info(String.format("Autotagged %d/%d", done, keys.size()));
        }
    }

    public List<Scored<String>> getAutoTags(String key) {
        DocKeyEntry dke =
                ((SearchEngineImpl) engine.getSearchEngine()).getDocumentTerm(key);
        if(dke == null) {
            //
            // No document by that name here...
            return null;
        }
        List<String> autotags = (List<String>) ((InvFileDiskPartition) dke.getPartition()).getFieldStore().
                getSavedFieldData("autotag", dke.getID(), true);
        if(autotags.size() == 0) {

            //
            // No tags.
            return null;
        }
        List<Double> autotagScores = (List<Double>) ((InvFileDiskPartition) dke.getPartition()).getFieldStore().
                getSavedFieldData("autotag-score", dke.getID(), true);
        if(autotags.size() != autotagScores.size()) {
            logger.warning("Mismatched autotags and scores: " + autotags + " " +
                    autotagScores);
        }
        List<Scored<String>> ret = new ArrayList<Scored<String>>();
        int lim = Math.min(autotags.size(), autotagScores.size());
        for(int i = 0; i < lim; i++) {
            ret.add(new Scored<String>(autotags.get(i), autotagScores.get(i)));
        }
        return ret;
    }

    public static void main(String[] args) throws Exception {

        if(args.length < 2) {
            System.err.println("Usage:  Reindexer <index dir> <bdb dir> [<bdb dir>]...");
            return;
        }

        //
        // Use the labs format logging.
        Logger logger = Logger.getLogger("");
        for(Handler h : logger.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }

        Log.setLogger(logger);
        Log.setLevel(3);

        ItemSearchEngine engine = new ItemSearchEngine(args[0],
                "/com/sun/labs/aura/util/resource/reindexConfig.xml");

        Reindexer re = new Reindexer(engine);
        for(int i = 1; i < args.length; i++) {
            re.reindex(args[i]);
        }

        engine.shutdown();
    }
}
