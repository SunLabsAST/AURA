/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.impl.crawler;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.util.ItemScheduler;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.util.StatService;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the set of feed crawling threads
 * @author plamere
 */
public class FeedManager implements AuraService, Configurable {

    private Set<Thread> runningThreads =
            Collections.synchronizedSet(new HashSet<Thread>());
    private Logger logger;
    private int defaultCrawlingPeriod = 60 * 60;
    private long lastPullCount = 0;

    /**
     * Starts crawling all of the feeds
     */
    public void start() {
        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    crawlFeeds();
                }
            };
            t.setDaemon(true);
            t.setName("crawlerThread-" + i);
            runningThreads.add(t);
            t.start();
        }
    }

    /**
     * Stops crawling the feeds
     */
    public void stop() {
        runningThreads.clear();
    }

    /**
     * Reconfigures this component
     * @param ps the property sheet
     * @throws com.sun.labs.util.props.PropertyException
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        feedScheduler = (ItemScheduler) ps.getComponent(PROP_FEED_SCHEDULER);
        statService = (StatService) ps.getComponent(PROP_STAT_SERVICE);
        numThreads = ps.getInt(PROP_NUM_THREADS);
        logger = ps.getLogger();
        try {
            //
            // Create our counters.
            statService.create(COUNTER_ENTRY_PULL_COUNT);
            statService.create(COUNTER_FEED_ERROR_COUNT);
            statService.create(COUNTER_FEED_PULL_COUNT);
        } catch (RemoteException rx) {
            throw new PropertyException(ps.getInstanceName(), PROP_STAT_SERVICE,
                    "Unable to create counters");
        }
    }

    public BlogFeed createFeed(URL feedUrl) throws RemoteException, AuraException {
        BlogFeed feed = new BlogFeed(feedUrl.toExternalForm(), "");
        feed.flush(dataStore);
        return feed;
    }

    public void crawlAllFeeds() throws AuraException, RemoteException {
        Set<Item> feeds = dataStore.getAll(Item.ItemType.FEED);
        for (Item ifeed : feeds) {
            BlogFeed feed = new BlogFeed(ifeed);
            crawlFeed(feed);
        }
    }

    public void crawlFeed(BlogFeed feed) throws AuraException, RemoteException {
        crawlFeed(dataStore, feed);
    }

    /**
     * Gets a feed id from the feed scheduler, and pulls it, adding
     * any new feed entries to the item store
     */
    private void crawlFeeds() {
        ItemScheduler myFeedScheduler = feedScheduler;
        DataStore myItemStore = dataStore;
        String key = null;
        try {
            while (runningThreads.contains(Thread.currentThread())) {
                try {
                    key = myFeedScheduler.getNextItemKey();
                    int nextCrawl = defaultCrawlingPeriod;
                    try {
                        Item item = myItemStore.getItem(key);
                        if (item != null) {
                            if (item.getType() == ItemType.FEED) {
                                BlogFeed feed = new BlogFeed(item);
                                if (needsCrawl(feed)) {
                                    crawlFeed(myItemStore, feed);
                                    nextCrawl += feed.getNumConsecutiveErrors() *
                                            defaultCrawlingPeriod;
                                }
                            } else {
                                logger.warning("Expected FEED type, found " +
                                        item.getType() + " for " + item.getKey());
                            }
                        }
                    } finally {
                        feedScheduler.releaseItem(key, nextCrawl);
                    }
                } catch (InterruptedException ex) {
                    break;
                } catch (RemoteException ex) {
                    break;
                } catch (AuraException ex) {
                    logger.warning("AuraException in crawler, still trying " +
                            ex.getMessage());
                } catch (Throwable ex) {
                    logger.warning("Unexpected exception when crawling feed " +
                            key + " exception: " + ex.getMessage());
                }
            }
        } finally {
            runningThreads.remove(Thread.currentThread());
            logger.info("Crawling thread shutdown " + runningThreads.size() +
                    " remaining");
        }
    }

    /**
     * Crawls a single feed
     * @param myItemStore the item store where new entries should be desposited
     * @param feed the feed to crawl
     * @throws java.rmi.RemoteException
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    private void crawlFeed(DataStore myItemStore, BlogFeed feed) throws RemoteException, AuraException {
        boolean ok = false;
        try {
            // pull the feeds, and add the appropriate attention data 
            // for each entry

            List<BlogEntry> entries = FeedUtils.processFeed(feed);
            Set<Attention> attentions =
                    myItemStore.getAttentionForTarget(feed.getKey());

            for (BlogEntry entry : entries) {
                if (dataStore.getItem(entry.getKey()) == null) {
                    entry.flush(myItemStore);
                    for (Attention feedAttention : attentions) {
                        Attention.Type userAttentionType =
                                getUserAttentionFromFeedAttention(feedAttention.getType());
                        if (userAttentionType != null) {
                            Attention entryAttention = StoreFactory.newAttention(
                                    feedAttention.getSourceKey(), entry.getKey(),
                                    userAttentionType);
                            dataStore.attend(entryAttention);
                        }
                    }
                }
            }
            statService.incr(COUNTER_ENTRY_PULL_COUNT, entries.size());
            logger.info(entries.size() + " entries from  " + feed.getURL());
            ok = true;
        } catch (AuraException ex) {
            logger.warning("trouble processing " + feed.getKey() + " " +
                    ex.getMessage());
            statService.incr(COUNTER_FEED_ERROR_COUNT, 1);
        } catch (RemoteException rx) {
            logger.log(Level.SEVERE, "remote exception processing " +
                    feed.getKey(), rx);
            statService.incr(COUNTER_FEED_ERROR_COUNT, 1);
        }
        long feedPullCount = statService.incr(COUNTER_FEED_PULL_COUNT);
        feed.pulled(ok);
        feed.flush(myItemStore);

        //
        // Fetch stats from the stats server.
        if (feedPullCount - lastPullCount > 100) {
            logger.info(String.format("Feeds: %d pulls: %d errors: %d entries: %d ppm: %.3f epm: %.3f threads: %d",
                    feedScheduler.size(), statService.get(COUNTER_FEED_PULL_COUNT),
                    statService.get(COUNTER_FEED_ERROR_COUNT),
                    statService.get(COUNTER_ENTRY_PULL_COUNT),
                    statService.getAveragePerMinute(COUNTER_FEED_PULL_COUNT),
                    statService.getAveragePerMinute(COUNTER_ENTRY_PULL_COUNT),
                    runningThreads.size()));
            lastPullCount = feedPullCount;
        }
    }

    /**
     * Determines if a feed needs to be crawled
     * @param feed the feed to check
     * @return if the feed hasn't been crawled 
     */
    private boolean needsCrawl(BlogFeed feed) {
        long now = System.currentTimeMillis();
        return ((now - feed.getLastPullTime()) >  defaultCrawlingPeriod * 1000);
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
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    private DataStore dataStore;
    /**
     * The statistics service that we'll use to count things.
     */
    @ConfigComponent(type = com.sun.labs.aura.util.StatService.class)
    public static final String PROP_STAT_SERVICE = "statService";
    private StatService statService;
    public static final String COUNTER_ENTRY_PULL_COUNT = "fm.entryPullCount";
    public static final String COUNTER_FEED_ERROR_COUNT = "fm.feedErrorCount";
    public static final String COUNTER_FEED_PULL_COUNT = "fm.feedPullCount";
    /**
     * the configurable property for the feed itemScheuler used by this manager
     */
    @ConfigComponent(type = ItemScheduler.class)
    public final static String PROP_FEED_SCHEDULER = "feedScheduler";
    private ItemScheduler feedScheduler;
    /**
     * the configurable property for the number of threads used by this manager
     */
    @ConfigInteger(defaultValue = 10, range = {0, 1000})
    public final static String PROP_NUM_THREADS = "numThreads";
    private int numThreads;
}
