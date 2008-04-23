/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.impl.crawler;

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
import java.util.concurrent.LinkedBlockingQueue;
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

    public FeedSchedulerImpl() {
        fss.feedDiscoveryQueue = new LinkedBlockingQueue();
    }

    @Override
    synchronized public void newProperties(final PropertySheet ps) throws PropertyException {
        try {
            super.newProperties(ps);
            logger = ps.getLogger();
            robotsCacheSize = ps.getInt(PROP_ROBOTS_CACHE_SIZE);
            userAgent = ps.getString(PROP_USER_AGENT);
            stateDir = ps.getString(PROP_STATE_DIR);
            robotsManager = new RobotsManager(userAgent, robotsCacheSize);

            File visitedStateDir = new File(stateDir, VISITED_STATE);
            visitedSet = new PersistentStringSet(visitedStateDir.getPath());
            createStateFileDirectory();
            loadState();
        } catch (IOException ex) {
            throw new PropertyException(ex, ps.getInstanceName(), stateDir, "Can't create the visitedSet " + ex);
        }
    }


    public void addUrlForDiscovery(URLForDiscovery ufd) throws RemoteException {
        if (isGoodToVisit(ufd.getUrl()) && !visitedSet.contains(ufd.getUrl())) {
            visitedSet.add(ufd.getUrl());
            fss.feedDiscoveryQueue.add(ufd);

            synchronized (this) {
                if (++fss.postCount % 1000 == 0) {
                    saveState();
                }
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
        URLForDiscovery ufd = fss.feedDiscoveryQueue.take();
        if (fss.pulled++ % 1000 == 0) {
            System.out.println("URLS checked " + fss.pulled + " remaining " + fss.feedDiscoveryQueue.size());
        }
        return ufd;
    }

    /**
     * Saves the priority queue to a file.
     */
    private void saveState() {
        FileOutputStream fos = null;
        try {
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
                fss.feedDiscoveryQueue.clear();
                fss.feedDiscoveryQueue.addAll(newFss.feedDiscoveryQueue);
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
    private boolean isGoodToVisit(String surl) {
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
        return isCrawlable(surl);
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

}
class FeedSchedulerState implements Serializable {
    LinkedBlockingQueue<URLForDiscovery> feedDiscoveryQueue;
    int pulled;
    int postCount = 0;

    FeedSchedulerState() {
    }
}