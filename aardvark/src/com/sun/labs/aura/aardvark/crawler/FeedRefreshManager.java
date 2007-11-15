/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;

/**
 *
 * @author plamere
 */
public class FeedRefreshManager implements Configurable {
    private DelayQueue<Feed> feedQueue = new DelayQueue<Feed>();
    private Map<String, Feed> feedMap = new HashMap<String, Feed>();

    private int feedCount = 0;


    public Feed getNextFeedForRefresh() throws InterruptedException {
        return feedQueue.take();
    }
    
    public void returnFeed(Feed feed) {
        feed.setNextPullTime(feed.getLastPullTime() + (feed.getConsecutiveErrors() + 1) * minimumFeedDelay);
        feedQueue.add(feed);
    }

    public Feed getFeed(String keyOrLink) {
        return feedMap.get(keyOrLink);
    }

    public void addFeed(Feed feed) {
        feedMap.put(feed.getKey(), feed);
        feedMap.put(feed.getFeedUrl().toExternalForm(), feed);
        feedCount++;
        returnFeed(feed);
    }

    /**
     * Gets the number of feeds
     * @return the number of feeds
     */
    public int getNumFeeds() {
        return feedCount;
    }

    @ConfigInteger(defaultValue=60, range={1, 60 * 24 * 7 })
    public static String PROP_MINIMUM_FEED_DELAY_IN_MINUTES = "minimumFeedDelayInMinutes";
    private long minimumFeedDelay = 0L;

    public void newProperties(PropertySheet ps) throws PropertyException {
        minimumFeedDelay = ps.getInt(PROP_MINIMUM_FEED_DELAY_IN_MINUTES) * 60 * 1000L;
    }
}
