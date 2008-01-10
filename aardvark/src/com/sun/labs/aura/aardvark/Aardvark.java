/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.  Which I did, which is why this text is here.
 */
package com.sun.labs.aura.aardvark;

import com.sun.labs.aura.aardvark.crawler.FeedCrawler;
import com.sun.labs.aura.aardvark.util.FeedUtils;
import com.sun.labs.aura.aardvark.util.OPMLProcessor;
import com.sun.labs.aura.aardvark.recommender.RecommenderManager;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.ItemStoreStats;
import com.sun.labs.aura.aardvark.store.SimpleAttention;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Feed;
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
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * A Blog Recommender
 */
public class Aardvark implements Configurable {

    private final static String VERSION = "aardvark version 0.20";

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

    @ConfigBoolean(defaultValue = false)
    public final static String PROP_AUTO_ENROLL_TEST_FEEDS =
            "autoEnrollTestFeeds";

    private boolean autoEnrollTestFeeds;

    @ConfigBoolean(defaultValue = false)
    public final static String PROP_AUTO_ENROLL_MEGA_TEST_FEEDS =
            "autoEnrollMegaTestFeeds";

    private boolean autoEnrollMegaTestFeeds;

    /**
     * the configurable property for the RecommenderManager used by this manager
     */
    @ConfigComponent(type = RecommenderManager.class)
    public final static String PROP_RECOMMENDER_MANAGER =
            "recommenderManager";

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
        } catch(IOException ioe) {
            throw new AuraException("Problem loading config", ioe);
        }
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        itemStore = (ItemStore) ps.getComponent(PROP_ITEM_STORE);
        feedCrawler = (FeedCrawler) ps.getComponent(PROP_FEED_CRAWLER);
        recommenderManager =
                (RecommenderManager) ps.getComponent(PROP_RECOMMENDER_MANAGER);
        autoEnrollTestFeeds = ps.getBoolean(PROP_AUTO_ENROLL_TEST_FEEDS);
        autoEnrollMegaTestFeeds =
                ps.getBoolean(PROP_AUTO_ENROLL_MEGA_TEST_FEEDS);
        logger = ps.getLogger();
    }

    /**
     * Starts all processing
     */
    public void startup() {
        logger.info("started");
        feedCrawler.start();

        if(autoEnrollTestFeeds) {
            autoEnroll();
        }
    }

    /**
     * Stops all processing
     */
    public void shutdown() throws AuraException {
        feedCrawler.stop();
        recommenderManager.shutdown();
        try {
        itemStore.close();
        } catch(RemoteException rx) {
            
        }
        logger.info("shutdown");
    }

    /**
     * Gets the user from the openID
     * @param openID the openID for the user
     * @return the user or null if the user doesn't exist
     */
    public User getUser(String openID) throws AuraException {
        try {
            return (User) itemStore.get(openID);
        } catch(RemoteException rx) {
            throw new AuraException("Error communicating with item store", rx);
        }
    }

    /**
     * Enrolls a user in the recommender
     * @param openID the openID of the user
     * @return the user
     * @throws AuraException if the user is already enrolled or a problem occurs while enrolling the user
     */
    public User enrollUser(String openID) throws AuraException {
        logger.info("Added user " + openID);
        if(getUser(openID) == null) {
            try {
                User theUser = itemStore.newItem(User.class, openID);
                itemStore.put(theUser);
                return theUser;
            } catch(RemoteException rx) {
                throw new AuraException("Error communicating with item store",
                                        rx);
            }
        } else {
            throw new AuraException("attempting to enroll duplicate user " +
                                    openID);
        }
    }

    /**
     * Adds a feed of a particular type for a user
     * @param user the user
     * @param feedURL the url of the feed to add
     * @param type the type of attention the user pays to the URL
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public void addUserFeed(User user, URL feedURL, Attention.Type type) throws AuraException {
        Feed feed = feedCrawler.createFeed(feedURL);
        if(feed != null) {
            SimpleAttention userAttention =
                    new SimpleAttention(user, feed, type);
            try {
                itemStore.attend(userAttention);
            } catch(RemoteException rx) {
                throw new AuraException("Error communicating with item store",
                                        rx);
            }
        } else {
            throw new AuraException("Invalid feed " + feedURL);
        }
    }

    /**
     * Adds a new fed to the system
     * @param feedURL the feed to add
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public void addFeed(URL feedURL) throws AuraException {
        feedCrawler.createFeed(feedURL);
    }

    /**
     * Adds a new feed to the system
     * @param feedURL the feed to add
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public void addFeed(String feedURL) throws AuraException {
        try {
            feedCrawler.createFeed(new URL(feedURL));
        } catch(MalformedURLException ex) {
            throw new AuraException("bad url " + feedURL, ex);
        }
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
            URL feedURL = new URL(feed);
            User user = enrollUser(openID);
            addUserFeed(user, feedURL, Attention.Type.STARRED_FEED);
            return user;
        } catch(MalformedURLException ex) {
            throw new AuraException("Bad url " + feed, ex);
        }
    }

    /**
     * Gets the feed for the particular user
     * @param user the user
     * @return the feed
     */
    public SyndFeed getRecommendedFeed(User user) throws AuraException {

        // freshen the user:
        User freshUser = getUser(user.getKey());
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("atom");  // BUG - what are the possible feed types
        feed.setTitle("Aardvark recommendations for " + freshUser.getKey());
        feed.setDescription("Recommendations created for " + freshUser);
        feed.setPublishedDate(new Date());
        feed.setEntries(FeedUtils.getSyndEntries(getRecommendedEntries(freshUser)));
        return feed;
    }

    /**
     * Returns interesting stats about aardvark
     * @return the stats
     */
    public Stats getStats() throws AuraException {
        try {
            ItemStoreStats itemStoreStats = itemStore.getStats();
            int feedPullCount = feedCrawler.getFeedPullCount();
            int feedErrorCount = feedCrawler.getFeedErrorCount();
            return new Stats(VERSION, itemStoreStats.getNumUsers(),
                             itemStoreStats.getNumEntries(),
                             itemStoreStats.getNumAttentions(),
                             itemStoreStats.getNumFeeds(),
                             feedPullCount, feedErrorCount);
        } catch(RemoteException rx) {
            throw new AuraException("Error communicating with item store", rx);
        }
    }

    /**
     * Given a user ID return the set of recommended entries for the user
     * @param user the user id
     * @return an array of recommended entries
     */
    private List<Entry> getRecommendedEntries(User user) {
        List<Entry> recommendations =
                recommenderManager.getRecommendations(user);
        return recommendations;
    }

    private void addLocalOpml(String name) {
        try {
            logger.info("Enrolling local opml " + name);
            OPMLProcessor op = new OPMLProcessor();
            URL opmlFile = Aardvark.class.getResource(name);
            List<URL> urls = op.getFeedURLs(opmlFile);
            for(URL url : urls) {
                try {
                    addFeed(url);
                } catch(AuraException ex) {
                    logger.warning("Problems enrolling " + url);
                }
            }
        } catch(IOException ex) {
            logger.warning("Problems loading opml " + name);
        } finally {
            logger.info("Finished enrolling local opml" + name);
        }
    }

    private void autoEnroll() {
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    // Thread.sleep(10 * 60 * 1000L);
                    Thread.sleep(10 * 1000L);
                    addLocalOpml("autoEnrolledFeeds.opml.xml");
                    if(autoEnrollMegaTestFeeds) {
                        addLocalOpml("tech_blogs.opml");
                        addLocalOpml("politics_blogs.opml");
                        addLocalOpml("news_blogs.opml");
                    }
                } catch(InterruptedException ex) {
                    logger.info("autoenroller interrupted");
                } catch(Throwable t) {
                    logger.severe("bad thing happend " + t);
                }
            }
        };
        t.start();
    }

    public static void main(String[] args) throws Exception {
        // enroll test

        Aardvark aardvark = getDefault();

        aardvark.startup();
        for(int i = 0; i < 20 * 60; i++) {
            Thread.sleep(3000L);
            Stats stats = aardvark.getStats();
            System.out.println(i + ") " + stats);
        }
        aardvark.shutdown();
    }
}
