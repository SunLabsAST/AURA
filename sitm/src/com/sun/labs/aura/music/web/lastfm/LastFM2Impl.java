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
import com.sun.labs.aura.music.web.WebServiceAccessor;
import com.sun.labs.aura.music.web.XmlUtil;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Wrapper for Last.fm API 2.0
 * @author plamere
 */
public class LastFM2Impl extends WebServiceAccessor implements LastFM2 {

    private Commander commander;

    public LastFM2Impl() throws AuraException, IOException {

        super("Last.fm v2", "LASTFM_API_KEY");

        commander = new Commander("last.fm2", "http://ws.audioscrobbler.com/2.0/", "&api_key=" + API_KEY, true);
        commander.setRetries(1);
        commander.setTimeout(10000);
        commander.setTraceSends(false);
        commander.setMinimumCommandPeriod(500);
    }

    @Override
    public void setTrace(boolean trace) {
        commander.setTraceSends(trace);
    }

    @Override
    public void setMinimumCommandPeriod(long period) {
        commander.setMinimumCommandPeriod(period);
    }

    @Override
    public LastArtist2 getArtistInfoByName(String artistName) throws IOException {
        String url = getArtistInfoByNameURL(artistName);
        return getArtistInfoFromLastFM(url);
    }

    @Override
    public LastArtist2 getArtistInfoByMBID(String mbid) throws IOException {
        String url = getArtistInfoByMbidURL(mbid);
        return getArtistInfoFromLastFM(url);
    }

    @Override
    public LastAlbum2 getAlbumInfoByName(String artistName, String albumName) throws IOException {
        String url = getAlbumInfoByNameURL(artistName, albumName);
        return getAlbumInfoFromLastFM(url);
    }

    @Override
    public LastAlbum2 getAlbumInfoByMBID(String mbid) throws IOException {
        String url = getAlbumInfoByMbidURL(mbid);
        return getAlbumInfoFromLastFM(url);
    }

    @Override
    public LastTrack getTrackInfoByName(String artistName, String trackName) throws IOException {
        String url = getTrackInfoByNameURL(artistName, trackName);
        return getTrackInfoFromLastFM(url);
    }

    @Override
    public LastTrack getTrackInfoByMBID(String mbid) throws IOException {
        String url = getTrackInfoByMbidURL(mbid);
        return getTrackInfoFromLastFM(url);
    }

    @Override
    public SocialTag[] getTrackTopTagsByName(String artistName, String trackName) throws IOException {
        String url = getTrackTopTagsByNameURL(artistName, trackName);
        return getTrackTopTagsFromLastFM(url);
    }

    @Override
    public SocialTag[] getTrackTopTagsByMBID(String mbid) throws IOException {
        String url = getTrackTopTagsByMbidURL(mbid);
        return getTrackTopTagsFromLastFM(url);
    }

    @Override
    public String[] getNeighboursForUser(String user) throws IOException {
        String url = getNeighboursForUserURL(user);
        return getNeighboursForUserFromLastFM(url);
    }

    @Override
    public LastItem[] getTopArtistsForTag(String tag) throws IOException {
        String url = getTopArtistForTagURL(tag);
        return getTopArtistsForTagFromLastFM(url);
    }

    @Override
    public SocialTag[] getArtistTags(String artistName) throws IOException {
        String url = getArtistTagURL(artistName);
        return getTagsFromLastFM(url);
    }

    @Override
    public LastArtist[] getSimilarArtists(String artist) throws IOException {
        String url = getSimilarArtistsURL(artist);
        return getSimilarArtistsFromLastFM(url);
    }

    /**
     * Get a list of available charts for this user, expressed as date ranges
     * which can be sent to the getWeeklyArtistChartByUser() function
     * @param user lastfm username
     * @return array where index 0 is the date range's lower bound and index 1 is it's upper bound
     * @throws java.io.IOException
     */
    @Override
    public List<Integer[]> getWeeklyChartListByUser(String user) throws IOException {
        String url = getWeeklyChartListURL(user);
        return getWeeklyChartListFromLastFM(url);
    }

    @Override
    public List<LastItem> getWeeklyArtistChartByUser(String user) throws IOException {
        return getWeeklyArtistChartByUser(user, 0, 0);
    }

