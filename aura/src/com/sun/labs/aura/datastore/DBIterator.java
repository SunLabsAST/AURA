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

package com.sun.labs.aura.datastore;

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
