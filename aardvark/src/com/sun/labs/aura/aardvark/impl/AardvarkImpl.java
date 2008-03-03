/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.  Which I did, which is why this text is here.
 */
package com.sun.labs.aura.aardvark.impl;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.aardvark.Stats;
import com.sun.labs.aura.aardvark.crawler.FeedCrawler;
import com.sun.labs.aura.aardvark.impl.crawler.FeedUtils;
import com.sun.labs.aura.aardvark.impl.crawler.OPMLProcessor;
import com.sun.labs.aura.aardvark.recommender.RecommenderManager;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Blog Recommender
 */
public class AardvarkImpl implements Configurable, Aardvark, AuraService {

    private final static String VERSION = "aardvark version 0.30";

    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_ITEM_STORE = "itemStore";

    private DataStore dataStore;

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
    public static AardvarkImpl getDefault() throws AuraException {
        try {
            ConfigurationManager cm = new ConfigurationManager();
            URL configFile =
                    AardvarkImpl.class.getResource("aardvarkConfig.xml");
            cm.addProperties(configFile);
            return (AardvarkImpl) cm.lookup("aardvark");
        } catch(IOException ioe) {
            throw new AuraException("Problem loading config", ioe);
        }
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_ITEM_STORE);
        feedCrawler = (FeedCrawler) ps.getComponent(PROP_FEED_CRAWLER);
        recommenderManager =
                (RecommenderManager) ps.getComponent(PROP_RECOMMENDER_MANAGER);
        autoEnrollTestFeeds = ps.getBoolean(PROP_AUTO_ENROLL_TEST_FEEDS);
        autoEnrollMegaTestFeeds =
                ps.getBoolean(PROP_AUTO_ENROLL_MEGA_TEST_FEEDS);
        logger = ps.getLogger();
    }

    /**
     * Gets the user from the openID
     * @param openID the openID for the user
     * @return the user or null if the user doesn't exist
     */
    public User getUser(String openID) throws AuraException, RemoteException {
        try {
            return dataStore.getUser(openID);
        } catch(RemoteException rx) {
            throw new AuraException("Error communicating with item store", rx);
        }
    }

    public List<Attention> getAttentionData(User user) throws AuraException, RemoteException {
        // TODO: waiting for support for getting attention by user from the datastore
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Set<BlogFeed> getFeeds(User user, Attention.Type type) throws AuraException, RemoteException {

        Set<Item> items = dataStore.getItems(user, type, ItemType.FEED);
        Set<BlogFeed> feeds = new HashSet<BlogFeed>();
        for(Item item : items) {
            feeds.add(new BlogFeed(item));
        }
        return feeds;
    }

    /**
     * Enrolls a user in the recommender
     * @param openID the openID of the user
     * @return the user
     * @throws AuraException if the user is already enrolled or a problem occurs while enrolling the user
     */
    public User enrollUser(String openID) throws AuraException, RemoteException {
        logger.info("Added user " + openID);
        if(getUser(openID) == null) {
            try {
                User theUser = StoreFactory.newUser(openID, openID);
                return dataStore.putUser(theUser);
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
    public void addUserFeed(User user, URL feedURL, Attention.Type type) throws AuraException, RemoteException {
        BlogFeed feed = feedCrawler.createFeed(feedURL);
        Attention userAttention =
                StoreFactory.newAttention(user, feed.getItem(), type);
        dataStore.attend(userAttention);
    }

    /**
     * Adds a new fed to the system
     * @param feedURL the feed to add
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public void addFeed(URL feedURL) throws AuraException, RemoteException {
        feedCrawler.createFeed(feedURL);
    }

    /**
     * Adds a new feed to the system
     * @param feedURL the feed to add
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public void addFeed(String feedURL) throws AuraException, RemoteException {
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
    public User enrollUser(String openID, String feed) throws AuraException, RemoteException {
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
    public SyndFeed getRecommendedFeed(User user) throws AuraException, RemoteException {

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
    public Stats getStats() throws AuraException, RemoteException {
        try {
            long numEntries = dataStore.getItemCount(ItemType.BLOGENTRY);
            long numFeeds = dataStore.getItemCount(ItemType.FEED);

            // TODO: Add these stats when they are supported in the store
            //long numUsers = dataStore.getItemCount(ItemType.USER);
            long numUsers = 0L;
            long numAttentions = 0L;

            int feedPullCount = feedCrawler.getFeedPullCount();
            int feedErrorCount = feedCrawler.getFeedErrorCount();
            return new Stats(VERSION, numUsers,
                    numEntries, numAttentions, numFeeds,
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
    private List<BlogEntry> getRecommendedEntries(User user) {
        try {
            List<BlogEntry> recommendations =
                    recommenderManager.getRecommendations(user);
            return recommendations;
        } catch(RemoteException rx) {
            logger.log(Level.SEVERE, "Error getting recommendations", rx);
            return Collections.emptyList();
        }
    }

    private void addLocalOpml(String name) {
        try {
            logger.info("Enrolling local opml " + name);
            OPMLProcessor op = new OPMLProcessor();
            URL opmlFile = AardvarkImpl.class.getResource(name);
            List<URL> urls = op.getFeedURLs(opmlFile);
            for(URL url : urls) {
                try {
                    addFeed(url);
                } catch(AuraException ex) {
                    logger.log(Level.WARNING, "Problems enrolling " + url, ex);
                }
            }
        } catch(IOException ex) {
            logger.log(Level.WARNING, "Problems loading opml " + name, ex);
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
                    if(autoEnrollTestFeeds) {
                        addLocalOpml("autoEnrolledFeeds.opml.xml");
                    }
                    if(autoEnrollMegaTestFeeds) {
                        addLocalOpml("tech_blogs.opml");
                        addLocalOpml("politics_blogs.opml");
                        addLocalOpml("news_blogs.opml");
                    }
                } catch(Throwable t) {
                    logger.severe("bad thing happend " + t);
                }
            }
        };
        t.start();
    }

    public void start() {
        autoEnroll();
    }

    public void stop() {
    }
}
