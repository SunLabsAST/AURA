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
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class RecordSet<K, V> implements Iterable<Record<K, V>>, Iterator<Record<K, V>> {
    boolean sorted = false;
    List<Record<K, V>> recordList = null;
    Record<K, V> nextRecord;
    KeyedInputStream<K, V> input;
    String id;
    
    public RecordSet(KeyedInputStream<K, V> input) {
        this(input, null);
    }
    
    public RecordSet(KeyedInputStream<K, V> input, String id) {
        this.input = input;
        next(); // queue first record
        sorted = input.isSorted();
        this.id = id;
    }
    
    public boolean isSorted() {
        return sorted;
    }
    
    /**
     * Setting true reset any iterators
     * 
     * @param grouped
     */
    public void setSorted(boolean sorted) {
        // if coming from a sorted file or already sorted, don't sort
        if(!this.sorted && sorted) {
            List<Record<K, V>> recordList = new ArrayList<Record<K, V>>();
            for(Record<K, V> record : this) {
                recordList.add(record);
            }
            Collections.sort(recordList);
            this.recordList = recordList;
            this.sorted = true;
        }
    }
    
    /**
     * Non-grouped iterators are not thread safe
     * 
     * @return
     */
    public Iterator<Record<K, V>> iterator() {
        if(recordList != null) {
            return recordList.iterator();
        } else {
            try {
                input.reset();
                next();
                return this;
            } catch (IOException ex) {
                throw new IllegalStateException("Error resetting input");
            }
        }
    }

    public boolean hasNext() {
        if(recordList != null) {
            return !recordList.isEmpty();
        } else {
            return nextRecord != null;
        }  
    }

    public Record<K, V> next() {
        if(recordList != null) {
            throw new ConcurrentModificationException("Sorted sets should be accessed through iterator()");
        }
        Record<K, V> returnRecord = nextRecord;
        try {
            nextRecord = input.read();
        } catch(IOException ioe) {
            nextRecord = null;
        }
        return returnRecord;
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    public String getId() {
        if(id == null) {
            id = Integer.toString(hashCode());
        }
        return id;
    }
}
