/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.impl.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses robots.txt file to determine
 * whether a URL is accessible or not
 * 
 */
public class RobotsManager {

    private String userAgent;
    private Map<String, List<String>> robotCache;
    private static Pattern agentPattern = Pattern.compile("User-agent:\\s*(\\S+)\\s*", Pattern.CASE_INSENSITIVE);
    private static Pattern disallowPattern = Pattern.compile("Disallow:\\s*(\\S+)\\s*", Pattern.CASE_INSENSITIVE);
    private boolean monitor = false;
    private Logger logger;
    private Set<String> skipHosts;
    private HostScheduler hostScheduler;
    private HostCounter hostCounter;

    /**
     * Creates a robots manager
     * @param userAgent the user agent
     */
    public RobotsManager(String userAgent, int maxCacheSize, Logger logger) {
        this.userAgent = userAgent;
        this.skipHosts = new HashSet();
        LRUMap cache = new LRUMap();
        cache.setMaxSize(maxCacheSize);
        robotCache = Collections.synchronizedMap(cache);
        this.logger = logger;
        this.hostScheduler = new HostScheduler();
        this.hostCounter = new HostCounter();
    }

    /**
     * Determines if the given url is crawlable
     * @param surl the url
     * @return if the robots.txt associated with the url allows access to this url
     */
    public boolean isCrawlable(String surl) {

        if (monitor && robotCache.size() % 100 == 0) {
            System.out.println("robots cache size: " + robotCache.size());
        }

        try {
            URL url = new URL(surl);
            String path = url.getFile();
            String host = url.getHost();
            
            if (skipHosts.contains(host)) {
                return false;
            }

            if (!hostCounter.checkHost(host)) {
                return false;
            }

            List<String> disallowed = getDisallowedPaths(url);
            for (String prefix : disallowed) {
                if (path.startsWith(prefix)) {
                    if (monitor) {
                        System.out.println("    DENIED by robots.txt " + url + " prefix " + prefix);
                    }
                    return false;
                }
            }
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    public void addSkipHost(URL url) {
        skipHosts.add(url.getHost());
    }

    public long getEarliestPullTime(URL url) {
        return hostScheduler.getEarliestPullTime(url.getHost());
    }

    public boolean testAndSetIsOkToPullNow(URL url) {
        return hostScheduler.testAndSetIsOkToPullNow(url.getHost());
    }

    /**
     * Gets the list of paths that are disallowed
     * @param url the url of interest
     * @return list of disallowed paths
     */
    private List<String> getDisallowedPaths(URL url) {
        String host = url.getHost();
        List<String> disallowedPaths = robotCache.get(host);
        if (disallowedPaths == null) {
            disallowedPaths = loadRobots(host);
            robotCache.put(host, disallowedPaths);
        }
        return disallowedPaths;
    }

    // loads robot.txt file. 
    // Format described here: http://www.robotstxt.org/orig.html
    //
    private List<String> loadRobots(String host) {
        List<String> defaultDisallowed = new ArrayList();
        //"<field>:<optionalspace><value><optionalspace>
        try {
            URL url = new URL("http://" + host + "/robots.txt");
            URLConnection connection = FeedUtils.openConnection(url, "robots");
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(10000);

            InputStream is = connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            try {
                String line;

                while ((line = in.readLine()) != null) {
                    line = stripComments(line);

                    Matcher agentMatcher = agentPattern.matcher(line);
                    if (agentMatcher.matches()) {
                        String agent = agentMatcher.group(1);
                        if (agent.equalsIgnoreCase(userAgent)) {
                            // we found our user agent, so use this
                            return loadDisalloweds(in);
                        }
                        if (agent.equals("*")) {
                            // we found the default rules, save them in case
                            // we don't find our agent
                            defaultDisallowed = loadDisalloweds(in);
                        } else {
                            // skip other agents
                            loadDisalloweds(in);
                        }
                    }
                }
                logger.info("Robots.txt (" + robotCache.size() + "): " + url);
            } finally {
                in.close();
            }
        } catch (IOException ex) {
        // lots of sites don't have a robots.txt, so just
        // ignore any I/O errors
        }
        return defaultDisallowed;
    }

    private List<String> loadDisalloweds(BufferedReader in) throws IOException {
        List<String> disalloweds = new ArrayList();
        String line = null;

        while ((line = in.readLine()) != null) {

            if (line.length() == 0) {
                break;
            }

            line = stripComments(line);

            Matcher matcher = disallowPattern.matcher(line);
            if (matcher.matches()) {
                String path = matcher.group(1);
                disalloweds.add(path);
            }
        }
        return disalloweds;
    }

    private String stripComments(String line) {
        int commentIndex = line.indexOf("#");
        if (commentIndex > 0) {
            return line.substring(0, commentIndex).trim();
        }
        return line;
    }

    public static void test(RobotsManager rm, String url, boolean crawlable) {
        if (rm.isCrawlable(url) != crawlable) {
            System.out.println("Fail on " + url);
        }
    }

    public static void main(String[] args) {
        RobotsManager rm = new RobotsManager("foo", 100000, Logger.getAnonymousLogger());
        test(rm, "http://en.wikipedia.org/w/index.php?title=Special:RecentChanges&amp;feed=rss", false);
    }
}

class LRUMap<K, V> extends LinkedHashMap<K, V> {

    private int maxSize = 10000;

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maxSize;
    }
}


class HostCounter {
    // BUG, make this configurable
    private final static int MAX_HOST_VISITS = 3000;
    private LRUMap<String, Integer> hostMap = new LRUMap();

