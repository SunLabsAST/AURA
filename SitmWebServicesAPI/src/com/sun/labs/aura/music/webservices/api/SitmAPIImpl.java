/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices.api;

import com.sun.labs.aura.music.web.Commander;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author plamere
 */
public class SitmAPIImpl extends SitmAPI {

    private Commander commander;
    private Monitor monitor;
    private boolean trace;

    protected SitmAPIImpl(String host, boolean trace, boolean debug, boolean periodicDump) throws IOException {
        this.trace = trace;
        String suffix = debug  ? "&debug=true" : "";
        commander = new Commander("sitm", host, suffix);
        commander.setTraceSends(trace);
        monitor = new Monitor(false, periodicDump);
    }


    public void resetStats() {
        monitor.reset();
    }

    public List<Scored<Item>> artistSearch(String searchString) throws IOException {
        try {
            long start = monitor.opStart();
            searchString = encode(searchString);
            List<Scored<Item>> items = new ArrayList<Scored<Item>>();
            Document doc = commander.sendCommand("ArtistSearch?name=" + searchString);
            long servletTime = checkStatus("artistSearch", doc);
            Element docElement = doc.getDocumentElement();
            NodeList itemList = docElement.getElementsByTagName("artist");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element artistElement = (Element) itemList.item(i);
                String key = artistElement.getAttribute("key");
                String sscore = artistElement.getAttribute("score");
                String name = artistElement.getAttribute("name");
                double score = Double.parseDouble(sscore);
                Item item = new Item(key, name);
                items.add(new Scored<Item>(item, score));
            }
            monitor.opFinish("artistSearch", start, servletTime);
            return items;
        } catch (IOException ex) {
            monitor.opError("artistSearch");
            throw ex;
        }
    }

    // http://search.east/SitmWebServices/ArtistSocialTags?name=weezer
    public List<Scored<Item>> artistSocialTags(String key, int count) throws IOException {
        try {
            long start = monitor.opStart();
            List<Scored<Item>> items = new ArrayList<Scored<Item>>();
            Document doc = commander.sendCommand("GetArtistTags?key=" + key + "&max=" + count);
            long servletTime = checkStatus("artistSocialTags", doc);
            Element docElement = doc.getDocumentElement();
            NodeList itemList = docElement.getElementsByTagName("artist_tag");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element artistElement = (Element) itemList.item(i);
                String skey = artistElement.getAttribute("key");
                String name = artistElement.getAttribute("name");
                String sscore = artistElement.getAttribute("score");
                float score = Float.parseFloat(sscore);
                Item item = new Item(skey, name);
                items.add(new Scored<Item>(item, score));
            }
            monitor.opFinish("artistSocialTags", start, servletTime);
            return items;
        } catch (IOException ex) {
            monitor.opError("artistSocialTags");
            throw ex;
        }
    }

    public List<Scored<Item>> tagSearch(String searchString, int count) throws IOException {
        try {
            long start = monitor.opStart();
            searchString = encode(searchString);
            List<Scored<Item>> items = new ArrayList<Scored<Item>>();
            Document doc = commander.sendCommand("ArtistTagSearch?max=" + count + "&name=" + searchString);
            long servletTime = checkStatus("tagSearch", doc);
            Element docElement = doc.getDocumentElement();
            NodeList itemList = docElement.getElementsByTagName("artist_tag");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element artistElement = (Element) itemList.item(i);
                String key = artistElement.getAttribute("key");
                String sscore = artistElement.getAttribute("score");
                String name = artistElement.getAttribute("name");
                double score = Double.parseDouble(sscore);
                Item item = new Item(key, name);
                items.add(new Scored<Item>(item, score));
            }
            monitor.opFinish("tagSearch", start, servletTime);
            return items;
        } catch (IOException ex) {
            monitor.opError("tagSearch");
            throw ex;
        }
    }

    public List<Scored<Item>> findSimilarArtistsByKey(String key, int count) throws IOException {
        try {
            long start = monitor.opStart();
            List<Scored<Item>> items = new ArrayList<Scored<Item>>();
            Document doc = commander.sendCommand("FindSimilarArtists?key=" + key + "&max=" + count);
            long servletTime = checkStatus("findSimilarArtistsByKey", doc);
            Element docElement = doc.getDocumentElement();
            NodeList itemList = docElement.getElementsByTagName("artist");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element artistElement = (Element) itemList.item(i);
                String skey = artistElement.getAttribute("key");
                String sscore = artistElement.getAttribute("score");
                String name = artistElement.getAttribute("name");
                double score = Double.parseDouble(sscore);
                Item item = new Item(skey, name);
                items.add(new Scored<Item>(item, score));
            }
            monitor.opFinish("findSimilarArtistsByKey", start, servletTime);
            return items;
        } catch (IOException ex) {
            monitor.opError("findSimilarArtistsByKey");
            throw ex;
        }
    }

    public List<Scored<Item>> findSimilarArtistsByName(String name, int count) throws IOException {
        try {
            long start = monitor.opStart();
            List<Scored<Item>> items = new ArrayList<Scored<Item>>();
            Document doc = commander.sendCommand("FindSimilarArtists?name=" + name + "&max=" + count);
            long servletTime = checkStatus("findSimilarArtist", doc);
            Element docElement = doc.getDocumentElement();
            NodeList itemList = docElement.getElementsByTagName("artist");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element artistElement = (Element) itemList.item(i);
                String skey = artistElement.getAttribute("key");
                String sscore = artistElement.getAttribute("score");
                String sname = artistElement.getAttribute("name");
                double score = Double.parseDouble(sscore);
                Item item = new Item(skey, sname);
                items.add(new Scored<Item>(item, score));
            }
            monitor.opFinish("findSimilarArtistsByName", start, servletTime);
            return items;
        } catch (IOException ex) {
            monitor.opError("findSimilarArtistsByName");
            throw ex;
        }
    }

    // http://search.east/SitmWebServices/GetItem?itemID=6fe07aa5-fec0-4eca-a456-f29bff451b04
    public Item getItem(String key, boolean compact) throws IOException {
        try {
            long start = monitor.opStart();
            List<Scored<Item>> items = new ArrayList<Scored<Item>>();
            String compactArg = compact ? "&outputType=small" : "&outputType=full";
            Document doc = commander.sendCommand("GetItems?key=" + key + compactArg);
            long servletTime = checkStatus("getItem", doc);
            monitor.opFinish("getItem", start, servletTime);
            return null;
        } catch (IOException ex) {
            monitor.opError("getItem");
            throw ex;
        }
    }

    public List<Item> getItems(List<String> keys, boolean compact) throws IOException {
        try {
            List<Item> items = new ArrayList<Item>();
            long start = monitor.opStart();
            String compactArg = compact ? "&outputType=small" : "&outputType=full";
            String keyList = "";
            for (String key : keys) {
                keyList += key;
                keyList += ",";
            }
            keyList = keyList.replace(",$", "");
            Document doc = commander.sendCommand("GetItems?key=" + keyList + compactArg);
            Element docElement = doc.getDocumentElement();
            NodeList itemList = docElement.getElementsByTagName("artist");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element artistElement = (Element) itemList.item(i);
                String skey = artistElement.getAttribute("key");
                String sname = artistElement.getAttribute("name");
                Item item = new Item(skey, sname);
                items.add(item);
            }
            long servletTime = checkStatus("getItems", doc);
            monitor.opFinish("getItems", start, servletTime);
            return items;
        } catch (IOException ex) {
            monitor.opError("getItems");
            throw ex;
        }
    }

    // http://search.east/SitmWebServices/FindSimilarArtistTags?name=metal&max=100
    public List<Scored<Item>> findSimilarArtistTags(String key, int count) throws IOException {
        try {
            long start = monitor.opStart();
            Document doc = commander.sendCommand("FindSimilarArtistTags?key=" + key + "&max=" + count);
            long servletTime = checkStatus("findSimilarArtistTags", doc);
            monitor.opFinish("findSimilarArtistTags", start, servletTime);
            return null;
        } catch (IOException ex) {
            monitor.opError("findSimilarArtistTags");
            throw ex;
        }
    }
    /*
    public List<Scored<Item>> getTags(int count) throws IOException {
    List<Scored<Item>> items = new ArrayList<Scored<Item>>();
    Document doc = commander.sendCommand("GetTags?max=" + count);
    checkStatus("getTags", doc);
    return null;
    }
     * */

    public List<Item> getArtists(int count) throws IOException {
        try {
            long start = monitor.opStart();
            List<Item> items = new ArrayList<Item>();
            Document doc = commander.sendCommand("GetArtists?max=" + count);
            long servletTime = checkStatus("getArtists", doc);
            Element docElement = doc.getDocumentElement();
            NodeList itemList = docElement.getElementsByTagName("artist");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element artistElement = (Element) itemList.item(i);
                String key = artistElement.getAttribute("key");
                String name = artistElement.getAttribute("name");
                Item item = new Item(key, name);
                items.add(item);
            }
            monitor.opFinish("getArtists", start, servletTime);
            return items;
        } catch (IOException ex) {
            monitor.opError("getArtists");
            throw ex;
        }
    }

    public List<Item> getArtistTags(int count) throws IOException {
        try {
            long start = monitor.opStart();
            List<Item> items = new ArrayList<Item>();
            Document doc = commander.sendCommand("GetTags?max=" + count);
            long servletTime = checkStatus("getArtistTags", doc);
            Element docElement = doc.getDocumentElement();
            NodeList itemList = docElement.getElementsByTagName("artist_tag");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element artistElement = (Element) itemList.item(i);
                String key = artistElement.getAttribute("key");
                String name = artistElement.getAttribute("name");
                Item item = new Item(key, name);
                items.add(item);
            }
            monitor.opFinish("getArtistTags", start, servletTime);
            return items;
        } catch (IOException ex) {
            monitor.opError("getArtistTags");
            throw ex;
        }
    }

    public void getStats() throws IOException {
        try {
            long start = monitor.opStart();
            Document doc = commander.sendCommand("GetStats");
            long servletTime = checkStatus("getStats", doc);
            monitor.opFinish("getStats", start, servletTime);
        } catch (IOException ex) {
            monitor.opError("getStats");
            throw ex;
        }
    }
    
    public void showStats() {
        monitor.dumpAllStats();
    }

    // http://search.east/SitmWebServices/FindSimilarArtistFromWordCloud?wordCloud=%27(indie,1)(punk,1)(emo,.5)%27
    public List<Scored<Item>> findSimilarArtistFromWordCloud(String cloud, int count) throws IOException {
        try {
            long start = monitor.opStart();
            cloud = encode(cloud);
            Document doc = commander.sendCommand("FindSimilarArtistsFromWordCloud?wordCloud=" + cloud + "&max=" + count);
            long servletTime = checkStatus("findSimilarArtistFromWordCloud", doc);
            monitor.opFinish("findSimilarArtistFromWordCloud", start, servletTime);
            return null;
        } catch (IOException ex) {
            monitor.opError("findSimilarArtistFromWordCloud");
            throw ex;
        }
    }

    public long checkStatus(String msg, Document doc) throws IOException {
        Element docElement = doc.getDocumentElement();
        NodeList nlist = docElement.getElementsByTagName("results");
        if (nlist.getLength() != 1) {
            throw new IOException(msg + ":" + "Improper results format");
        }
        Element results = (Element) nlist.item(0);
        String code = (results.getAttribute("status"));
        if (!"OK".equals(code)) {
            if (trace) {
                Commander.dumpDocument(doc);
            }
            throw new IOException(msg + " bad result status status " + code
                    + "\n\nreturned XML is:\n\n" + Commander.convertToString(doc));
        }
        NodeList timeNodes = results.getElementsByTagName("time");
        if (timeNodes.getLength() != 1) {
            throw new IOException(msg + ":" + "Improper time format");
        }

        Element timeNode = (Element) timeNodes.item(0);
        String ms = (timeNode.getAttribute("ms"));
        long millis = Long.parseLong(ms);

        return millis;
    }

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (IOException ex) {
            return s;
        }
    }
}
