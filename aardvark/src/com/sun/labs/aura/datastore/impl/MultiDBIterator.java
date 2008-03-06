
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
