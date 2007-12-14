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
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
                            feeds.add(new URL(url));
                        } catch (MalformedURLException ex) {
                        // skip feeds with bad urls
                        }
                    }
                }
            }
            return feeds;
        } catch (SAXParseException ex) {
            System.err.println("error " + ex  + " at " + 
                    ex.getPublicId() +":" + ex.getSystemId() +
                    " at: " + ex.getLineNumber() + " col:" + ex.getColumnNumber());
            throw new IOException("trouble parsing" + ex.getMessage());
        } catch (SAXException ex) {
            System.err.println("error " + ex);
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
                        feeds.add(new URL(url));
                    } catch (MalformedURLException ex) {
                    // skip feeds with bad urls
                    }
                }
            }
            return feeds;
        } catch (SAXParseException ex) {
            System.err.println("error " + ex  + " at " + 
                    ex.getPublicId() +":" + ex.getSystemId() +
                    " at: " + ex.getLineNumber() + " col:" + ex.getColumnNumber());
            throw new IOException("trouble parsing" + ex.getMessage());
        } catch (SAXException ex) {
            throw new IOException("trouble parsing" + ex.getMessage());
        } catch (ParserConfigurationException ex) {
            throw new IOException("parse config trouble " + ex.getMessage());
        }
    }

    public List<String> getURLsAsStrings(InputStream is) throws IOException {
        try {

            List<String> feeds = new ArrayList<String>();
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
                    feeds.add(url);
                }
            }
            return feeds;
        } catch (SAXException ex) {
            throw new IOException("trouble parsing" + ex.getMessage());
        } catch (ParserConfigurationException ex) {
            throw new IOException("parse config trouble " + ex.getMessage());
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

    static void dumpDocument(File path, Document document) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(document);
            Result result = new StreamResult(path);

            // Write the DOM document to the file
            // Get Transformer
            Transformer xformer =
                    TransformerFactory.newInstance().newTransformer();
            // Write to a file

            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.setOutputProperty(OutputKeys.METHOD, "xml");
            xformer.setOutputProperty(
                    "{http://xml.apache.org/xalan}indent-amount", "4");

            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            System.out.println("TransformerConfigurationException: " + e);
        } catch (TransformerException e) {
            System.out.println("TransformerException: " + e);
        }
    }
}
