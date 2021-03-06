/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.util;

import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.impl.store.BerkeleyDataWrapper;
import com.sun.labs.aura.datastore.impl.store.ItemSearchEngine;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.minion.IndexListener;
import com.sun.labs.minion.SearchEngine;
import com.sun.labs.minion.engine.SearchEngineImpl;
import com.sun.labs.minion.indexer.entry.DocKeyEntry;
import com.sun.labs.minion.indexer.partition.InvFileDiskPartition;
import com.sun.labs.minion.util.Getopt;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.SimpleLabsLogFormatter;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Indexes the data in a number of BDBs into a single search index.
 */
public class Reindexer implements IndexListener {

    private ItemSearchEngine engine;

    private Logger logger;

    private BerkeleyDataWrapper bdw;

    public Reindexer(ItemSearchEngine engine, boolean addAutoTags) {
        this.engine = engine;
        if(addAutoTags) {
            this.engine.getSearchEngine().addIndexListener(this);
        }
        logger = Logger.getLogger("");
    }

    public void reindex(String db) throws Exception {
        bdw = new BerkeleyDataWrapper(db, logger);
        reindex(db, bdw);
        bdw.close();
    }

    public void reindex(String db, BerkeleyDataWrapper bdw) throws Exception {
        logger.info("Reindexing " + db);
        engine.redefineFields(bdw);
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
    }

    public void partitionAdded(SearchEngine e, Set<Object> keys) {

        int done = 0;
        for(Object o : keys) {

            String key = o.toString();
            List<Scored<String>> at = getAutoTags(key);
            if(at != null) {
                try {
                    ItemImpl item = bdw.getItem(key);
                    item.setField("autotag", (Serializable) at);
                    item.storeMap();
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
                ((SearchEngineImpl) engine.getSearchEngine()).getDocumentTerm(
                key);
        if(dke == null) {
            //
            // No document by that name here...
            return null;
        }
        List<String> autotags = (List<String>) ((InvFileDiskPartition) dke.
                getPartition()).getFieldStore().
                getSavedFieldData("autotag", dke.getID(), true);
        if(autotags.size() == 0) {

            //
            // No tags.
            return null;
        }
        List<Double> autotagScores = (List<Double>) ((InvFileDiskPartition) dke.
                getPartition()).getFieldStore().
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

        String flags = "ad:b:o:";
        Getopt gopt = new Getopt(args, flags);
        List<String> dbs = new ArrayList<String>();
        String indexDir = null;
        String oldDir = null;
        boolean addAutoTags = false;
        int c;
        while((c = gopt.getopt()) != -1) {
            switch(c) {
                case 'a':
                    addAutoTags = true;
                    break;
                case 'd':
                    indexDir = gopt.optArg;
                    break;
                case 'b':
                    dbs.add(gopt.optArg);
                    break;
                case 'o':
                    oldDir = gopt.optArg;
                    break;
            }
        }

        if(indexDir == null || dbs.size() == 0) {
            System.err.println(
                    "Usage:  Reindexer -a -d <index dir> -b <bdb dir> [-b <bdb dir>] [-o <old dir>]...");
            return;
        }
        //
        // Use the labs format logging.
        Logger logger = Logger.getLogger("");
        for(Handler h : logger.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }
        logger.setLevel(Level.FINE);

        NanoWatch sw = new NanoWatch();
        sw.start();

        ItemSearchEngine engine = new ItemSearchEngine(indexDir,
                "/com/sun/labs/aura/util/resource/reindexConfig.xml");
        Reindexer re = new Reindexer(engine, addAutoTags);
        for(String db : dbs) {
            re.reindex(db);
        }

        engine.shutdown();

        sw.stop();
        logger.info(String.format("Reindex took: %.2fs", sw.getTimeMillis() / 1000));
        if(oldDir == null) {
            return;
        }

        if(oldDir.equals(indexDir)) {
            System.err.println("Can't move new directory to old!");
            return;
        }

        File newf = new File(indexDir);
        File oldf = new File(oldDir);
        File savef = new File(oldf.getParentFile(), "save.idx");
        if(oldf.exists() && oldf.isDirectory()) {
            boolean ret = oldf.renameTo(savef);
            if(!ret) {
                System.err.println("Unable to move " + oldf + " to " + savef);
                return;
            }
            ret = newf.renameTo(oldf);
            if(!ret) {
                System.err.println("Unable to move " + newf + " to " + oldf);
            }
        }
    }
}
