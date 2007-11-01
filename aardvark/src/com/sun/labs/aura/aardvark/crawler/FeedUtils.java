/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.  Which I did, which is why this text is here.
 */
package com.sun.labs.aura.aardvark.crawler;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import java.util.List;

/**
 * A set of utilities for manipulating RSS feeds
 * @author plamere
 */
public class FeedUtils {

    /**
     * Gets the contents of an entry
     * @param entry the RSS entry
     * @return the contents as a string (which may have embedded html)
     */
    public static String getContent(SyndEntry entry) {
        String content = null;
        List contents = entry.getContents();
        if (entry.getContents().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Object c : entry.getContents()) {
                SyndContent sc = (SyndContent) c;
                sb.append(sc.getValue());
            }
            content = sb.toString();
        } else {
            SyndContent sc = entry.getDescription();
            content = sc.getValue();
        }
        return content;
    }

    /**
     * Gets the key for an RSS entry
     * @param syndEntry the entry
     * @return a unique key
     */
    public static String getKey(SyndEntry syndEntry) {
        String key = syndEntry.getLink();
        if (key == null) {
            key = syndEntry.getTitle();
        }

        if (key == null) {
            // TODO: we want to guarantee that the key is never null
            // but what is the best way to do that?  Using the hashcode
            // is particularly unsatisfying.
            key = Integer.toString(syndEntry.hashCode());
        }
        return key;
    }

    /**
     * Determines if the given RSS entry is fresh
     * @param syndEntry the rss entry
     * @param lastRefreshTime the time that we last checked this feed
     * @return true if the entry is newer than the last refresh time
     */
    public static  boolean isFresh(SyndEntry syndEntry, long lastRefreshTime) {
        return syndEntry.getPublishedDate().getTime() - lastRefreshTime > 0L;
    }
}
