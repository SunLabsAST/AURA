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

package com.sun.labs.aura.util.io;

import com.sun.labs.minion.SearchEngineFactory;
import com.sun.labs.minion.engine.SearchEngineImpl;
import com.sun.labs.minion.indexer.dictionary.DictionaryIterator;
import com.sun.labs.minion.indexer.entry.QueryEntry;
import com.sun.labs.minion.indexer.partition.DiskPartition;
import com.sun.labs.minion.indexer.partition.InvFileDiskPartition;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TestDataGenerator {
    
    public static void main(String[] args) throws Exception {
        SearchEngineImpl e = (SearchEngineImpl) SearchEngineFactory.getSearchEngine(args[0]);
        int fn = 1;
        for(DiskPartition dp : e.getPM().getActivePartitions()) {
            System.out.println("Processing " + dp);
            DictionaryIterator di = ((InvFileDiskPartition) dp).getFieldIterator("tag");
            if(di == null) {
                continue;
            }
            
            //
            // We'll randomize things by hashing them.
            Map<String,Integer> m = new HashMap<String,Integer>();
            while(di.hasNext()) {
                QueryEntry qe = di.next();
                m.put(qe.getName().toString(), qe.getN());
            }
            KeyedOutputStream<String, Integer> output =
                    new StructuredKeyedOutputStream<String, Integer>(String.format("%s-%04d",
                    args[1], fn++), false);
            for(Map.Entry<String,Integer> ent : m.entrySet()) {
                output.write(ent.getKey(), ent.getValue());
            }
            System.out.println("Done " + dp + " wrote " + m.size() + " records");
            output.close();
        }
        e.close();
    }
}
