/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.  Which I did, which is why this text is here.
 */
package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
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
        String content = "(empty)";
        if (entry.getContents().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Object c : entry.getContents()) {
                SyndContent sc = (SyndContent) c;
                sb.append(sc.getValue());
            }
            content = sb.toString();
        } else {
            SyndContent sc = entry.getDescription();
            if (sc != null) {
                content = sc.getValue();
            } else {
                content = entry.getTitle();
            }
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
     * Takes a list of Entry objects and returns the corresponding list
     * of SyndEntry objects
     * @param entries the list of Entry objects
     * @return a list of SyndEntry objects
     */
    public static List<SyndEntry> getSyndEntries(List<Entry> entries) {
        List<SyndEntry> list = new ArrayList<SyndEntry>();
        for (Entry e : entries) {
            try {
                list.add(e.getSyndEntry());
            } catch (AuraException ex) {
                // Need a logger in here at least.
                // TODO: Paul should check how to handle this.
            }
        }
        return list;
    }

    /**
     * Converts an entry to a string representation
     * @param entry the entry to be converted
     * @return a string representation of the entry
     * @throws AuraException if an error occurs during the conversion
     */
    public static String toString(SyndEntry entry) throws AuraException {

        try {
            SyndFeed feed = new SyndFeedImpl();
            SyndFeedOutput output = new SyndFeedOutput();
            feed.setFeedType("atom_1.0");
            List<SyndEntry> entries = new ArrayList<SyndEntry>();
            entries.add(entry);
            feed.setEntries(entries);
            return output.outputString(feed);
        } catch (FeedException ex) {
            throw new AuraException("Can't convert entry to string", ex);
        }
    }

    /**
     * Converts a string representation of an entry into a SyndEntry
     * @param xml the string representation of the entry
     * @return the entry
     * @throws AuraException if an error occurs during the conversion
     */
    public static SyndEntry toSyndEntry(String xml) throws AuraException {
        try {
            SyndFeedInput syndFeedInput = new SyndFeedInput();
            Reader stringReader = new StringReader(xml);
            SyndFeed feed = syndFeedInput.build(stringReader);
            List entries = feed.getEntries();
            if (entries.size() == 1) {
                return (SyndEntry) entries.get(0);
            } else {
                throw new AuraException("Unexpected feed size");
            }
        } catch (FeedException ex) {
            throw new AuraException("Feed processing exception", ex);
        }
    }


    /**
     * Determines if the given RSS entry is fresh
     * @param syndEntry the rss entry
     * @param lastRefreshTime the time that we last checked this feed
     * @return true if the entry is newer than the last refresh time
     */
    public static boolean isFresh(SyndEntry syndEntry, long lastRefreshTime) {
        Date date = syndEntry.getPublishedDate();
        if (date != null) {
            return date.getTime() - lastRefreshTime > 0L;
        } else {
            return true;
        }
    }
}