    /**
     * Get an artist chart for a user profile, for a given date range. If no date
     * range is supplied (from,to=0), it will return the most recent artist chart for this user.
     * Call getWeeklyChartListByUser() to get the list of available date ranges.
     * 
     * @param user lastfm username
     * @param from The date at which the chart should start from in posix time
     * @param to The date at which the chart should end on in posix time
     * @return Map mbid->playcount
     * @throws java.io.IOException
     */
    @Override
    public List<LastItem> getWeeklyArtistChartByUser(String user, int from, int to) throws IOException {
        String url = getWeeklyArtistChartURL(user, from, to);
        return getWeeklyArtistChartFromLastFM(url);
    }

    public LastArtist[] getSimilarArtistsFromLastFM(String url) throws IOException {

        List<LastArtist> items = new ArrayList<LastArtist>();

        Document doc = commander.sendCommand(url);
        checkStatus(doc);

        Element docElement = doc.getDocumentElement();
        Element simArtistsNode = (Element) XmlUtil.getDescendent(docElement, "similarartists");

        for (Node artistNode : XmlUtil.getDescendents(simArtistsNode, "artist")) {
            Element artist = (Element) artistNode;

            String artistName = XmlUtil.getElementContents(artist, "name");
            String artistMbid = XmlUtil.getElementContents(artist, "mbid");
            items.add(new LastArtist(artistName, artistMbid));
        }
        return items.toArray(new LastArtist[0]);
    }

    private LastItem[] getTopArtistsForTagFromLastFM(String url) throws IOException {

        List<LastItem> items = new ArrayList<LastItem>();

        Document doc = commander.sendCommand(url);
        checkStatus(doc);

        Element docElement = doc.getDocumentElement();
        Element topArtistsNode = (Element) XmlUtil.getDescendent(docElement, "topartists");

        for (Node artistNode : XmlUtil.getDescendents(topArtistsNode, "artist")) {
            Element artist = (Element) artistNode;

            String artistName = XmlUtil.getElementContents(artist, "name");
            String sfreq = XmlUtil.getElementContents(artist, "tagcount");
            double freq = 1;
            if (sfreq != null) {
                freq = Double.parseDouble(sfreq);
            }
            String mbid = XmlUtil.getElementContents(artist, "mbid");
            items.add(new LastItem(artistName, mbid, (int)freq));
        }
        return items.toArray(new LastItem[0]);
    }

    private SocialTag[] getTagsFromLastFM(String url) throws IOException {
        List<SocialTag> tags = new ArrayList<SocialTag>();

        Document doc = commander.sendCommand(url);
        checkStatus(doc);

        Element docElement = doc.getDocumentElement();
        Element topTagsNode = (Element) XmlUtil.getDescendent(docElement, "toptags");

        for (Node tagNode : XmlUtil.getDescendents(topTagsNode, "tag")) {
            Element tag = (Element) tagNode;

            String tagName = XmlUtil.getElementContents(tag, "name");

            int freq = 50;
            String sfreq = XmlUtil.getElementContents(tag, "count");
            if (sfreq != null) {
                freq = sint(sfreq);
            }
            tags.add(new SocialTag(tagName, freq));
        }
        Collections.sort(tags, LastItem.FREQ_ORDER);
        Collections.reverse(tags);
        return tags.toArray(new SocialTag[0]);
    }


    private LastArtist2 getArtistInfoFromLastFM(String url) throws IOException {
        LastArtist2 artist = new LastArtist2();

        Document doc = commander.sendCommand(url);
        checkStatus(doc);

        Element docElement = doc.getDocumentElement();
        Element artistNode = (Element) XmlUtil.getDescendent(docElement, "artist");

        if (artistNode != null) {
            artist.setName(XmlUtil.getElementContents(artistNode, "name"));
            artist.setMbid(XmlUtil.getElementContents(artistNode, "mbid"));
            artist.setUrl(XmlUtil.getElementContents(artistNode, "url"));

            List<Node> imageList = XmlUtil.getDescendents(artistNode, "image");

            if (imageList != null) {
                for (int i = 0; i < imageList.size(); i++) {
                    Element image = (Element) imageList.get(i);
                    String size = image.getAttribute("size");
                    String imageUrl = image.getTextContent();
                    if (size != null && imageUrl != null) {
                        if (size.equalsIgnoreCase("small")) {
                            artist.setSmallImage(imageUrl);
                        } else if (size.equalsIgnoreCase("medium")) {
                            artist.setMediumImage(imageUrl);
                        } else if (size.equalsIgnoreCase("large")) {
                            artist.setLargeImage(imageUrl);
                        }
                    }
                }
            }

            String streamable = XmlUtil.getDescendentText(artistNode, "streamable");
            artist.setStreamable("1".equals(streamable));

            Element statsNode = (Element) XmlUtil.getDescendent(artistNode, "stats");
            if (statsNode != null) {
                artist.setPlaycount(XmlUtil.getElementContentsAsInteger(statsNode, "playcount"));
                artist.setListeners(XmlUtil.getElementContentsAsInteger(statsNode, "listeners"));
            }

            Element bioNode = (Element) XmlUtil.getDescendent(artistNode, "bio");
            if (bioNode != null) {
                artist.setBioSummary(XmlUtil.getDescendentText(bioNode, "summary"));
                artist.setBioFull(XmlUtil.getDescendentText(bioNode, "content"));
            }
        }

        return artist;
    }

