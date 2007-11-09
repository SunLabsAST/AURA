/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.  Which I did, which is why this text is here.
 */
package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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
            list.add(e.getSyndEntry());
        }
        return list;
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

    public static List<Entry> processFeed(ItemStore itemStore, URL feedUrl) throws AuraException {
        SyndFeed feed = readFeed(feedUrl);
        List<Entry> entries = new ArrayList<Entry>();
        List entryList = feed.getEntries();
        for (Object o : entryList) {
            SyndEntry syndEntry = (SyndEntry) o;
            Entry entry = convertSyndEntryToFreshEntry(itemStore, syndEntry);
            if (entry != null) {
                entries.add(entry);
            }
        }
        return entries;
    }

    public static Entry convertSyndEntryToFreshEntry(ItemStore itemStore, SyndEntry syndEntry) throws AuraException {
        String key = getKey(syndEntry);
        if (itemStore.get(key) == null) {
            Entry entry = itemStore.newItem(Entry.class, key);
            entry.setSyndEntry(syndEntry);
            entry.setContent(getContent(syndEntry));
            itemStore.put(entry);
            return entry;
        } else {
            return null;
        }
    }

    public static SyndFeed readFeed(URL url) throws AuraException {
        try {
            SyndFeedInput syndFeedInput = new SyndFeedInput();
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-agent", "aardvark");
            return syndFeedInput.build(new XmlReader(connection));
        } catch (IOException ex) {
            throw new AuraException("I/O error while reading " + url, ex);
        } catch (FeedException ex) {
            throw new AuraException("feed error while reading " + url, ex);
        }
    }

    public static void dumpFeed(String surl) {
        System.out.println("Trying " + surl);
        SyndFeedInput syndFeedInput = new SyndFeedInput();
        try {
            URL url = new URL(surl);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-agent", "aardvark");
            SyndFeed feed = syndFeedInput.build(new XmlReader(connection));

            System.out.println("---" + feed.getTitle() + "---");
            System.out.println("  link " + feed.getLink());
            List entryList = feed.getEntries();
            for (Object o : entryList) {
                SyndEntry syndEntry = (SyndEntry) o;
                System.out.println(" " + syndEntry.getTitle());
            }
        } catch (IOException ex) {
            System.out.println("I/O Exception while dumping " + surl + " " + ex);
        } catch (FeedException ex) {
            System.out.println("Feed Exception while dumping " + surl + " " + ex);
        }
    }
}
