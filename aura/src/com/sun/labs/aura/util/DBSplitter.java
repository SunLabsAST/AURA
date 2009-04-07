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

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.impl.BinaryTrie;
import com.sun.labs.aura.datastore.impl.DSBitSet;
import com.sun.labs.aura.datastore.impl.store.BerkeleyDataWrapper;
import com.sun.labs.aura.datastore.impl.store.persist.ItemImpl;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import com.sun.labs.minion.util.Getopt;
import com.sun.labs.minion.util.StopWatch;
import java.io.File;
import java.util.logging.Logger;

/**
 * Used to split a database node into N nodes (where N should be a power of 2).
 */
public class DBSplitter {
    private int numSegments;
    private String initialHashBits;
    private String fsNamePrefix;
    private Logger logger = Logger.getLogger("");
    
    public DBSplitter(int numSegments, String initialHashBits,
            String fsNamePrefix) {
        //
        // verify that numSegments is a power of two
        if (Integer.highestOneBit(numSegments)
                != Integer.lowestOneBit(numSegments)) {
            throw new RuntimeException("numSegments must be a power of two");
        }
                this.numSegments = numSegments;
        this.initialHashBits = initialHashBits;
        this.fsNamePrefix = fsNamePrefix;

    }
    
    public static String[] getNewPrefixes(int numSplits, String srcPrefix) {
        String[] results = new String[numSplits];
        int numNewBits = Integer.toString(numSplits, 2).length() - 1;
        for (int i = 0; i < numSplits; i++) {
            //
            // Get a string representation of the new (leftmost) bits of the
            // hash
            DSBitSet front = DSBitSet.parse(i);
            front.setPrefixLength(numNewBits);
            
            //
            // Construct a full length hash string (based on the new total
            // length) including the old rightmost bits
            results[i] = front.toString() + srcPrefix;
        }
        return results;
    }
    
    /**
     * Splits the database into numSegments parts.
     */
    public void split() throws Exception {
        //
        // Open the database that has the initial hash string
        BerkeleyDataWrapper bdbRoot = new BerkeleyDataWrapper(
                fsNamePrefix + initialHashBits + "/" +
                initialHashBits + "/db", logger);
        
        //
        // Open the target database environments, putting them into a
        // trie by hash code value
        BinaryTrie<BerkeleyDataWrapper> trie =
                new BinaryTrie<BerkeleyDataWrapper>();
        String[] prefixes = getNewPrefixes(numSegments, initialHashBits);
        for (String prefix : prefixes) {            
            //
            // Open a db and put it in the trie
            BerkeleyDataWrapper curr = new BerkeleyDataWrapper(
                    fsNamePrefix + prefix + "/" +
                    prefix + "/db", logger);
            DSBitSet currPrefix = DSBitSet.parse(prefix);
            trie.add(curr, currPrefix);
        }
        
        //
        // Iterate over the entire database writing the data out to each
        // of the children.  Start with the items, then do the attention
        StopWatch sw = new StopWatch();
        sw.start();
        DBIterator<ItemImpl> itemIt = bdbRoot.getItemIterator();
        System.out.print("About to transfer " + bdbRoot.getItemCount(null) +
                " items........");
        while(itemIt.hasNext()) {
            ItemImpl item = itemIt.next();
            BerkeleyDataWrapper target =
                    trie.get(DSBitSet.parse(item.hashCode()));
            target.putItem(item);
        }
        itemIt.close();
        System.out.println("Done");
        
        DBIterator<Attention> attnIt = bdbRoot.getAttentionAddedSince(0);
        System.out.print("About to transfer " + bdbRoot.getAttentionCount(null) +
                " attentions.....");
        while (attnIt.hasNext()) {
            Attention attn = attnIt.next();
            BerkeleyDataWrapper target = 
                    trie.get(DSBitSet.parse(attn.hashCode()));
            target.putAttention((PersistentAttention)attn);
        }
        attnIt.close();
        System.out.println("Done");
        sw.stop();
        System.out.println("Transfer finished in " + sw.getTime() / 1000 + " seconds");
        
        bdbRoot.close();
        long numItems = 0;
        long numAttns = 0;
        for (BerkeleyDataWrapper bdb : trie.getAll()) {
            numItems += bdb.getItemCount(null);
            numAttns += bdb.getAttentionCount(null);
            bdb.close();
        }
        System.out.println("Accounted for " + numItems + " items and " +
                numAttns + " attentions in new splits.");
    }
    
    public static void main(String[] args) throws Exception {
        String flags = "n:h:p:";
        Getopt gopt = new Getopt(args, flags);
        int numSplits = 0;
        String srcHash = null;
        String namePrefix = null;
        int c;
        while((c = gopt.getopt()) != -1) {
            switch(c) {
                case 'n':
                    numSplits = Integer.parseInt(gopt.optArg);
                    break;
                case 'h':
                    srcHash = gopt.optArg;
                    break;
                case 'p':
                    namePrefix = gopt.optArg;
                    break;
            }
        }

        if (numSplits == 0 || srcHash == null || namePrefix == null) {
            System.out.println("DBSplitter -n <numSplits> -h <existingHash> -p <dirPrefix>");
            return;
        }
        
        //
        // Check to see that the src db dir exists in the current dir
        File dbDir = new File(namePrefix + srcHash);
        if (!dbDir.exists() || !dbDir.isDirectory()) {
            System.out.println("prefix and hash (" + namePrefix + srcHash +
                    ") must combine into a valid directory");
            return;
        }
        
        DBSplitter dbs = new DBSplitter(numSplits, srcHash, namePrefix);
        dbs.split();
    }
}
