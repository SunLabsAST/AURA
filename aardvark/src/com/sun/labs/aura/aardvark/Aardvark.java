/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.  Which I did, which is why this text is here.
 */
package com.sun.labs.aura.aardvark;

import com.sun.labs.aura.aardvark.crawler.FeedCrawler;
import com.sun.labs.aura.aardvark.crawler.FeedUtils;
import com.sun.labs.aura.aardvark.recommender.RecommenderManager;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.ItemStoreStats;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * A Blog Recommender
 */
public class Aardvark implements Configurable {

    private final static String VERSION = "aardvark version 0.1";
    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = ItemStore.class)
    public final static String PROP_ITEM_STORE = "itemStore";
    private ItemStore itemStore;

    /**
     * the configurable property for the FeedCrawler used by this manager
     */
    @ConfigComponent(type = FeedCrawler.class)
    public final static String PROP_FEED_CRAWLER = "feedCrawler";
    private FeedCrawler feedCrawler;
    
    @ConfigBoolean(defaultValue=false)
    public final static String PROP_AUTO_ENROLL_TEST_FEEDS = "autoEnrollTestFeeds";
    private boolean autoEnrollTestFeeds;

    /**
     * the configurable property for the RecommenderManager used by this manager
     */
    @ConfigComponent(type = RecommenderManager.class)
    public final static String PROP_RECOMMENDER_MANAGER = "recommenderManager";
    private RecommenderManager recommenderManager;
    private Logger logger;

    /**
     * A factory method that gets an instance of Aardvark with the default
     * configuration
     * @return the default configuraiton of aardvark
     * @throws AuraException if an exception occurs while configuring aardvark
     */
    public static Aardvark getDefault() throws AuraException {
        try {
            ConfigurationManager cm = new ConfigurationManager();
            URL configFile = Aardvark.class.getResource("aardvarkConfig.xml");
            cm.addProperties(configFile);
            return (Aardvark) cm.lookup("aardvark");
        } catch (IOException ioe) {
            throw new AuraException("Problem loading config", ioe);
        }
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        itemStore = (ItemStore) ps.getComponent(PROP_ITEM_STORE);
        feedCrawler = (FeedCrawler) ps.getComponent(PROP_FEED_CRAWLER);
        recommenderManager = (RecommenderManager) ps.getComponent(PROP_RECOMMENDER_MANAGER);
        autoEnrollTestFeeds = ps.getBoolean(PROP_AUTO_ENROLL_TEST_FEEDS);
        logger = ps.getLogger();
    }

    /**
     * Starts all processing
     */
    public void startup() {
        logger.info("started");
        feedCrawler.start();
        
        if (autoEnrollTestFeeds) {
            autoEnroll();
        }
    }

    /**
     * Stops all processing
     */
    public void shutdown() {
        logger.info("starting aardvark shutdown");
        feedCrawler.stop();
        recommenderManager.shutdown();
        logger.info("shutdown");
    }

    /**
     * Gets the user from the openID
     * @param openID the openID for the user
     * @return the user or null if the user doesn't exist
     */
    public User getUser(String openID) {
        User u = null;
        try {
            u = (User) itemStore.get(openIDtoKey(openID));
        } catch (AuraException ex) {
            logger.warning("Error retrievingi user for " + openID + ex);
        }
        return u;
    }

    /**
     * Enrolls a user in the recommender
     * @param openID the openID of the user
     * @param feed the starred item feed of the user
     * @return the user
     * @throws AuraException
     */
    public User enrollUser(String openID, String feed) throws AuraException {
        try {
            logger.info("Added user " + openID);
            if (getUser(openID) == null) {
                if (isValidFeed(feed)) {
                    User user = itemStore.newItem(User.class, openIDtoKey(openID));
                    user.setStarredItemFeedURL(new URL(feed));
                    itemStore.put(user);
                    return user;
                } else {
                    throw new AuraException("Invalid feed " + feed);
                }
            } else {
                throw new AuraException("attempting to enroll duplicate user " + openID);
            }
        } catch (MalformedURLException ex) {
            throw new AuraException("Bad starred item feed url" + ex);
        }
    }

    /**
     * Determines if the feed is valid
     * @param feed the feed to check
     * @return true if the feed is valid
     */
    private boolean isValidFeed(String feed) {
        return feed.startsWith("http://") ||
                feed.startsWith("file:/"); // TODO write me
    }

    /**
     * Gets the feed for the particular user
     * @param user the user
     * @return the feed
     */
    public SyndFeed getRecommendedFeed(User user) {
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("atom");  // BUG - what are the possible feed types
        feed.setTitle("Aardvark recommendations for " + user.getKey());
        feed.setDescription("Recommendations created for " + user);
        feed.setPublishedDate(new Date());
        feed.setEntries(FeedUtils.getSyndEntries(getRecommendedEntries(user)));
        return feed;
    }

    /**
     * Returns interesting stats about aardvark
     * @return the stats
     */
    public Stats getStats() {
        ItemStoreStats itemStoreStats = null;
        try {
            itemStoreStats = itemStore.getStats();
        } catch (AuraException ex) {
            logger.warning("Failed to retrieve itemStoreStats: " + ex);
            return new Stats(VERSION, 0, 0, 0);
        }
        return new Stats(VERSION, itemStoreStats.getNumUsers(),
                itemStoreStats.getNumEntries(),
                itemStoreStats.getNumAttentions());
    }

    /**
     * Given a user ID return the set of recommended entries for the user
     * @param user the user id
     * @return an array of recommended entries
     */
    private List<Entry> getRecommendedEntries(User user) {
        List<Entry> recommendations = recommenderManager.getRecommendations(user);
        return recommendations;
    }

    /**
     * Converts an user's openID to a key
     * @param openID the user ID
     * @return the key
     */
    private String openIDtoKey(String openID) {
        return openID;        // TODO: this is a bug
    }
    
    private void autoEnroll() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    enrollUser("delicious", "http://del.icio.us/rss/");
                    enrollUser("digg", "http://digg.com/rss/index.xml");
                    enrollUser("google news", "http://news.google.com/news?ned=us&topic=h&output=atom");
                    enrollUser("slashdot", "http://rss.slashdot.org/Slashdot/slashdot");
                    enrollUser("reddit", "http://reddit.com/.rss");
                    enrollUser("blogs.sun.com", "http://blogs.sun.com/main/feed/entries/atom");
                    enrollUser("engadget", "http://feeds.engadget.com/weblogsinc/engadget");
                    enrollUser("gizmodo", "http://feeds.gawker.com/gizmodo/full");
                    enrollUser("mediaor", "http://archive.mediaor.com/rss");
                    enrollUser("dzonenew", "http://dzone.com/links/feed/queue/rss.xml");
                    enrollUser("dzonepopular", "http://dzone.com/links/feed/frontpage/rss.xml");
                } catch (AuraException ex) {
                    logger.severe("Problem enrolling item feeds" + ex);
                } 
            }
        };

        t.start();
    }


    public static void main(String[] args) throws Exception {
        // enroll test

        Aardvark aardvark = getDefault();

        aardvark.startup();
        for (int i = 0; i < 20 * 60; i++) {
            Thread.sleep(3000L);
            Stats stats = aardvark.getStats();
            System.out.println(i + ") " + stats);
        }
        aardvark.shutdown();
    }
}
