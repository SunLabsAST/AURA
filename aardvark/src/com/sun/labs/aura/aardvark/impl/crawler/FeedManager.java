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
import com.sun.labs.aura.datastore.StoreFactory;
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

    private Set<Thread> runningThreads = Collections.synchronizedSet(new HashSet<Thread>());
    private int feedPullCount = 0;
    private int feedErrorCount = 0;
    private int entryPullCount = 0;
    private Logger logger;
    private long startTime = 0;

    /**
     * Starts crawling all of the feeds
     */
    public void start() {
        startTime = System.currentTimeMillis();
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
        numThreads = ps.getInt(PROP_NUM_THREADS);
        logger = ps.getLogger();
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

    public int getFeedErrorCount() throws RemoteException {
        return feedErrorCount;
    }

    public int getFeedPullCount() throws RemoteException {
        return feedPullCount;
    }

    /**
     * Gets a feed id from the feed scheduler, and pulls it, adding
     * any new feed entries to the item store
     */
    private void crawlFeeds() {
        ItemScheduler myFeedScheduler = feedScheduler;
        DataStore myItemStore = dataStore;
        try {
            while (runningThreads.contains(Thread.currentThread())) {
                try {
                    String key = myFeedScheduler.getNextItemKey();
                    try {
                        Item item = myItemStore.getItem(key);
                        if (item != null) {
                            BlogFeed feed = new BlogFeed(item);
                            crawlFeed(myItemStore, feed);
                        }
                    } finally {
                        feedScheduler.releaseItem(key, 0);
                    }
                } catch (InterruptedException ex) {
                    break;
                } catch (RemoteException ex) {
                    break;
                } catch (AuraException ex) {
                    logger.warning("AuraException in crawler, still trying " + ex.getMessage());
                }
            }
        } finally {
            runningThreads.remove(Thread.currentThread());
            logger.info("Crawling thread shutdown " + runningThreads.size() 
                    + " remaining");
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
            Set<Attention> attentions = myItemStore.getAttentionForTarget(feed.getKey());

            for (BlogEntry entry : entries) {
                entry.flush(myItemStore);
                for (Attention feedAttention : attentions) {
                    Attention.Type userAttentionType =
                            getUserAttentionFromFeedAttention(feedAttention.getType());
                    if (userAttentionType != null) {
                        Attention entryAttention = StoreFactory.newAttention(
                                feedAttention.getSourceKey(), entry.getKey(), userAttentionType);
                        dataStore.attend(entryAttention);
                    }
                }
            }
            entryPullCount += entries.size();
            logger.info(entries.size() + " entries from  " + feed.getURL());
            ok = true;
        } catch (AuraException ex) {
            logger.warning("trouble processing " + feed.getKey() + " " +
                    ex.getMessage());
            feedErrorCount++;
        } catch (RemoteException rx) {
            logger.log(Level.SEVERE, "remote exception processing " + feed.getKey(), rx);
            feedErrorCount++;
        }
        feedPullCount++;
        feed.pulled(ok);
        feed.flush(myItemStore);

        if (feedPullCount % 100 == 0) {
            float mins = (System.currentTimeMillis() - startTime) / (1000.0f * 60.0f);
            float ppm = feedPullCount / mins;
            logger.info("Feeds: " + feedScheduler.size() + " FeedPulls: " + feedPullCount 
                    + " errors: " + feedErrorCount + " entries: " 
                    + entryPullCount + " ppm: " + ppm +
                    " Threads: " + runningThreads.size());
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
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    private DataStore dataStore;
    /**
     * the configurable property for the feed itemScheuler used by this manager
     */
    @ConfigComponent(type = ItemScheduler.class)
    public final static String PROP_FEED_SCHEDULER = "feedScheduler";
    private ItemScheduler feedScheduler;
    /**
     * the configurable property for the number of threads used by this manager
     */
    @ConfigInteger(defaultValue = 10, range = {1, 1000})
    public final static String PROP_NUM_THREADS = "numThreads";
    private int numThreads;
}
