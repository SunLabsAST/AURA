/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.impl.crawler;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DBIterator;
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
import com.sun.syndication.feed.synd.SyndFeed;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Date;
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
    private long lastPullCount = 0;
    private boolean started = false;
    private int links;
    private int matchingLinks;
    private long startTime;

    /**
     * Starts crawling all of the feeds
     */
    public void start() {
        started = true;
        startTime = System.currentTimeMillis();
        startFeedCrawlingThreads();
        startFeedDiscoveryThreads();
    }

    /**
     * Starts the feed crawling threads
     */
    private void startFeedCrawlingThreads() {
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
     * Starts the feed discovery threads
     */
    private void startFeedDiscoveryThreads() {
        for (int i = 0; i < numDiscoveryThreads; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    discoverNewFeeds();
                }
            };
            t.setDaemon(true);
            t.setName("discoveryThread-" + i);
            runningThreads.add(t);
            t.start();
        }
    }

    /**
     * Stops crawling the feeds
     */
    public void stop() {
        started = false;
        runningThreads.clear();
    }

    /**
     * Reconfigures this component
     * @param ps the property sheet
     * @throws com.sun.labs.util.props.PropertyException
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        feedScheduler = (FeedScheduler) ps.getComponent(PROP_FEED_SCHEDULER);
        statService = (StatService) ps.getComponent(PROP_STAT_SERVICE);
        numThreads = ps.getInt(PROP_NUM_THREADS);
        numDiscoveryThreads = ps.getInt(PROP_NUM_DISCOVERY_THREADS);
        logger = ps.getLogger();
        defaultCrawlingPeriod = ps.getInt(PROP_CRAWLING_PERIOD);

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

        if (started) {
            start();
        }
    }

    public void crawlAllFeeds() throws AuraException, RemoteException {
        List<Item> feeds = dataStore.getAll(Item.ItemType.FEED);
        for (Item ifeed : feeds) {
            BlogFeed feed = new BlogFeed(ifeed);
            crawlFeed(feed);
        }
    }

    public void crawlFeed(BlogFeed feed) throws AuraException, RemoteException {
        crawlFeed(dataStore, feed, null);
    }

    /**
     * Gets a feed id from the feed scheduler, and pulls it, adding
     * any new feed entries to the item store. This method is typcally run
     * in its own thread and will run continuously.  It is aggresive about
     * catching exceptions since we never want this thread to stop during normal 
     * crawling.
     */
    private void crawlFeeds() {
        FeedScheduler myFeedScheduler = feedScheduler;
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
                                    processIncomingLinkAttentionData(myItemStore, feed);
                                    crawlFeed(myItemStore, feed, null);
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
                    logger.warning("RemoteException " + ex.getMessage());
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
            logger.warning("Crawling thread shutdown " + runningThreads.size() +
                    " remaining");
        }
    }

    /**
     * Discovers new feeds. This method (typically run in its own thread), will
     * take a url for discovery from the feedScheduler url queue and (if possible)
     * finds the best feed asociated with the URL.  If the feed is not already
     * enrolled, it is added to the datastore
     * 
     */
    private void discoverNewFeeds() {
        FeedScheduler myFeedScheduler = feedScheduler;
        String key = null;
        try {
            while (runningThreads.contains(Thread.currentThread())) {
                try {
                    URLForDiscovery urlForDiscovery = myFeedScheduler.getUrlForDiscovery();
                    discoverFeed(urlForDiscovery);
                } catch (InterruptedException ex) {
                    break;
                } catch (RemoteException ex) {
                    logger.warning("RemoteException " + ex.getMessage());
                    break;
                } catch (Throwable ex) {
                    logger.warning("Unexpected exception when crawling feed " +
                            key + " exception: " + ex.getMessage());
                }
            }
        } finally {
            runningThreads.remove(Thread.currentThread());
            logger.warning("Crawling thread shutdown " + runningThreads.size() +
                    " remaining");
        }
    }

    private void discoverFeed(URLForDiscovery urlForDiscovery) throws IOException, RemoteException {
        // if the url has already been discovered, then we can apply the attention and leave

        try {
            Attention attn = urlForDiscovery.getAttention();
            if (attn != null) {
                Item destItem = dataStore.getItem(urlForDiscovery.getUrl());
                if (destItem != null) {
                    Item src = dataStore.getItem(attn.getSourceKey());
                    if (src != null) {
                        addAttention(src, destItem, attn.getType());
                    }
                    logger.info("short circuit discovery for " + destItem.getKey());
                    return;
                }
            }

            URL feedUrl = FeedUtils.findBestFeed(urlForDiscovery.getUrl());
            if (feedUrl != null) {
                SyndFeed syndFeed = FeedUtils.readFeed(feedUrl);
                String canonicalLink = syndFeed.getLink();
                if (canonicalLink == null) {
                    canonicalLink = urlForDiscovery.getUrl();
                }

                BlogFeed feed = null;
                Item item = dataStore.getItem(canonicalLink);
                if (item == null) {
                    item = StoreFactory.newItem(ItemType.FEED, canonicalLink, syndFeed.getTitle());
                    feed = new BlogFeed(item);
                    feed.setPullLink(feedUrl.toExternalForm());
                    feed.setNumIncomingLinks(1);
                    feed.setNumStarredEntries(0);
                    logger.info("discovery: added new feed " + feed.getPullLink() + " for " + urlForDiscovery);
                }


                if (attn != null) {
                    Item src = dataStore.getItem(attn.getSourceKey());
                    if (src != null) {
                        Item discoveredItem = dataStore.getItem(urlForDiscovery.getUrl());
                        if (discoveredItem != null) {
                            addAttention(src, discoveredItem, attn.getType());
                        } else {
                            addAttention(src, item, attn.getType());
                        }
                    }
                }

                if (feed != null) {
                    crawlFeed(dataStore, feed, syndFeed);
                }
            }
        } catch (IOException e) {
        //System.out.println("          Trouble crawling feed " + feedUrl + " : " + e);
        } catch (AuraException e) {
            System.out.println("          Trouble accessing database while processing  feed " +
                    urlForDiscovery.getUrl() + " : " + e);
        }
    }

    /**
     * Crawls a single feed
     * @param myItemStore the item store where new entries should be desposited
     * @param feed the feed to crawl
     * @param syndFeed the previously pulled feed data (or null if the feed needs to be pulled)
     * @throws java.rmi.RemoteException
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    private void crawlFeed(DataStore myItemStore, BlogFeed feed, SyndFeed syndFeed) throws RemoteException, AuraException {
        boolean ok = false;
        try {

            // pull the feeds, and add the appropriate attention data 
            // for each discoveredEntry

            logger.info("Crawling feed " + feed.getKey() + ((syndFeed == null ? " (full)" : "")));
            List<BlogEntry> entries = FeedUtils.processFeed(feed, syndFeed);
            List<Attention> attentions =
                    myItemStore.getAttentionForTarget(feed.getKey());

            int newEntries = 0;
            for (BlogEntry entry : entries) {
                if (dataStore.getItem(entry.getKey()) == null) {
                    newEntries++;
                    // the discoveredEntry gets the authority of the feed
                    entry.setAuthority(feed.getAuthority());
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

                    // extract the anchors and add attention for those
                    List<Anchor> anchors = FeedUtils.extractAnchors(entry.getContent());
                    for (Anchor anchor : anchors) {
                        links++;
                        Item target = dataStore.getItem(anchor.getDestURL());
                        if (target != null) {
                            addAttention(entry.getItem(), target, Attention.Type.LINKS_TO);
                        } else {
                            queueAttention(entry.getItem(), anchor.getDestURL(), Attention.Type.LINKS_TO);
                        }

                    }
                }
            }
            logger.info("Links found " + links + " matching " + matchingLinks);
            logger.info(newEntries + " new entries from  " + feed.getCannonicalURL());
            statService.incr(COUNTER_ENTRY_PULL_COUNT, newEntries);
            ok =
                    true;
        } catch (AuraException ex) {
            logger.warning("trouble processing " + feed.getKey() + " " +
                    ex.getMessage());
            statService.incr(COUNTER_FEED_ERROR_COUNT, 1);
        } catch (RemoteException rx) {
            logger.log(Level.SEVERE, "remote exception processing " +
                    feed.getKey(), rx);
            statService.incr(COUNTER_FEED_ERROR_COUNT, 1);
        } catch (IOException ex) {
            logger.info("I/O trouble  " + feed.getKey() + " " + ex.getMessage());
            statService.incr(COUNTER_FEED_ERROR_COUNT, 1);
        }

        logger.fine(Thread.currentThread().getName() + " stats-flush");
        long feedPullCount = statService.incr(COUNTER_FEED_PULL_COUNT);
        logger.fine(Thread.currentThread().getName() + " feed-flush");
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
            lastPullCount =
                    feedPullCount;
        }

    }

    private void queueAttention(Item src, String targetURL, Attention.Type type) throws RemoteException {
        Attention feedAttention = StoreFactory.newAttention(src.getKey(), null, type);
        feedScheduler.addUrlForDiscovery(
                new URLForDiscovery(targetURL, URLForDiscovery.Priority.NORMAL, feedAttention));
    }

    private void addAttention(Item src, Item target, Attention.Type type) throws AuraException, RemoteException {

        // if the attention is a LINKS_TO type, we do a bit of extra work:
        //   - no self links
        //   - add links from entries to entries and from feeds to feeds
        if (type == Attention.Type.LINKS_TO && src.getType() == ItemType.BLOGENTRY) {

            BlogEntry srcEntry = new BlogEntry(src);

            if (target.getType() == ItemType.BLOGENTRY) {
                // don't create self links
                BlogEntry targetEntry = new BlogEntry(target);
                if (isOkToLink(srcEntry.getFeedKey(), targetEntry.getFeedKey())) {
                    Attention entryAttention = StoreFactory.newAttention(
                            srcEntry.getKey(), targetEntry.getKey(), Attention.Type.LINKS_TO);
                    dataStore.attend(entryAttention);

                    if (!isAlreadyLinked(srcEntry.getFeedKey(), targetEntry.getFeedKey())) {
                        Attention feedAttention = StoreFactory.newAttention(
                                srcEntry.getFeedKey(), targetEntry.getFeedKey(), Attention.Type.LINKS_TO);
                        dataStore.attend(feedAttention);
                    }

                }
            } else if (srcEntry.getFeedKey() != null && target.getType() == ItemType.FEED) {
                if (!target.getKey().equals(srcEntry.getFeedKey())) {
                    if (!isAlreadyLinked(srcEntry.getFeedKey(), target.getKey())) {
                        Attention feedAttention = StoreFactory.newAttention(
                                srcEntry.getFeedKey(), target.getKey(), Attention.Type.LINKS_TO);
                        dataStore.attend(feedAttention);
                    }

                }
            }
        } else {
            // not a LINKS_TO from a blogentry attention so just add it
            if (!src.getKey().equals(target.getKey())) {
                Attention feedAttention = StoreFactory.newAttention(
                        src.getKey(), target.getKey(), type);
                dataStore.attend(feedAttention);
            }

        }
    }

    private boolean isOkToLink(String surl1, String surl2) {
        if (surl1 == null || surl2 == null) {
            return false;
        } else {
            try {
                URL url1 = new URL(surl1);
                URL url2 = new URL(surl2);
                return !url1.getHost().equals(url2.getHost());
            } catch (MalformedURLException ex) {
                return false;
            }

        }
    }

    /**
     * Determines if the two items are already linked
     * @param srcKey the source of the potential link
     * @param targetKey the target of the potential link
     * @return true if the items are linked
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    boolean isAlreadyLinked(String srcKey, String targetKey) throws AuraException, RemoteException {
        for (Attention attn : dataStore.getAttentionForTarget(targetKey)) {
            if (attn.getType() == Attention.Type.LINKS_TO) {
                if (srcKey.equals(attn.getSourceKey())) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * For the given feed, update the incoming links field for the feed
     * by counting the new links pointing to this feed since the last pull
     * @param myDataStore the datastore 
     * @param feed the feed to be update
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    private void processIncomingLinkAttentionData(DataStore myDataStore, BlogFeed feed) throws AuraException, RemoteException {
        List<Attention> incoming = myDataStore.getAttentionForTarget(feed.getKey());
        int count = 0;
        if (incoming != null) {
            for (Attention attn : incoming) {
                if (attn.getType() == Attention.Type.LINKS_TO) {
                    count++;
                }
            }
        }
        feed.setNumIncomingLinks(count);
    }

    /**
     * Determines if a feed needs to be crawled
     * @param feed the feed to check
     * @return if the feed hasn't been crawled 
     */
    private boolean needsCrawl(BlogFeed feed) {
        long now = System.currentTimeMillis();
        return ((now - feed.getLastPullTime()) > defaultCrawlingPeriod * 1000);
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
    @ConfigComponent(type = FeedScheduler.class)
    public final static String PROP_FEED_SCHEDULER = "feedScheduler";
    private FeedScheduler feedScheduler;
    /**
     * the configurable property for the number of threads used by this manager
     */
    @ConfigInteger(defaultValue = 10, range = {0, 1000})
    public final static String PROP_NUM_THREADS = "numThreads";
    private int numThreads;
    /**
     * the configurable property for the default carwling period (in seconds)
     */
    @ConfigInteger(defaultValue = 3600, range = {10, 36000})
    public final static String PROP_CRAWLING_PERIOD = "crawlingPeriod";
    private int defaultCrawlingPeriod;
    /**
     * the configurable property for the number of feed discovery threads
     */
    @ConfigInteger(defaultValue = 3, range = {0, 1000})
    public final static String PROP_NUM_DISCOVERY_THREADS = "numDiscoveryThreads";
    private int numDiscoveryThreads;
}
