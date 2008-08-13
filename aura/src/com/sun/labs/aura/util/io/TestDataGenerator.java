/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
