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

package com.sun.labs.aura.music.web.apml;

import com.sun.labs.aura.music.web.Cache;
import com.sun.labs.aura.music.web.lastfm.LastItem;
import com.sun.labs.aura.music.web.lastfm.LastFM;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class LastFMConceptRetriever {

    private static final float MIN_CONCEPT_SCORE = .03f;
    private LastFM lastfm;
    private Cache<LastItem[]> itemCache;
    private Cache<LastItem[]> userCache;
    private final static LastItem[] EMPTY_ITEM = new LastItem[0];
    private Thread crawler = null;

    public LastFMConceptRetriever() throws IOException {
        this(10000, 30, new File("./cache"), new File("./users"));
    }

    public LastFMConceptRetriever(int maxItemsInCache, int maxDaysInCache, File itemDir, File userDir) throws IOException {
        itemCache = new Cache<LastItem[]>(maxItemsInCache, maxDaysInCache, itemDir);
        userCache = new Cache<LastItem[]>(maxItemsInCache, maxDaysInCache, userDir);
        lastfm = new LastFM();
    }

    public void startCrawler() {
        if (crawler == null) {
            crawler = new Thread() {

                @Override
                public void run() {
                    crawlUsers("rj", 3000L, false);
                }
            };
            crawler.setDaemon(true);
            crawler.setName("last.fm-concept-crawler");
            crawler.start();
        }

    }

    public void stopCrawler() {
        if (crawler != null) {
            crawler.interrupt();
            crawler = null;
        }
    }

    public APML getAPMLForUser(String user) throws IOException {
        LastItem[] artists = getTopArtistsForUser(user);
        Concept[] implicit = getImplicitConceptsFromArtists(artists);
        Concept[] explicit = getExplicitConceptsForUser(artists);
        APML apml = new APML("music taste for " + user);
        
        apml.setDefaultProfile("music");
        Profile music = new Profile("music", implicit, explicit);
        apml.addProfile(music);

        return apml;
    }

    public Concept[] getImplicitConceptsForUser(String user) throws IOException {
        LastItem[] artists = getTopArtistsForUser(user);
        return getImplicitConceptsFromArtists(artists);
    }

    private Concept[] getExplicitConceptsForUser(LastItem[] artists) {
        List<Concept> conceptList = new ArrayList<Concept>();
        LastItem mostFrequentArtist = findMostFrequentItem(artists);
        for (LastItem artist : artists) {
            Concept concept = new Concept(artist.getName(), artist.getFreq() / (float) mostFrequentArtist.getFreq());
            conceptList.add(concept);
        }
        return normalizeAndPrune(conceptList, 0);
    }

    private Concept[] getImplicitConceptsFromArtists(LastItem[] artists) throws IOException {
        Map<String, Float> conceptMap = new HashMap<String, Float>();
        LastItem mostFrequentArtist = findMostFrequentItem(artists);

        for (LastItem item : artists) {
            LastItem[] tags = null;
            try {
                tags = getArtistTags(item.getName());
            } catch (IOException ioe) {
                continue;
            }

            LastItem mostFrequentTag = findMostFrequentItem(tags);
            float artistWeight = item.getFreq() / (float) mostFrequentArtist.getFreq();
            for (LastItem tag : tags) {
                float tagWeight = tag.getFreq() / (float) mostFrequentTag.getFreq();
                accum(conceptMap, tag.getName(), tagWeight * artistWeight);
            }

        }
        List<Concept> conceptList = new ArrayList<Concept>();

        for (Entry<String, Float> entry : conceptMap.entrySet()) {
            Concept concept = new Concept(entry.getKey(), entry.getValue());
            conceptList.add(concept);
        }

        Concept[] concepts = normalizeAndPrune(conceptList, MIN_CONCEPT_SCORE);
        return concepts;
    }

    private LastItem[] getArtistTags(String artist) throws IOException {
        LastItem[] tags = itemCache.get(artist);
        if (tags == null) {
            try {
                tags = lastfm.getArtistTags(artist, false);
            } catch (FileNotFoundException ex) {
                tags = EMPTY_ITEM;
            }

            itemCache.put(artist, tags);
        }

        return tags;
    }

    private LastItem[] getTopArtistsForUser(String user) throws IOException {
        LastItem[] artists = userCache.get(user);
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

    private Concept[] normalizeAndPrune(List<Concept> conceptList, float minValue) {
        List<Concept> returnList = new ArrayList();
        Collections.sort(conceptList, Concept.VAL_ORDER);
        Collections.reverse(conceptList);

        float maxValue = 1.0f;
        if (conceptList.size() > 0) {
            maxValue = conceptList.get(0).getValue();
        }

        int lastIndex = 0;
        for (Concept c : conceptList) {
            Concept normConcept = new Concept(c.getKey(), c.getValue() / maxValue, c.getFrom(), c.getUpdate());
            if (normConcept.getValue() < minValue) {
                break;
            }
            returnList.add(normConcept);
            lastIndex++;
        }

        returnList = returnList.subList(0, lastIndex);
        return returnList.toArray(new Concept[0]);
    }

    private void accum(Map<String, Float> conceptMap, String key, float val) {
        Float v = conceptMap.get(key);
        if (v == null) {
            conceptMap.put(key, val);
        } else {
            conceptMap.put(key, v + val);
        }

    }

    private LastItem findMostFrequentItem(LastItem[] items) {
        LastItem maxItem = null;

        for (LastItem item : items) {
            if (maxItem == null || item.getFreq() > maxItem.getFreq()) {
                maxItem = item;
            }

        }
        return maxItem;
    }

    void dumpUserConcepts(String user) throws IOException {
        System.out.printf("Concepts for %s\n", user);
        Concept[] concepts = getImplicitConceptsForUser(user);
        for (Concept concept : concepts) {
            System.out.printf("  %s\n", concept.toXML(true));
        }

    }

    void crawlUsers(String initialUser, long delay, boolean trace) {
        Queue<String> outstandingUsers = new LinkedList<String>();
        Set<String> completedUsers = new HashSet<String>();
        outstandingUsers.offer(initialUser);
        long sumTime = 0L;

        while (outstandingUsers.size() > 0) {
            String user = outstandingUsers.remove();
            if (!completedUsers.contains(user)) {
                completedUsers.add(user);

                try {
                    long start = System.currentTimeMillis();
                    Concept[] concepts = getImplicitConceptsForUser(user);
                    long delta = System.currentTimeMillis() - start;
                    sumTime +=
                            delta;

                    float time = delta / 1000.f;
                    float avg = sumTime / (1000.f * completedUsers.size());


                    if (trace) {
                        System.out.printf("%d %d %d %.3f %.3f %s\n", completedUsers.size(), outstandingUsers.size(),
                                concepts.length, time, avg, user);
                    }

                    String[] neighbors = lastfm.getSimilarUsers(user);
                    for (String neighbor : neighbors) {
                        if (!completedUsers.contains(neighbor)) {
                            outstandingUsers.offer(neighbor);
                        }

                    }
                    Thread.sleep(delay);
                } catch (IOException ex) {
                    System.out.println("Trouble getting info for user " + user);

                } catch (InterruptedException ex) {
                    System.out.println("Interrupted!");
                    break;
                } catch (Throwable t) {
                    System.out.println("Some awful thing happened " + t);
                    t.printStackTrace();
                }

            }
        }

    }

    public static void main(String[] args) {
        try {
            LastFMConceptRetriever lcr = new LastFMConceptRetriever();
            lcr.crawlUsers("rj", 1000, false);
        } catch (IOException ioe) {
            System.out.println("error " + ioe);
        }
    }
}