    private void checkStatus(Document doc) throws IOException {
        Element docElement = doc.getDocumentElement();
        if (!docElement.getTagName().equals("lfm")) {
            throw new IOException("Mising LFM elemement in lastfm resonse");
        }
        if (!docElement.getAttribute("status").equals("ok")) {
            String err = XmlUtil.getElementContents(docElement, "error");
            throw new IOException("LFM Error " + err);
        }
    }

    private LastAlbum2 getAlbumInfoFromLastFM(String url) throws IOException {
        LastAlbum2 album = new LastAlbum2();
        Document doc = commander.sendCommand(url);

        checkStatus(doc);

        Element docElement = doc.getDocumentElement();
        Element albumNode = (Element) XmlUtil.getDescendent(docElement, "album");

        if (albumNode != null) {
            album.setName(XmlUtil.getElementContents(albumNode, "name"));
            album.setMbid(XmlUtil.getElementContents(albumNode, "mbid"));
            album.setUrl(XmlUtil.getElementContents(albumNode, "url"));
            album.setArtistName(XmlUtil.getElementContents(albumNode, "artist"));
            album.setLfmID(XmlUtil.getElementContents(albumNode, "id"));
            album.setReleaseDate(XmlUtil.getElementContents(albumNode, "releasedate"));
            album.setListeners(XmlUtil.getElementContentsAsInteger(albumNode, "listeners"));
            album.setPlaycount(XmlUtil.getElementContentsAsInteger(albumNode, "playcount"));

            List<Node> imageList = XmlUtil.getDescendents(albumNode, "image");
            if (imageList != null) {
                for (int i = 0; i < imageList.size(); i++) {
                    Element image = (Element) imageList.get(i);
                    String size = image.getAttribute("size");
                    String imageUrl = image.getTextContent();
                    if (size != null && imageUrl != null) {
                        if (size.equalsIgnoreCase("small")) {
                            album.setSmallImage(imageUrl);
                        } else if (size.equalsIgnoreCase("medium")) {
                            album.setMediumImage(imageUrl);
                        } else if (size.equalsIgnoreCase("large")) {
                            album.setLargeImage(imageUrl);
                        } else if (size.equalsIgnoreCase("extralarge")) {
                            album.setHugeImage(imageUrl);
                        }
                    }
                }
            }

            Element wikiNode = (Element) XmlUtil.getDescendent(albumNode, "wiki");
            if (wikiNode != null) {
                album.setWikiSummary(XmlUtil.getDescendentText(wikiNode, "summary"));
                album.setWikiFull(XmlUtil.getDescendentText(wikiNode, "content"));
            }
        }
        return album;
    }

    private List<Integer[]> getWeeklyChartListFromLastFM(String url) throws IOException {
        List<Integer[]> chartList = new ArrayList<Integer[]>();
        Document doc = commander.sendCommand(url);

        checkStatus(doc);

        Element docElement = doc.getDocumentElement();
        Element weekChartListNode = (Element) XmlUtil.getDescendent(docElement, "weeklychartlist");
        List<Node> chartNodes = XmlUtil.getDescendents(weekChartListNode, "chart");

        if (chartNodes != null) {
            for (int i=0; i<chartNodes.size(); i++) {
                Element chart = (Element) chartNodes.get(i);
                try {
                    Integer[] chartArray = new Integer[2];
                    chartArray[0] = Integer.parseInt(chart.getAttribute("from"));
                    chartArray[1] = Integer.parseInt(chart.getAttribute("to"));

                    chartList.add(chartArray);
                } catch (NumberFormatException e) {}
            }
        }
        return chartList;
    }

