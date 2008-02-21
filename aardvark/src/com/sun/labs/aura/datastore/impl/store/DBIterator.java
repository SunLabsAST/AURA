
package com.sun.labs.aura.datastore.impl.store;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An iterator that is returned from the data store.  This behaves like a
 * normal iterator, but has the requirement that it must be closed when
 * use of it is finished.  A finally clause that closes the iterator should
 * be used wherever this iterator is used.
 */
public interface DBIterator<E> extends Remote {
    /**
     * Returns true if the iteration has more elements
     * 
     * @return true if the iteration has more elements
     */
    public boolean hasNext() throws RemoteException;
    
    /**
     * Returns the next element in the iteration. Calling this method
     * repeatedly until the hasNext() method returns false will return each
     * element in the underlying collection exactly once.
     * 
     * @return the next element in the iteration
     */
    public E next() throws RemoteException;
    
    /**
     * Close this iterator, releasing it from use and freeing the DB resource.
     */
    public void close() throws RemoteException;
}
