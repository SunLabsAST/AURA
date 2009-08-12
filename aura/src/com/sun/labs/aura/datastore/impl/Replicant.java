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

import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.aura.datastore.impl.store.LowLevelSearch;
import com.sun.labs.aura.datastore.impl.store.persist.FieldDescription;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * A replicant used for storing and retrieving data to/from disk in the
 * datastore.
 */
public interface Replicant extends ItemStore, LowLevelSearch, Component, Remote {
    public enum StatName {
        ATTEND,
        EXPLAIN_SIM,
        FIND_SIM,
        FIND_SIM_AUTOTAGS,
        GET_ALL,
        GET_ALL_ITR,
        GET_ATTN,
        GET_ATTN_CNT,
        GET_ATTN_ITR,
        GET_ATTN_SINCE,
        GET_ATTN_SINCE_CNT,
        GET_ATTN_SINCE_ITR,
        GET_AUTOTAGGED,
        GET_DV_KEY,
        GET_DV_CLOUD,
        GET_EXPLAIN,
        GET_ITEM,
        GET_ITEMS,
        GET_ITEMS_SINCE,
        GET_LAST_ATTN,
        GET_SCORED_ITEMS,
        GET_TOP_AUTOTAG_TERMS,
        GET_TOP_TERMS,
        GET_TOP_TERM_COUNTS,
        GET_TOP_VALUES,
        GET_USER,
        NEW_ITEM,
        PUT_ITEM,
        UPDATE_ITEM,
        QUERY,
        PROCESS_ATTN
    }

    /**
     * Get the prefix code for the objects in this partition cluster.
     * 
     * @return the prefix code
     */
    public DSBitSet getPrefix() throws RemoteException;

    /**
     * Sets the prefix code for this replicant.  This should be used after
     * a split.
     * 
     * @param newPrefix the prefix code
     */
    public void setPrefix(DSBitSet newPrefix) throws RemoteException;

    /**
     * Delete the attention that is related to the given item either as a 
     * source or a target.  (Generally: isSrc should be true for users and
     * otherwise false)
     * 
     * @param itemKey the item key that we're removing attention for
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void removeAttention(String itemKey)
            throws AuraException, RemoteException;
    
    /**
     * Delete a set of attention by ID
     * 
     * @param ids a list of attention ids to delete
     */
    public void deleteAttention(List<Long> ids)
            throws AuraException, RemoteException;
    
    /**
     * Gets the on-disk size of the database component of the replicant in
     * bytes.
     * 
     * @return the size in bytes
     */
    public long getDBSize() throws RemoteException;
    
    /**
     * Gets the on-disk size of the index component of the replicant in bytes.
     * 
     * @return the size in bytes
     */
    public long getIndexSize() throws RemoteException;

    /**
     * Gets a map describing the defined fields
     * 
     * @return the field descriptions in this replicant
     */
    public Map<String,FieldDescription> getFieldDescriptions()
            throws RemoteException;
    
    /**
     * Returns an enumset containing the currently logged stat names
     * @return
     */
    public EnumSet<StatName> getLoggedStats() throws RemoteException;
    
    /**
     * Sets the logged stat names
     */
    public void setLoggedStats(EnumSet<StatName> loggedStats) throws RemoteException;

    /**
     * Get a string representation of the log level
     */
    public String getLogLevel() throws RemoteException;
 
    /**
     * Sets the log level based on the provided string
     */
     public boolean setLogLevel(String logLevel) throws RemoteException;
}
