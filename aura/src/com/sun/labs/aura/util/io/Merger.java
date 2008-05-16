package com.sun.labs.aura.util.io;

import java.io.IOException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Logger;

/**
 * Merges a number of keyed input files into a single keyed output file.
 * @param K the type of the key for the files
 * @param V the type of the value for the files
 */
public class Merger<K,V> {
    
    public void merge(List<KeyedInputStream<K,V>> inputs, KeyedOutputStream<K,V> output, Combiner<V> c) throws IOException {
        
        //
        // Make a heap of the elements.
        PriorityQueue<HE> q = new PriorityQueue<HE>();
        for(KeyedInputStream<K,V> input : inputs) {
            HE h = new HE(input);
            if(h.next()) {
                q.offer(h);
            }
        }
        
        //
        // Process ye heap!
        if(c != null) {
            processHeap(q, output, c);
        } else {
            processHeap(q, output);
        }
    }
    
    /**
     * Processes the heap, combining the values using the given combiner.
     */
    private void processHeap(PriorityQueue<HE> q, KeyedOutputStream<K,V> output, Combiner<V> c) throws IOException {
        while(q.size() > 0) {
            HE top = q.peek();
            K ck = top.curr.getKey();
            V cv = null;
            while(top != null && top.curr.getKey().equals(ck)) {
                top = q.poll();
                cv = c.combine(cv, top.curr.getValue());
                if(top.next()) {
                    q.offer(top);
                }
            }
            output.write(ck, cv);
        }
    }
    
    /**
     * Processes the heap, without combining the results of values with the
     * same key.  Values with the same key will be written into successive 
     * records in the file.
     */
    private void processHeap(PriorityQueue<HE> q, KeyedOutputStream<K,V> output) throws IOException {
        while(q.size() > 0) {
            HE he = q.poll();
            output.write(he.getCurr());
            if(he.next()) {
                q.offer(he);
            }
        }
    }
    
    /**
     * An element for the merge heap.  The input streams are buffered, so we
     * don't need to worry about buffering here.
     */
    protected class HE implements Comparable<HE> {
        private KeyedInputStream<K,V> input;
        
        private Record<K,V> curr;
        
        public HE(KeyedInputStream<K,V> input) {
            this.input = input;
        }

        public int compareTo(Merger<K, V>.HE o) {
            return curr.compareTo(o.curr);
        }
        
        public Record<K,V> getCurr() {
            return curr;
        }
        
        public boolean next() throws IOException {
            curr = input.read();
            return curr != null;
            
        }
        
    }

}
