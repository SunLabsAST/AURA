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

package com.sun.labs.aura.music.web.lastfm;

import com.sun.labs.aura.music.web.Commander;
import com.sun.labs.aura.music.web.XmlUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/*
 * LastFM.java
 *
 * Created on Aug 27, 2007, 6:39:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author plamere
 */
public class LastFM {

    private Commander commander;

    public LastFM() throws IOException {
        commander = new Commander("last.fm", "http://ws.audioscrobbler.com/1.0/", "");
        commander.setRetries(1);
        commander.setTimeout(10000);
        commander.setTraceSends(false);
        commander.setMinimumCommandPeriod(500);
    }

    public SocialTag[] getArtistTags(String artistName) throws IOException {
        String url = getArtistTagURL(artistName, false);
        return getTagsFromLastFM(url);
    }

    public SocialTag[] getAlbumTags(String artistName, String trackName) throws IOException {
        String url = getAlbumTagURL(artistName, trackName);
        return getTagsFromLastFM(url);
    }

    public SocialTag[] getTrackTags(String artistName, String trackName) throws IOException {
        String url = getTrackTagURL(artistName, trackName);
        return getTagsFromLastFM(url);
    }

    public SocialTag[] getArtistTags(String artistName, boolean raw) throws IOException {
        String url = getArtistTagURL(artistName, raw);
        return getTagsFromLastFM(url);
    }

    public LastItem[] getArtistFans(String artistName) throws IOException {
        String url = getArtistFanURL(artistName);
        return getFansFromLastFM(url);
    }

    public LastUser getUser(String userName) throws IOException {
        String url = getUserURL(userName);
        return getFanFromLastFM(url);
    }

    public void setMinimumCommandPeriod(long period) {
        commander.setMinimumCommandPeriod(period);
    }


