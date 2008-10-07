package com.sun.labs.aura.util;

import com.sun.labs.aura.datastore.impl.store.BerkeleyDataWrapper;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gives a count of the number of items and attentions in a BDB
 */
public class DBCount {

    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.OFF);
        long totalItems = 0;
        long totalAttns = 0;
        for (String dbEnv : args) {
            BerkeleyDataWrapper bdb = new BerkeleyDataWrapper(dbEnv, logger);
            long currItems = bdb.getItemCount(null);
            long currAttns = bdb.getAttentionCount(null);
            System.out.println(dbEnv + " has " + currItems + " items and " +
                    currAttns + " attentions.");
            totalItems += currItems;
            totalAttns += currAttns;
        }
        if (args.length > 1) {
            System.out.println("Total of " + totalItems + " items and " +
                    totalAttns + " attentions.");
        }
    }
}
