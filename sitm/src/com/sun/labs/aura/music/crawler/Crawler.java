/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.crawler;

import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author plamere
 */

public interface Crawler extends Component, Remote {
    /**
     * Updates (via a recrawl), the item with the given id. The item should already exist
     * @param id the item of interest
     * @throws java.rmi.RemoteException
     */
    public void update(String id) throws AuraException, RemoteException;

    /**
     * Adds a new item with the given ID to the crawler
     * @param id the ID of the new item
     * @throws java.rmi.RemoteException
     */
    public void add(String newID) throws AuraException, RemoteException;
}
