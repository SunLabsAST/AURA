
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