    private List<LastItem> getWeeklyArtistChartFromLastFM(String url) throws IOException {
        List<LastItem> artistChart = new ArrayList<LastItem>();
        Document doc = commander.sendCommand(url);

        checkStatus(doc);

        Element docElement = doc.getDocumentElement();
        Element weekArtistChartNode = (Element) XmlUtil.getDescendent(docElement, "weeklyartistchart");
        List<Node> artistsNodes = XmlUtil.getDescendents(weekArtistChartNode, "artist");
        if (artistsNodes != null) {
            for (int i=0; i<artistsNodes.size(); i++) {
                try {
                    Element artist = (Element) artistsNodes.get(i);

                    String mbid = XmlUtil.getElementContents(artist, "mbid");
                    if (mbid.length()>0) {
                        artistChart.add(new LastItem(XmlUtil.getElementContents(artist, "name"),
                                XmlUtil.getElementContents(artist, "mbid"),
                                XmlUtil.getElementContentsAsInteger(artist, "playcount")));
                    }
                } catch (NumberFormatException e) {}
            }
        }
        return artistChart;
    }

    private LastTrack getTrackInfoFromLastFM(String url) throws IOException {
        LastTrack track = new LastTrack();
        Document doc = commander.sendCommand(url);

        checkStatus(doc);

        Element docElement = doc.getDocumentElement();
        Element trackNode = (Element) XmlUtil.getDescendent(docElement, "track");

        if (trackNode != null) {
            track.setLfmid(XmlUtil.getElementContents(trackNode, "id"));
            track.setName(XmlUtil.getElementContents(trackNode, "name"));
            track.setMbid(XmlUtil.getElementContents(trackNode, "mbid"));
            track.setUrl(XmlUtil.getElementContents(trackNode, "url"));
            track.setDuration(XmlUtil.getElementContentsAsInteger(trackNode, "duration"));
            track.setStreamble(XmlUtil.getElementContentsAsInteger(trackNode, "streamable") == 1);
            track.setListeners(XmlUtil.getElementContentsAsInteger(trackNode, "listeners"));
            track.setPlaycount(XmlUtil.getElementContentsAsInteger(trackNode, "playcount"));

            // the artist info

            Element artistNode = (Element) XmlUtil.getDescendent(trackNode, "artist");
            if (artistNode != null) {
                track.setArtistName(XmlUtil.getElementContents(artistNode, "name"));
                track.setArtistMbid(XmlUtil.getElementContents(artistNode, "mbid"));
                track.setArtistUrl(XmlUtil.getElementContents(artistNode, "url"));
            }

            // the album info

            Element albumNode = (Element) XmlUtil.getDescendent(trackNode, "album");
            if (albumNode != null) {
                track.setAlbumPosition(sint(albumNode.getAttribute("position")));
                track.setAlbumArtist(XmlUtil.getElementContents(albumNode, "artist"));
                track.setAlbumTitle(XmlUtil.getElementContents(albumNode, "title"));
                track.setAlbumMbid(XmlUtil.getElementContents(albumNode, "mbid"));
                track.setAlbumUrl(XmlUtil.getElementContents(albumNode, "url"));

                List<Node> imageList = XmlUtil.getDescendents(albumNode, "image");
                if (imageList != null) {
                    for (int i = 0; i < imageList.size(); i++) {
                        Element image = (Element) imageList.get(i);
                        String size = image.getAttribute("size");
                        String imageUrl = image.getTextContent();
                        if (size != null && imageUrl != null) {
                            if (size.equalsIgnoreCase("small")) {
                                track.setSmallImage(imageUrl);
                            } else if (size.equalsIgnoreCase("medium")) {
                                track.setMediumImage(imageUrl);
                            } else if (size.equalsIgnoreCase("large")) {
                                track.setLargeImage(imageUrl);
                            } else if (size.equalsIgnoreCase("extralarge")) {
                                track.setLargeImage(imageUrl);
                            }
                        }
                    }
                }
            }

            // the top tags
            Element topTags = (Element) XmlUtil.getDescendent(trackNode, "toptags");
            List<Node> tagList = XmlUtil.getDescendents(topTags, "tag");
            if (tagList != null) {
                for (int i = 0; i < tagList.size(); i++) {
                    Node tagNode =  tagList.get(i);
                    String tag = XmlUtil.getDescendentText(tagNode, "tag");
                    track.addTopTag(tag);
                }
            }

            // the wiki info
            Element wikiNode = (Element) XmlUtil.getDescendent(trackNode, "wiki");
            if (wikiNode != null) {
                track.setWikiSummary(XmlUtil.getDescendentText(wikiNode, "summary"));
                track.setWikiContent(XmlUtil.getDescendentText(wikiNode, "content"));
            }
        }
        return track;
    }

