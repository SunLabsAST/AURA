
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
        }
    }

    public void migrate() throws AuraException, RemoteException {
        //
        // Migrate all items
        logger.info("Migrating items");
        long itemCnt = 0;
        DBIterator<ItemImpl> items = source.getItemIterator();
        while (items.hasNext()) {
            destination.putItem(items.next());
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
            destination.defineField(null, fd.getName(),
                    fd.getCapabilities(), fd.getType());
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
