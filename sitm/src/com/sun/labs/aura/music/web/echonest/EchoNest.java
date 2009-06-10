/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.music.web.echonest;

import com.sun.labs.aura.music.web.Commander;
import com.sun.labs.aura.music.web.XmlUtil;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author plamere
 */
public class EchoNest {

    private Commander commander;
    private String API_KEY = null;
    private final static int MAX_ROWS = 15;

    public EchoNest() throws IOException, AuraException {

        try {
            Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("com/sun/labs/aura/music/resource/api.properties"));
            API_KEY = properties.getProperty("ECHONEST_API_KEY");
        } catch (IOException ex) {
            throw new AuraException("No EchoNest API key available. " +
                    "Please set properties file (com/sun/labs/aura/music/resource/api.properties) to use.");
        } catch (NullPointerException ex2) {
            throw new AuraException("No EchoNest API key available. " +
                    "Please set properties file (com/sun/labs/aura/music/resource/api.properties) to use.");
        }

        commander = new Commander("EchoNest", "http://developer.echonest.com/api/", "&api_key=" + API_KEY);
        commander.setRetries(0);
        commander.setTimeout(10000);
        commander.setTraceSends(false);
        commander.setTrace(false);
        commander.setMinimumCommandPeriod(1000);
        //commander.setMinimumCommandPeriod(0);
    }

    public List<EchoArtist> artistSearch(String query) throws IOException {
        List<EchoArtist> artists = new ArrayList<EchoArtist>();
        String cmdURL = "search_artists?query=" + encode(query);
        Document doc = commander.sendCommand(cmdURL);
        Element docElement = doc.getDocumentElement();
        Element artistList = (Element) XmlUtil.getDescendent(docElement, "artists");
        if (artistList != null) {
            NodeList itemList = artistList.getElementsByTagName("artist");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element item = (Element) itemList.item(i);
                String name = XmlUtil.getDescendentText(item, "name");
                String enid = XmlUtil.getDescendentText(item, "id");
                String mbid = XmlUtil.getDescendentText(item, "mbid");
                EchoArtist artist = new EchoArtist(name, enid, mbid);
                artists.add(artist);
            }
        }
        return artists;
    }

    private String encode(String parameter) {
        try {
            return URLEncoder.encode(parameter, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return parameter;
        }
    }

    public EchoArtist getProfileByName(String qname) throws IOException {
        String cmdURL = "get_profile?name=" + encode(qname);
        Document doc = commander.sendCommand(cmdURL);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("artist");
        if (itemList.getLength() == 1) {
            Element item = (Element) itemList.item(0);
            String name = XmlUtil.getDescendentText(item, "name");
            String enid = XmlUtil.getDescendentText(item, "id");
            String mbid = XmlUtil.getDescendentText(item, "mbid");
            EchoArtist artist = new EchoArtist(name, enid, mbid);
            return artist;
        } else if (itemList.getLength() > 1) {
            System.err.println("Woah - mroe than one artist return, which was unexpected");
        }
        return null;
    }

    public EchoArtist getProfileByMBID(String mbid) throws IOException {
        String cmdURL = "get_profile?mbid=" + mbid;
        Document doc = commander.sendCommand(cmdURL);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("artist");
        if (itemList.getLength() == 1) {
            Element item = (Element) itemList.item(0);
            String name = XmlUtil.getDescendentText(item, "name");
            String enid = XmlUtil.getDescendentText(item, "id");
            EchoArtist artist = new EchoArtist(name, enid, mbid);
            return artist;
        } else if (itemList.getLength() > 1) {
            System.err.println("Woah - mroe than one artist return, which was unexpected");
        }
        return null;
    }

    public float getFamiliarity(String id) throws IOException {
        float familiarity = 0f;
        String cmdURL = "get_familiarity?id=" + id;
        Document doc = commander.sendCommand(cmdURL);
        Element docElement = doc.getDocumentElement();
        Element artistElement = XmlUtil.getFirstElement(docElement, "artist");
        String sFam = XmlUtil.getDescendentText(artistElement, "familiarity");
        if (sFam != null) {
            familiarity = Float.parseFloat(sFam);
        }
        return familiarity;
    }

    public float getHotness(String id) throws IOException {
        float hotness = 0f;
        String cmdURL = "get_hotttnesss?id=" + id;
        Document doc = commander.sendCommand(cmdURL);
        Element docElement = doc.getDocumentElement();
        Element artistElement = XmlUtil.getFirstElement(docElement, "artist");
        String sFam = XmlUtil.getDescendentText(artistElement, "hotttnesss");
        if (sFam != null) {
            hotness = Float.parseFloat(sFam);
        }
        return hotness;
    }

    public List<Scored<EchoArtist>> getTopHotttArtists(int count) throws IOException {
        List<Scored<EchoArtist>> artists = new ArrayList<Scored<EchoArtist>>();

        String cmdURL = "get_top_hottt_artists?rows=" + count;

        Document doc = commander.sendCommand(cmdURL);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("artist");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);
            String name = XmlUtil.getDescendentText(item, "name");
            String id = XmlUtil.getDescendentText(item, "id");
            String mbid = XmlUtil.getDescendentText(item, "mbid");
            String sHotness = XmlUtil.getDescendentText(item, "hotttnesss");
            double hotness = 1;

            if (sHotness != null) {
                hotness = Double.parseDouble(sHotness);
            }

            EchoArtist artist = new EchoArtist(name, id, mbid);
            artists.add(new Scored<EchoArtist>(artist, hotness));
        }
        return artists;
    }

    public List<Scored<EchoArtist>> getSimilarArtists(String id, int start, int count) throws IOException {
        String cmdURL = "get_similar?id=" + id + "&start=" + start + "&rows=" + count;
        return fetchSimilarArtists(cmdURL);
    }

    public List<Scored<EchoArtist>> getSimilarArtistsByName(String name, int start, int count) throws IOException {
        String cmdURL = "get_similar?name=" + name + "&start=" + start + "&rows=" + count;
        return fetchSimilarArtists(cmdURL);
    }

    private List<Scored<EchoArtist>> fetchSimilarArtists(String url) throws IOException {
        List<Scored<EchoArtist>> artists = new ArrayList<Scored<EchoArtist>>();

        Document doc = commander.sendCommand(url);
        Element docElement = doc.getDocumentElement();
        Element similar = (Element) XmlUtil.getDescendent(docElement, "similar");
        NodeList itemList = similar.getElementsByTagName("artist");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);
            String name = XmlUtil.getDescendentText(item, "name");
            String enid = XmlUtil.getDescendentText(item, "id");
            String mbid = XmlUtil.getDescendentText(item, "mbid");
            String srank = XmlUtil.getDescendentText(item, "rank");

            int rank = 1;
            if (srank != null) {
                rank = Integer.parseInt(srank);
            }

            EchoArtist artist = new EchoArtist(name, enid, mbid);
            artists.add(new Scored<EchoArtist>(artist, 1.0 / rank));
        }
        return artists;
    }

    /**
     * Gets links to audio for an artist
     * @param mbaid the musicbrainz artist id
     * @return the (possibly empty) list of URLs to audio
     */
    public List<String> getAudio(String mbaid) throws IOException {
        return getAudio(mbaid, MAX_ROWS);
    }

    /**
     * Gets links to audio for an artist
     * @param mbaid the musicbrainz artist id
     * @param count the maxium number to return (total max is 15)
     * @return the (possibly empty) list of URLs to audio
     */
    public List<String> getAudio(String mbaid, int count) throws IOException {
        List<String> results = new ArrayList<String>();

        String cmdURL = "get_audio?mbid=" + mbaid + "&rows=" + count;

        Document doc = commander.sendCommand(cmdURL);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("url");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);
            String url = item.getTextContent();
            if (url != null) {
                results.add(url);
            }
        }
        return results;
    }

    private String encodeName(String artistName) {
        try {
            String encodedName = URLEncoder.encode(artistName, "UTF-8");
            return encodedName;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static void dump(EchoNest nest, String mbid) throws IOException {
        for (String url : nest.getAudio(mbid, 15)) {
            System.out.printf("%s %s\n", mbid, url);
        }
    }

    public void crawl(String path) throws IOException {
        Set<String> visited = new HashSet<String>();
        List<EchoArtist> todo = new LinkedList<EchoArtist>();

        List<Scored<EchoArtist>> seeds = getTopHotttArtists(15);
        for (Scored<EchoArtist> sartist : seeds) {
            todo.add(sartist.getItem());
        }

        PrintWriter out = new PrintWriter(path);
        try {
            while (todo.size() > 0) {
                EchoArtist artist = todo.remove(0);
                if (!visited.contains(artist.getId())) {
                    visited.add(artist.getId());
                    float familiarity = getFamiliarity(artist.getId());
                    float hotness = getHotness(artist.getId());
                    System.out.printf("Visited:%d/%d  Familiarity %6.4f Hotness: %6.4f Artist:%s\n",
                            visited.size(), todo.size(), familiarity, hotness, artist.toString());
                    List<Scored<EchoArtist>> similar = getSimilarArtists(artist.getId(), 0, 15);
                    for (Scored<EchoArtist> sim : similar) {
                        EchoArtist simArtist = sim.getItem();
                        System.out.println("   " + sim.getItem().getName());
                        if (!visited.contains(sim.getItem().getId())) {
                            todo.add(sim.getItem());
                        }
                        out.printf("%s|%s|%6.4f|%6.4f|%s|%s\n", artist.getId(), artist.getName(), familiarity, hotness,
                                simArtist.getId(), simArtist.getName());
                    }
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                }
            }
        } finally {
            out.close();
        }
    }

    public static void main(String[] args) throws IOException, AuraException {
        EchoNest echoNest = new EchoNest();
        echoNest.crawl("echocrawl.txt");
    //dump(echoNest, "11eabe0c-2638-4808-92f9-1dbd9c453429");
    //dump(echoNest, "83d91898-7763-47d7-b03b-b92132375c47");
    //dump(echoNest, "299278d3-25dd-4f30-bae4-5b571c28034d");
    }
}
