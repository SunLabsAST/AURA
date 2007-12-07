/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A class for manipulating OPML files
 * @author plamere
 */
public class OPMLProcessor {

    /**
     * Given an opml file, return the list of urls to feeds
     * @param opml the opml file
     * @return a list of urls to feeds
     * @throws java.io.IOException if an error occurs
     */
    public List<URL> getFeedURLs(URL opml) throws IOException {

        if (opml == null) {
            throw new IOException("url is null");
        }

        URLConnection connection = opml.openConnection();
        connection.setConnectTimeout(30000);
        InputStream is = connection.getInputStream();

        try {
            is = opml.openStream();
            List<URL> urls = getFeedURLs(is);
            return urls;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public List<URL> getFeedURLs(File opml) throws IOException {

        if (opml == null) {
            throw new IOException("file is null");
        }

        InputStream is = null;
        
        is = new BufferedInputStream(new FileInputStream(opml));

        try {
            is = new BufferedInputStream(new FileInputStream(opml));
            List<URL> urls = getFeedURLs(is);
            return urls;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    public List<URL> getURLs(URL opml) throws IOException {
        if (opml == null) {
            throw new IOException("url is null");
        }

        InputStream is = null;
        try {
            URLConnection connection = opml.openConnection();
            connection.setConnectTimeout(30000);
            is = connection.getInputStream();
            List<URL> urls = getURLs(is);
            return urls;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Given an opml file, return the list of urls to feeds
     * @param is input stream to OPML data
     * @return a list of urls to feeds
     * @throws java.io.IOException if an error occurs
     */
    public List<URL> getFeedURLs(InputStream is) throws IOException {
        try {

            List<URL> feeds = new ArrayList<URL>();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document doc = builder.parse(is);
            Element docElement = doc.getDocumentElement();
            NodeList itemList = docElement.getElementsByTagName("outline");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element item = (Element) itemList.item(i);
                String type = item.getAttribute("type");
                if (isFeed(type)) {
                    String url = item.getAttribute("xmlUrl");
                    if (url == null) {
                        url = item.getAttribute("url");
                    }
                    if (url != null) {
                        try {
                            System.out.println("Adding " + url);
                            feeds.add(new URL(url));
                        } catch (MalformedURLException ex) {
                        // skip feeds with bad urls
                        }
                    }
                }
            }
            return feeds;
        } catch (SAXException ex) {
            throw new IOException("trouble parsing" + ex.getMessage());
        } catch (ParserConfigurationException ex) {
            throw new IOException("parse config trouble " + ex.getMessage());
        }
    }

    public List<URL> getURLs(InputStream is) throws IOException {
        try {

            List<URL> feeds = new ArrayList<URL>();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document doc = builder.parse(is);
            Element docElement = doc.getDocumentElement();
            NodeList itemList = docElement.getElementsByTagName("outline");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element item = (Element) itemList.item(i);
                String url = item.getAttribute("xmlUrl");
                if (url == null || url.length() == 0) {
                    url = item.getAttribute("url");
                }
                if (url != null && url.length() > 0) {
                    try {
                        System.out.println("Adding " + url);
                        feeds.add(new URL(url));
                    } catch (MalformedURLException ex) {
                    // skip feeds with bad urls
                    }
                }
            }
            return feeds;
        } catch (SAXException ex) {
            throw new IOException("trouble parsing" + ex.getMessage());
        } catch (ParserConfigurationException ex) {
            throw new IOException("parse config trouble " + ex.getMessage());
        }
    }

    public void crawl(Set<URL> feeds, URL url) throws IOException {
        // just for testing
        if (feeds.size() >= 10) {
            return;
        }
        delay(5000L);
        List<URL> l = getURLs(url);

        for (URL u : l) {
            if (u.toExternalForm().endsWith(".opml")) {
                System.out.println("recursing " + u);
                crawl(feeds, u);
            } else {
                feeds.add(u);
            }
        }
    }
    
    private void delay(long milli) {
        try {
            Thread.sleep(milli);
        } catch (InterruptedException e) {

        }
    }

    public void saveAsOPML(File path, Collection<URL> urls) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(path));
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<opml>");
        writer.println("    <body>");
        for (URL u : urls) {
            writer.printf("        <outline type=\"rss\" xmlUrl=\"%s\"/>\n", u.toExternalForm());
        }
        writer.println("    </body>");
        writer.println("</opml>");
        writer.close();
    }

    private boolean isFeed(String type) {
        return "rss".equals(type);
    }

    private boolean isLink(String type) {
        return "link".equals(type);
    }

    public static void main(String[] args) {
        try {
            OPMLProcessor op = new OPMLProcessor();
            Set<URL> urls = new HashSet<URL>();
            op.crawl(urls, new URL("http://www.opmlmanager.com/userlist"));
            op.saveAsOPML(new File("bigopml.opml"), urls);
            
            // now make sure that we can load it
            
            op.getFeedURLs(new File("bigopml.opml"));
        } catch (IOException ioe) {
            System.out.println("Trouble " + ioe);
        }
    }
}
