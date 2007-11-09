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
import java.util.concurrent.DelayQueue;

/**
 *
 * @author plamere
 */
public class FeedManager implements Configurable {
    

    private DelayQueue<Feed> feedQueue = new DelayQueue<Feed>();


    public Feed getNextFeedForRefresh() throws InterruptedException {
        return feedQueue.take();
    }
    
    public void returnFeed(Feed feed) {
        addFeed(feed);
    }

    public void addFeed(Feed feed) {
        long nextPullTime = (feed.getConsecutiveErrors() + 1) * minimumFeedDelay;
        feed.setNextPullTime(nextPullTime);
        feedQueue.add(feed);
    }

    @ConfigInteger(defaultValue=60, range={5, 60 * 24 * 7 })
    public static String PROP_MINIMUM_FEED_DELAY_IN_MINUTES = "minimumFeedDelayInMinutes";
    private long minimumFeedDelay = 0L;

    public void newProperties(PropertySheet ps) throws PropertyException {
        minimumFeedDelay = ps.getInt(PROP_MINIMUM_FEED_DELAY_IN_MINUTES) * 60 * 1000L;
    }
}
