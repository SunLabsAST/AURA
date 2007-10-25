/*
 * FeedFetcher.java
 *
 * Created on Oct 24, 2007, 3:57:00 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author plamere
 */
public abstract class FeedFetcher {

    ItemStore itemStore = null;

    abstract void start();

    abstract void stop();
    private Set<URL> feeds = Collections.synchronizedSet(new HashSet<URL>());

    void newProperties() {
        itemStore.addItemListener("user", new MyListener());
    }

    void run() {
    }

    class MyListener implements ItemListener {

        public void created(Item[] items) {
            checkForNewFeeds(items);
        }

        public void changed(Item[] items) {
            checkForNewFeeds(items);
        }
        
        private void checkForNewFeeds(Item[] items) {
            for (Item item : items) {
                User user = (User) item;
                URL feed = user.getStarredItemFeedURL();
                if (!feeds.contains(feed)) {
                    feeds.add(feed);
                }
            }
        }

        public void deleted(Item[] items) {
           // TODO 
        }
    }
}