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

package com.sun.labs.aura.util;

import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 Item Scheduler is a component that is used to schedule periodic processing of items.   
 * Typically there will be a single item scheduler for each type of item that 
 * needs to be scheduled.  The item scheduler performs the following functions:
 * <ul>
 *    <li> Connects to the item store to retrieve all items of the appropriate type 
 *    <li> Adding a new item listener to the item store so that new items will be recognized and added to the scheduler
 *    <li> Adds the items+delay to an internal delay queue
 *    <li> Provides a remote-accessible method that will retrieve (blocking if necessary) 
 *    the next item in the delay queue when it is ready to be processed
 *    <li> Manages item leases so if an item is not returned to the scheduler after 
 *    a configurable lease time, the item is returned to the delay queue
 * </ul>
 * @author plamere
 */

public interface ItemScheduler extends Component, Remote {
    public final static int SCHEDULE_IMMEDIATELY = 0;
    public final static int SCHEDULE_DEFAULT = -1;

    /**
     * Gets the key of the next item that needs to be scheduled
     * @return the key of the item
     * @throws java.lang.InterruptedException if the call was interrupted
     */
    public String  getNextItemKey() throws InterruptedException, RemoteException;

    /**
     * Release an item that was previously retrieved with 'getNextItemID'
     * @param itemKey the key of the item to release
     * @param secondsUntilNextProcessing the number of seconds until the given
     * item should be made available for processing If zero, then the default scheduling
     * period will be used to schedule the item.
     */
    public void releaseItem(String itemKey, int secondsUntilNextProcessing) throws RemoteException;

    /**
     * Gets the total number of items that are currently being scheduled
     * @return the number of items being scheduled.
     */
    public int size() throws RemoteException;
}
