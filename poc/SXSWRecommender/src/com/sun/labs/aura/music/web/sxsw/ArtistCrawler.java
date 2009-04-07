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

package com.sun.labs.aura.music.web.sxsw;

import com.sun.labs.aura.music.web.Utilities;
import com.sun.labs.aura.music.web.echonest.EchoArtist;
import com.sun.labs.aura.music.web.echonest.EchoNest;
import com.sun.labs.aura.music.web.googlemaps.GMaps;
import com.sun.labs.aura.music.web.googlemaps.Location;
import com.sun.labs.aura.music.web.lastfm.LastArtist2;
import com.sun.labs.aura.music.web.lastfm.LastFM;
import com.sun.labs.aura.music.web.lastfm.LastFM2;
import com.sun.labs.aura.music.web.lastfm.LastItem;
import com.sun.labs.aura.music.web.lastfm.SocialTag;
import com.sun.labs.aura.music.web.youtube.Youtube2;
import com.sun.labs.aura.music.web.youtube.YoutubeVideo;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldInfo;
import com.sun.labs.minion.Result;
import com.sun.labs.minion.ResultSet;
import com.sun.labs.minion.SearchEngine;
import com.sun.labs.minion.SearchEngineException;
import com.sun.labs.minion.SearchEngineFactory;
import com.sun.labs.minion.SimpleIndexer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class ArtistCrawler {

    private final static LastArtist2 NO_INFO = new LastArtist2();
    private final static String DBLOCATION = "/lab/mir/SXSW.db";
    private final static String WEBLOCATION = "/lab/mir/SXSW.web/";
    private final static String DBNAME = DBLOCATION + "/crawler.ser";
    private final static String FIELD_SOCIAL_TAGS = "tags";
    private final static String FIELD_NAME = "name";
    private final static String FIELD_TYPE = "type";
    private final static String ARTIST_TYPE = "artist";
    private final static String TAG_TYPE = "tag";
    private final static String SXSW_ARTISTS_URL = "http://sxsw.com/music/shows/bands";
    private static float DISTANCE_FLOOR = 5;
    private final static String ECHO_NONE = "";
    private NumberFormat formatter = NumberFormat.getInstance();
    private DBCore dbCore;
    private int match = 0;
    private int total = 0;
    private LastFM2 lfm;
    private LastFM lastfm;
    private Youtube2 yt2;
    private EchoNest echoNest;
    private GMaps gmaps;
    private final static int maxPerPage = 75;
    private boolean quick = true;
    private TagFilter tagFilter = new TagFilter();
    private final static int MIN_TAG_THRESHOLD = 5;
    private TagCounter tagCounter;
    private DitherManager ditherManager = new DitherManager();
    private SearchEngine searchEngine;
    private Map<String, DocumentVector> dvCache = new HashMap<String, DocumentVector>();

    public ArtistCrawler() throws IOException, SearchEngineException {

        lfm = new LastFM2();
        lastfm = new LastFM();
        yt2 = new Youtube2();
        gmaps = new GMaps();
        echoNest = new EchoNest();
        tagCounter = new TagCounter();

        searchEngine = SearchEngineFactory.getSearchEngine(DBLOCATION);
        defineFields();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                saveDatabase();
                try {
                    if (searchEngine != null) {
                        searchEngine.flush();
                        searchEngine.close();
                    }
                } catch (SearchEngineException ex) {
                    System.out.println("Can't close search engine");
                }
            }
        });


        loadAll();

        if (quick) { // anti-social but quick
            lastfm.setMinimumCommandPeriod(0L);
            lfm.setMinimumCommandPeriod(0L);
        }
    }

    void loadAll() throws IOException {
        loadDatabase();
        loadArtistsFromSXSW();
        patchUpArtists();
        System.out.printf("%d artists\n", dbCore.getArtists().size());
    }

    private void loadArtistsFromSXSW() throws IOException {
        int oldSize = dbCore.getArtists().size();
        HashSet<String> names = new HashSet<String>();
        SXSWImporter importer = new SXSWImporter(DBLOCATION);
        System.out.printf("Loading from %s\n", SXSW_ARTISTS_URL);
        List<Artist> artists = importer.getArtists(new URL(SXSW_ARTISTS_URL));
        //List<Artist> artists = importer.getArtists(SXSW_ARTISTS_PATH);

        // check for new artists
        for (Artist artist : artists) {
            if (!dbCore.hasArtist(artist)) {
                System.out.println("Adding new artist " + artist.getName());
                dbCore.addArtist(artist);
            }
        }

        // check for missing artists
        for (Artist artist : artists) {
            names.add(artist.getName().toLowerCase());
        }

        for (Artist artist : dbCore.getArtists()) {
            if (!names.contains(artist.getName().toLowerCase())) {
                System.out.println("We lost artist " + artist.getName());
                dbCore.removeArtist(artist);
            }
        }
        saveDatabase();
        int newSize = dbCore.getArtists().size();

        if (oldSize != newSize) {
            System.out.printf("Database changed size, was %d, now %d\n", oldSize, newSize);
        }
    }

    void indexArtists() {
        SimpleIndexer indexer = searchEngine.getSimpleIndexer();
        for (Artist artist : dbCore.getArtists()) {
            index(indexer, artist);
        }
        indexer.finish();
    }

    void listArtists() {
        List<Artist> artists = dbCore.getArtists();
        Collections.sort(artists, Artist.POPULARITY_SORT);
        Collections.reverse(artists);
        for (Artist artist : artists) {
            System.out.printf("%d %s\n", artist.getArtistInfo().getListeners(), artist.getName());
        }
    }

    void indexTags() {
        countTags();
        SimpleIndexer indexer = searchEngine.getSimpleIndexer();
        for (SocialTag tag : tagCounter.getAll()) {
            indexTag(indexer, tag);
        }
        indexer.finish();
        merge();
    }

    void merge() {
        searchEngine.merge();
    }

    void listTags() {
        countTags();
        for (SocialTag tag : tagCounter.getAll()) {
            System.out.printf("%d %s\n", tag.getFreq(), tag.getName());
        }
    }

    void indexTag(SimpleIndexer indexer, SocialTag tag) {
        // BUG: if we set MAX_ARTIST_TAG to Integer.MAX_VALUE
        // certain popular tags don't index properly.

        int MAX_ARTIST_TAGS = 200;

        indexer.startDocument(tag.getName());
        indexer.addField(FIELD_NAME, tag.getName());
        indexer.addField(FIELD_TYPE, TAG_TYPE);
        List<Scored<Artist>> artists = getScoredArtistsWithTag(tag.getName());

        if (artists.size() > MAX_ARTIST_TAGS) {
            artists = artists.subList(0, MAX_ARTIST_TAGS);
        }


        for (Scored<Artist> sartist : artists) {
            Artist artist = sartist.getItem();
            //String nname = Utilities.normalize(sartist.getItem().getName());
            String nname = sartist.getItem().getName();
            if (nname.length() > 0 && artist.getArtistInfo().getListeners() > 500 && sartist.getScore() > MIN_TAG_THRESHOLD) {
                indexer.addTerm(FIELD_SOCIAL_TAGS, nname, (int) sartist.getScore());
                if (false) {
                    if (tag.getName().equalsIgnoreCase("alternative")) {
                        System.out.printf("%f %s\n", sartist.getScore(), nname);
                    }
                }
            }
        }
        indexer.endDocument();
    }

    Tree createTagTree() throws IOException {
        SimilarityEngine sim = new SimilarityEngine() {

            public double getDistance(String key1, String key2) {
                DocumentVector dv1 = getDV(key1);
                DocumentVector dv2 = getDV(key2);
                double distance = 1.0 - dv1.getSimilarity(dv2);
                if (distance < 0) {
                    distance = 0;
                }
                return distance;
            }
        };
        List<Scored<String>> tags = getAllTags();
        Tree tree = new Tree(sim, tags, true);
        return tree;
    }

    Tree createArtistTree(int max) throws IOException {
        SimilarityEngine sim = new SimilarityEngine() {

            public double getDistance(String key1, String key2) {
                DocumentVector dv1 = getDV(key1);
                DocumentVector dv2 = getDV(key2);
                double distance = 1.0 - dv1.getSimilarity(dv2);
                if (distance < 0) {
                    distance = 0;
                }
                return distance;
            }
        };
        List<Scored<String>> artists = getAllArtists(max);
        Tree tree = new Tree(sim, artists, false);
        return tree;
    }

    List<Scored<String>> getAllArtists(int max) {
        List<Artist> artists = dbCore.getArtists();
        Collections.sort(artists, Artist.POPULARITY_SORT);
        Collections.reverse(artists);

        if (artists.size() > max) {
            artists = artists.subList(0, max);
        }

        List<Scored<String>> retlist = new ArrayList<Scored<String>>();

        for (Artist artist : artists) {
            retlist.add(new Scored<String>(artist.getName(), artist.getArtistInfo().getListeners()));
        }
        return retlist;
    }

    DocumentVector getDV(String key) {
        DocumentVector dv = dvCache.get(key);
        if (dv == null) {
            dv = searchEngine.getDocumentVector(key, FIELD_SOCIAL_TAGS);
            dvCache.put(key, dv);
        }
        return dv;
    }

    private List<Scored<String>> getAllTags() {
        List<Scored<String>> tags = new ArrayList<Scored<String>>();
        countTags();
        for (SocialTag tag : tagCounter.getAll()) {
            tags.add(new Scored<String>(tag.getName(), tag.getFreq()));
        }
        return tags;
    }

    void index(SimpleIndexer indexer, Artist artist) {
        indexer.startDocument(artist.getName());
        indexer.addField(FIELD_NAME, artist.getName());
        indexer.addField(FIELD_TYPE, ARTIST_TYPE);
        for (SocialTag stag : artist.getTags()) {
            if (stag.getFreq() > 0) {
                indexer.addTerm(FIELD_SOCIAL_TAGS, stag.getName(), stag.getFreq());
                if (false && artist.getName().equalsIgnoreCase("Ben Harper")) {
                    System.out.printf("%s %d\n", stag.getName(), stag.getFreq());
                }
            }
        }
        indexer.endDocument();
    }

    void defineFields() throws SearchEngineException {
        FieldInfo typeFieldInfo = new FieldInfo(FIELD_TYPE,
                EnumSet.of(FieldInfo.Attribute.TOKENIZED, FieldInfo.Attribute.INDEXED,
                FieldInfo.Attribute.VECTORED, FieldInfo.Attribute.SAVED),
                FieldInfo.Type.STRING);
        searchEngine.defineField(typeFieldInfo);
        FieldInfo nameFieldInfo = new FieldInfo(FIELD_NAME,
                EnumSet.of(FieldInfo.Attribute.TOKENIZED, FieldInfo.Attribute.INDEXED,
                FieldInfo.Attribute.VECTORED, FieldInfo.Attribute.SAVED),
                FieldInfo.Type.STRING);
        searchEngine.defineField(nameFieldInfo);

        FieldInfo stFieldInfo = new FieldInfo(FIELD_SOCIAL_TAGS,
                EnumSet.of(FieldInfo.Attribute.INDEXED,
                FieldInfo.Attribute.VECTORED, FieldInfo.Attribute.SAVED),
                FieldInfo.Type.STRING);
        searchEngine.defineField(stFieldInfo);
    }

    private void patchUpArtists() throws IOException {
        ArtistPatcher patcher = new ArtistPatcher(dbCore, tagFilter);
        patcher.applyPatches();
    }

    public void dumpArtistPopPages() throws IOException {
        List<Artist> artists = dbCore.getArtists();
        Collections.sort(artists, Artist.POPULARITY_SORT);
        Collections.reverse(artists);
        int numPages = (int) Math.ceil(artists.size() / (double) maxPerPage);

        for (int i = 0; i < numPages; i++) {
            PrintWriter out = new PrintWriter(WEBLOCATION + getFileName(i));

            int startIndex = i * maxPerPage;
            int endIndex = i * maxPerPage + maxPerPage;
            if (endIndex > artists.size()) {
                endIndex = artists.size();
            }

            addHeader(out, i, numPages);
            for (int j = startIndex; j < endIndex; j++) {
                addArtistDescription(out, artists.get(j));
            }
            addFooter(out, i, numPages);
            out.close();
        }
    }

    public void dumpNewArtistsPage() throws IOException {
        List<Artist> newArtists = getNewArtists();
        if (newArtists.size() > 0) {
            List<Artist> artists = newArtists;
            Collections.sort(artists, Artist.POPULARITY_SORT);
            Collections.reverse(artists);

            PrintWriter out = new PrintWriter(WEBLOCATION + "sxsw_new.html");

            addHeader(out);
            addSelector(out, "Newest");
            out.println("<div class=\"tagtitle\">Recently added artists</div>");
            for (Artist artist : artists) {
                addArtistDescription(out, artist);
            }
            addFooter(out);
            out.close();
        }
    }

    private List<Artist> getNewArtists() throws IOException {
        List<Artist> newArtists = new ArrayList<Artist>();
        SXSWImporter importer = new SXSWImporter(DBLOCATION);
        System.out.printf("Loading from %s\n", SXSW_ARTISTS_URL);
        List<Artist> oldArtists = importer.getArtists("resources/last.html");

        Set<String> oldNames = new HashSet<String>();

        for (Artist artist : oldArtists) {
            oldNames.add(artist.getName());
        }

        for (Artist artist : dbCore.getArtists()) {
            if (!oldNames.contains(artist.getName())) {
                newArtists.add(artist);
            }
        }
        return newArtists;
    }

    public void dumpHotArtistsPage() throws IOException {
        List<Artist> artists = dbCore.getArtists();
        Collections.sort(artists, Artist.HOTNESS_SORT);
        Collections.reverse(artists);
        artists = artists.subList(0, 100);

        PrintWriter out = new PrintWriter(WEBLOCATION + "sxsw_hot.html");

        addHeader(out);
        addSelector(out, "Hottest");
        out.println("<div class=\"tagtitle\">The hottest artists appearing at SXSW this year</div>");
        for (Artist artist : artists) {
            addArtistDescription(out, artist);
        }
        addFooter(out);
        out.close();
    }

    public void dumpRisingArtistsPage() throws IOException {
        List<Artist> artists = dbCore.getArtists();
        Collections.sort(artists, Artist.RISING_SORT);
        Collections.reverse(artists);
        artists = artists.subList(0, 100);

        PrintWriter out = new PrintWriter(WEBLOCATION + "sxsw_rising.html");

        addHeader(out);
        addSelector(out, "Rising");
        out.println("<div class=\"tagtitle\">The unknown artists that are getting a lot of buzz</div>");
        for (Artist artist : artists) {
            addArtistDescription(out, artist);
        }
        addFooter(out);
        out.close();
    }

    public void dumpMapPage() throws IOException {
        List<Artist> artists = dbCore.getArtists();
        Collections.sort(artists, Artist.POPULARITY_SORT);
        Collections.reverse(artists);
        PrintWriter out = new PrintWriter(WEBLOCATION + "sxsw_map.html");

        copyText("resources/mapheader.html", out);
        for (Artist artist : artists) {
            if (artist.getLocation() != null) {
                out.printf("    addArtist(map, %f, %f, '%s');\n",
                        artist.getLocation().getLatitude(),
                        artist.getLocation().getLongitude(),
                        getMapDescription(artist));
            }
        }
        copyText("resources/mapfooter.html", out);
        out.close();
    }

    public void dumpZoomedMapPage() throws IOException {
        List<Artist> artists = dbCore.getArtists();
        Collections.sort(artists, Artist.POPULARITY_SORT);
        Collections.reverse(artists);

        ditherManager.clear();

        PrintWriter out = new PrintWriter(WEBLOCATION + "sxsw_map.html");
        copyText("resources/mapheader.html", out);

        int zoomLevel = 0;
        double minDistance = 3000;  // kilometers
        while (artists.size() > 0) {
            List<Artist> addedList = addZoomed(artists, out, zoomLevel, minDistance);
            artists.removeAll(addedList);
            minDistance = minDistance / 2;
            if (minDistance < DISTANCE_FLOOR) {
                minDistance = 0;
            }
            zoomLevel++;
        }
        copyText("resources/mapfooter.html", out);
        out.close();
    }

    private List<Artist> addZoomed(List<Artist> artists, PrintWriter out, int zoomlevel, double minDistance) {
        List<Artist> inlist = new ArrayList<Artist>();
        for (Artist artist : artists) {
            if (getMinDistance(artist, inlist) >= minDistance) {
                inlist.add(artist);
                if (artist.getLocation() != null) {
                    Location location = ditherManager.getLocation(artist);
                    out.printf("    addArtistWithZoom(mgr, %d, %f, %f, '%s');\n",
                            zoomlevel,
                            location.getLatitude(),
                            location.getLongitude(),
                            getMapDescription(artist));
                }
            }
        }
        // System.out.printf("Zoom level %d  mindistance %f  num %d\n", zoomlevel, minDistance, inlist.size());
        return inlist;
    }

    private double getMinDistance(Artist artist, List<Artist> artistList) {
        double minDistance = Double.MAX_VALUE;
        for (Artist inArtist : artistList) {
            double distance = getDistance(artist, inArtist);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    private double getDistance(Artist a1, Artist a2) {
        Location loc1 = a1.getLocation();
        Location loc2 = a2.getLocation();

        if (loc1 != null && loc2 != null) {
            return loc1.getDistance(loc2);
        } else {
            return 0;
        }
    }

    public void showDistances() throws IOException {
        List<Artist> artists = dbCore.getArtists();
        Collections.sort(artists, Artist.POPULARITY_SORT);
        Collections.reverse(artists);
        Location last = null;
        String lastPlace = null;
        for (Artist artist : artists) {
            if (artist.getLocation() != null && last != null) {
                float distance = artist.getLocation().getDistance(last);
                System.out.printf("Distance from %s to %s is %f miles\n",
                        lastPlace, artist.getWhere(), distance * 0.621371192);
            }
            last = artist.getLocation();
            lastPlace = artist.getWhere();
        }
    }

    private String getMapDescription(Artist artist) {
        return getArtistDescription(artist, true);
    }

    private String getArtistDescription(Artist artist, boolean forJS) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"artist\">");
        if (artist.getArtistInfo().getLargeImage().length() > 0) {
            sb.append(String.format("<img class=\"artistimage\" src=\"%s\" alt=\"Artist Image\" />", artist.getArtistInfo().getLargeImage()));
        }
        sb.append(String.format("<div class=\"artistdescription\">"));
        if (artist.getUrl().length() > 0) {
            sb.append(String.format("<span class=\"artistname\"><a href=\"%s\"> %s </a></span> <span class=\"locale\">(%s)</span>",
                    artist.getUrl(), fmtName(artist.getName(), forJS), fmtName(artist.getWhere(), forJS)));
        } else {
            sb.append(String.format("<span class=\"artistname\">%s</span> <span class=\"locale\">(%s)</span>",
                    fmtName(artist.getName(), forJS), fmtName(artist.getWhere(), forJS)));
        }
        sb.append(getBio(artist, forJS));
        sb.append(String.format(("<b> (" + formatBigNumber(artist.getArtistInfo().getListeners()) + " Last.fm listeners)</b>")));
        if (artist.getHotness() > 0) {
            int pcHotness = (int) (artist.getHotness() * 100);
            sb.append(("<i> (Hotness: " + pcHotness + ")</i>"));
        }
        sb.append(getTagCloud(artist));
        sb.append("<div class=\"artistlinks\">");
        sb.append("<table class=\"linktable\">");
        sb.append("<tr><td>");
        sb.append(getArtistRadioLink(artist));
        sb.append("</td><td>");
        sb.append(getArtistPhotoLink(artist));
        sb.append("</td><td>");
        sb.append(getArtistVideoLink(artist));
        sb.append("</td></tr></table>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        return sb.toString();
    }

    public void dumpArtistAlphaPages() throws IOException {
        List<Artist> artists = dbCore.getArtists();
        Collections.sort(artists, Artist.ALPHA_SORT);

        for (char initialLetter = 'a'; initialLetter <= 'z'; initialLetter++) {
            PrintWriter out = new PrintWriter(WEBLOCATION + getFileName(initialLetter));
            addHeader(out, initialLetter);
            List<Artist> alphaArtists = getArtistsForLetter(artists, initialLetter);
            // special case, add all other chars to the 'z' bucket
            if (initialLetter == 'z') {
                alphaArtists.addAll(getArtistsForNonLetter(artists));
            }
            for (Artist artist : alphaArtists) {
                addArtistDescription(out, artist);
            }
            addFooter(out, initialLetter);
            out.close();
        }
    }

    private List<Artist> getArtistsForLetter(List<Artist> artists, char initialLetter) {
        List<Artist> returnList = new ArrayList<Artist>();

        for (Artist artist : artists) {
            String name = artist.getName().toLowerCase().trim();
            name = name.replaceFirst("^the\\s+", "");
            if (name.length() > 0 && name.charAt(0) == initialLetter) {
                returnList.add(artist);
            }
        }

        return returnList;
    }

    private List<Artist> getArtistsForNonLetter(List<Artist> artists) {
        List<Artist> returnList = new ArrayList<Artist>();

        for (Artist artist : artists) {
            String name = artist.getName().toLowerCase().trim();
            name = name.replaceFirst("^the\\s+", "");
            if (name.length() > 0 && !Character.isLetter(name.charAt(0))) {
                returnList.add(artist);
            }
        }
        return returnList;
    }

    public void dumpTagPage(String tag) throws IOException {
        List<Artist> artists = getArtistsWithTag(tag);
        PrintWriter out = new PrintWriter(WEBLOCATION + getFileName(tag));
        addHeader(out, artists.size(), tag);
        for (Artist artist : artists) {
            addArtistDescription(out, artist);
        }
        addFooter(out, tag);
        out.close();
    }

    public void dumpMissingListeners() throws IOException {
        List<Artist> artists = dbCore.getArtists();
        PrintWriter out = new PrintWriter("missing.dat");
        for (Artist artist : artists) {
            if (artist.getArtistInfo().getListeners() == 0) {
                out.println(artist.getName());
            }
        }
        out.close();
    }

    public void dumpTagIndex() throws IOException {
        PrintWriter out = new PrintWriter(WEBLOCATION + "tagindex.html");
        addHeader(out);
        addSelector(out, "Tag");
        out.println(getTagCloud());
        addFooter(out);
        out.close();
    }

    private void countTags() {
        tagCounter = new TagCounter();
        for (Artist artist : dbCore.getArtists()) {
            SocialTag[] tags = artist.getTags();
            for (SocialTag tag : tags) {
                //String ntag = Utilities.normalize(tag.getName());
                //tc.accum(ntag, 1);
                tagCounter.accum(tag.getName(), 1);
            }
        }
    }

    public void dumpTagPages() throws IOException {

        for (SocialTag st : tagCounter.getAll()) {
            dumpTagPage(st.getName());
        }
    }

    private List<Scored<Artist>> getScoredArtistsWithTag(String tag) {
        List<Scored<Artist>> scoredArtists = new ArrayList<Scored<Artist>>();
        for (Artist artist : dbCore.getArtists()) {
            int freq = artist.getTagFreq(tag);
            if (freq > MIN_TAG_THRESHOLD) {
                scoredArtists.add(new Scored<Artist>(artist, freq));
            }
        }

        Collections.sort(scoredArtists, ScoredComparator.COMPARATOR);
        Collections.reverse(scoredArtists);
        return scoredArtists;
    }

    private List<Artist> getArtistsWithTag(String tag) {
        List<Artist> artists = new ArrayList<Artist>();
        for (Artist artist : dbCore.getArtists()) {
            int freq = artist.getTagFreq(tag);
            if (freq > MIN_TAG_THRESHOLD) {
                artists.add(artist);
            }
        }
        Collections.sort(artists, Artist.POPULARITY_SORT);
        Collections.reverse(artists);
        return artists;
    }

    private void addHeader(PrintWriter out) {
        copyText("resources/header.html", out);
    }

    private void addHeader(PrintWriter out, int page, int numPages) {
        copyText("resources/header.html", out);
        addSelector(out, "Popularity");
        out.println(getPager(page, numPages));
    }

    private void addHeader(PrintWriter out, char c) {
        copyText("resources/header.html", out);
        addSelector(out, "Alpha");
        out.println(getAlphaPager(c));
    }

    private void addHeader(PrintWriter out, int num, String tag) {
        copyText("resources/header.html", out);
        addSelector(out, "Tag");
        out.println("<div class=\"tagtitle\">" + num + " artists tagged with <b>" + "<a href=\"http://last.fm/tag/" + tag + "\">" + tag + "</a>" + "</b></div>");
    }

    private void addFooter(PrintWriter out, int page, int numPages) {
        out.println(getPager(page, numPages));
        copyText("resources/footer.html", out);
    }

    private void addFooter(PrintWriter out, char c) {
        out.println(getAlphaPager(c));
        copyText("resources/footer.html", out);
    }

    private void addFooter(PrintWriter out) {
        copyText("resources/footer.html", out);
    }

    private void addFooter(PrintWriter out, String tag) {
        copyText("resources/footer.html", out);
    }

    private void addSelector(PrintWriter out, String cur) {
        out.print("<div class=\"browser\">");
        out.print("Browse by: ");
        addSelector(out, cur, "Popularity", "sxsw.html");
        addSelector(out, cur, "Alpha", "sxsw_a.html");
        addSelector(out, cur, "Tag", "tagindex.html");
        addSelector(out, cur, "Map", "sxsw_map.html");
        addSelector(out, cur, "Hottest", "sxsw_hot.html");
        addSelector(out, cur, "Rising", "sxsw_rising.html");
        addSelector(out, cur, "Newest", "sxsw_new.html");
        out.print("</div>");
    }

    private void addSelector(PrintWriter out, String cur, String label, String link) {
        if (cur.equals(label)) {
            out.print("<span class=\"browseselected\">" + label + "</span>");
        } else {
            out.print("<span class=\"browse\"><a href=\"" + link + "\">" + label + "</a></span>");
        }
    }

    private String getPager(int page, int numPages) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"pager\">");
        if (page > 0) {
            sb.append(createPageLink("&lt;&lt;", page - 1));
        } else {
            sb.append(createNoLink("&lt;&lt;"));
        }

        for (int i = 0; i < numPages; i++) {
            if (i == page) {
                sb.append(createNoPageLink(i));
            } else {
                sb.append(createPageLink(i));
            }
        }

        if (page < numPages - 1) {
            sb.append(createPageLink("&gt;&gt;", page + 1));
        } else {
            sb.append(createNoLink("&gt;&gt;"));
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String getAlphaPager(char letter) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"pager\">");
        if (letter > 'a') {
            sb.append(createAlphaLink("&lt;&lt;", (char) (letter - 1)));
        } else {
            sb.append(createNoLink("&lt;&lt;"));
        }

        for (char c = 'a'; c <= 'z'; c++) {
            if (c == letter) {
                sb.append(createNoAlphaLink(c));
            } else {
                sb.append(createAlphaLink(c));
            }
        }
        if (letter < 'z') {
            sb.append(createAlphaLink("&gt;&gt;", (char) (letter + 1)));
        } else {
            sb.append(createNoLink("&lt;&lt;"));
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String getFileName(int page) {
        if (page == 0) {
            return "sxsw.html";
        } else {
            return "sxsw_" + (page + 1) + ".html";
        }
    }

    private String getFileName(char c) {
        return "sxsw_" + (c) + ".html";
    }

    private String getFileName(String name) {
        String nname = Utilities.normalize(name);
        return nname + ".html";
    }

    private String createPageLink(int pageNumber) {
        return createPageLink(Integer.toString(pageNumber + 1), pageNumber);
    }

    private String createNoPageLink(int pageNumber) {
        return createNoPageLink(Integer.toString(pageNumber + 1), pageNumber);
    }

    private String createPageLink(String label, int pageNumber) {
        return "<span class=\"page\"><a href=\"" + getFileName(pageNumber) + "\">" + label + "</a></span>";
    }

    private String createNoPageLink(String label, int pageNumber) {
        return "<span class=\"pagenolink\">" + label + "</span>";
    }

    private String createAlphaLink(char label) {
        return "<span class=\"page\"><a href=\"" + getFileName(label) + "\">" + label + "</a></span>";
    }

    private String createAlphaLink(String label, char page) {
        return "<span class=\"page\"><a href=\"" + getFileName(page) + "\">" + label + "</a></span>";
    }

    private String createNoAlphaLink(char label) {
        return "<span class=\"pagenolink\">" + label + "</span>";
    }

    private String createNoLink(String label) {
        return "<span class=\"page\">" + label + "</span>";
    }

    private void copyText(String path, PrintWriter out) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(path));
            String line = null;
            while ((line = in.readLine()) != null) {
                out.println(line);
            }
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(ArtistCrawler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(ArtistCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void addArtistDescription(PrintWriter out, Artist artist) {
        out.println(getArtistDescription(artist, false));
    }

    String formatBigNumber(int val) {
        return formatter.format(val);
    }

    private String getArtistRadioLink(Artist artist) {
        String link = "<span class=\"left\"><a target=\"listen_view\" href=\"http://www.last.fm/music/" + encode(artist.getName()) + "?autostart\">" +
                "Listen at Last.fm </a></span>";
        return link;
    }

    private String getTagCloud(Artist artist) {
        if (artist.getTags().length > 0) {
            SocialTag[] tags = artist.getTags();
            Arrays.sort(tags, LastItem.ALPHA_ORDER);

            StringBuilder sb = new StringBuilder();
            sb.append("<div class=\"tagcloud\">");
            for (SocialTag tag : tags) {
                int size = scoreToFontSize(tag.getFreq());
                if (tag.getFreq() > MIN_TAG_THRESHOLD) {
                    sb.append("<a href=\"" + getFileName(tag.getName()) + " \">");
                    sb.append("<span style=\"font-size:" + size + "pt;\"> " + tag.getName() + "</span>");
                    sb.append("</a>");
                    sb.append(" ");
                }
            }
            sb.append("</div>");
            return sb.toString();
        } else {
            return "";
        }
    }

    private String getTagCloud() {
        List<SocialTag> tags = tagCounter.getAll();
        Collections.sort(tags, LastItem.ALPHA_ORDER);
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"tagcloudindex\">");
        for (SocialTag tag : tags) {
            int size = scoreToFontSize(tag.getFreq());
            if (tag.getFreq() > MIN_TAG_THRESHOLD) {
                sb.append("<a href=\"" + getFileName(tag.getName()) + " \">");
                sb.append("<span style=\"font-size:" + size + "pt;\"> " + tag.getName() + "</span>");
                sb.append("</a>");
                sb.append(" ");
            }
        }
        sb.append("</div>");
        return sb.toString();
    }

    private static int scoreToFontSize(int score) {
        int min = 6;
        int max = 18;
        int range = max - min;
        int fs = (int) Math.round(score / 100.0 * range + min);
        return fs;
    }

    public String getArtistVideoLink(Artist artist) {
        String link = "";
        if (artist.getVideos().length > 0) {
            YoutubeVideo ytv = artist.getVideos()[0];
            link = "<span class=\"right\"><a href=\"" + ytv.getURL() + "\" target=\"listen_view\">Watch on Youtube" + "</a></span>";
        }
        return link;
    }
    //http://flickr.com/search/show/?q=the+shackletons+(show+OR+concert+OR+sxsw)%20&s=rel

    public String getArtistPhotoLink(Artist artist) {
        String encodedName = encode("\"" + artist.getName() + "\"");
        String url = "http://flickr.com/search/show/?q=" + encodedName + "+(show+OR+concert+OR+sxsw+OR+live+OR+band)&amp;mt=photos";
        String link = "<span class=\"middle\"><a href=\"" + url + "\" target=\"listen_view\">See on Flickr" + "</a></span>";
        return link;
    }

    public String getArtistVideoEmbeddedObject(Artist artist) {
        String html = "";
        if (artist.getVideos().length > 0) {
            html = readFile("video.html");
            String embeddedUrl = artist.getVideos()[0].getURL().toExternalForm();
            html = html.replaceAll("__YOUTUBE_URL__", embeddedUrl);
        }
        return html;
    }

    private String readFile(String path) {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(path));
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(ArtistCrawler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(ArtistCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sb.toString();
    }

    private String getBio(Artist artist) {
        String bio = artist.getArtistInfo().getBioSummary();
        if (bio.length() == 0) {
            return "<i> Don't know much about " + artist.getName() + "</i>";
        } else {
            // fixup by replacing smart quotes with regular quotes
            bio = bio.replaceAll("\u2018", "'");
            bio = bio.replaceAll("\u2019", "'");
            bio = bio.replaceAll("\ubfef", "'");
            return bio;
        }
    }

    private String getBio(Artist artist, boolean forJS) {
        String bio = artist.getArtistInfo().getBioSummary();
        if (bio.length() == 0) {
            bio = "<i> Don't know much about " + artist.getName() + "</i>";
        }
        bio = fmt(bio, forJS);
        return bio;
    }

    private String fmt(String s, boolean forJS) {
        s = encodeHTML(s);
        if (forJS) {
            s = fmtJS(s);
        }
        return s;
    }

    private String fmtName(String s, boolean forJS) {
        s = fullEncodeHTML(s);
        if (forJS) {
            s = fmtJS(s);
        }
        return s;
    }

    private String fmtJS(String s) {
        s = s.replaceAll("'", "\\\\'");
        s = s.replaceAll("\n", "");
        return s;
    }

    public void resolveArtists() {
        int count = 0;
        for (Artist artist : dbCore.getArtists()) {
            try {
                if (artist.getArtistInfo() == null) {
                    artist.setArtistInfo(lfm.getArtistInfoByName(artist.getName()));
                    dbCore.setDirty(true);
                }
                if (artist.getVideos() == null) {
                    List<YoutubeVideo> ytList = yt2.musicVideoSearch(artist.getName(), 5);
                    artist.setVideos(ytList.toArray(new YoutubeVideo[0]));
                    dbCore.setDirty(true);
                }

                if (artist.getTags() == null) {
                    SocialTag[] filteredTags = filterTags(lastfm.getArtistTags(artist.getName()));
                    artist.setTags(filteredTags);
                    dbCore.setDirty(true);
                }


                // artist.setLocation(null); // Force recrawl of location info
                if (artist.getLocation() == null) {
                    Location location = gmaps.getLocation(artist.getWhere());
                    if (location != null) {
                        artist.setLocation(location);
                        System.out.printf("%.4f %.4f %s, %s\n", location.getLatitude(),
                                location.getLongitude(), artist.getWhere(), artist.getName());
                        dbCore.setDirty(true);
                    } else {
                        System.out.println("Can't find location for " + artist.getName() + " (" + artist.getWhere() + ")");
                    //dbCore.removeArtist(artist);
                    }
                }

                if (artist.getEchoID() == null) {

                    String id = getEchoNestID(artist);
                    if (id != null && id.length() > 0) {
                        float hotness = echoNest.getHotness(id);
                        artist.setEchoID(id);
                        artist.setHotness(hotness);
                    } else {
                        artist.setEchoID(ECHO_NONE);
                    }
                    dbCore.setDirty(true);
                }

                log(artist.getName(), artist);
                if (++count % 100 == 0) {
                    saveDatabase();
                }
            } catch (IOException ioe) {
                System.err.println("Trouble getting info for " + artist + ": " + ioe);
                artist.setArtistInfo(NO_INFO);
                artist.setVideos(new YoutubeVideo[0]);
                artist.setTags(new SocialTag[0]);
            }
        }
        saveDatabase();
        countTags();
    }

    public void echoQueryCheck() {
        for (Artist artist : dbCore.getArtists()) {
            try {
                EchoArtist echoArtist = echoNest.getProfileByName(artist.getName());
                if (echoArtist == null) {
                    System.out.println("no match|" + artist.getName() + "|" + artist.getArtistInfo().getListeners());
                } else {
                    // verify match"
                    String aname = Utilities.normalize(artist.getName());
                    String ename = Utilities.normalize(echoArtist.getName());
                    if (aname.equals(ename)) {
                        System.out.printf("match|%s|%s|%d\n", artist.getName(), echoArtist.getName(),
                                artist.getArtistInfo().getListeners());
                    } else {
                        System.out.printf("suspicious match|%s|%s|%d\n", artist.getName(), echoArtist.getName(),
                                artist.getArtistInfo().getListeners());
                    }
                }
            } catch (IOException ioe) {
                System.out.printf("API Exception|%s|%s\n", artist.getName(), ioe.getMessage());
            }
        }
    }

    public String getEchoNestID(Artist artist) throws IOException {
        String id = null;
        EchoArtist echoArtist = null;
        try {
            if (artist.getArtistInfo().getMbid().length() > 0) {
                echoArtist = echoNest.getProfileByMBID(artist.getArtistInfo().getMbid());
                if (echoArtist != null) {
                    System.out.printf("MBAMBAID ID Matched [%s]  to [%s]\n", artist.getName(), echoArtist.getName());
                }
            }
            if (echoArtist == null) {
                echoArtist = echoNest.getProfileByName(artist.getName());
            }
        } catch (IOException ioe) {
        }

        if (echoArtist == null) {
            System.out.printf("NO Match for [%s]\n", artist.getName());
        } else {
            // verify match"
            String aname = Utilities.normalize(artist.getName());
            String ename = Utilities.normalize(echoArtist.getName());
            if (aname.equals(ename)) {
                System.out.printf("Matched [%s]  to [%s]\n", artist.getName(), echoArtist.getName());
                id = echoArtist.getId();
            } else {
                System.out.printf("Suspicious match [%s]  to [%s]\n", artist.getName(), echoArtist.getName());
            }
        }
        return id;
    }

    public void resolveEchonest() throws IOException {
        int count = 0;
        int suspicious = 0;
        for (Artist artist : dbCore.getArtists()) {
            EchoArtist echoArtist = null;
            try {
                echoArtist = echoNest.getProfileByName(artist.getName());
            } catch (IOException ioe) {
            }

            if (echoArtist == null) {
                System.out.printf("NO Match for [%s]\n", artist.getName());
            } else {
                // verify match"
                String aname = Utilities.normalize(artist.getName());
                String ename = Utilities.normalize(echoArtist.getName());
                if (aname.equals(ename)) {
                    System.out.printf("Matched [%s]  to [%s]\n", artist.getName(), echoArtist.getName());
                    count++;
                } else {
                    suspicious++;
                    System.out.printf("Suspicious match [%s]  to [%s]\n", artist.getName(), echoArtist.getName());
                }
            }
        }
        System.out.printf("Found %d of %d, %d matches were suspiciouos\n", count, dbCore.getArtists().size(), suspicious);
    }

    private SocialTag[] filterTags(SocialTag[] tags) {
        List<SocialTag> returnList = new ArrayList<SocialTag>();

        for (SocialTag tag : tags) {
            if (tag.getFreq() > MIN_TAG_THRESHOLD) {
                String mappedName = tagFilter.mapTagName(tag.getName());
                if (mappedName != null) {
                    SocialTag newTag = new SocialTag(mappedName, tag.getFreq());
                    returnList.add(newTag);
                }
            }
        }
        return returnList.toArray(new SocialTag[0]);
    }
    private boolean showTags = false;
    private boolean showVideos = false;

    private void log(String name, Artist artist) {
        total++;
        if (artist.getArtistInfo() == null) {
            System.out.printf("%d/%d '%s' '%s'\n", match, total, "(NONE)", name);
        } else {
            match++;
            // artist.getArtistInfo().dump(true);

            if (showVideos) {
                System.out.printf(" %d Videos:", artist.getVideos().length);
                for (YoutubeVideo video : artist.getVideos()) {
                    System.out.printf("    %d %s\n", video.getViewCount(), video.getTitle());
                }
            }

            if (showTags) {
                SocialTag[] tags = artist.getTags();
                System.out.printf(" %d Tags:", tags.length);
                for (SocialTag tag : tags) {
                    System.out.print(tag.getName() + " ");
                }
                System.out.println();
            }
        }
    }

    void showInterestingTagInfo() {
        try {
            PrintWriter out = new PrintWriter("tags.txt");
            for (SocialTag st : tagCounter.getAll()) {
                out.printf("%d %s\n", st.getFreq(), st.getName());
            }
            out.close();
        } catch (IOException ioe) {
            System.err.println("Trouble writing tag data");
        }

        // now lets find out how many tags are needed to cover all of our artits
        Set<Artist> foundSet = new HashSet<Artist>();

        // first filter out artists with no tags

        for (Artist artist : dbCore.getArtists()) {
            if (artist.getTags().length == 0) {
                foundSet.add(artist);
                System.out.println("No tags: " + artist.getName() + " listeners: " + artist.getArtistInfo().getListeners());
            }
        }

        System.out.println("Artists with no tags: " + foundSet.size());

        for (SocialTag st : tagCounter.getAll()) {
            int tagMatch = 0;
            for (Artist artist : dbCore.getArtists()) {
                if (artist.hasTag(st.getName())) {
                    tagMatch++;
                    foundSet.add(artist);
                }
            }
            System.out.printf("%s freq %d tagMatch %d total %d remaining %d\n",
                    st.getName(), st.getFreq(), tagMatch, foundSet.size(), dbCore.getArtists().size() - foundSet.size());
        }
    }

    public void generatePages() throws IOException {
        File webdir = new File(WEBLOCATION);

        if (!webdir.exists()) {
            webdir.mkdir();
        }

        dumpArtistPopPages();
        dumpArtistAlphaPages();
        dumpTagPages();
        dumpTagIndex();
        dumpZoomedMapPage();
        dumpNewArtistsPage();
        dumpHotArtistsPage();
        dumpRisingArtistsPage();
        dumpMissingListeners();
    }

    public void doAll() throws IOException {
        resolveArtists();
        generatePages();
    }

    public List<Scored<Artist>> searchArtist(String query, int maxResults) throws SearchEngineException {
        String squery = "(type = artist) <AND> (name <matches> \"*" + query + "*\")";
        ResultSet rs = searchEngine.search(squery);
        List<Scored<Artist>> retList = new ArrayList<Scored<Artist>>();
        for (Result result : rs.getResults(0, maxResults)) {
            retList.add(new Scored<Artist>(dbCore.getArtist(result.getKey()), result.getScore()));
        }
        return retList;
    }

    public List<Scored<String>> searchTag(String query, int maxResults) throws SearchEngineException {
        String squery = "(type = tag) <AND> (name <matches> \"*" + query + "*\")";
        ResultSet rs = searchEngine.search(squery);
        List<Scored<String>> retList = new ArrayList<Scored<String>>();
        for (Result result : rs.getResults(0, maxResults)) {
            retList.add(new Scored<String>(result.getKey(), result.getScore()));
        }
        return retList;
    }

    public List<Scored<String>> search(String query, int maxResults) throws SearchEngineException {
        String squery = "(name <matches> \"*" + query + "*\")";
        ResultSet rs = searchEngine.search(squery);
        List<Scored<String>> retList = new ArrayList<Scored<String>>();
        for (Result result : rs.getResults(0, maxResults)) {
            retList.add(new Scored<String>(result.getKey(), result.getScore()));
        }
        return retList;
    }

    public List<Scored<Artist>> findSimilarArtist(Artist artist, int maxResults) throws SearchEngineException {
        List<Scored<Artist>> retList = new ArrayList<Scored<Artist>>();
        //DocumentVector dv = searchEngine.getDocumentVector(item.getKey(), "socialtags");
        DocumentVector dv = searchEngine.getDocumentVector(artist.getName(), FIELD_SOCIAL_TAGS);
        if (dv != null) {
            ResultSet rs = dv.findSimilar("-score", 1);
            for (Result result : rs.getResults(0, maxResults, new TypeFilter(ARTIST_TYPE))) {
                retList.add(new Scored<Artist>(dbCore.getArtist(result.getKey()), result.getScore()));
            }

            if (false) {
                Map<String, Float> terms = dv.getTopWeightedTerms(20);
                for (String term : terms.keySet()) {
                    System.out.printf("%.6f %s\n", terms.get(term), term);
                }
            }
        }
        return retList;
    }

    public List<Scored<String>> findSimilarTag(String tag, int maxResults) throws SearchEngineException {
        List<Scored<String>> retList = new ArrayList<Scored<String>>();
        //DocumentVector dv = searchEngine.getDocumentVector(item.getKey(), "socialtags");
        DocumentVector dv = searchEngine.getDocumentVector(tag, FIELD_SOCIAL_TAGS);
        if (dv != null) {
            ResultSet rs = dv.findSimilar("-score", 1);
            for (Result result : rs.getResults(0, maxResults, new TypeFilter(TAG_TYPE))) {
                retList.add(new Scored<String>(result.getKey(), result.getScore()));
            }

            if (false) {
                Map<String, Float> terms = dv.getTopWeightedTerms(20);
                for (String term : terms.keySet()) {
                    System.out.printf("%.6f %s\n", terms.get(term), term);
                }
            }
        }
        return retList;
    }

    public static void main(String[] args) throws IOException, SearchEngineException {
        ArtistCrawler resolver = new ArtistCrawler();
        resolver.doAll();
        resolver.dumpMissingListeners();
        System.out.println("Done.");
        System.exit(0);
    }

    private String encode(String parameter) {
        try {
            return URLEncoder.encode(parameter, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return parameter;
        }
    }

    public boolean saveDatabase() {
        boolean ok = true;
        if (dbCore.isDirty()) {
            File dbfile = new File(DBNAME);
            ObjectOutputStream oos = null;
            try {
                FileOutputStream fos = new FileOutputStream(dbfile);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(dbCore);
                oos.close();
                System.out.println("DB Saved to " + dbfile);
                dbCore.setDirty(false);
            } catch (IOException ex) {
                ok = false;
            } finally {
                try {
                    oos.close();
                } catch (IOException ex) {
                    ok = false;
                }
            }
        }
        return ok;
    }

    public List<Artist> getAllArtists() {
        return dbCore.getArtists();
    }

    public void loadDatabase() {
        File dbfile = new File(DBNAME);
        if (dbfile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(dbfile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Object dbobj = ois.readObject();
                if (dbobj != null) {
                    dbCore = (DBCore) dbobj;
                    dbCore.setDirty(false);
                }
            } catch (ClassNotFoundException ex) {
                System.err.println("Can't find the class " + ex);
            } catch (IOException ex) {
                System.err.println("Can't read database " + ex);
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ex) {
                    System.err.println("Trouble with the db close " + ex);
                }
            }
        }
        if (dbCore == null) {
            dbCore = new DBCore();
        }
    }

    public static String fullEncodeHTML(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') {
                out.append("&amp;");
            } else if (c == '<') {
                out.append("&lt;");
            } else if (c == '"') {
                out.append("&quot;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c > 127) {
                out.append("&#" + (int) c + ";");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String encodeHTML(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127) {
                out.append("&#" + (int) c + ";");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}

class TagCounter {

    private Map<String, Integer> map = new HashMap<String, Integer>();

    public void set(String name, int value) {
        map.put(name, value);
    }

    public void accum(String name, int value) {
        Integer d = map.get(name);
        if (d == null) {
            d = Integer.valueOf(0);
        }
        map.put(name, d + value);
    }

    List<SocialTag> getAll() {
        List<SocialTag> results = new ArrayList<SocialTag>();

        for (Map.Entry<String, Integer> e : map.entrySet()) {
            results.add(new SocialTag(e.getKey(), e.getValue()));
        }
        Collections.sort(results, SocialTag.FREQ_ORDER);
        Collections.reverse(results);
        return results;
    }
}

class DBCore implements Serializable {

    private static final long serialVersionUID = 6143835715594L;
    private List<Artist> artists;
    private boolean dirty = false;

    DBCore() {
        artists = new ArrayList<Artist>();
    }

    void addArtist(Artist artist) {
        artists.add(artist);
        dirty = true;
    }

    void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    boolean isDirty() {
        return dirty;
    }

    List<Artist> getArtists() {
        return new ArrayList<Artist>(artists);
    }

    boolean hasArtist(Artist artist) {
        for (Artist a : artists) {
            if (a.getName().equalsIgnoreCase(artist.getName())) {
                return true;
            }
        }
        return false;
    }

    Artist getArtist(String name) {
        for (Artist a : artists) {
            if (a.getName().equalsIgnoreCase(name)) {
                return a;
            }
        }

        // if we can't find and exact match, try with normalized names

        String nname = Utilities.normalize(name);

        for (Artist a : artists) {
            if (Utilities.normalize(a.getName()).equals(nname)) {
                return a;
            }
        }
        return null;
    }

    void removeArtist(Artist artist) {
        artists.remove(artist);
        System.out.println("Removing artist " + artist.getName());
        setDirty(true);
    }
}

class DitherManager {

    private Set<String> locations = new HashSet<String>();
    private float latSpan = .05f;
    private float longSpan = .05f;
    private Random rng = new Random();

    Location getLocation(Artist artist) {
        Location location = null;
        if (artist.getLocation() != null) {
            if (locations.contains(artist.getWhere())) {
                float ditheredLatitude = artist.getLocation().getLatitude() + random() * latSpan;
                float ditheredLongitude = artist.getLocation().getLongitude() + random() * longSpan;
                location = new Location(ditheredLatitude, ditheredLongitude);
            } else {
                locations.add(artist.getWhere());
                location = artist.getLocation();
            }
        }

        return location;
    }

    float random() {
        return (float) rng.nextGaussian();
    }

    void clear() {
        locations.clear();
    }
}