    private SocialTag[] getTrackTopTagsFromLastFM(String url) throws IOException {
        Document doc = commander.sendCommand(url);
        checkStatus(doc);

        List<SocialTag> tagList = new ArrayList<SocialTag>();
        Element docElement = doc.getDocumentElement();
        Element topTagsNode = (Element) XmlUtil.getDescendent(docElement, "toptags");
        if (topTagsNode != null) {
            List<Node> tagNodes = XmlUtil.getDescendents(topTagsNode, "tag");
            if (tagNodes != null) {
                for (int i = 0; i < tagNodes.size(); i++) {
                    Element tagNode = (Element) tagNodes.get(i);
                    String name = XmlUtil.getDescendentText(tagNode, "name");
                    int count = sint(XmlUtil.getDescendentText(tagNode, "count"));
                    if (count <= 1) {
                        count = 1;
                    }
                    SocialTag st = new SocialTag(name, count);
                    tagList.add(st);
                }
            }
        }
        return tagList.toArray(new SocialTag[0]);
    }

    private String[] getNeighboursForUserFromLastFM(String url) throws IOException {
        Document doc = commander.sendCommand(url);
        checkStatus(doc);

        List<String> userIds = new ArrayList<String>();
        Element docElement = doc.getDocumentElement();
        Element neighboursNode = (Element) XmlUtil.getDescendent(docElement, "neighbours");
        if (neighboursNode != null) {
            List<Node> userNodes = XmlUtil.getDescendents(neighboursNode, "user");
            for (int i = 0; i < userNodes.size(); i++) {
                userIds.add(XmlUtil.getDescendentText(userNodes.get(i), "name"));
            }
        }
        return userIds.toArray(new String[0]);
    }

    private String getArtistTagURL(String artistName) {
        String encodedArtistName = encodeName(artistName);
        return "?method=artist.gettoptags&artist=" + encodedArtistName;
    }

    //http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=sonny%20bono&api_key=b25b959554ed76058ac220b7b2e0a026
    private String getArtistInfoByNameURL(String artistName) {
        String encodedArtistName = encodeName(artistName);
        return "?method=artist.getinfo&artist=" + encodedArtistName;
    }

    private String getArtistInfoByMbidURL(String mbid) {
        return "?method=artist.getinfo&mbid=" + mbid;
    }

    private String getAlbumInfoByMbidURL(String mbid) {
        return "?method=album.getinfo&mbid=" + mbid;
    }

    private String getTrackInfoByMbidURL(String mbid) {
        return "?method=track.getinfo&mbid=" + mbid;
    }

    private String getWeeklyChartListURL(String user) {
        return "?method=user.getweeklychartlist&user=" + user;
    }

    private String getWeeklyArtistChartURL(String user, int from, int to) {
        String url = "?method=user.getweeklyartistchart&user=" + user;
        if (from>0 && to>0) {
            url += "&from=" + from + "&to=" + to;
        }
        return url;
    }

    private String getTopArtistForTagURL(String tagName) {
        String encodedTagName = encodeName(tagName);
        return "?method=tag.gettopartists&tag=" + encodedTagName;
    }

    private String getSimilarArtistsURL(String artistName) {
        String encodedName = encodeName(artistName);
        return "?method=artist.getsimilar&artist=" + encodedName;
    }

    private String getTrackInfoByNameURL(String artistName, String trackName) {
        String encodedArtistName = encodeName(artistName);
        String encodedTrackName = encodeName(trackName);
        return "?method=track.getinfo&artist=" + encodedArtistName + "&track=" + encodedTrackName;
    }

    private String getTrackTopTagsByMbidURL(String mbid) {
        return "?method=track.gettoptags&mbid=" + mbid;
    }

    private String getNeighboursForUserURL(String user) {
        return "?method=user.getneighbours&user=" + user;
    }

