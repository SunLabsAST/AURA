/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.aura.aardvark.util.FeedUtils;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.SimpleAttention;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class SimpleFeedCrawler implements FeedCrawler {

    private DelayQueue<DelayedFeed> feedQueue = new DelayQueue<DelayedFeed>();
    private Set<String> hostSet = new HashSet<String>();
    private int feedPullCount = 0;
    private int feedErrorCount = 0;
    /** the configurable property for the itemstore used by this manage     */
    @ConfigComponent(type = ItemStore.class)
    public final static String PROP_ITEM_STORE = "itemStore";
    private ItemStore itemStore;
    /** The minimum time (in minutes) between feed pulls */
    @ConfigInteger(defaultValue = 60, range = {0, 60 * 24 * 7})
    public final static String PROP_MINIMUM_FEED_DELAY_IN_MINUTES = "minimumFeedDelayInMinutes";
    private long minimumFeedDelay = 0L;
    /** Determines if we should validate feeds when added */
    @ConfigBoolean(defaultValue = false)
    public final static String PROP_VALIDATE_ON_ADD = "validateOnAdd";
    private boolean validateOnAdd = false;
    @ConfigBoolean(defaultValue = false)
    public final static String PROP_TEST_MODE = "testMode";
    private boolean testMode = false;
    @ConfigBoolean(defaultValue = true)
    public final static String PROP_AUTO_ENROLL_ASSOCIATED_FEEDS = "autoEnrollAssociatedFeeds";
    private boolean autoEnrollAssociatedFeeds = true;

    // manages the number of crawling threads
    @ConfigInteger(defaultValue = 1, range = {1, 100})
    public final static String PROP_CRAWLING_THREADS = "crawlingThreads";
    private int crawlingThreads = 0;
    private Logger logger;
    private volatile Thread crawler = null;
    private ExecutorService executorService;

    public Feed createFeed(URL feedUrl) {
        try {
            Item item = itemStore.get(feedUrl.toExternalForm());

            Feed feed = null;
            if (item == null) {
                if (validateOnAdd) {
                    FeedUtils.readFeed(feedUrl);
                }
                feed = itemStore.newItem(Feed.class, feedUrl.toExternalForm());
                releaseFeed(feed);
                logger.info("Adding new feed to item store " + feedUrl);
            } else if (!(item instanceof Feed)) {
                logger.warning("URL not associated with a feed " + feedUrl);
                return null;
            } else {
                feed = (Feed) item;
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
        logger.info("Crawler started, processing " + feedQueue.size() + " feeds");
        while (crawler != null) {
            try {
                long feedID = getNextFeedForRefresh(3000L);
                if (crawler != null && feedID > 0) {
                    executorService.execute(new FeedPuller(feedID));
                }
            } catch (InterruptedException ex) {
            }
        }
        logger.info("Crawler finished");
    }

    class FeedPuller implements Runnable {

        private long feedID;

        FeedPuller(long feedID) {
            this.feedID = feedID;
        }

        public void run() {
            if (crawler != null) {
                Feed feed = null;
                try {
                    feed = (Feed) itemStore.get(feedID);
                    processFeed(feed);
                } catch (AuraException ex) {
                    logger.warning("Can't pull feed " + feedID);
                } catch (Throwable t) {
                    logger.severe("Something bad happened " + t);
                } finally {
                    if (feed != null) {
                        releaseFeed(feed);
                    }
                }
            }
        }
    }

    public void crawlAllFeeds() throws AuraException {
        if (crawler == null) {
            Set<Feed> feeds = itemStore.getAll(Feed.class);
            for (Feed feed : feeds) {
                processFeed(feed);
            }
        } else {
            throw new AuraException("Can't crawlAllFeeds when crawler is already running");
        }
    }

    /**
     * Adds all the feeds found in the item store to the feed queue
     */
    private void populateFeedQueue() {
        try {
            feedQueue.clear();
            Set<Feed> feeds = itemStore.getAll(Feed.class);
            for (Feed feed : feeds) {
                enqueueFeed(feed);
            }
        } catch (AuraException ex) {
            logger.severe("Can't get feeds from the item store " + ex.getMessage());
        }
    }

    /**
     * Processes a feed
     * @param feed the feed to be processed
     */
    private void processFeed(Feed feed) {
        List<Entry> entries = pullFeed(feed);
        if (entries != null) {
            logger.info("Processed " + entries.size() + " new entries from " + feed.getKey());
            if (autoEnrollAssociatedFeeds) { // TODO Disabled for now
                enrollAssociatedFeeds(entries);
            }
        }
    }

    public void crawlFeed(Feed feed) throws AuraException {
        processFeed(feed);
    }

    /**
     * Pull the feed, adding the entries and the associated attention data
     * to the item store. 
     * @param feed the feed to pull
     * 
     */
    private List<Entry> pullFeed(Feed feed) {
        List<Entry> entries = null;
        boolean ok = false;
        try {
            // pull the feeds, and add the appropriate attention data 
            // for each entry

            feed.setLastPullTime(System.currentTimeMillis());
            entries = FeedUtils.processFeed(itemStore, feed);
            for (Attention feedAttention : feed.getAttentionData()) {
                Attention.Type userAttentionType = getUserAttentionFromFeedAttention(feedAttention.getType());
                if (userAttentionType != null) {
                    for (Entry entry : entries) {
                        Attention entryAttention = new SimpleAttention(feedAttention.getUserID(),
                                entry.getID(), userAttentionType, System.currentTimeMillis());
                        itemStore.attend(entryAttention);
                    }
                }
            }
            ok = true;
        } catch (AuraException ex) {
            logger.warning("trouble processing " + feed.getKey() + " " + ex.getMessage());
            feedErrorCount++;
        }
        feedPullCount++;

        if (ok) {
            feed.setNumPulls(feed.getNumPulls() + 1);
            feed.setNumConsecutiveErrors(0);
        } else {
            feed.setNumErrors(feed.getNumErrors() + 1);
            feed.setNumConsecutiveErrors(feed.getNumConsecutiveErrors() + 1);
        }

        return entries;
    }

    /**
     * Examine each entry and try to find any associated feeds for the entry
     * and enroll tem
     * @param entries the entries to process
     */
    private void enrollAssociatedFeeds(List<Entry> entries) {
        // if the feed is an aggregation of other feeds then we
        // can try to find the associated feeds
        if (FeedUtils.isAggregatedFeed(entries)) {
            for (Entry entry : entries) {
                String link = null;
                try {
                    link = entry.getSyndEntry().getUri();
                    URL url = new URL(link);
                    if (!linkHasBeenProcessed(url)) {
                        List<URL> feeds = FeedUtils.findFeeds(url);
                        for (URL f : feeds) {
                            createFeed(f);
                        }
                        processedLink(url);
                    }
                } catch (MalformedURLException ex) {
                    logger.info("Bad URL, Skipping " + link);
                } catch (IOException ex) {
                    logger.info("Problem loading, Skipping " + link);
                } catch (AuraException ex) {
                    logger.info("Problem getting SyndEntry, Skipping " + link);
                }
            }
        }
    }

    /**
     * Given a feed attention return the associated user attention
     * @param feedAttentionType the feed attention type
     * @return the user attention type
     */
    private Attention.Type getUserAttentionFromFeedAttention(Attention.Type feedAttentionType) {
        Attention.Type userAttentionType = null;
        if (feedAttentionType == Attention.Type.STARRED_FEED) {
            userAttentionType = Attention.Type.STARRED;
        } else if (feedAttentionType == Attention.Type.SUBSCRIBED_FEED) {
            userAttentionType = Attention.Type.SUBSCRIBED;
        } else if (feedAttentionType == Attention.Type.DISLIKED_FEED) {
            userAttentionType = Attention.Type.DISLIKED;
        }
        return userAttentionType;
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
        if (!testMode && crawler == null) {

            executorService = Executors.newFixedThreadPool(crawlingThreads);

            populateFeedQueue();

            crawler = new Thread() {

                @Override
                public void run() {
                    crawler();
                }
            };
            crawler.setName("aardvark-feed-crawler");
            crawler.setDaemon(true);
            crawler.start();
        }
    }

    public synchronized void stop() {
        if (crawler != null) {
            try {
                Thread t = crawler;
                crawler = null;
                t.join();
                executorService.shutdown();
                executorService.awaitTermination(120, TimeUnit.SECONDS);
                logger.info("Crawling stopped");
            } catch (InterruptedException ex) {
                logger.warning("interrupted while stopping");
            }
        }
    }

    /**
     * Polls for the next feed to refresh
     * @param maxWait the maximum time in milliseconds to wait
     * @return the feed or 0 if we timed out
     * @throws java.lang.InterruptedException
     */
    public long getNextFeedForRefresh(long maxWait) throws InterruptedException {
        if (false) {
            DelayedFeed dfeed = feedQueue.peek();
            if (dfeed != null) {
                logger.info("Feed queue - size " + feedQueue.size() + " next in " + dfeed.getDelay(TimeUnit.MILLISECONDS) + " ms");
            }

        }
        DelayedFeed dfeed = feedQueue.poll(maxWait, TimeUnit.MILLISECONDS);

        if (dfeed != null) {
            return dfeed.getFeedID();
        } else {
            return 0;
        }
    }

    /**
     * Release a previously retrieve feed. 
     * @param feed the feed to release
     */
    public void releaseFeed(Feed feed) {
        try {
            itemStore.put(feed);
        } catch (AuraException ex) {
            logger.warning("Can't store feed " + feed.getKey());
        } finally {
            enqueueFeed(feed);
        }
    }

    /**
     * Release a previously retrieve feed. 
     * @param feed the feed to release
     */
    public void enqueueFeed(Feed feed) {
        feedQueue.add(new DelayedFeed(feed));
    }

    public int getFeedErrorCount() {
        return feedErrorCount;
    }

    public int getFeedPullCount() {
        return feedPullCount;
    }

    public synchronized void newProperties(PropertySheet ps) throws PropertyException {
        itemStore = (ItemStore) ps.getComponent(PROP_ITEM_STORE);
        minimumFeedDelay = ps.getInt(PROP_MINIMUM_FEED_DELAY_IN_MINUTES) * 60 * 1000L;
        validateOnAdd = ps.getBoolean(PROP_VALIDATE_ON_ADD);
        testMode = ps.getBoolean(PROP_TEST_MODE);
        crawlingThreads = ps.getInt(PROP_CRAWLING_THREADS);
        autoEnrollAssociatedFeeds = ps.getBoolean(PROP_AUTO_ENROLL_ASSOCIATED_FEEDS);
        logger = ps.getLogger();
        // BUG: until the config logLevel is fixed
        logger.setLevel(Level.SEVERE);

    }

    /**
     * Represents a feed with its delay until the feed should be pulled next.
     */
    class DelayedFeed implements Delayed {

        private long feedID;
        private long nextPullTime;

        public DelayedFeed(Feed feed) {
            this.feedID = feed.getID();
            nextPullTime = feed.getLastPullTime() + (feed.getNumConsecutiveErrors() + 1) * minimumFeedDelay;
        }

        /**
         * Gets the feed represented by this DelayedFeed
         * @return the feed
         * @throws com.sun.labs.aura.aardvark.util.AuraException
         */
        public long getFeedID() {
            return feedID;
        }

        public long getDelay(TimeUnit unit) {
            return unit.convert(nextPullTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        public int compareTo(Delayed o) {
            long result = getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS);
            return result < 0 ? -1 : result > 0 ? 1 : 0;
        }
    }
}
