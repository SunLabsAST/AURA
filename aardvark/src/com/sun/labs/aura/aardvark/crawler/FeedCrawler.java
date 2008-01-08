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
import com.sun.labs.util.props.Configurable;
import java.net.URL;

/**
 * Interface for a feed crawler
 */
public interface FeedCrawler extends Configurable {

    /**
     * Starts crawling the feeds
     */
    void start();

    /**
     * Stops crawling the feeds
     */
    void stop();

    /**
     * Create a feed for a given url
     * @param feedUrl the url
     * @return the feed
     */
    public Feed createFeed(URL feedUrl);

    /**
     * Crawls all of the feeds once. This method is suitable for use
     * in testing, when a single threaded crawl is desireable.
     * @throws AuraException
     */
	public void crawlAllFeeds() throws AuraException ;

    /**
     * Crawls a single feed. This method is used 
     * in testing, only
     * @throws AuraException
     */
	public void crawlFeed(Feed feed) throws AuraException ;

    /**
     * Gets the number of feeds errors
     * @return the number of feeds errors
     */
    int getFeedErrorCount();
    /**
     * Gets the number of feeds pulled
     * @return the number of feeds pulled
     */
    int getFeedPullCount();
   
}
