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

import com.sleepycat.je.DatabaseException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.impl.store.BerkeleyDataWrapper;
import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.minion.util.Getopt;
import com.sun.labs.util.LabsLogFormatter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Rewrites a BDB by reading everything
 */
public class RewriteBDB {

    protected static Logger logger =
            Logger.getLogger(RewriteBDB.class.getName());

    protected BerkeleyDataWrapper source;

    protected BerkeleyDataWrapper destination;

    public RewriteBDB(String source, String destination) throws DatabaseException {
        this(new BerkeleyDataWrapper(source, logger),
                new BerkeleyDataWrapper(destination, logger));
    }

    public RewriteBDB(BerkeleyDataWrapper source,
                      BerkeleyDataWrapper destination) {
        this.source = source;
        this.destination = destination;
    }

    public void migrate() throws AuraException, RemoteException {
        //
        // Migrate all items
        logger.info("Migrating items");
        long itemCnt = 0;
        DBIterator<ItemImpl> items = source.getItemIterator();
        List<ItemImpl> toAdd = new ArrayList<ItemImpl>(1000);
        while(items.hasNext()) {
            toAdd.add(items.next());
            itemCnt++;
            if(toAdd.size() >= 3000) {
                destination.putItems(toAdd);
                logger.info(String.format("put %d items, %d so far", toAdd.size(), itemCnt));
                toAdd.clear();
            }
        }
        if(toAdd.size() >= 0) {
            destination.putItems(toAdd);
            logger.info(String.format("put %d items, %d so far", toAdd.size(),
                                      itemCnt));
            toAdd.clear();
        }

        items.close();
        logger.info("Migrated " + itemCnt + " items");

        //
        // Migrate all attention
        logger.info("Migrating attentions");
        long attnCnt = 0;
        DBIterator<Attention> attns =
                source.getAttentionIterator(new AttentionConfig());
        List<PersistentAttention> aa = new ArrayList<PersistentAttention>();
        while(attns.hasNext()) {
            PersistentAttention pa = (PersistentAttention) attns.next();
            attnCnt++;
            if(pa.getID() == 1) {
                logger.info(String.format("skipping: %s", pa));
                continue;
            }
            aa.add(pa);
            if(aa.size() >= 10000) {
                destination.putAttention(aa);
                logger.info(String.format("put %d attentions, %d so far",
                                          aa.size(),
                                          attnCnt));
                aa.clear();
            }
        }
        if(aa.size() >= 0) {
            destination.putAttention(aa);
            logger.info(String.format("put %d attentions, %d so far",
                                      aa.size(),
                                      attnCnt));
            aa.clear();
        }
        attns.close();
        logger.info("Migrated " + attnCnt + " attentions");

        //
        // Migrate field definitions
        Map<String, FieldDescription> fds = source.getFieldDescriptions();
        for(FieldDescription fd : fds.values()) {
            destination.defineField(fd.getName(),
                                    fd.getType(), fd.getCapabilities());
        }
        logger.info("Migrated field descriptions");
    }

    public void close() throws AuraException {
        source.close();
        destination.close();
    }

    public static void usage() {
        System.out.println(
                "RewriteBDB -s <src dbenv> [-s <src dbenv> ...] -d <dest dbenv>");
    }

    public static void main(String[] args) throws Exception {
        //
        // Use the labs format logging.
        for(Handler h : Logger.getLogger("").getHandlers()) {
            h.setLevel(Level.ALL);
            h.setFormatter(new LabsLogFormatter());
            try {
                h.setEncoding("utf-8");
            } catch(Exception ex) {
                logger.severe("Error setting output encoding");
            }
        }

        Logger.getLogger("").setLevel(Level.FINEST);

        String flags = "s:d:";
        List<String> sources = new ArrayList<String>();
        String dest = null;

        Getopt opt = new Getopt(args, flags);
        int i;
        while((i = opt.getopt()) != -1) {
            switch(i) {
                case 's':
                    sources.add(opt.optArg);
                    break;
                case 'd':
                    dest = opt.optArg;
                    break;
            }
        }
        if(sources.size() == 0 || dest == null) {
            usage();
            return;
        }

        BerkeleyDataWrapper destination = null;
        try {
            logger.info(String.format("Opening destination %s", dest));
            destination = new BerkeleyDataWrapper(dest, logger);
        } catch(DatabaseException ex) {
            logger.log(Level.SEVERE, "Error opening destination", ex);
            return;
        }


        for(String src : sources) {
            try {
                logger.info(String.format("Opening source %s", src));
                BerkeleyDataWrapper source =
                        new BerkeleyDataWrapper(src, logger);
                RewriteBDB rwbdb = new RewriteBDB(source, destination);
                rwbdb.migrate();
                source.close();
            } catch(DatabaseException ex) {
                logger.log(Level.SEVERE,
                           String.format("Error opening source %s", src), ex);
            }
        }

        destination.close();
    }
}
