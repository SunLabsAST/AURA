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
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.impl.store.BerkeleyDataWrapper;
import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.minion.util.Getopt;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Rewrites a BDB by reading everything
 */
public class RewriteBDB {
    protected Logger logger = Logger.getLogger("");
    
    protected BerkeleyDataWrapper source;
    protected BerkeleyDataWrapper destination;

    public RewriteBDB(String srcDBEnv, String destDBEnv) {
        try {
            source = new BerkeleyDataWrapper(srcDBEnv, logger);
            destination = new BerkeleyDataWrapper(destDBEnv, logger);
        } catch (DatabaseException e) {
            logger.log(Level.SEVERE, "Unable to open DBs", e);
        } catch (AuraException e) {
            logger.log(Level.SEVERE, "Unable to open DBs", e);
        }
    }

    public void migrate() throws AuraException, RemoteException {
        //
        // Migrate all items
        logger.info("Migrating items");
        long itemCnt = 0;
        DBIterator<Item> items = source.getAllIterator(null);
        while (items.hasNext()) {
            destination.putItem((ItemImpl)items.next());
            itemCnt++;
        }
        items.close();
        logger.info("Migrated " + itemCnt + " items");

        //
        // Migrate all attention
        logger.info("Migrating attentions");
        long attnCnt = 0;
        DBIterator<Attention> attns =
                source.getAttentionIterator(new AttentionConfig());
        while (attns.hasNext()) {
            destination.putAttention((PersistentAttention)attns.next());
            attnCnt++;
        }
        attns.close();
        logger.info("Migrated " + attnCnt + " attentions");

        //
        // Migrate field definitions
        Map<String,FieldDescription> fds = source.getFieldDescriptions();
        for (FieldDescription fd : fds.values()) {
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
        System.out.println("RewriteBDB -s <src dbenv> -d <dest dbenv>");
    }

    public static void main(String[] args) throws Exception {
        String flags = "s:d:";
        String src = null;
        String dest = null;

        Getopt opt = new Getopt(args, flags);
        int i;
        while ((i = opt.getopt()) != -1) {
            switch(i) {
                case 's':
                    src = opt.optArg;
                    break;
                case 'd':
                    dest = opt.optArg;
                    break;
            }
        }
        if (src == null || dest == null) {
            usage();
            return;
        }

        RewriteBDB rwbdb = new RewriteBDB(src, dest);
        rwbdb.migrate();
        rwbdb.close();
    }
}
