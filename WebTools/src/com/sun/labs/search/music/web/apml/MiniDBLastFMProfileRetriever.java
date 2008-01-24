/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web.apml;

import com.sun.labs.search.music.minidatabase.Artist;
import com.sun.labs.search.music.minidatabase.MusicDatabase;
import com.sun.labs.search.music.minidatabase.Scored;
import com.sun.labs.search.music.minidatabase.Tag;
import com.sun.labs.search.music.web.Cache;
import com.sun.labs.search.music.web.lastfm.Item;
import com.sun.labs.search.music.web.lastfm.LastFM;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class MiniDBLastFMProfileRetriever implements APMLRetriever, ConceptRetriever {

    private static final float MIN_CONCEPT_SCORE = .03f;
    private LastFM lastfm;
    private final static Item[] EMPTY_ITEM = new Item[0];
    private MusicDatabase mdb;
    private Cache<Item[]> userCache;


    public MiniDBLastFMProfileRetriever(MusicDatabase db) throws IOException {
        this.mdb = db;
        lastfm = new LastFM();
        userCache = new Cache<Item[]>(1000, 1, null);
    }


    public APML getAPMLForUser(String user) throws IOException {
        APML apml = new APML("Taste for last.fm user " + user);
        apml.addProfile(getOverallProfileForUser(user));
        apml.addProfile(getWeeklyProfileForUser(user));
        return apml;
    }

    private Profile getOverallProfileForUser(String user) throws IOException {
        Item[] artists = getTopArtistsForUser(user);
        Concept[] implicit = getImplicitFromExplicit(artists);
        Concept[] explicit = APML.getExplicitConceptsFromItems(artists);
        return new Profile("overall-music", implicit, explicit);
    }

    private Profile getWeeklyProfileForUser(String user) throws IOException {
        Item[] artists = getWeeklyArtistsForUser(user);
        Concept[] implicit = getImplicitFromExplicit(artists);
        Concept[] explicit = APML.getExplicitConceptsFromItems(artists);
        return new Profile("weekly-music", implicit, explicit);
    }

    public Concept[] getImplicitFromExplicit(Item[] artists) throws IOException {
        Map<String, Float> conceptMap = new HashMap<String, Float>();
        Item mostFrequentArtist = APML.findMostFrequentItem(artists);

        for (Item item : artists) {
            Item[] tags = null;
            try {
                tags = getArtistTags(item.getName());
            } catch (IOException ioe) {
                continue;
            }

            Item mostFrequentTag = APML.findMostFrequentItem(tags);
            float artistWeight = item.getFreq() / (float) mostFrequentArtist.getFreq();
            for (Item tag : tags) {
                float tagWeight = tag.getFreq() / (float) mostFrequentTag.getFreq();
                accum(conceptMap, tag.getName(), tagWeight * artistWeight);
            }
        }
        List<Concept> conceptList = new ArrayList<Concept>();

        for (String key : conceptMap.keySet()) {
            float value = conceptMap.get(key);
            Concept concept = new Concept(key, value);
            conceptList.add(concept);
        }

        Concept[] concepts = APML.normalizeAndPrune(conceptList, MIN_CONCEPT_SCORE);
        return concepts;
    }

    private Item[] getArtistTags(String artistName) throws IOException {
        Artist artist = mdb.artistLookup(artistName);
        if (artist != null) {
            List<Scored<Tag>> scoredTags = artist.getMostFrequentTags(50);
            Item[] itags = new Item[scoredTags.size()];
            int index = 0;
            for (Scored<Tag> scoredTag : scoredTags) {
                itags[index++] = new Item(scoredTag.getItem().getName(), (int) scoredTag.getScore());
            }
            return itags;
        } else {
            return EMPTY_ITEM;
        }
    }

    private Item[] getTopArtistsForUser(String user) throws IOException {
        Item[] artists = userCache.get(user);
        if (artists == null) {
            try {
                artists = lastfm.getTopArtistsForUser(user);
            } catch (FileNotFoundException ex) {
                artists = EMPTY_ITEM;
            }

            userCache.put(user, artists);
        }

        return artists;
    }

    private Item[] getWeeklyArtistsForUser(String user) throws IOException {
        Item[] artists = null;
            try {
                artists = lastfm.getWeeklyArtistsForUser(user);
            } catch (FileNotFoundException ex) {
                artists = EMPTY_ITEM;
            }
        return artists;
    }


    private void accum(Map<String, Float> conceptMap, String key, float val) {
        Float v = conceptMap.get(key);
        if (v == null) {
            conceptMap.put(key, val);
        } else {
            conceptMap.put(key, v + val);
        }

    }



    public static void main(String[] args) {
        try {
            LastFMProfileRetriever lcr = new LastFMProfileRetriever();
            lcr.crawlUsers("rj", 1000, true);
        } catch (IOException ioe) {
            System.out.println("error " + ioe);
        }
    }
}
