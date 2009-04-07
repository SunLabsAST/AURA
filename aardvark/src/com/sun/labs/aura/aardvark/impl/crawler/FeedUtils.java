/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.aardvark.impl.crawler;

import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of utilities for manipulating RSS feeds
 * @author plamere
 */
public class FeedUtils {

    private final static boolean debug = false;
    private static Logger logger =  Logger.getLogger("com.sun.labs.aura.aardvark.impl.crawler.FeedUtils");

    enum DocType {
        HTML, XML, OTHER
    }

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
        return key.trim();
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
    public static List<BlogEntry> processFeed(BlogFeed feed, SyndFeed syndFeed) throws IOException, AuraException, RemoteException {
        try {

            if (syndFeed == null) {
                URL feedUrl = new URL(feed.getPullLink());
                syndFeed = readFeed(feedUrl);
            }

            feed.setName(syndFeed.getTitle());
            feed.setDescription(syndFeed.getDescription());

            if (syndFeed.getImage() != null) {
                feed.setImage(syndFeed.getImage().getUrl());
            }

            List<BlogEntry> entries = new ArrayList<BlogEntry>();
            List entryList = syndFeed.getEntries();
            for (Object o : entryList) {
                //System.out.println("entry " + o);
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
            throw new AuraException("bad url " + feed.getPullLink() + " for " + feed.getCannonicalURL(), ex);
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

        if (title != null) {
            title = title.trim();
        }

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

        Date publishedDate = syndEntry.getPublishedDate();
        if (publishedDate == null) {
            publishedDate = new Date();
        }

        entry.setPublishDate(publishedDate);
        entry.setSyndEntry(syndEntry);
        entry.setContent(getContent(syndEntry));
        entry.setFeedKey(feed.getKey());
        return entry;
    }

    static String normalizeURL(String url) {
        return url.trim();
    }

    static String normalizeAnchorText(String text) {
        return text.replaceAll("\\s+", " ").toLowerCase().trim();
    }

    /**
     * Given a URL, returns the SyndFeed
    //     * @param surl the surl
     * @return the feed 
     * @throws com.sun.labs.aura.aardvark.util.AuraException if an error occurs while 
     * loading or parsing the feed
     */
    public static SyndFeed readFeed(URL url) throws IOException {
        URLConnection connection = null;
        try {
            SyndFeedInput syndFeedInput = new SyndFeedInput();
            connection = openConnection(url, "read-feed");
            return syndFeedInput.build(new XmlReader(connection));
        } catch (FeedException ex) {
            throw new IOException("feed error while reading " + url);
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

    public static WireFeed readWireFeed(URL url) throws AuraException {
        URLConnection connection = null;
        try {
            WireFeedInput wireFeedInput = new WireFeedInput();
            connection = openConnection(url, "read-wire-feed");
            return wireFeedInput.build(new XmlReader(connection));
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
     * @param surl the surl to the html
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
                                    try {
                                        href = href.trim();
                                        if (href.length() > 8) {
                                            URI resolvedURI = uri.resolve(href);
                                            URL hrefURL = resolvedURI.toURL();
                                            feeds.add(hrefURL);
                                        }
                                    } catch (Exception e) {
                                        throw new IOException("Warning  = couldn't resolve URL " + e);
                                    }
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
     * Give the URL find the best feed associated with the URL.  If the URL
     * is already a feed, just return the url
     * @param url the feed to search for the best url
     * @return the feed
     * @throws java.io.IOException
     */
    public static URL findBestFeed(String surl) throws IOException {
        URL url = new URL(surl);
        DocType type = getDocType(url);
        if (type == DocType.XML) {
            return url;
        } else if (type == DocType.HTML) {
            List<URL> feeds = findFeeds(url);
            if (feeds.size() > 0) {
                return feeds.get(0);
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * Determines if the content at the given url is xml content
     * @param url
     * @return
     */
    private static DocType getDocType(URL url) throws IOException {
        DocType docType = DocType.OTHER;
        URLConnection connection = openConnection(url, false, "getDocType");
        String type = connection.getContentType();
        if (type != null) {
            type = type.toLowerCase();
        }

        if (type != null) {
            if (type.indexOf("xml") >= 0) {
                docType = DocType.XML;
            } else if (type.indexOf("html") >= 0) {
                docType = DocType.HTML;
            }
        }
        if (false) { // debug
            if (docType == DocType.OTHER) {
                System.out.println("SKIPPING  Unknown type " + type + " for " + url);
            }
        }
        return docType;
    }

    static URLConnection openConnection(URL url, String reason) throws IOException {
        return openConnection(url, true, reason);
    }

    static URLConnection openConnection(URL url, boolean fullPull, String reason) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-agent", "aardvark-crawler");
        connection.setReadTimeout(30000);
        connection.setConnectTimeout(30000);
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setInstanceFollowRedirects(true);
        }
        if (fullPull) {
            logger.info("connection: " + reason + " " + url.toString());
        }
        return connection;
    }

    /**
     * Given a URL, return all of the link elements contained with the resource
     * @param surl the surl
     * @return a list of all of the link elements
     */
    private static List<String> getLinkElements(URL url) {
        InputStream is = null;
        List<String> elements = new ArrayList<String>();
        try {
            URLConnection connection = openConnection(url, "get-link-elemens");

            //System.out.println("   loading " + surl.toExternalForm());
            // System.out.println("con type is " + connection.getContentType() + " for " + surl);

            if (isHtmlContent(connection)) {

                is = connection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();

                int c;
                while ((c = br.read()) != -1 && sb.length() < 100000) {
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

    private static boolean isHtmlContent(URLConnection connection) {
        String contentType = connection.getContentType();
        if (contentType != null) {
            return contentType.startsWith("text/html");
        }
        return false;
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
            URLConnection connection = openConnection(url, "dump-feed");
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

    public static void feedReadTest(String url) {
        try {
            URL feedUrl = new URL(url);
            {
                SyndFeed syndFeed = readFeed(feedUrl);

                System.out.println("Feed link: " + syndFeed.getLink());
                System.out.println("Feed: " + syndFeed);

                List entryList = syndFeed.getEntries();
                for (Object o : entryList) {
                    SyndEntry syndEntry = (SyndEntry) o;
                    System.out.println("Entry: " + o);
                }
            }


            {
                WireFeed wireFeed = readWireFeed(feedUrl);
                System.out.println("wiredfeed " + wireFeed);
            }
        } catch (IOException ex) {
            System.out.println("IOexception " + ex);
        } catch (AuraException ex) {
            System.out.println("Aura exception " + ex);
        }
    }

    public static String showCanonicalTitle(URL feedURL) {
        try {
            SyndFeed syndFeed = readFeed(feedURL);
            String link = syndFeed.getLink();
            String label = "OK";

            if (link == null) {
                link = feedURL.toExternalForm();
                label = "no";
            }
            System.out.printf("%3s %-60s %-60s\n", label, link, feedURL.toExternalForm());
            return link;
        } catch (IOException ex) {
            System.out.println("   error loading " + feedURL.toExternalForm());
        }
        return null;
    }

    public static String getCanonicalTitle(URL feedURL) {
        try {
            SyndFeed syndFeed = readFeed(feedURL);
            String link = syndFeed.getLink();
            String label = "OK";

            if (link == null) {
                link = feedURL.toExternalForm();
                label = "no";
            }
            System.out.printf("%3s %-60s %-60s\n", label, link, feedURL.toExternalForm());
            return link;
        } catch (IOException ex) {
            System.out.println("   error loading " + feedURL.toExternalForm());
        }
        return null;
    }

    public static void showCanonicalTitles(String opml) {
        Set<String> skipSet = new HashSet<String>();
        try {
            OPMLProcessor op = new OPMLProcessor();

            List<URL> feedURLs = op.getFeedURLs(new File(opml));
            for (URL url : feedURLs) {
                if (!skipSet.contains(url.toExternalForm())) {
                    skipSet.add(url.toExternalForm());
                    SyndFeed synFeed = readFeed(url);

                    String canonicalLink = showCanonicalTitle(url);
                    if (canonicalLink != null) {
                        if (!skipSet.contains(canonicalLink)) {
                            skipSet.add(canonicalLink);
                            List<URL> feeds = findFeeds(new URL(canonicalLink));
                            for (URL feedURL : feeds) {
                                System.out.println("    feed: " + feedURL);
                            }
                        } else {
                            System.out.println("    dup");
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Can't load opml " + opml);
        }
    }

    public static List<Anchor> extractAnchors(String htmlContent) {
        List<Anchor> anchors = new ArrayList<Anchor>();
        //String anchorRegex = "<a\\s+href=\\s+\"(http://[^\"]*)\"\\s*>([^<]+)</a>";
        String anchorRegex = "<a\\s+href=\\s*\"(http://[^\"]*)\"\\s*>([^<]*)</a>";
        Pattern anchorPattern = Pattern.compile(anchorRegex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher anchorMatcher = anchorPattern.matcher(htmlContent);
        while (anchorMatcher.find()) {
            String url = normalizeURL(anchorMatcher.group(1));
            if (url.length() > 8) {
                String anchorText = normalizeAnchorText(anchorMatcher.group(2));
                Anchor anchor = new Anchor(url, anchorText);
                anchors.add(anchor);
            }
        }
        return anchors;
    }

    private static boolean isGoodToVisit(String surl) {
        String[] filteredExtensions = {".mp3", ".mp4", "jpg", "gif", ".png", "avi", "qt"};
        if (surl == null) {
            return false;
        }

        surl = surl.toLowerCase();
        for (String ext : filteredExtensions) {
            if (surl.endsWith(ext)) {
                return false;
            }
        }


        try {
            URL url = new URL(surl);
            if (url.getHost() == null) {
                return false;
            }

            if (url.getHost().length() == 0) {
                return false;
            }
        } catch (MalformedURLException ex) {
            return false;
        }
        return robots.isCrawlable(surl);
    }
    static RobotsManager robots = new RobotsManager("aardvark-crawler", 100000, Logger.getAnonymousLogger());
    // this is some experimental code - not for general use
    public static void simpleCrawler(String html) {
        try {
            int feedCount = 0;
            int entryCount = 0;
            PrintWriter out = new PrintWriter("feeds.txt");
            Set<String> visitedSet = new HashSet<String>();

            List<String> htmlURLs = new ArrayList<String>();
            htmlURLs.add(html);

            while (htmlURLs.size() > 0) {
                String nextPageToVisit = htmlURLs.remove(0);

                if ((isGoodToVisit(nextPageToVisit)) && (!visitedSet.contains(nextPageToVisit))) {
                    visitedSet.add(nextPageToVisit);
                    System.out.printf("visited %d, remaining: %d, feeds: %d  entries: %d - %s\n", visitedSet.size(),
                            htmlURLs.size(), feedCount, entryCount, nextPageToVisit);
                    try {
                        URL feedUrl = findBestFeed(nextPageToVisit);
                        if (feedUrl != null && !visitedSet.contains(feedUrl.toExternalForm())) {
                            visitedSet.add(feedUrl.toExternalForm());
                            try {
                                SyndFeed syndFeed = readFeed(feedUrl);
                                String canonicalLink = syndFeed.getLink();
                                if (canonicalLink == null) {
                                    canonicalLink = nextPageToVisit;
                                }
                                visitedSet.add(canonicalLink);
                                out.println(canonicalLink + " " + feedUrl.toExternalForm());
                                out.flush();
                                feedCount++;
                                List entryList = syndFeed.getEntries();
                                for (Object o : entryList) {
                                    SyndEntry syndEntry = (SyndEntry) o;
                                    String content = getContent(syndEntry);
                                    List<Anchor> anchors = extractAnchors(content);
                                    for (Anchor anchor : anchors) {
                                        htmlURLs.add(anchor.getDestURL());
                                    }
                                }
                                entryCount += entryList.size();
                            } catch (IOException e) {
                                System.out.println("          Trouble reading feed from " + feedUrl);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("          Trouble reading html " + nextPageToVisit);
                    }
                }
            }
            out.close();
        } catch (IOException ioe) {
            System.out.println("Trouble opening log file");
        }
    }

    public static void main(String[] args) {
        simpleCrawler("http://blogs.sun.com/searchguy");
    //simpleCrawler("http:///digg.com/linux_unix/Open_source_in_schools_could_save_the_taxpayer_billions_2");
    }
}
