/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

import java.io.IOException;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class RecordSet<K, V> implements Iterable<Record<K, V>>, Iterator<Record<K, V>> {
    boolean grouped = false;
    PriorityQueue<Record<K, V>> recordHeap = null;
    boolean iterated = false;
    Record<K, V> nextRecord;
    KeyedInputStream<K, V> input;
    
    public RecordSet(KeyedInputStream<K, V> input) {
        this.input = input;
        next(); // queue first record
        iterated = false; // reset multiple iterators flag
        grouped = input.isSorted();
    }
    
    public boolean isGrouped() {
        return grouped;
    }
    
    /**
     * Setting true reset any iterators
     * 
     * @param grouped
     */
    public void setGrouped(boolean grouped) {
        // if coming from a sorted file or already heaped, don't heap
        if(!this.grouped && grouped) {
            PriorityQueue<Record<K, V>> recordHeap =
                    new PriorityQueue<Record<K, V>>();
            for(Record<K, V> record : this) {
                recordHeap.add(record);
            }
            this.recordHeap = recordHeap;
            iterated = false; // reset multiple iterations flag
            this.grouped = true;
        }
    }
    
    /**
     * Non-grouped iterators are not thread safe
     * 
     * @return
     */
    public Iterator<Record<K, V>> iterator() {
        if(recordHeap != null) {
            return recordHeap.iterator();
        } else {
            try {
                input.reset();
                return this;
            } catch (IOException ex) {
                throw new IllegalStateException("Error resetting input");
            }
        }
    }

    public boolean hasNext() {
        if(recordHeap != null) {
            return !recordHeap.isEmpty();
        } else {
            return nextRecord != null;
        }
    }

    public Record<K, V> next() {
        iterated = true;
        Record<K, V> returnRecord;
        if(recordHeap != null) {
            returnRecord = recordHeap.poll();
        } else {
            returnRecord = nextRecord;
            try {
                nextRecord = input.read();
            } catch(IOException ioe) {
                nextRecord = null;
            }
        }
        return returnRecord;
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }
}