    public int getPopularity(String artistName) throws IOException {
        String url = getTopArtistAlbumsURL(artistName);
        Document doc = commander.sendCommand(url);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("album");
        int reach = 0;
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);
            String sreach = XmlUtil.getElementContents(item, "reach");
            if (sreach != null) {
                reach += Integer.parseInt(sreach);
            }
        }
        return reach;
    }

    public LastItem[] getTopArtistsForUser(String user) throws IOException {
        String url = getTopArtistsForUserURL(user);
        return getTopArtistForUserFromLastFM(url);
    }

    public LastItem[] getWeeklyArtistsForUser(String user) throws IOException {
        String url = getWeeklyArtistsForUserURL(user);
        return getTopArtistForUserFromLastFM(url);
    }

    public String[] getSimilarUsers(String user) throws IOException {
        List<String> users = new ArrayList<String>();
        String url = "user/" + user + "/neighbours.xml";

        Document doc = commander.sendCommand(url);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("user");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            String userName = item.getAttribute("username");
            if (userName != null) {
                users.add(userName);
            }
        }
        return users.toArray(new String[0]);
    }

    public LastArtist[] getSimilarArtists(String artist) throws IOException {
        List<LastArtist> artistList = new ArrayList();
        String url = getSimilarArtistURL(artist);
        Document doc = commander.sendCommand(url);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("artist");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);
            String artistName = XmlUtil.getElementContents(item, "name");
            String mbaid = XmlUtil.getElementContents(item, "mbid");
            artistList.add(new LastArtist(artistName, mbaid));
        }
        return artistList.toArray(new LastArtist[0]);
    }

    public void setTrace(boolean trace) {
        commander.setTraceSends(trace);
    }

    private LastItem[] getTopArtistForUserFromLastFM(String url) throws IOException {
        List<LastItem> items = new ArrayList<LastItem>();

        Document doc = commander.sendCommand(url);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("artist");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            String artistName = XmlUtil.getElementContents(item, "name");
            int freq = 0;

            String mbid = XmlUtil.getElementContents(item, "mbid");

            String sfreq = XmlUtil.getElementContents(item, "playcount");
            if (sfreq != null) {
                freq = Integer.parseInt(sfreq);
            }
            LastItem artistItem = new LastItem(artistName, mbid, freq);
            items.add(artistItem);
        }
        return items.toArray(new LastItem[0]);
    }

    public LastUser getFanFromLastFM(String url) throws IOException {
        LastUser lastUser = new LastUser();
        Document doc = commander.sendCommand(url);
        Element docElement = doc.getDocumentElement();

        String name = docElement.getAttribute("username");
        lastUser.setName(name);

        String realname = XmlUtil.getElementContents(docElement, "realname");
        if (realname != null && realname.length() > 0) {
            lastUser.setRealName(realname);
        }

        String country = XmlUtil.getElementContents(docElement, "country");
        if (country != null && country.length() > 0) {
            lastUser.setCountry(country);
        }

        String sage = XmlUtil.getElementContents(docElement, "age");
        if (sage != null && sage.length() > 0) {
            int age = Integer.parseInt(sage);
            lastUser.setAge(age);
        }

        String spc = XmlUtil.getElementContents(docElement, "playcount");
        if (spc != null && spc.length() > 0) {
            int playcount = Integer.parseInt(spc);
            lastUser.setPlayCount(playcount);
        }

        String sgender = XmlUtil.getElementContents(docElement, "gender");
        if (sgender != null) {
            if (sgender.equals("f")) {
                lastUser.setGender(LastUser.Gender.Female);
            } else if (sgender.equals("m")) {
                lastUser.setGender(LastUser.Gender.Male);
            }
        }
        return lastUser;
    }

    public LastItem[] getTopArtistsForTag(String tag) throws IOException {
        String url = getTopArtistsForTagURL(tag);
        return getTopArtistForTagFromLastFM(url);
    }

    private LastItem[] getTopArtistForTagFromLastFM(String url) throws IOException {
        List<LastItem> items = new ArrayList<LastItem>();

        Document doc = commander.sendCommand(url);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("artist");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element artist = (Element) itemList.item(i);
            String artistName = artist.getAttribute("name");
            String sfreq = artist.getAttribute("count");
            int freq = 1;
            if (sfreq != null) {
                freq = Integer.parseInt(sfreq);
            }
            String mbid = XmlUtil.getElementContents(artist, "mbid");
            LastItem artistItem = new LastItem(artistName, mbid, freq);
            items.add(artistItem);
        }
        return items.toArray(new LastItem[0]);
    }

    private SocialTag[] getTagsFromLastFM(String url) throws IOException {
        List<SocialTag> tags = new ArrayList<SocialTag>();

        //        if (true) { return tags.toArray(new SocialTag[0]); }
        Document doc = commander.sendCommand(url);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("tag");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            String tagName = XmlUtil.getElementContents(item, "name");
            int freq = 50;

            String sfreq = XmlUtil.getElementContents(item, "count");
            if (sfreq != null) {
                if (sfreq.length() > 0) {
                    freq = Integer.parseInt(sfreq);
                } else {
                    freq = 0;
                }
            }
            SocialTag tag = new SocialTag(tagName, freq);
            tags.add(tag);
        }
        Collections.sort(tags, LastItem.FREQ_ORDER);
        Collections.reverse(tags);
        return tags.toArray(new SocialTag[0]);
    }

    private LastItem[] getFansFromLastFM(String url) throws IOException {
        List<LastItem> tags = new ArrayList<LastItem>();
        Document doc = commander.sendCommand(url);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("user");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);
            String userName = item.getAttribute("username");
            String sweight = XmlUtil.getElementContents(item, "weight");
            int weight = 0;
            if (sweight != null) {
                if (sweight.length() > 0) {
                    weight = Integer.parseInt(sweight);
                } else {
                    weight = 0;
                }
            }
            LastItem tag = new LastItem(userName, weight);
            tags.add(tag);
        }
        Collections.sort(tags, LastItem.FREQ_ORDER);
        Collections.reverse(tags);
        return tags.toArray(new LastItem[0]);
    }

    private String getTopArtistsForTagURL(String tag) {
        String url = "tag/" + encodeName(tag) + "/topartists.xml";
        return url;
    }

    private String getArtistTagURL(String artistName, boolean raw) {
        String encodedArtistName = encodeName(artistName);
        String url = "artist/" + encodedArtistName + "/toptags.xml";
        if (raw) {
            url += "?alt";
        }
        return url;
    }

    private String getArtistFanURL(String artistName) {
        String encodedArtistName = encodeName(artistName);
        String url = "artist/" + encodedArtistName + "/fans.xml";
        return url;
    }

    private String getUserURL(String userName) {
        String encodedName = encodeName(userName);
        String url = "user/" + encodedName + "/profile.xml";
        return url;
    }

    private String getSimilarArtistURL(String artistName) {
        String encodedArtistName = encodeName(artistName);
        String url = "artist/" + encodedArtistName + "/similar.xml";
        return url;
    }

    private String getTopArtistAlbumsURL(String artistName) {
        String encodedArtistName = encodeName(artistName);
        String url = "artist/" + encodedArtistName + "/topalbums.xml";
        return url;
    }

    private String encodeName(String artistName) {
        try {
            String encodedName = URLEncoder.encode(artistName, "UTF-8");
            // lastfm double encodes things (Crazy!)
            encodedName = URLEncoder.encode(encodedName, "UTF-8");
            return encodedName;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private String getTrackTagURL(String artistName, String trackName) {
        String encodedArtistName = encodeName(artistName);
        String encodedSongName = encodeName(trackName);

        // http://ws.audioscrobbler.com/1.0/artist/My+Chemical+Romance/toptags.xml
        // http://ws.audioscrobbler.com/1.0/track/Metallica/enter%20sandman/toptags.xml

        String url = "track/" + encodedArtistName + "/" + encodedSongName + "/toptags.xml";
        return url;
    }

    private String getAlbumTagURL(String artistName, String albumName) {
        String encodedArtistName = encodeName(artistName);
        String encodedAlbumName = encodeName(albumName);

        // http://ws.audioscrobbler.com/1.0/artist/My+Chemical+Romance/toptags.xml
        // http://ws.audioscrobbler.com/1.0/track/Metallica/enter%20sandman/toptags.xml

        String url = "album/" + encodedArtistName + "/" + encodedAlbumName + "/toptags.xml";
        return url;
    }

    private String getTopArtistsForUserURL(String user) {
        String url = "user/" + user + "/topartists.xml";
        return url;
    }

    private String getWeeklyArtistsForUserURL(String user) {
        String url = "user/" + user + "/weeklyartistchart.xml";
        return url;
    }

    void dumpArtistTags(String artistName) throws IOException {
        System.out.printf("Tags for %s\n", artistName);
        SocialTag[] tags = getArtistTags(artistName);
        for (SocialTag tag : tags) {
            System.out.printf("    %d %s\n", tag.getFreq(), tag.getName());
        }
    }

    void dumpArtistFans(String artistName) throws IOException {
        System.out.printf("Fans for %s\n", artistName);
        LastItem[] fans = getArtistFans(artistName);
        float sumAge = 0;
        int ageCount = 0;
        for (LastItem fan : fans) {
            LastUser user = getUser(fan.getName());
            System.out.println("  " + user);
            if (user.getAge() != 0) {
                ageCount++;
                sumAge += user.getAge();
            }
        }
        if (ageCount > 0) {
            System.out.printf("==== count: %d  avg: %.2f\n", ageCount, sumAge / ageCount);
        }

    }

    LastUser dumpUser(String userName) throws IOException {
        LastUser user = getUser(userName);
        System.out.println("  " + user);
        return user;
    }

    void dumpFavoriteArtists(String user) throws IOException {
        System.out.printf("Artists for %s\n", user);
        LastItem[] items = getTopArtistsForUser(user);
        for (LastItem item : items) {
            System.out.printf("    %d %s\n", item.getFreq(), item.getName());
        }
    }

    static void showPopularity(LastFM lastFM, String artistName) throws IOException {
        System.out.println("Popularity for " + artistName + ": " + lastFM.getPopularity(artistName));
    }

    static void showSimilarArtists(LastFM lastFM, String artistName) throws IOException {
        LastArtist[] simArtists = lastFM.getSimilarArtists(artistName);
        System.out.println("Artists similar to " + artistName);
        for (LastArtist a : simArtists) {
            System.out.printf("%s %s\n", a.getMbaid(), a.getArtistName());
        }
    }

    public static void main3(String[] args) {
        try {
            LastFM lastfm = new LastFM();
            lastfm.dumpUser("rj");
            lastfm.dumpUser("lamere");
            lastfm.dumpUser("musicmobs");
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        try {
            LastFM lastfm = new LastFM();
            lastfm.dumpArtistFans("weezer");
            lastfm.dumpArtistFans("deerhoof");
            lastfm.dumpArtistFans("Hannah Montanta");
            lastfm.dumpArtistFans("elvis presley");
            lastfm.dumpArtistFans("the beatles");
            lastfm.dumpArtistFans("Björk");
            lastfm.dumpArtistFans("Mötley Crüe");

        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }

    public static void main2(String[] args) {
        try {
            LastFM lastfm = new com.sun.labs.aura.music.web.lastfm.LastFM();
            showSimilarArtists(lastfm, "AC/DC");
            showSimilarArtists(lastfm, "Belle & Sebastian");
            showSimilarArtists(lastfm, "Pink Floyd");

            showPopularity(lastfm, "the beatles");
            showPopularity(lastfm, "elvis presley");
            showPopularity(lastfm, "weezer");
            showPopularity(lastfm, "Björk");

            showSimilarArtists(lastfm, "the beatles");
            showSimilarArtists(lastfm, "weezer");
            showSimilarArtists(lastfm, "Deerhoof");

            lastfm.dumpFavoriteArtists("lamere");

            lastfm.dumpArtistTags("weezer");
            lastfm.dumpArtistTags("deerhoof");
            lastfm.dumpArtistTags("elvis presley");
            lastfm.dumpArtistTags("the beatles");
            lastfm.dumpArtistTags("Björk");
            lastfm.dumpArtistTags("Mötley Crüe");
            lastfm.dumpArtistTags("Bad Artist Name");

        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }
}
