/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.impl.crawler;

import com.sun.labs.aura.util.PersistentStringSet;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemSchedulerImpl;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class FeedSchedulerImpl extends ItemSchedulerImpl implements FeedScheduler {

    private RobotsManager robotsManager;
    private Logger logger;
    private FeedSchedulerState fss = new FeedSchedulerState();
    private PersistentStringSet visitedSet;
    private final static String FEED_STATE = "feed.state";
    private final static String VISITED_STATE = "visited.state";
    private DelayQueue<URLForDiscovery> feedDiscoveryQueue;
    private boolean discoveryEnabled = true;

    public FeedSchedulerImpl() {
        feedDiscoveryQueue = new DelayQueue();
    }

    @Override
    synchronized public void newProperties(final PropertySheet ps) throws PropertyException {
        try {
            super.newProperties(ps);
            logger = ps.getLogger();
            robotsCacheSize = ps.getInt(PROP_ROBOTS_CACHE_SIZE);
            userAgent = ps.getString(PROP_USER_AGENT);
            stateDir = ps.getString(PROP_STATE_DIR);
            robotsManager = new RobotsManager(userAgent, robotsCacheSize, logger);
            maxFeeds = ps.getInt(PROP_MAX_FEEDS);

            // add hosts to skip
            // BUG this should be configurable, and probably it is best to do
            // this another way.

            robotsManager.addSkipHost(new URL("http://dailymotion.com"));
            robotsManager.addSkipHost(new URL("http://www.dailymotion.com"));
            robotsManager.addSkipHost(new URL("http://www.metacafe.com"));
            robotsManager.addSkipHost(new URL("http://metacafe.com"));
            robotsManager.addSkipHost(new URL("http://vmware.simplefeed.net"));
            robotsManager.addSkipHost(new URL("http://twitter.com"));
            robotsManager.addSkipHost(new URL("http://www.mahalo.com"));
            robotsManager.addSkipHost(new URL("http://11870.com"));
            robotsManager.addSkipHost(new URL("http://bitacoras.com"));
            robotsManager.addSkipHost(new URL("http://jaanix.com"));
            robotsManager.addSkipHost(new URL("http://viddler.com"));
            robotsManager.addSkipHost(new URL("http://search.ebay.com"));
            robotsManager.addSkipHost(new URL("http://brightkite.com"));
            robotsManager.addSkipHost(new URL("http://vimeo.com"));
            robotsManager.addSkipHost(new URL("http://d.hatena.ne.jp"));

            File visitedStateDir = new File(stateDir, VISITED_STATE);
            visitedSet = new PersistentStringSet(visitedStateDir.getPath());
            createStateFileDirectory();
            loadState();
        } catch (IOException ex) {
            throw new PropertyException(ex, ps.getInstanceName(), stateDir, "Can't create the visitedSet " + ex);
        }
    }

    public void addUrlForDiscovery(URLForDiscovery ufd) throws RemoteException {
        if (isGoodToVisit(ufd) && !visitedSet.contains(ufd.getUrl())) {
            visitedSet.add(ufd.getUrl());
            try {
                URL url = new URL(ufd.getUrl());
                if (ufd.getPriority() == URLForDiscovery.Priority.NORMAL) {
                    ufd.setNextProcessingTime(robotsManager.getEarliestPullTime(url));
                } else {
                    ufd.setNextProcessingTime(0L);
                }
                feedDiscoveryQueue.add(ufd);

                synchronized (this) {
                    if (++fss.postCount % 1000 == 0) {
                        saveState();
                    }
                }
            } catch (MalformedURLException ex) {
                logger.info("dropping malfomed URL " + ufd.getUrl());
            }
        }
    }

    /**
     * Blocks waiting for the next url that needs to be crawled
     * @return the url to be crawled
     * @throws java.lang.InterruptedException
     * @throws java.rmi.RemoteException
     */
    public URLForDiscovery getUrlForDiscovery() throws InterruptedException, RemoteException {
        URLForDiscovery ufd = null;

        while (ufd == null) {
            ufd = feedDiscoveryQueue.take();
            try {
                URL url = new URL(ufd.getUrl());
                if (ufd.getPriority() == URLForDiscovery.Priority.NORMAL) {
                    //System.out.println("size " + getNumFeeds() + " max " +maxFeeds);
                    if (getNumFeeds() >= maxFeeds) {
                        discoveryEnabled = false;
                        logger.info("Discovery disabled: at max feeds, ignoring discovery for " + ufd.getUrl());
                        ufd = null;
                        continue;
                    }
                    if (!robotsManager.testAndSetIsOkToPullNow(url)) {
                        ufd.setNextProcessingTime(robotsManager.getEarliestPullTime(url));
                        feedDiscoveryQueue.add(ufd);
                        ufd = null;
                        continue;
                    }
                } else {
                    logger.info("Discover: high priority URL " + ufd.getUrl());
                }
            } catch (MalformedURLException e) {
                //the URL has already been verified, so this should never happen
                // so be noisy if it does.
                logger.warning("Unexpected bad url " + ufd.getUrl());
                ufd = null;
                continue;
            }
        }
        logger.info("Discover: (" + (fss.pulled++) + "/" + feedDiscoveryQueue.size() + ") " + ufd.getUrl());
        return ufd;
    }
    

    private int getNumFeeds() {
        try {
            return (int)dataStore.getItemCount(ItemType.FEED);
        } catch (AuraException ex) {
            return size();
        } catch (RemoteException ex) {
            return size();
        }
    }


    private void reportPosition(String msg, String s) {
        List<URLForDiscovery> list = new ArrayList(feedDiscoveryQueue);
        for (int i = 0; i < list.size(); i++) {
            URLForDiscovery u = list.get(i);
            //System.out.println(i + " " + u.getPriority() + " " + u);
            if (u.getUrl().indexOf(s) >= 0) {
                System.out.println(msg + " hit at pos " + i + " " + u);
            }
        }
    }

    /**
     * Saves the priority queue to a file.
     */
    private void saveState() {
        FileOutputStream fos = null;
        try {
            fss.discoveryQueue = new ArrayList(feedDiscoveryQueue);
            File stateFile = new File(stateDir, FEED_STATE);
            fos = new FileOutputStream(stateFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(fss);
            oos.close();
        } catch (IOException ex) {
            logger.warning("Can't save the state of the feedscheduler " + ex);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Loads the previously saveed priority queue
     */
    private void loadState() {
        ObjectInputStream ois = null;
        try {
            File stateFile = new File(stateDir, FEED_STATE);
            FileInputStream fis = new FileInputStream(stateFile);
            ois = new ObjectInputStream(fis);
            FeedSchedulerState newFss = (FeedSchedulerState) ois.readObject();

            if (newFss != null) {
                fss.postCount = newFss.postCount;
                fss.pulled = newFss.pulled;
                feedDiscoveryQueue.clear();
                feedDiscoveryQueue.addAll(newFss.discoveryQueue);
                logger.info("restored discovery queue with " + newFss.discoveryQueue.size() + " entries");
            }
        } catch (IOException ex) {
        // no worries if there was no file
        } catch (ClassNotFoundException ex) {
            logger.warning("Bad format in feedscheduler statefile " + ex);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(FeedSchedulerImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Creates the directory for the state file if necessary
     */
    private void createStateFileDirectory() {
        File stateDirFile = new File(stateDir);
        if (!stateDirFile.exists()) {
            stateDirFile.mkdirs();
        }
    }

    /**
     * Performs quick triage on a url, to see if it is worth adding to the queue.
     * Skip files with media extensions, bad hosts or refused by the robots.txt file
     * @param surl the url to check
     * @return true if the url should be crawled
     */
    private boolean isGoodToVisit(URLForDiscovery ufd) {

        if (ufd.getPriority() != URLForDiscovery.Priority.HIGH && !discoveryEnabled) {
            return false;
        }

        String surl = ufd.getUrl();

        String[] filteredExtensions = {".mp3", ".mp4", "jpg", "gif", ".png", "avi", "qt"};

        if (surl == null) {
            return false;
        }

        surl = surl.toLowerCase();
        for (String ext : filteredExtensions) {
            if (surl.endsWith(ext)) {
                return false;
            }
        }

        try {
            URL url = new URL(surl);
            if (url.getHost() == null) {
                return false;
            }

            if (url.getHost().length() == 0) {
                return false;
            }
        } catch (MalformedURLException ex) {
            return false;
        }

        // if the attention data is 'STARRED_FEED' then this is likely to
        // be a feed such as a google starred item feed, which means that
        // we can ignore the robots.txt

        if (ufd.getAttention() != null && isFeedOriented(ufd.getAttention().getType())) {
            return true;
        } else {
            return isCrawlable(surl);
        }
    }

    private boolean isFeedOriented(Attention.Type type) {
        return type == Attention.Type.STARRED_FEED ||
                type == Attention.Type.SUBSCRIBED_FEED ||
                type == Attention.Type.DISLIKED_FEED;
    }

    public boolean isCrawlable(String surl) {
        return robotsManager.isCrawlable(surl);
    }
    /** the maximum size of the robots.txt cache */
    @ConfigInteger(defaultValue = 100000, range = {0, Integer.MAX_VALUE})
    public final static String PROP_ROBOTS_CACHE_SIZE = "robotsCacheSize";
    private int robotsCacheSize;
    /** the user agent name to use when crawling */
    @ConfigString(defaultValue = "aardvark-crawler")
    public final static String PROP_USER_AGENT = "userAgent";
    private String userAgent;
    /** the directory for the feedscheduler.state */
    @ConfigString(defaultValue = "feedScheduler")
    public final static String PROP_STATE_DIR = "stateDir";
    private String stateDir;

    @ConfigInteger(defaultValue = 100000, range = { 10, Integer.MAX_VALUE})
    public final static String PROP_MAX_FEEDS = "maxFeeds";
    private int maxFeeds;
}

class FeedSchedulerState implements Serializable {

    List<URLForDiscovery> discoveryQueue;
    int pulled;
    int postCount = 0;

    FeedSchedulerState() {
    }

    void dump() {
        int count = 0;
        for (URLForDiscovery ufd : discoveryQueue) {
            count++;
            System.out.println(count + " " + ufd.toString());
        }
    }
}
