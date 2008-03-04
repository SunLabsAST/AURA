/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.  Which I did, which is why this text is here.
 */
package com.sun.labs.aura.aardvark.impl.crawler;

import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of utilities for manipulating RSS feeds
 * @author plamere
 */
public class FeedUtils {

    private final static boolean debug = false;

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
    public static List<SyndEntry> getSyndEntries(List<BlogEntry> entries) throws AuraException {
        List<SyndEntry> list = new ArrayList<SyndEntry>();
        for (BlogEntry e : entries) {
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

    /**
     * Given a URL to an RSS feed, extract the entries, 
     * @param feed the feed to process
     * @return a list of fresh entries
     * @throws com.sun.labs.aura.aardvark.util.AuraException if a proble occurs while retriveing the feed
     */
    public static List<BlogEntry> processFeed(BlogFeed feed) throws AuraException, RemoteException {
        try {
            URL feedUrl = new URL(feed.getURL());
            SyndFeed syndFeed = readFeed(feedUrl);

            feed.setName(syndFeed.getTitle());
            feed.setLink(syndFeed.getLink());
            
            List<BlogEntry> entries = new ArrayList<BlogEntry>();
            List entryList = syndFeed.getEntries();
            for (Object o : entryList) {
                SyndEntry syndEntry = (SyndEntry) o;
                BlogEntry entry = convertSyndEntryToFreshEntry(feed, syndEntry);
                if (entry != null) {
                    if (debug) {
                        System.out.println("   Adding entry " + entry.getKey());
                    }
                    entries.add(entry);

                    if (debug) {
                        System.out.println("   Done entry " + entry.getKey());
                    }
                }
            }
            return entries;
        } catch (MalformedURLException ex) {
            throw new AuraException("bad url " + feed.getURL(), ex);
        }
    }

    /**
     * Determines if the list of entries came from an aggregated feed
     * @param entries the entries to check
     * @return true if some of  entries  come from different hosts
     */
    public static boolean isAggregatedFeed(List<BlogEntry> entries) {
        String lastHost = null;
        String link = null;
        for (BlogEntry entry : entries) {
            try {
                link = entry.getSyndEntry().getLink();
                if (link != null) {
                    URL url = new URL(link);
                    String host = url.getHost();
                    if (host != null) {
                        if (lastHost == null) {
                            lastHost = host;
                        } else {
                            if (!lastHost.equals(host)) {
                                return true;
                            }
                        }
                    }
                }
            } catch (MalformedURLException ex) {
            // silently ignore bad URLs
            }
        }
        return false;
    }

    /**
     * Converts the syndEntry into a BlogEntry 
     * @param feed the owning feed
     * @param syndEntry the feed entry
     * @return the itemstore entry or null if the syndEntry was a duplicate
     * @throws com.sun.labs.aura.aardvark.util.AuraException if an error occurs while accesing the itemstore
     * @throws java.rmi.RemoteException if there is an error communicating with the item store.
     */
    public static BlogEntry convertSyndEntryToFreshEntry(BlogFeed feed, SyndEntry syndEntry) throws AuraException, RemoteException {
        String key = getKey(syndEntry);
        String title = syndEntry.getTitle();
        BlogEntry entry = new BlogEntry(key, title);

        List categories = syndEntry.getCategories();

        if (categories != null) {
            for (Object o : categories) {
                SyndCategory category = (SyndCategory) o;
                entry.addTag(category.getName(), 1);
            }
        }

        String author = syndEntry.getAuthor();
        if (author != null) {
            entry.setAuthor(author);
        }

        entry.setSyndEntry(syndEntry);
        entry.setContent(getContent(syndEntry));
        entry.setFeedKey(feed.getKey());
        return entry;
    }

    /**
     * Given a URL, returns the SyndFeed
    //     * @param url the url
     * @return the feed 
     * @throws com.sun.labs.aura.aardvark.util.AuraException if an error occurs while 
     * loading or parsing the feed
     */
    public static SyndFeed readFeed(URL url) throws AuraException {
        URLConnection connection = null;
        try {
            SyndFeedInput syndFeedInput = new SyndFeedInput();
            connection = url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setRequestProperty("User-agent", "aardvark");
            return syndFeedInput.build(new XmlReader(connection));
        } catch (IOException ex) {
            throw new AuraException("I/O error while reading " + url, ex);
        } catch (FeedException ex) {
            throw new AuraException("feed error while reading " + url, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.getInputStream().close();
                } catch (IOException ex) {
                // Logger.getLogger(FeedUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Given a URL to some html, find the associated RSS feeds
     * @param url the url to the html
     * @return a list of URLs to the associated feeds
     * @throws java.io.IOException if an error occurs while retrieving or parsing the html
     */
    public static List<URL> findFeeds(URL url) throws IOException {
        try {
            List<URL> feeds = new ArrayList<URL>();
            URI uri = url.toURI();

            List<String> links = getLinkElements(url);
            for (String link : links) {
                Map<String, String> attributes = getAttributes(link);
                String rel = attributes.get("rel");
                if (rel != null) {
                    rel = rel.toLowerCase();
                    if (rel.contains("alternate")) {
                        String type = attributes.get("type");
                        if (type != null) {
                            type = type.toLowerCase();
                            if (type.contains("rss") || type.contains("atom")) {
                                String href = attributes.get("href");
                                if (href != null) {
                                    URI resolvedURI = uri.resolve(href);
                                    URL hrefURL = resolvedURI.toURL();
                                    feeds.add(hrefURL);
                                }
                            }
                        }
                    }
                }
            }
            return feeds;
        } catch (URISyntaxException ex) {
            throw new IOException("bad URI syntax found: " + url);
        }
    }

    /**
     * Given a URL, return all of the link elements contained with the resource
     * @param url the url
     * @return a list of all of the link elements
     */
    private static List<String> getLinkElements(URL url) {
        InputStream is = null;
        List<String> elements = new ArrayList<String>();
        try {
            is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            int c;
            while ((c = br.read()) != -1) {
                sb.append((char) c);
            }
            br.close();
            String content = sb.toString();

            String linkregex = "<link[^>]*>";
            Pattern linkPattern = Pattern.compile(linkregex, Pattern.CASE_INSENSITIVE);
            Matcher linkMatcher = linkPattern.matcher(content);
            while (linkMatcher.find()) {
                elements.add(linkMatcher.group());
            }
        } catch (IOException ex) {
        // silently ignore bad urls
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
            // silently ignore bad urls
            }
        }
        return elements;
    }

    /**
     * Given an element, return a map of the attribute/value pairs contained within the element
     * @param element the element
     * @return the (possibly empy) map of attribute/value pairs
     */
    private static Map<String, String> getAttributes(String element) {
        Map<String, String> map = new HashMap<String, String>();
        String attributeregex = "\\s(\\w+)=['\"]([^'\"]*)['\"]";
        Pattern attributePattern = Pattern.compile(attributeregex, Pattern.CASE_INSENSITIVE);
        Matcher attrMatcher = attributePattern.matcher(element);
        while (attrMatcher.find()) {
            map.put(attrMatcher.group(1).toLowerCase(), attrMatcher.group(2));
        }
        return map;
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