    HostCounter() {
        hostMap.setMaxSize(50000);
    }

    HostCounter(int maxSize) {
        hostMap.setMaxSize(maxSize);
    }

    synchronized boolean checkHost(String host) {
        Integer c = hostMap.get(host);
        if (c == null) {
            c = new Integer(0);
        }
        c = c + 1;
        hostMap.put(host, c);
        return c < MAX_HOST_VISITS;
    }

    private void dump() {
        for (String host : hostMap.keySet()) {
            System.out.println("host counter " + host + " " + hostMap.get(host));
        }
    }
}

class HostScheduler {
    private final static long MIN_PERIOD_MS = 30000L;
    private LRUMap<String, HostSchedule> hostMap = new LRUMap();

    HostScheduler() {
        setMaxSize(100000);
    }

    void setMaxSize(int maxSize) {
        hostMap.setMaxSize(maxSize);
    }


    long getEarliestPullTime(String host) {
        return getHostSchedule(host).getEarliestNextPull();
    }

    synchronized boolean testAndSetIsOkToPullNow(String host) {
        HostSchedule hs = getHostSchedule(host); 
        if (hs.getEarliestNextPull() <= System.currentTimeMillis()) {
            hs.mostRecentPull = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    void setPeriod(String host, long period) {
        getHostSchedule(host).period = period;
    }

    private synchronized HostSchedule getHostSchedule(String host) {
        HostSchedule hostSchedule = hostMap.get(host);
        if (hostSchedule == null) {
            hostSchedule = new HostSchedule(MIN_PERIOD_MS, 0L);
            hostMap.put(host, hostSchedule);
        }
        return hostSchedule;
    }

    private void dump() {
        for (String host : hostMap.keySet()) {
            System.out.println("host timer " + host + " " + hostMap.get(host));
        }
    }
}

class HostSchedule {
    long period;
    long mostRecentPull;

    public HostSchedule(long period, long mostRecentPull) {
        this.period = period;
        this.mostRecentPull = mostRecentPull;
    }

    public String toString() {
        return "period " + period + " sched " + mostRecentPull;
    }

    public long getEarliestNextPull() {
        return period + mostRecentPull;
    }
}