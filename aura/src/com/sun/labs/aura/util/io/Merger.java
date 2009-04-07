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

import java.io.IOException;
import java.util.List;
import java.util.PriorityQueue;

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
        
        public boolean equals(Object o) {
            if (o instanceof Merger.HE) {
                Merger.HE om = (Merger.HE)o;
                return input.equals(om.input);
            }
            return false;
        }
        
        public int hashCode() {
            return input.hashCode();
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
