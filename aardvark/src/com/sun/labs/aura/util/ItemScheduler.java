/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES..
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
}
