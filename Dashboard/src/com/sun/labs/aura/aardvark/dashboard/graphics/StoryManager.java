/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.sun.labs.aura.aardvark.dashboard.story.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class StoryManager {

    private String baseUrl;
    private int maxStoriesPerMinute = 60;
    private long curTime;
    private DelayQueue<DelayedStory> queue = new DelayQueue<DelayedStory>();
    private Thread t;
    private final static long pollPeriod = 60000L;
    private StoryPointFactory factory;
    private boolean asyncMode = false;
    private boolean liveMode = true;
    private boolean showTimes = false;
    private boolean showConnections = false;
    private InstrumentedConnector storyConnector = new InstrumentedConnector("GetStories");
    private InstrumentedConnector findSimConnector = new InstrumentedConnector("FindSim");
    private InstrumentedConnector miscConnector = new InstrumentedConnector("misc");
    private long lastStoryTime = 0;

    public StoryManager(StoryPointFactory factory, String baseUrl) {
        this.baseUrl = baseUrl;
        this.factory = factory;
    }

    public InteractivePoint getNext() {
        DelayedStory ds = queue.poll();
        if (ds != null) {
            return ds.getStory();
        }
        return null;
    }

    public void findSimilar(final Story story, final int max) {
        Thread t = new Thread() {

            public void run() {
                List<Story> stories = fetchSimilarStories(story, max);
                int which = 1;
                System.out.println("FindSim " + story.getTitle());
                for (Story similarStory : stories) {
                    InteractivePoint cpoint = factory.createTileStoryPoint(similarStory, which++);
                    System.out.println("  Sim " + similarStory.getTitle());
                    cpoint.add("home");
                    DelayedStory ds = new DelayedStory(cpoint, -1);
                    queue.add(ds);
                }
            }
        };
        t.setName("find-similar");
        t.start();
    }

    public void getTagInfo(final Story story, final int max) {
        Thread t = new Thread() {

            public void run() {
                List<TagInfo> tagInfos = fetchTagInfo(story, max);
                int which = 1;
                for (TagInfo ti : tagInfos) {
                    InteractivePoint cpoint = factory.createTagInfoTileStoryPoint(story, ti, which++);
                    cpoint.add("home");
                    DelayedStory ds = new DelayedStory(cpoint, -1);
                    queue.add(ds);
                }
            }
        };
        t.setName("getTagInfo");
        t.start();
    }

    public void query(final String query, final int max) {
        Thread t = new Thread() {

            public void run() {
                factory.clearAllStories();
                List<Story> stories = queryForStories(query, max);
                int which = 0;
                for (Story similarStory : stories) {
                    InteractivePoint cpoint = factory.createTileStoryPoint(similarStory, which++);
                    System.out.println("  Sim " + similarStory.getTitle());
                    cpoint.add("home");
                    DelayedStory ds = new DelayedStory(cpoint, -1);
                    queue.add(ds);
                }
            }
        };
        t.setName("getTagInfo");
        t.start();
    }

    public void getStoriesSimilarToTag(final String tag, final int max) {
        Thread t = new Thread() {

            public void run() {
                List<Story> stories = fetchStoriesSimilarTag(tag, max);
                int which = 1;
                System.out.println("FindSimStoryForTag " + tag);
                for (Story similarStory : stories) {
                    InteractivePoint cpoint = factory.createTileStoryPoint(similarStory, which++);
                    System.out.println("  Sim " + similarStory.getTitle());
                    cpoint.add("home");
                    DelayedStory ds = new DelayedStory(cpoint, -1);
                    queue.add(ds);
                }
            }
        };
        t.setName("getStoriesSimilarToTag-" + tag);
        t.start();
    }

    public void getTagsSimilarToTag(final TagInfo tagInfo, final int max) {
        Thread t = new Thread() {

            public void run() {
                List<TagInfo> tags = fetchTagsSimilarToTag(tagInfo, max);
                if (tags != null) {
                    int which = 1;
                    for (TagInfo simTag : tags) {
                        InteractivePoint cpoint = factory.createTagInfoTileStoryPoint(null, simTag, which++);
                        cpoint.add("home");
                        DelayedStory ds = new DelayedStory(cpoint, -1);
                        queue.add(ds);
                    }
                }
            }
        };
        t.setName("getTagInfo");
        t.start();
    }

    public void getTagsSimilarToTagForGraph(final TagInfo tagInfo, final int max) {
        Thread t = new Thread() {

            public void run() {
                List<TagInfo> tags = fetchTagsSimilarToTag(tagInfo, max);
                if (tags != null) {
                    int which = 1;
                    for (TagInfo simTag : tags) {
                        InteractivePoint cpoint = factory.createTagInfoTileStoryPoint(null, simTag, which++);
                        cpoint.add("home");
                        DelayedStory ds = new DelayedStory(cpoint, -1);
                        queue.add(ds);
                    }
                }
            }
        };
        t.setName("getTagInfo");
        t.start();
    }

    public void start() {
        queue.clear();
        t = new Thread() {

            public void run() {
                collector();
            }
        };
        t.start();
    }

    public void stop() {
        t = null;
    }

    public void setMaxStoriesPerMinute(int maxStories) {
        this.maxStoriesPerMinute = maxStories;
    }

    public void setShowTimes(boolean enable) {
        this.showTimes = enable;
    }

    public int getMaxStoriesPerMinute() {
        return maxStoriesPerMinute;
    }

    public boolean isAsyncMode() {
        return asyncMode;
    }

    public void setAsyncMode(boolean asyncMode) {
        this.asyncMode = asyncMode;
        if (asyncMode) {
            liveMode = false;
            lastStoryTime = 0;
        }
    }

    public boolean isLiveMode() {
        return liveMode;
    }

    public void setLiveMode(boolean liveMode) {
        this.liveMode = liveMode;
        if (liveMode) {
            asyncMode = false;
            // start an hour ago
            lastStoryTime = System.currentTimeMillis() - 60 * 60 * 3600;
        }
    }

    public void setStartTime(long time) {
        curTime = time;
    }

    private void collector() {
        int minutes = 0;
        int storyCount = 0;
        float avgStoriesPerMinute = 0;
        long baseTime = System.currentTimeMillis();

        long timeMax = 0;
        long timeMin = Long.MAX_VALUE;
        long timeSum = 0;
        long timeCount = 0;

        try {
            while (t != null) {
                long now = System.currentTimeMillis();
                List<Story> stories;

                // There are 3 collection modes:
                //  asyncMode - ignore when stories were added to the store, just
                //      add them at maxStoriesPerMinute - suitable when a new crawl is in place
                //  liveMode - collect the most recent stories added
                //  normalMode - collect stories from a particular period

                if (asyncMode) {
                    stories = collectStories(lastStoryTime + 1, 0, maxStoriesPerMinute);
                } else if (liveMode) {
                    // start from an hour ago
                    stories = collectStories(lastStoryTime, pollPeriod, maxStoriesPerMinute);
                //stories = collectStories(-1, -pollPeriod, maxStoriesPerMinute);
                } else {
                    stories = collectStories(curTime, pollPeriod, maxStoriesPerMinute);
                }

                storyCount += stories.size();
                minutes++;
                avgStoriesPerMinute = storyCount / (float) minutes;

                if (stories.size() > 0) {
                    int delta = (int) (pollPeriod / stories.size());
                    //System.out.print("autotags: ");
                    for (Story story : stories) {
                        //CPoint storyPoint = factory.createBoxStoryPoint(story);
                        //InteractivePoint cpoint = factory.createTileStoryPoint(story);
                        if (factory != null) {
                            InteractivePoint cpoint = factory.createHeadlineStoryPoint(story);
                            baseTime += delta;
                            DelayedStory ds = new DelayedStory(cpoint, baseTime);
                            queue.add(ds);
                        }
                        lastStoryTime = story.getPulltime();
                    //System.out.print(fmtAutoTags(story.getAutotags()));
                    //System.out.print(" " + story.getAutotags().size());
                    }
                    System.out.println();
                }
                curTime += pollPeriod;
                long elapsed = System.currentTimeMillis() - now;

                if (false && liveMode) {
                    // adjust the delay by 
                    Thread.sleep(pollPeriod - elapsed);
                } else {
                    Thread.sleep(1000L);  // mininum sleep is 1 sec
                    while (queue.size() > maxStoriesPerMinute * 2) {
                        Thread.sleep(pollPeriod);
                    }
                }

                if (showTimes) {
                    long delta = System.currentTimeMillis() - now;
                    timeCount++;
                    if (delta > timeMax) {
                        timeMax = delta;
                    }
                    if (delta < timeMin) {
                        timeMin = delta;
                    }
                    timeSum += delta;
                    long avg = timeSum / timeCount;


                    System.out.printf("GetStorys: %d  found: %d cur: %s  min: %d max: %d  avg: %d\n",
                            timeCount, stories.size(), delta, timeMin, timeMax, avg);

                }
            }
        } catch (InterruptedException ie) {

        }
    }

    public void stressTest(int loops, boolean collectAll) {
        long lastStoryTime = 0;
        int sumCalls = 0;
        int sumReturns = 0;

        for (int i = 0; i < loops; i++) {
            long now = System.currentTimeMillis();
            int totalCalls = 0;
            int totalReturns = 0;
            List<Story> stories = collectStories(lastStoryTime + 1, 0, maxStoriesPerMinute);
            totalCalls++;
            totalReturns += stories.size();
            if (stories.size() > 0) {
                for (Story story : stories) {
                    List<Story> simStories = fetchSimilarStories(story, 10);
                    totalCalls++;
                    totalReturns += simStories.size();
                    long time = System.currentTimeMillis() - now;
                    System.out.println("    calls: " + totalCalls + " objs " + totalReturns + " tot time " + time + " avg per call " + time / totalCalls);
                    if (collectAll) {
                        for (Story simStory : simStories) {
                            List<TagInfo> tags = fetchTagInfo(simStory, 10);
                            totalCalls++;
                            totalReturns += tags.size();
                            for (TagInfo tag : tags) {
                                List<Story> moreStories = fetchStoriesSimilarTag(tag.getTagName(), 10);
                                totalCalls++;
                                totalReturns += moreStories.size();
                            }
                        }
                    }
                    lastStoryTime = story.getPulltime();
                }
            }
            sumCalls += totalCalls;
            sumReturns += totalReturns;
            long delta = System.currentTimeMillis() - now;
            System.out.println("-----------");
            System.out.printf("STRESS TEST %d) Total calls: %d  Total objs: %d This: %d Time: %d ms\n", i,
                    sumCalls, sumReturns, totalCalls, delta);
            storyConnector.showTimes();
            findSimConnector.showTimes();
            miscConnector.showTimes();
            System.out.println("-----------");
        }
    }

    private String fmtAutoTags(List<ScoredString> tags) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (ScoredString c : tags) {
            sb.append(c.getName());
            sb.append(",");
        }
        sb.append("] ");
        return sb.toString();
    }

    private List<Story> collectStories(long startTime, long delta, int max) {

        try {
            URL url = null;

            String start;
            if (startTime < 0) {
                start = "now";
            } else {
                start = Long.toString(startTime);
            }
            String surl = baseUrl + "/GetStories" +
                    "?max=" + max +
                    "&time=" + start +
                    "&delta=" + delta;
            url = new URL(surl);

            System.out.println("collecting stories from " + url + " at " + new Date());

            InputStream is = storyConnector.open(url);
            List<Story> stories = Util.loadStories(is);
            storyConnector.close(is);
            // System.out.println("collected " + stories.size() + " stories");
            return stories;
        } catch (IOException ex) {
            Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<Story>();
        }
    }

    private List<Story> fetchSimilarStories(Story story, int max) {

        try {
            URL url = null;

            String key = URLEncoder.encode(story.getUrl(), "utf-8");
            String surl = baseUrl + "/FindSimilar" + "?max=" + max + "&key=" + key;
            url = new URL(surl);
            //System.out.println("URL is " + url);
            InputStream is = findSimConnector.open(url);
            List<Story> stories = Util.loadStories(is);
            findSimConnector.close(is);

            if (stories.size() > max) {
                stories = stories.subList(0, max);
            }
            return stories;
        } catch (IOException ex) {
            Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<Story>();
        }
    }

    private List<Story> queryForStories(String query, int max) {

        try {
            URL url = null;

            query = URLEncoder.encode(query, "utf-8");
            String surl = baseUrl + "/QueryEntries" + "?max=" + max + "&query=" + query;
            url = new URL(surl);
            System.out.println("URL is " + url);
            InputStream is = miscConnector.open(url);
            List<Story> stories = Util.loadStories(is);
            findSimConnector.close(is);

            if (stories.size() > max) {
                stories = stories.subList(0, max);
            }
            return stories;
        } catch (IOException ex) {
            Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<Story>();
        }
    }

    private List<Story> fetchStoriesSimilarTag(String tag, int max) {

        try {
            URL url = null;

            tag = URLEncoder.encode(tag, "utf-8");
            String surl = baseUrl + "/GetTaggedStories" +
                    "?max=" + max + "&tag=" + tag;
            url = new URL(surl);


            InputStream is = miscConnector.open(url);
            List<Story> stories = Util.loadStories(is);
            miscConnector.close(is);
            // System.out.println("fsst collected " + stories.size() + " stories");
            if (stories.size() > max) {
                stories = stories.subList(0, max);
            }
            return stories;
        } catch (IOException ex) {
            Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<Story>();
        }
    }

    private List<TagInfo> fetchTagInfo(Story story, int max) {

        try {
            URL url = null;

            String key = URLEncoder.encode(story.getUrl(), "utf-8");
            String surl = baseUrl + "/GetDocumentTags" +
                    "?max=" + max + "&key=" + key;
            url = new URL(surl);
            InputStream is = miscConnector.open(url);
            List<TagInfo> tagInfos = Util.loadTagInfo(is);
            miscConnector.close(is);
            // System.out.println("collected " + tagInfos.size() + " tagInfos");
            if (tagInfos.size() > max) {
                tagInfos = tagInfos.subList(0, max);
            }
            return tagInfos;
        } catch (IOException ex) {
            Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<TagInfo>();
        }
    }

    private List<TagInfo> fetchTagsSimilarToTag(TagInfo tag, int max) {

        try {
            URL url = null;
            StringBuilder sb = new StringBuilder();

            List<ScoredString> simTags = tag.getSimTags();

            if (simTags.size() > max) {
                simTags = simTags.subList(0, max);
            }

            for (int i = 0; i < simTags.size(); i++) {
                sb.append(simTags.get(i).getName());
                if (i < simTags.size() - 1) {
                    sb.append(",");
                }
            }

            String tags = sb.toString().trim();
            tags = URLEncoder.encode(tags, "utf-8");

            if (tags.length() > 0) {
                String surl = baseUrl + "/GetTagInfo" +
                        "?tag=" + tags;
                url = new URL(surl);
                InputStream is = miscConnector.open(url);
                List<TagInfo> tagInfos = Util.loadTagInfo(is);
                miscConnector.close(is);
                // System.out.println("collected " + tagInfos.size() + " tagInfos");
                if (tagInfos.size() > max) {
                    tagInfos = tagInfos.subList(0, max);
                }
                return tagInfos;
            }
            return null;
        } catch (IOException ex) {
            Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<TagInfo>();
        }
    }

    public void thumbsUp(Story story) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void thumbsDown(Story story) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void main(String[] args) throws Exception {
        StoryManager sm = new StoryManager(null, "http://www.aardvark.tastekeeper.com/DashboardWebServices");
        //StoryManager sm = new StoryManager(null, "http://localhost:8080/DashboardWebServices");
        sm.setAsyncMode(true);
        sm.setLiveMode(false);
        //sm.stressTest(10, false);
        sm.stressTest(100, false);
    }

    class InstrumentedConnector {

        private URL url;
        private String name;
        private int attempts;
        private int errors;
        private long totalTime;
        private long startTime;
        private long timeMax = 0l;
        private long timeMin = Long.MAX_VALUE;

        InstrumentedConnector(String name) {
            this.name = name;
        }

        InputStream open(URL url) throws IOException {
            startTime = System.currentTimeMillis();

            if (showConnections) {
                System.out.println(name + " -> " + url);
            }

            this.url = url;
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            InputStream is = connection.getInputStream();
            return new BufferedInputStream(is);
        }

        void close(InputStream is) throws IOException {
            close(is, true);
        }

        void close(InputStream is, boolean ok) throws IOException {
            try {
                if (!ok) {
                    errors++;
                }
                is.close();
            } finally {
                accumTimes();
            }
        }

        private void accumTimes() {
            long delta = System.currentTimeMillis() - startTime;
            attempts++;
            if (delta > timeMax) {
                timeMax = delta;
            }
            if (delta < timeMin) {
                timeMin = delta;
            }
            totalTime += delta;

            if (showTimes) {
                showTimes();
            }
        }

        void showTimes() {
            if (attempts > 0) {
                long avg = totalTime / attempts;
                System.out.printf("%s conns: %d min: %d max: %d  avg: %d errors: %d\n",
                        name, attempts, timeMin, timeMax, avg, errors);
            }
        }
    }
}

class DelayedStory implements Delayed {

    private InteractivePoint storyPoint;
    private long processingTime;

    DelayedStory(InteractivePoint cpoint, long processingTime) {
        this.storyPoint = cpoint;
        this.processingTime = processingTime;
    }

    public InteractivePoint getStory() {
        return storyPoint;
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(processingTime - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS);
    }

    public int compareTo(Delayed o) {
        long result = getDelay(TimeUnit.MILLISECONDS) -
                o.getDelay(TimeUnit.MILLISECONDS);
        return result < 0 ? -1 : result > 0 ? 1 : 0;
    }

    public String toString() {
        return "delayed story " + processingTime;
    }
}
