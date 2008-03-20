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
import com.sun.labs.aura.aardvark.impl.crawler.FeedManager;
import com.sun.labs.aura.aardvark.impl.crawler.FeedUtils;
import com.sun.labs.aura.aardvark.impl.crawler.OPMLProcessor;
import com.sun.labs.aura.recommender.RecommenderManager;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.recommender.Recommendation;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.StatService;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Blog Recommender
 */
public class AardvarkImpl implements Configurable, Aardvark, AuraService {

    private final static String VERSION = "aardvark version 0.30";
    private final static String RESOURCE_PATH = "/com/sun/labs/aura/aardvark/resource/";
    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    private DataStore dataStore;
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

    @ConfigComponent(type = StatService.class)
    public final static String PROP_STAT_SERVICE = "statService";
    private StatService statService;

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
        } catch (IOException ioe) {
            throw new AuraException("Problem loading config", ioe);
        }
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        recommenderManager = (RecommenderManager) ps.getComponent(PROP_RECOMMENDER_MANAGER);
        statService = (StatService) ps.getComponent(PROP_STAT_SERVICE);
        autoEnrollTestFeeds = ps.getBoolean(PROP_AUTO_ENROLL_TEST_FEEDS);
        autoEnrollMegaTestFeeds = ps.getBoolean(PROP_AUTO_ENROLL_MEGA_TEST_FEEDS);
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
        } catch (RemoteException rx) {
            throw new AuraException("Error communicating with item store", rx);
        }
    }

    public SortedSet<Attention> getLastAttentionData(User user, Type type, int count) throws AuraException, RemoteException {
        return dataStore.getLastAttentionForSource(user.getKey(), type, count);
    }

    public Set<BlogFeed> getFeeds(User user, Attention.Type type) throws AuraException, RemoteException {

        Set<Item> items = dataStore.getItems(user, type, ItemType.FEED);
        Set<BlogFeed> feeds = new HashSet<BlogFeed>();
        for (Item item : items) {
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
        if (getUser(openID) == null) {
            try {
                User theUser = StoreFactory.newUser(openID, openID);
                return dataStore.putUser(theUser);
            } catch (RemoteException rx) {
                throw new AuraException("Error communicating with item store",
                        rx);
            }
        } else {
            throw new AuraException("attempting to enroll duplicate user " +
                    openID);
        }
    }
    
    public User updateUser(User user) throws AuraException, RemoteException {
        try {
            return dataStore.putUser(user);
        } catch (RemoteException rx) {
            throw new AuraException("Error communicating with item store",
                    rx);
        }
    }

    
    /**
     * Adds a feed of a particular type for a user
     * @param user the user
     * @param feedURL the url of the feed to add
     * @param type the type of attention the user pays to the URL
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public void addUserFeed(User user, String feedURL, Attention.Type type) throws AuraException, RemoteException {
        addFeed(feedURL);
        Attention userAttention = StoreFactory.newAttention(user.getKey(), feedURL, type);
        dataStore.attend(userAttention);
    }

    /**
     * Adds a new feed to the system
     * @param feedURL the feed to add
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public void addFeed(String feedURL) throws AuraException, RemoteException {
        if (dataStore.getItem(feedURL) == null) {
            Item item = StoreFactory.newItem(ItemType.FEED, feedURL, "");
            dataStore.putItem(item);
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
            long numUsers = dataStore.getItemCount(ItemType.USER);
            long numAttentions = dataStore.getAttentionCount();
            long feedPullCount = statService.get(FeedManager.COUNTER_FEED_PULL_COUNT);
            long feedErrorCount = statService.get(FeedManager.COUNTER_FEED_ERROR_COUNT);

            return new Stats(VERSION, numUsers,
                    numEntries, numAttentions, numFeeds,
                    feedPullCount, feedErrorCount);
        } catch (RemoteException rx) {
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
            SortedSet<Recommendation> recommendations = 
                    recommenderManager.getRecommendations(user);
            List<BlogEntry> recommendedBlogEntries = new ArrayList<BlogEntry>();

            for (Recommendation r : recommendations) {
                recommendedBlogEntries.add(new BlogEntry(r.getItem()));
            }

            return recommendedBlogEntries;
        } catch (RemoteException rx) {
            logger.log(Level.SEVERE, "Error getting recommendations", rx);
            return Collections.emptyList();
        }
    }

    private void addLocalOpml(String name) {
        try {
            logger.info("Enrolling local opml " + name);
            OPMLProcessor op = new OPMLProcessor();
            String fullName = RESOURCE_PATH + name;
            URL opmlFile = AardvarkImpl.class.getResource(fullName);
            List<URL> urls = op.getFeedURLs(opmlFile);
            for (URL url : urls) {
                try {
                    addFeed(url.toExternalForm());
                } catch (AuraException ex) {
                    logger.log(Level.WARNING, "Problems enrolling " + url, ex);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Problems loading opml " + name, ex);
        } finally {
            logger.info("Finished enrolling local opml " + name);
        }
    }

    private void autoEnroll() {
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    // Thread.sleep(10 * 60 * 1000L);
                    if (autoEnrollTestFeeds) {
                        addLocalOpml("autoEnrolledFeeds.opml.xml");
                    }
                    if (autoEnrollMegaTestFeeds) {
                        addLocalOpml("tech_blogs.opml");
                        addLocalOpml("politics_blogs.opml");
                        addLocalOpml("news_blogs.opml");
                        // addLocalOpml("mega.opml");
                    }
                } catch (Throwable t) {
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

