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

package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.datastore.DBIterator;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.ForwardCursor;
import java.util.Iterator;

/**
 * An iterator that wraps the iterator returned from an EntityCursor and
 * provides a mechanism for closing the cursor.
 */
public class EntityIterator<E> implements DBIterator {
    private ForwardCursor cursor;
    
    protected Iterator<E> it;
    
    private Transaction txn;
    
    /**
     * An empty iterator
     */
    public EntityIterator() {
        it = null;
        cursor = null;
        txn = null;
    }
    
    public EntityIterator(ForwardCursor<E> cursor) {
        this.cursor = cursor;
        it = cursor.iterator();
    }
    
    public EntityIterator(ForwardCursor<E> cursor, Transaction txn) {
        this(cursor);
        this.txn = txn;
    }
    
    public boolean hasNext() {
        if (it != null) {
            return it.hasNext();
        }
        return false;
    }

    public E next() {
        if (it != null) {
            return it.next();
        }
        return null;
    }

    public void close() {
        try {
            if (cursor != null) {
                cursor.close();
                if (txn != null) {
                    txn.commitNoSync();
                }
            }
        } catch (DatabaseException e) {
            // ???
        }
    }

}
