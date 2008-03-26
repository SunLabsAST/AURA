package com.sun.labs.aura.util;

import com.sun.kt.search.Log;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.impl.store.BerkeleyDataWrapper;
import com.sun.labs.aura.datastore.impl.store.ItemSearchEngine;
import com.sun.labs.util.SimpleLabsLogFormatter;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Indexes the data in a number of BDBs into a single search index.
 */
public class Reindexer {
    
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
                "/com/sun/labs/aura/datastore/impl/store/itemSearchEngineConfig.xml");
        
        for(int i = 1; i < args.length; i++) {
            BerkeleyDataWrapper bdw = new BerkeleyDataWrapper(args[i], logger);
            logger.info("Opened: " + args[i]);
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
                                count, total, type, args[i]));
                    }
                }
                if(count % 5000 != 0) {
                    logger.info(String.format("Indexed %d/%d %s from %s", count,
                            total, type, args[i]));
                }
                
            }
            bdw.close();
        }
        
        engine.shutdown();
    }
}
