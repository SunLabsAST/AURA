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
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.impl.store.persist.PersistentAttention;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
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

    @SuppressWarnings(value="DMI_CALLING_NEXT_FROM_HASNEXT",
                      justification="hasNext calls next and caches the result")
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
