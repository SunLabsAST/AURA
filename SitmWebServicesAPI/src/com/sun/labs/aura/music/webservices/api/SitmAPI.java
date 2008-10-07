/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices.api;

import com.sun.labs.aura.music.web.Commander;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author plamere
 */
public class SitmAPI {
    private Commander commander;

    public SitmAPI(String host) throws IOException {
        commander = new Commander("sitm", host + "/SitmWebServices/", "");
        //commander.setTraceSends(true);
        //commander.setTrace(true);
    }

    public List<Scored<Item>> artistSearch(String searchString) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("ArtistSearch?name=" + searchString);
        checkStatus("artistSearch", doc);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("artist");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element artistElement = (Element) itemList.item(i);
            String key = artistElement.getAttribute("key");
            String sscore = artistElement.getAttribute("score");
            String popularity = artistElement.getAttribute("popularity");
            String name = artistElement.getAttribute("name");
            double score = Double.parseDouble(sscore);
            Item item = new Item(key, name);
            items.add(new Scored<Item>(item, score));
        }
        return items;
    }

    // http://search.east/SitmWebServices/ArtistSocialTags?name=weezer
    public List<Scored<Item>> artistSocialTags(String key, int count) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("ArtistSocialTags?key=" + key + "&max=" + count);
        checkStatus("artistSocialTags", doc);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("ArtistTag");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element artistElement = (Element) itemList.item(i);
            String skey = artistElement.getAttribute("key");
            String sscore = artistElement.getAttribute("score");
            float score = Float.parseFloat(sscore);
            Item item = new Item(skey, skey);
            items.add(new Scored<Item>(item, score));
        }
        return items;
    }

    public List<Scored<Item>> tagSearch(String searchString, int count) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("ArtistTagSearch?max=" + count + "&name=" + searchString);
        checkStatus("tagSearch", doc);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("artistTag");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element artistElement = (Element) itemList.item(i);
            String key = artistElement.getAttribute("key");
            String sscore = artistElement.getAttribute("score");
            String popularity = artistElement.getAttribute("popularity");
            String name = artistElement.getAttribute("name");
            double score = Double.parseDouble(sscore);
            Item item = new Item(key, name);
            items.add(new Scored<Item>(item, score));
        }
        return items;
    }
   
    public List<Scored<Item>> findSimilarArtistsByKey(String key, int count) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("FindSimilarArtist?key=" + key + "&max=" + count);
        checkStatus("findSimilarArtistsByKey", doc);
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
        return items;
    }

    public List<Scored<Item>> findSimilarArtistsByName(String name, int count) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("FindSimilarArtist?name=" + name + "&max=" + count);
        checkStatus("findSimilarArtist", doc);
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
        return items;
    }

    // http://search.east/SitmWebServices/GetItem?itemID=6fe07aa5-fec0-4eca-a456-f29bff451b04
    public Item getItem(String key, boolean compact) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        String compactArg = compact ? "&format=compact" : "";
        Document doc = commander.sendCommand("GetItem?key=" + key + compactArg);
        checkStatus("getItem", doc);
        return null;
    }

    public List<Item> getItems(List<String> keys, boolean compact) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        String compactArg = compact ? "&format=compact" : "";
        String keyList = "";
        for (String key : keys) {
            keyList += key;
            keyList += ",";
        }
        keyList = keyList.replace(",$", "");
        Document doc = commander.sendCommand("GetItem?key=" + keyList + compactArg);
        checkStatus("getItems", doc);
        return null;
    }

    // http://search.east/SitmWebServices/FindSimilarArtistTags?name=metal&max=100
    public List<Scored<Item>> findSimilarArtistTags(String key, int count) throws IOException {
        Document doc = commander.sendCommand("FindSimilarArtistTags?key=" + key + "&max=" + count);
        checkStatus("findSimilarArtistTags", doc);
        return null;
    }
    public List<Scored<Item>> getTags(int count) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("GetTags?max=" + count);
        checkStatus("getTags", doc);
        return null;
    }

    public List<Item> getArtists(int count) throws IOException {
        List<Item> items = new ArrayList<Item>();
        Document doc = commander.sendCommand("GetArtists?max=" + count);
        checkStatus("getArtists", doc);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("artist");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element artistElement = (Element) itemList.item(i);
            String key = artistElement.getAttribute("key");
            String name = artistElement.getAttribute("name");
            Item item = new Item(key, name);
            items.add(item);
        }
        return items;
    }

    public List<Item> getArtistTags(int count) throws IOException {
        List<Item> items = new ArrayList<Item>();
        Document doc = commander.sendCommand("GetTags?max=" + count);
        checkStatus("getArtistTags", doc);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("tag");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element artistElement = (Element) itemList.item(i);
            String key = artistElement.getAttribute("key");
            String name = artistElement.getAttribute("name");
            Item item = new Item(key, name);
            items.add(item);
        }
        return items;
    }

    public void getStats() throws IOException {
        Document doc = commander.sendCommand("GetStats");
        checkStatus("getStats", doc);
    }

    // http://search.east/SitmWebServices/FindSimilarArtistFromWordCloud?wordCloud=%27(indie,1)(punk,1)(emo,.5)%27
    public List<Scored<Item>> findSimilarArtistFromWordCloud(String cloud, int count) throws IOException {
        Document doc = commander.sendCommand("FindSimilarArtistFromWordCloud?wordCloud=" + cloud + "&max=" + count);
        checkStatus("findSimilarArtistFromWordCloud", doc);
        return null;
    }

    public void checkStatus(String msg, Document doc) throws IOException {
        if (true) {
            return;  // until the ws refactor is complete
        }
        Element docElement = doc.getDocumentElement();
        NodeList nlist = docElement.getElementsByTagName("status");
        if (nlist.getLength() != 1) {
            throw new IOException(msg + ":" + "Improper status format");
        }
        Element status = (Element) nlist.item(0);
        String code =  (status.getAttribute("code"));
        if (!"OK".equals(code)) {
            Commander.dumpDocument(doc);
            throw new IOException(msg + " bad status "  + code);
        }
    }
}
