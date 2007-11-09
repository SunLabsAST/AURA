/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.crawler;

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

    public Feed createFeed(URL feedUrl);

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
