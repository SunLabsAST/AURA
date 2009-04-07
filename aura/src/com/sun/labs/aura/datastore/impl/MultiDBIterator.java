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

package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.DBIterator;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

/**
 * An iterator over multiple DBIterators.  The iterator may be a simple one
 * that iterates over each iterator's items in order, or a more complex one
 * that, given a comparator, iterates over the entire set's items in order.
 */
public class MultiDBIterator<E> implements DBIterator {

    /**
     * The set of iterators that we're iterating over in the case of
     * a simple one iterator at a time approach.
     */
    protected Collection<DBIterator<E>> iterators;
    
    /**
     * The iterator over the set of iterators in the simple one iterator at
     * a time case.
     */
    protected Iterator<DBIterator<E>> itIt;
    
    /**
     * The iterator over the current values in the one iterator at a time
     * approach.
     */
    protected DBIterator<E> currValIt;
    
    /**
     * Constructs a one-iterator-at-a-time MultiDBIterator.
     * 
     * @param iterators a set of iterators over values to return values from
     */
    public MultiDBIterator(Collection<DBIterator<E>> iterators) {
        this.iterators = iterators;
        itIt = iterators.iterator();
        currValIt = itIt.next();
    }
    
    public boolean hasNext() throws RemoteException {
        if (iterators != null) {
            if (currValIt.hasNext()) {
                return true;
            }

            if (itIt.hasNext()) {
                while (itIt.hasNext() && !currValIt.hasNext()) {
                    currValIt = itIt.next();
                }
                return currValIt.hasNext();
            }
        }
        return false;
    }
    
    public E next() throws RemoteException {
        if (iterators != null) {
            if (currValIt.hasNext()) {
                return currValIt.next();
            }

            if (itIt.hasNext()) {
                while (itIt.hasNext() && !currValIt.hasNext()) {
                    currValIt = itIt.next();
                }
                return currValIt.next();
            }
        }
        return null;
    }

    public void close() throws RemoteException {
        //
        // Close each iterator.
        for (DBIterator<E> dbi : iterators) {
            dbi.close();
        }
    }
}
