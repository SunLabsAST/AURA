/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.search.music.web.apml;

import com.sun.labs.search.music.web.lastfm.Item;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author plamere
 */
public class PandoraProfileRetriever implements APMLRetriever {

    private ConceptRetriever cr;

    public PandoraProfileRetriever(ConceptRetriever cr) {
        this.cr = cr;
    }

    public APML getAPMLForUser(String user) throws IOException {
        APML apml =  new APML("Taste for Pandora user " + user);
        apml.addProfile(getProfileForUser(user));
        return apml;
    }
    
    public Profile getProfileForUser(String user) throws IOException {
        Item[] artists = getTopArtistsForUser(user);
        Concept[] implicit = cr.getImplicitFromExplicit(artists);
        Concept[] explicit = APML.getExplicitConceptsFromItems(artists);
        return new Profile("music", implicit, explicit);
    }

    /* 
    <mm:Artist>
    <dc:title>Kenny Garrett</dc:title>
    </mm:Artist>
     */
    private Item[] getTopArtistsForUser(String user) throws IOException {
        Map<String, Integer> map = new HashMap<String, Integer>();
        InputStream is = null;
        List<String> elements = new ArrayList<String>();
        try {
            URL url = new URL("http://feeds.pandora.com/feeds/people/" + user + "/favorites.xml");
            is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            int c;
            while ((c = br.read()) != -1) {
                sb.append((char) c);
            }
            br.close();
            String content = sb.toString();
            String tagregex = "<mm:Artist>\\s+<dc:title>([^<]+)</dc:title>";
            Pattern tagPattern = Pattern.compile(tagregex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher tagMatcher = tagPattern.matcher(content);
            int max = -Integer.MAX_VALUE;
            while (tagMatcher.find()) {
                String artist = tagMatcher.group(1).trim();
                artist = normalize(artist);
                Integer count = map.get(artist);
                if (count == null) {
                    count = new Integer(0);
                }
                map.put(artist, count + 1);
            }

        } finally {
            if (is != null) {
                is.close();
            }
        }

        Item[] items = new Item[map.size()];

        int count = 0;
        for (String artist : map.keySet()) {
            items[count++] = new Item(artist, map.get(artist));
        }
        return items;
    }

    private String normalize(String artist) {
        artist = artist.replaceAll("&amp;", "&");
        artist = artist.replaceAll("&quote;", "\"");
        artist = artist.replaceAll("&apos;", "\'");
        return artist;
    }

    public static void main(String[] args) throws Exception {

        LastFMProfileRetriever lcr = new LastFMProfileRetriever();
        APMLRetriever pcr = new PandoraProfileRetriever(lcr);

        {
            System.out.println("Paul Lamere");
            APML apml = pcr.getAPMLForUser("paul.lamere");
            System.out.println(apml);
        }

        {
            System.out.println("tconrad");
            APML apml = pcr.getAPMLForUser("tconrad");
            System.out.println(apml);
        }

        {
            System.out.println("tim");
            APML apml = pcr.getAPMLForUser("tim");
            System.out.println(apml);
        }
    }
}
