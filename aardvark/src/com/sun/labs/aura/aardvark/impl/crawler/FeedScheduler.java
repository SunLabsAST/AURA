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

package com.sun.labs.aura.aardvark.impl.crawler;

import com.sun.labs.aura.util.ItemScheduler;
import java.rmi.RemoteException;

/**
 *
 * @author plamere
 */
public interface FeedScheduler extends ItemScheduler {
    /**
     * Adds a url for discovery. The URL may (eventually) be crawled and inspected
     * to see if it is a feed or is assocaited with a feed. If a feed is found and
     * it is not already included in the datastore, it will be added to the store
     * 
     * @param ufd the url and associated info for discovery
     * @throws java.rmi.RemoteException
     */
    public void addUrlForDiscovery(URLForDiscovery ufd) throws RemoteException;


    /**
     * Gets a url for discovery
     * @return the url for discovery
     * @throws java.lang.InterruptedException
     * @throws java.rmi.RemoteException
     */
    public URLForDiscovery getUrlForDiscovery() throws InterruptedException, RemoteException;
    
    /**
     * Deterimines if the url is crawlable, via a robots.txt check
     * @param surl the url 
     * @return if the url is crawlable
     * @throws java.rmi.RemoteException
     */
    public boolean isCrawlable(String surl) throws RemoteException;
}
