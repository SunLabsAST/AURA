

package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Set;

/**
 * An iterator that 
 */
public class MultiNoDupDBIterator<E> extends MultiDBIterator<E> {
    /**
     * The current element to be returned
     */
    protected E currElem = null;
    
    /**
     * The set of keys or IDs of seel elements.  This will only work for
     * known types: ItemImpl and PersistentAttention
     */
    protected Set seenElems = null;

    /**
     * Constructs a one-iterator-at-a-time MultiDBNoDupIterator.
     * 
     * @param iterators a set of iterators over values to return values from
     */
    public MultiNoDupDBIterator(Collection<DBIterator<E>> iterators) {
        super(iterators);
    }
    
    public boolean hasNext() throws RemoteException {
        if (currElem != null) {
            return true;
        }
        
        Object id = null;
        do {
            if (super.hasNext()) {
                currElem = next();
                if (currElem instanceof Item) {
                    id = ((Item)currElem).getKey();
                } else if (currElem instanceof PersistentAttention) {
                    id = ((PersistentAttention)currElem).getID();
                }
            } else {
                currElem = null;
            }
        } while (currElem != null && seenElems.contains(id));
        
        if (currElem != null) {
            return true;
        }
        return false;
    }
    
    public E next() throws RemoteException {
        //
        // cue up the next one if need be
        hasNext();
        
        //
        // Now null out the curr and return it
        E next = currElem;
        currElem = null;
        return next;
    }
}
