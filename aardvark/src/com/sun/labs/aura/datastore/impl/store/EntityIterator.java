
package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.datastore.DBIterator;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import java.util.Iterator;

/**
 * An iterator that wraps the iterator returned from an EntityCursor and
 * provides a mechanism for closing the cursor.
 */
public class EntityIterator<E> implements DBIterator {
    private EntityCursor cursor;
    
    private Iterator<E> it;
    
    private Transaction txn;
    
    public EntityIterator(EntityCursor<E> cursor) {
        this.cursor = cursor;
        it = cursor.iterator();
    }
    
    public EntityIterator(EntityCursor<E> cursor, Transaction txn) {
        this(cursor);
        this.txn = txn;
    }
    
    public boolean hasNext() {
        return it.hasNext();
    }

    public E next() {
        return it.next();
    }

    public void close() {
        try {
            cursor.close();
            if (txn != null) {
                txn.commitNoSync();
            }
        } catch (DatabaseException e) {
            // ???
        }
    }

}
