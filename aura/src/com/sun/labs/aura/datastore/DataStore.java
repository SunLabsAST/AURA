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

import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.datastore.impl.store.ItemSearch;
import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This interface represents access to the entire data in the data store.
 * It provides access to the database style ItemStore interface and the
 * search engine style ItemSearch interface.
 */
public interface DataStore extends ItemStore, ItemSearch, Component, Remote {

    /**
     * Indicates that the data store is ready to begin operation.  This will be
     * true when all of the partition clusters have their replicants available.
     * @return <code>true</code> if the data store is ready to begin processing 
     * data, <code>false</code> otherwise.
     * @throws java.rmi.RemoteException
     */
    public boolean ready() throws RemoteException;

    public void registerPartitionCluster(PartitionCluster pc)
            throws RemoteException;

    public Replicant getReplicant(String prefix) throws RemoteException;

    /**
     * Register that a partition cluster split has occurred.  This will update
     * the trie with the new partitions.
     * 
     * @param zeroChild the new left/zero prefix child
     * @param oneChild the new right/one prefix child
     */
    public void registerPartitionSplit(PartitionCluster zeroChild,
            PartitionCluster oneChild)
            throws RemoteException;

    public List<String> getPrefixes() throws RemoteException;
}
