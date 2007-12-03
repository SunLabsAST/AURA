/*
 * AardvarkServiceImpl.java
 *
 * Created on November 5, 2007, 1:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.Stats;
import com.sun.labs.aura.aardvark.client.AardvarkService;
import com.sun.labs.aura.aardvark.client.WiEntrySummary;
import com.sun.labs.aura.aardvark.client.WiFeed;
import com.sun.labs.aura.aardvark.client.WiStats;
import com.sun.labs.aura.aardvark.client.WiUser;
import com.sun.labs.aura.aardvark.client.WiUserStatus;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 *
 * @author plamere
 */
public class AardvarkServiceImpl extends RemoteServiceServlet implements AardvarkService {
    private Aardvark aardvark;
    private final static Attention.Type[] feedTypes = {Attention.Type.STARRED_FEED, 
                Attention.Type.SUBSCRIBED_FEED, 
                Attention.Type.DISLIKED_FEED};
    private final static WiFeed[] EMPTY_WI_FEED = new WiFeed[0];

    @Override
    public void init(ServletConfig sc) throws ServletException {
        try {
            super.init(sc);
            aardvark = Aardvark.getDefault();
            sc.getServletContext().setAttribute("aardvark", aardvark);
            aardvark.startup();

            // pre-enroll a test user, just to make testing the web
            // interface a bit easier.

            if (aardvark.getUser("test") == null) {
                aardvark.enrollUser("test",
                        "http://www.google.com/reader/public/atom/user/07268145224739680674/state/com.google/starred");
            }
        } catch (AuraException ex) {
            aardvark = null;
            Logger.getLogger(AardvarkServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException("Can't load the aardvark engine", ex);
        }
    }

    public void destroy() {
        if (aardvark != null) {
            try {
                aardvark.shutdown();
            } catch (AuraException ex) {
                Logger.getLogger(AardvarkServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public WiStats getStats() {
        try {
            Stats stats = aardvark.getStats();
            return new WiStats(stats.getVersion(), stats.getNumUsers(), stats.getNumItems(), 
                    stats.getNumAttentionData(), stats.getNumFeeds(), 
                    stats.getFeedPullCount(), stats.getFeedErrorCount(),
                    Runtime.getRuntime().totalMemory());
        } catch (AuraException ex) {
            Logger.getLogger(AardvarkServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return new WiStats();
        }
    }

    public WiUserStatus registerUser(String name, String feed) {
        WiUserStatus wus = null;
        try {
            User user = aardvark.enrollUser(name, feed);
            WiUser wiUser = userToWiUser(user);
            wus = new WiUserStatus(null, wiUser);
            return wus;
        } catch (AuraException ex) {
            String status = ex.getMessage();
            wus = new WiUserStatus(status, null);
            return wus;
        }
    }

    public WiUserStatus loginUser(String name) {
        WiUserStatus wus = null;
        try {
            User user = aardvark.getUser(name);
            if (user != null) {
                WiUser wiUser = userToWiUser(user);
                wus = new WiUserStatus(null, wiUser);
            } else {
                wus = new WiUserStatus("Can't find user " + name, null);
            }
        } catch (AuraException ex) {
            wus = new WiUserStatus("Can't find user " + name, null);
        }
        return wus;
    }

    private static WiUser userToWiUser(User user) {
        WiUser wiUser = new WiUser(user.getKey(), user.getID(),
                user.getRecommenderFeedKey(), 0);
        return wiUser;
    }

    public WiEntrySummary[] getRecommendations(String name) {
        try {
            User user = aardvark.getUser(name);
            if (user != null) {
                SyndFeed feed = aardvark.getRecommendedFeed(user);
                WiEntrySummary[] summaries = new WiEntrySummary[feed.getEntries().size()];
                int index = 0;
                for (Object syndEntryObject : feed.getEntries()) {
                    SyndEntry syndEntry = (SyndEntry) syndEntryObject;
                    String title = syndEntry.getTitle();
                    String link = syndEntry.getLink();
                    summaries[index++] = new WiEntrySummary(title, link);
                }
                return summaries;
            } else {
                return new WiEntrySummary[0];
            }
        } catch (AuraException ex) {
            Logger.getLogger(AardvarkServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return new WiEntrySummary[0];
        }
    }

    public WiFeed[] getFeeds(String name) {
        List<WiFeed> feeds = new ArrayList<WiFeed>();
        try {
            User user = aardvark.getUser(name);
            for (Attention.Type feedType : feedTypes) {
                for (Feed feed : user.getFeeds(feedType)) {
                    WiFeed wiFeed = new WiFeed(feed.getKey(), null, feed.getID(), feedType.toString());
                    feeds.add(wiFeed);
                }
            }
        } catch (AuraException ex) {
            Logger.getLogger(AardvarkServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return EMPTY_WI_FEED;
        }
        return feeds.toArray(EMPTY_WI_FEED);
    }
}
