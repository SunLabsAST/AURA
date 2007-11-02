/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.  Which I did, which is why this text is here.
 */

package com.sun.labs.aura.aardvark;

import com.sun.labs.aura.aardvark.crawler.FeedCrawler;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author plamere
 */
public class Aardvark {
    private ItemStore itemStore;
    private FeedCrawler feedCrawler;


    public void startup() {
        feedCrawler.start();
    }

    public void shutdown() {
        feedCrawler.stop();
    }

    public User getUser(String openID) {
        User user = (User) itemStore.get(openIDtoKey(openID));
        return user;
    }

    public User enrollUser(String openID, String feed) throws AuraException {
        try {
            User user = (User) itemStore.newItem(User.class, openIDtoKey(openID));
            user.setStarredItemFeedURL(new URL(feed));
            return user;
        } catch (MalformedURLException ex) {
            throw new AuraException("Bad starred item feed url" + ex);
        }
    }

    public Entry[] getRecommendedEntries(String userID) {
        User user = getUser(userID);
        return null;
    }

    private String openIDtoKey(String openID) {
        return "User:" + openID;        // TODO: this is a bug
    }
}
