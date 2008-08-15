/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

import com.sun.labs.util.SimpleLabsLogFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Given a list of RecordSets, the elements with common keys are grouped into
 * a list of lists (one list per RecordSet) and passed to the merge function.
 * 
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public abstract class GroupedMerger<K, V> implements RecordMerger<K, V> {
    static Logger logger = Logger.getLogger(GroupedMerger.class.getName());
    
    static {
         for(Handler handler : logger.getHandlers()) {
            handler.setFormatter(new SimpleLabsLogFormatter());
        }
    }
    
    public void merge(Collection<RecordSet<K, V>> inputs,
                      KeyedOutputStream output) throws IOException {
        HashMap<Iterator<Record<K, V>>, Record<K, V>> queuedElements =
                new HashMap<Iterator<Record<K, V>>, Record<K, V>>();
        for(RecordSet<K, V> recordSet : inputs) {
            // setGrouped doesn't quite work right with the iterators, so this
            // needs to be first until that is fixed
            recordSet.setSorted(true);
            Iterator<Record<K, V>> iterator = recordSet.iterator();
            if(iterator.hasNext()) {
                queuedElements.put(iterator, iterator.next());
            }
        }
        
        while(!queuedElements.isEmpty()) {
            // create list of lists of records
            List<List<Record<K, V>>> recordSets = new ArrayList<List<Record<K, V>>>();

            // Iterate over all the iterators and each time remove the one at
            // the top of the sort order. Skimming the top each time will prevent
            // the queues from getting out of order.
            K minKey = Collections.min(queuedElements.values()).getKey();
            
            // Check each iterator and if it has records with the min key then
            // create a list of them
            Stack<Iterator<Record<K, V>>> finishedIterators =
                    new Stack<Iterator<Record<K, V>>>();
            for(Iterator<Record<K, V>> iterator : queuedElements.keySet()) {
                if(queuedElements.get(iterator).getKey().equals(minKey)) {
                    List<Record<K, V>> elements = new ArrayList<Record<K, V>>();
                    
                    // Start the process off with the queued record which is
                    // known to match the minKey
                    Record<K, V> nextRecord = queuedElements.get(iterator);
                    do {
                        elements.add(nextRecord);
                        if(iterator.hasNext()) {
                            // To save lookup time, only put the record in the
                            // queuedElements map at the end of pulling the
                            // set out of the iterator
                            nextRecord = iterator.next();
                        } else {
                            // If the iterator has run out, remove it from
                            // consideration
                            nextRecord = null;
                            finishedIterators.add(iterator);
                        }
                    } while(nextRecord != null && nextRecord.getKey().equals(minKey));
                    queuedElements.put(iterator, nextRecord);
                    recordSets.add(elements);
                }
            }
            logger.info(minKey + ": Generated " + recordSets.size() + " sets:" +
                        " Finished: " + finishedIterators.size());
            for(Iterator<Record<K, V>> iterator : finishedIterators) {
                queuedElements.remove(iterator);
            }
            mergeList(recordSets, output);
        }
    }

    public abstract void mergeList(List<List<Record<K, V>>> inputs,
                                   KeyedOutputStream output) throws IOException;
}
