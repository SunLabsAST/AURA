/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.impl.crawler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A standalone utility class that will crawl a site for OPML files
 * @author plamere
 */
public class OPMLCrawler {

    private List<URL> urlQueue = new LinkedList<URL>();
    private File dir;
    private long delay = 0L;
    private boolean localOnly = true;
    private URL site;
    private Set<URL> skipSet = new HashSet<URL>();
    private Set<String> xmlUrlSet = new HashSet<String>();
    private int maxFiles = Integer.MAX_VALUE;

    public OPMLCrawler(URL site, File cachedir) throws IOException {
        this.site = site;
        urlQueue.add(site);
        if (cachedir.exists() && !cachedir.isDirectory()) {
            throw new IOException("Bad cachedir");
        } else if (!cachedir.exists()) {
            cachedir.mkdir();
        }
        dir = cachedir;
    }

    public void crawl() {
        int count = 0;
        while (urlQueue.size() > 0) {
            URL url = urlQueue.remove(0);
            System.out.printf("%d/%d %s\n", ++count, urlQueue.size(), url.toExternalForm());
            processURL(url);
            if (count >= maxFiles) {
                break;
            }
        }
    }

    public void saveSummary(String filename) throws IOException {
        saveAsOPML(new File(dir, filename), xmlUrlSet);
    }

    public void saveAsOPML(File path, Collection<String> urls) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(path));
        //writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");

        writer.println("<opml>");
        writer.println("    <body>");
        for (String u : urls) {
            u = normalizeURL(u);
            if (u != null) {
                writer.printf("        <outline type=\"rss\" xmlUrl=\"%s\"/>\n", u);
            }
        }
        writer.println("    </body>");
        writer.println("</opml>");
        writer.close();
    }

    private String normalizeURL(String u) {
        try {
            u = u.replaceAll("\\&", "&amp;");
            u = u.replaceAll("<", "&lt;");
            u = u.replaceAll(">", "&gt;");
            u = u.replaceAll("'", "&apos;");

            URL url = new URL(u);
            URI uri = url.toURI();
            return uri.toURL().toExternalForm();
        } catch (URISyntaxException ex) {
        } catch (MalformedURLException ex) {
        }
        System.err.println("Tossing out " + u);
        return null;
    }

    private void processURL(URL url) {
        try {
            if (!skipSet.contains(url)) {
                skipSet.add(url);
                getLocalCopy(url);
                processLocalCopy(url);
            }
        } catch (IOException ioe) {
            System.err.println("Trouble fetching " + url);
        }
    }

    private void getLocalCopy(URL url) throws IOException {
        File cache = urlToCacheFile(url);
        if (!cache.exists()) {
            InputStream in = new BufferedInputStream(url.openStream());
            OutputStream out = new BufferedOutputStream(new FileOutputStream(cache));

            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
            }

            in.close();
            out.close();

            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
            }
        }
    }

    private void processLocalCopy(URL url) throws IOException {
        try {
            File cache = urlToCacheFile(url);
            InputStream is = new BufferedInputStream(new FileInputStream(cache));
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document doc = builder.parse(is);
            Element docElement = doc.getDocumentElement();
            NodeList itemList = docElement.getElementsByTagName("outline");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element item = (Element) itemList.item(i);
                String link = item.getAttribute("url");
                addLink(link);
                String xmlUrlLink = item.getAttribute("xmlUrl");
                if (xmlUrlLink != null) {
                    xmlUrlSet.add(xmlUrlLink);
                }
            }
            is.close();
        } catch (SAXException ex) {
            throw new IOException("trouble parsing" + ex.getMessage());
        } catch (ParserConfigurationException ex) {
            throw new IOException("parse config trouble " + ex.getMessage());
        }
    }

    private void addLink(String link) {
        if (link != null && link.length() > 0) {
            try {
                URL linkURL = new URL(link);
                if (isGoodLink(linkURL)) {
                    if (!urlQueue.contains(linkURL)) {
                        System.out.println("    Adding " + linkURL);
                        urlQueue.add(linkURL);
                    }
                }
            } catch (MalformedURLException ex) {
            // skip feeds with bad urls
            }
        }
    }

    private boolean isGoodLink(URL link) {
        if (localOnly) {
            return site.getAuthority().equals(link.getAuthority());
        } else {
            return true;
        }
    }

    private File urlToCacheFile(URL url) {
        String s = url.toExternalForm();
        s = s.replaceAll("https*://", "");
        s = s.replaceAll("/", "_");
        return new File(dir, s);
    }

    public static void main(String[] args) {
        String name = "http://www.opmlmanager.com/userlist";
        if (args.length == 1) {
            name = args[0];
        } else if (args.length > 1) {
            System.err.println("Usage: OPMLCrawler [sitename]");
            System.exit(1);
        }

        try {
            URL url = new URL(name);
            File dir = new File("/lab/mir/data/opml");
            OPMLCrawler crawler = new OPMLCrawler(url, dir);
            crawler.crawl();
            crawler.saveSummary("SUMMARY.opml");

            // validate the summary
            OPMLProcessor processor = new OPMLProcessor();
            processor.getFeedURLs(new File(dir, "SUMMARY.opml"));

        } catch (MalformedURLException ex) {
            System.err.println("Bad URL " + name);
            System.exit(2);
        } catch (IOException ioe) {
            System.err.println("Trouble creating crawler " + ioe);
            System.exit(3);
        }
    }
}
