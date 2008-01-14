/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.Component;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for a feed crawler
 */
public interface FeedCrawler extends Component, Remote {

    /**
     * Starts crawling the feeds
     */
    void start() throws RemoteException;

    /**
     * Stops crawling the feeds
     */
    void stop() throws RemoteException;

    /**
     * Create a feed for a given url
     * @param feedUrl the url
     * @return the feed
     */
    public Feed createFeed(URL feedUrl) throws RemoteException;

    /**
     * Crawls all of the feeds once. This method is suitable for use
     * in testing, when a single threaded crawl is desireable.
     * @throws AuraException
     */
    public void crawlAllFeeds() throws AuraException, RemoteException;

    /**
     * Crawls a single feed. This method is used 
     * in testing, only
     * @throws AuraException
     */
    public void crawlFeed(Feed feed) throws AuraException, RemoteException;

    /**
     * Gets the number of feeds errors
     * @return the number of feeds errors
     */
    int getFeedErrorCount() throws RemoteException;

    /**
     * Gets the number of feeds pulled
     * @return the number of feeds pulled
     */
    int getFeedPullCount() throws RemoteException;
}
