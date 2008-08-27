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
    }

    public List<Scored<Item>> artistSearch(String searchString) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("ArtistSearch?name=" + searchString);
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
        return items;
    }

    public List<Scored<Item>> tagSearch(String searchString, int count) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("ArtistTagSearch?max=" + count + "&name=" + searchString);
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
   
    public List<Scored<Item>> findSimilarArtists(String key, int count) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("FindSimilarArtist?key=" + key + "&max=" + count);
        return null;
    }

    // http://search.east/SitmWebServices/GetItem?itemID=6fe07aa5-fec0-4eca-a456-f29bff451b04
    public Item getItem(String key) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("GetItem?itemID=" + key);
        return null;
    }

    // http://search.east/SitmWebServices/FindSimilarArtistTags?name=metal&max=100
    public List<Scored<Item>> findSimilarArtistTags(String name, int count) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("FindSimilarArtistTags?name=" + name + "&max=" + count);
        return null;
    }
    public List<Scored<Item>> getTags(int count) throws IOException {
        List<Scored<Item>> items = new ArrayList<Scored<Item>>();
        Document doc = commander.sendCommand("GetTags?max=" + count);
        return null;
    }

    public List<Item> getArtists(int count) throws IOException {
        List<Item> items = new ArrayList<Item>();
        Document doc = commander.sendCommand("GetArtists?max=" + count);
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

    public void getStats() throws IOException {
        Document doc = commander.sendCommand("GetStats");
    }

    // http://search.east/SitmWebServices/FindSimilarArtistFromWordCloud?wordCloud=%27(indie,1)(punk,1)(emo,.5)%27
    public List<Scored<Item>> findSimilarArtistFromWordCloud(String cloud, int count) throws IOException {
        Document doc = commander.sendCommand("FindSimilarArtistFromWordCloud?wordCloud=" + cloud + "&max=" + count);
        return null;
    }
}
