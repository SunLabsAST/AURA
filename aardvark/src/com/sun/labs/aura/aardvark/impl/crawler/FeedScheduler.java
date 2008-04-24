/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
