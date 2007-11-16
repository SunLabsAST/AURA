/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.SimpleAttention;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import com.sun.syndication.feed.synd.SyndFeed;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class FeedManager implements FeedCrawler {

    private Set<String> hostSet = new HashSet<String>();
    private int feedPullCount = 0;
    private int feedErrorCount = 0;

    /**
     * the configurable property for the UserRefreshManager used by this manager
     */
    @ConfigComponent(type = FeedRefreshManager.class)
    public final static String PROP_FEED_REFRESH_MANAGER = "feedRefreshManager";
    private FeedRefreshManager feedRefreshManager;

    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = ItemStore.class)
    public final static String PROP_ITEM_STORE = "itemStore";
    private ItemStore itemStore;
    private Logger logger;
    private volatile Thread crawler;

    public Feed createFeed(URL feedUrl) {
        try {
            Feed feed = feedRefreshManager.getFeed(feedUrl.toExternalForm());
            if (feed == null) {
                SyndFeed synFeed = FeedUtils.readFeed(feedUrl);
                String key = synFeed.getLink();
                feed = new Feed(key, feedUrl);
                feedRefreshManager.addFeed(feed);
            }
            return feed;
        } catch (AuraException ex) {
            logger.warning("bad feed " + feedUrl);
            return null;
        }
    }

    /**
     * Crawls the feeds
     */
    private void crawler() {
        while (crawler != null) {

            Feed feed = null;
            try {
                logger.info("waiting for next feed");
                feed = feedRefreshManager.getNextFeedForRefresh(3000L);
                if (feed != null) {
                    logger.info("processing " + feed.getKey());
                    processFeed(feed);
                }
            } catch (InterruptedException ex) {
            } catch (Throwable ex) {
                logger.severe("trouble in the feed crawler " + ex.getMessage());
            } finally {
                if (feed != null) {
                    feedRefreshManager.releaseFeed(feed);
                }
            }
        }
        logger.info("Crawler finished");
    }

    /**
     * Processes a feed
     * @param feed the feed to be processed
     */
    private void processFeed(Feed feed) {
        boolean ok = false;
        try {
            List<Entry> entries = FeedUtils.processFeed(itemStore, feed.getFeedUrl());
            for (UserAttention userAttention : feed.getInterestedUsers()) {
                for (Entry entry : entries) {
                    Attention attention = new SimpleAttention(userAttention.getUser(),
                            entry, userAttention.getType());
                    itemStore.attend(attention);
                }
            }
            ok = true;

            // if the feed is an aggregation of other feeds then we 
            // can try to find the associated feeds

            if (FeedUtils.isAggregatedFeed(entries)) {
                for (Entry entry : entries) {
                    String link = entry.getSyndEntry().getUri();
                    URL url = new URL(link);
                    if (!linkHasBeenProcessed(url)) {
                        List<URL> feeds = FeedUtils.findFeeds(url);
                        for (URL f : feeds) {
                            createFeed(f);
                        }
                        processedLink(url);
                    }
                }
            }
        } catch (IOException ex) {
            logger.severe("I/O error while processing " + feed.getFeedUrl());
            feedErrorCount++;
        } catch (AuraException ex) {
            logger.warning("trouble proceesing" + feed.getFeedUrl());
            feedErrorCount++;
        }
        feedPullCount++;
        feed.setStatus(ok);
    }

    
    /**
     * Determines if this link has already been processed
     * @param url the link to test
     * @return true if the link has already been processed
     */
    private boolean linkHasBeenProcessed(URL url) {
        return hostSet.contains(url.getHost());
    }

    /**
     * Sets the link has processed
     * @param url the link
     */
    private void processedLink(URL url) {
        hostSet.add(url.getHost());
    }

    public synchronized void start() {
        logger.info("starting");
        if (crawler == null) {
            crawler = new Thread() {

        @Override
                public void run() {
                            crawler();
                        }
                    };
            crawler.setName("aardvark-feed-crawler");
            crawler.start();
        }
    }

    public synchronized void stop() {
        logger.info("stopping");
        if (crawler != null) {
            Thread t = crawler;
            crawler = null;
        //t.interrupt();
        }
    }

    public int getFeedErrorCount() {
        return feedErrorCount;
    }

    public int getFeedPullCount() {
        return feedPullCount;
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        feedRefreshManager = (FeedRefreshManager) ps.getComponent(PROP_FEED_REFRESH_MANAGER);
        itemStore = (ItemStore) ps.getComponent(PROP_ITEM_STORE);
        logger = ps.getLogger();
    }

    public int getNumFeeds() {
        return feedRefreshManager.getNumFeeds();
    }
}
