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
