/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.sun.labs.aura.aardvark.dashboard.story.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
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
    private String simulatedDataPath = "samplestories.xml";
    private boolean simulate = false;
    private boolean asyncMode = false;
    private boolean liveMode = true;

    public StoryManager(StoryPointFactory factory, String baseUrl, boolean simulate) {
        this.baseUrl = baseUrl;
        this.factory = factory;
        this.simulate = simulate;
    }

    public StoryPoint getNext() {
        DelayedStory ds = queue.poll();
        if (ds != null) {
            return ds.getStory();
        }
        return null;
    }

    public void findSimilar(final Story story, final int max) {
        Thread t = new Thread() {
            public void run() {
                List<Story> stories = findSimilarStories(story, max);
                int which = 0;
                System.out.println("FindSim " + story.getTitle());
                for (Story similarStory : stories) {
                    StoryPoint cpoint = factory.createTileStoryPoint(similarStory, which++);
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
        }
    }

    public boolean isLiveMode() {
        return liveMode;
    }

    public void setLiveMode(boolean liveMode) {
        this.liveMode = liveMode;
        if (liveMode) {
            asyncMode = false;
        }
    }

    public void setStartTime(long time) {
        curTime = time;
    }

    void collector() {
        int minutes = 0;
        int storyCount = 0;
        float avgStoriesPerMinute = 0;
        long lastStoryTime = 0;
        long baseTime = System.currentTimeMillis();

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
                    stories = collectStories(-1, -pollPeriod, maxStoriesPerMinute);
                } else {
                    stories = collectStories(curTime, pollPeriod, maxStoriesPerMinute);
                }

                storyCount += stories.size();
                minutes++;
                avgStoriesPerMinute = storyCount / (float) minutes;

                if (stories.size() > 0) {
                    int delta = (int) (pollPeriod / stories.size());
                    for (Story story : stories) {
                        //CPoint storyPoint = factory.createBoxStoryPoint(story);
                        //StoryPoint cpoint = factory.createTileStoryPoint(story);
                        StoryPoint cpoint = factory.createHeadlineStoryPoint(story);
                        baseTime += delta;
                        DelayedStory ds = new DelayedStory(cpoint, baseTime);
                        queue.add(ds);
                        lastStoryTime = story.getPulltime();
                    }
                }
                curTime += pollPeriod;
                long elapsed = System.currentTimeMillis() - now;

                if (liveMode) {
                    Thread.sleep(pollPeriod - elapsed);
                } else {
                    Thread.sleep(1000L);  // mininum sleep is 1 sec
                    while (queue.size() > maxStoriesPerMinute * 2) {
                        Thread.sleep(pollPeriod);
                    }
                }
            }
        } catch (InterruptedException ie) {

        }
    }

    private List<Story> collectStories(long startTime, long delta, int max) {

        try {
            URL url = null;

            if (simulate) {
                url = StoryManager.class.getResource(simulatedDataPath);
            } else {
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
            }

            System.out.println("collecting stories from " + url);

            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            InputStream is = connection.getInputStream();
            List<Story> stories = Util.loadStories(is);
            is.close();
            System.out.println("collected " + stories.size() + " stories");
            return stories;
        } catch (IOException ex) {
            Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<Story>();
        }
    }

    public List<Story> findSimilarStories(Story story, int max) {

        try {
            URL url = null;

            if (simulate) {
                url = StoryManager.class.getResource(simulatedDataPath);
            } else {
                String key = URLEncoder.encode(story.getUrl(), "utf-8");
                String surl = baseUrl + "/FindSimilar" +
                        "?max=" + max + "&key=" + key;
                url = new URL(surl);
            }


            System.out.println("find sim: " + url);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            InputStream is = connection.getInputStream();
            List<Story> stories = Util.loadStories(is);
            is.close();
            System.out.println("collected " + stories.size() + " stories");
            if (stories.size() > max) {
                stories = stories.subList(0, max);
            }
            return stories;
        } catch (IOException ex) {
            Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<Story>();
        }
    }

    public void thumbsUp(Story story) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void thumbsDown(Story story) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

class DelayedStory implements Delayed {

    private StoryPoint storyPoint;
    private long processingTime;

    DelayedStory(StoryPoint cpoint, long processingTime) {
        this.storyPoint = cpoint;
        this.processingTime = processingTime;
    }

    public StoryPoint getStory() {
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
