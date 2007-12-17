
package com.sun.labs.aura.aardvark.impl.bdb.store;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sun.labs.aura.aardvark.store.DBIterator;
import java.util.Iterator;

/**
 * An iterator that wraps the iterator returned from an EntityCursor and
 * provides a mechanism for closing the cursor.
 */
public class EntityIterator<E> implements DBIterator {
    private EntityCursor cursor;
    
    private Iterator<E> it;
    
    public EntityIterator(EntityCursor<E> cursor) {
        this.cursor = cursor;
        it = cursor.iterator();
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
        } catch (DatabaseException e) {
            // ???
        }
    }

}
