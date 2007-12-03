/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
        InputStream is = opml.openStream();
        return getFeedURLs(is);
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
                    try {
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

    private boolean isFeed(String type) {
        return "rss".equals(type);
    }
}