    private String getTrackTopTagsByNameURL(String artistName, String trackName) {
        String encodedArtistName = encodeName(artistName);
        String encodedTrackName = encodeName(trackName);
        return "?method=track.gettoptags&artist=" + encodedArtistName + "&track=" + encodedTrackName;
    }

    private String getAlbumInfoByNameURL(String artistName, String albumName) {
        String encodedArtistName = encodeName(artistName);
        String encodedAlbumName = encodeName(albumName);
        return "?method=album.getinfo&artist=" + encodedArtistName + "&album=" + encodedAlbumName;
    }

    private String encodeName(String artistName) {
        try {
            String encodedName = URLEncoder.encode(artistName, "UTF-8");
            return encodedName;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private int sint(String s) {
        int i = 0;
        try {
            if (s != null && s.length() > 0) {
                i = Integer.parseInt(s);
            }
        } catch (NumberFormatException ex) {
            double d = Double.parseDouble(s);
            i = (int) d;
        }
        return i;
    }

    public static void main(String[] args) throws IOException, AuraException {
        LastFM2Impl lfm2 = new LastFM2Impl();

        for (LastItem lI : lfm2.getTopArtistsForTag("rock")) {
            System.out.println(lI.toString());
        }

        System.out.println("-------------------");

        for (LastArtist lA : lfm2.getSimilarArtists("Our Lady Peace")) {
            System.out.println(lA.getArtistName()+" :: "+lA.getMbaid());
        }

        System.out.println("-------------------");

        List<Integer[]> c = lfm2.getWeeklyChartListByUser("ddcarnage");
        int cnt=0;
        for (Integer[] i : c) {
            System.out.println(i[0]+"->"+i[1]);

            List<LastItem> m = lfm2.getWeeklyArtistChartByUser("ddcarnage", i[0], i[1]);
            for (LastItem lI : m) {
                System.out.println("  "+lI.getName()+" ("+lI.getMBID()+")->"+lI.getFreq());
            }
            if (cnt++>=2) {
                break;
            }
        }
    }

    public static void main2(String[] args) throws IOException, AuraException {
        LastFM2Impl lfm2 = new LastFM2Impl();

        if (false) { // artist test
            {
                LastArtist2 la2 = lfm2.getArtistInfoByName("deerhoof");
                la2.dump(true);
            }

            {
                LastArtist2 la2 = lfm2.getArtistInfoByName("sonny & cher");
                la2.dump(true);
            }

            {
                LastArtist2 la2 = lfm2.getArtistInfoByName("sonny bono");
                la2.dump(true);
            }

            {
                LastArtist2 la2 = lfm2.getArtistInfoByName("bono");
                la2.dump(true);
            }

            {
                LastArtist2 la2 = lfm2.getArtistInfoByMBID("11eabe0c-2638-4808-92f9-1dbd9c453429");
                la2.dump(true);
            }

            {
                LastArtist2 la2 = lfm2.getArtistInfoByName("sonny & crap");
                la2.dump(true);
            }
        } else if (false) { // album test

            {
                LastAlbum2 album = lfm2.getAlbumInfoByName("the beatles", "revolver");
                album.dump();
            }
            {
                LastAlbum2 album = lfm2.getAlbumInfoByName("deerhoof", "friend opportunity");
                album.dump();
            }
            {
                LastAlbum2 album = lfm2.getAlbumInfoByName("weezer", "weezer");
                album.dump();
            }
            {
                LastAlbum2 album = lfm2.getAlbumInfoByName("cher", "believe");
                album.dump();
            }
        } else if (true) {
            {
                LastTrack track = lfm2.getTrackInfoByName("cher", "believe");
                track.dump();
            }
            {
                LastTrack track = lfm2.getTrackInfoByName("the beatles", "revolution");
                track.dump();
            }
            {
                LastTrack track = lfm2.getTrackInfoByName("dave brubeck", "take five");
                track.dump();
            }
            {
                LastTrack track = lfm2.getTrackInfoByName("daft punk", "harder, better, faster, stronger");
                track.dump();
                SocialTag[] tags = lfm2.getTrackTopTagsByName("daft punk", "harder, better, faster, stronger");
                for (SocialTag st : tags) {
                    System.out.println("   " + st.getName() + " " + st.getFreq());
                }
            }
            {
                LastTrack track = lfm2.getTrackInfoByName("Aphex Twin", "?Mi-1=-a?Di(n)(?Fij(n-1)+Fexti(n-1))");
                track.dump();
            }
        }
    }

}
