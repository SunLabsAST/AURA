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

package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.TestStatus;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class TestManager {

    final static float MAX_SIMILARITY_ERROR = .1f;
    final static float MAX_SEARCH_ERROR = .1f;
    private final static TestStatus UNKNOWN_TEST = new TestStatus(false, 0L, "unknown test");
    private Map<String, Test> tests = new LinkedHashMap<String, Test>();
    private List<String> shortTestNames = new ArrayList<String>();

    public TestManager() {
        addTest(true, new PingTest());
        addTest(true, new NormalArtistPopularityTest());
        addTest(true, new NormalTagPopularityTest());
        addTest(true, new ArtistScoreTest(100));
        addTest(true, new ArtistSearchTest(100));
        addTest(true, new ArtistTagSearch(100));
        addTest(true, new DistinctiveTagsTest());
        addTest(true, new FindSimilarExecTime());
        addTest(true, new FindSimilarQuickCheck());
        addTest(true, new FindSimilarSymmetric());
        addTest(true, new FreshArtists());
        addTest(true, new FreshListeners());
        addTest(true, new FreshArtistTags());
        addTest(true, new ArtistAddedSince());
        addTest(true, new ArtistSelfSimilarity());
        addTest(true, new ArtistTagSelfSimilarity());
        addTest(true, new ArtistTagSimilarityScore());
        addTest(true, new AttentionCount(Type.RATING));
        addTest(true, new AttentionCount(Type.VIEWED));
        addTest(true, new AttentionCount(Type.LOVED));
        addTest(true, new AttentionCount(Type.DISLIKED));
        addTest(true, new RandomAttention(100));
        addTest(true, new AttentionResolved(100));
        addTest(true, new AttentionFullTest(100));

        addTest(false, new ArtistScoreTest(1000));
        addTest(false, new ArtistSearchTest(1000));
        addTest(false, new ArtistTagSearch(1000));
        addTest(false, new FreshArtists(1000));
        addTest(false, new FreshListeners(1000));
        addTest(false, new FreshArtistTags(1000));
        addTest(false, new ArtistAddedSince(1000));
        addTest(false, new ArtistSelfSimilarity(1000));
        addTest(false, new ArtistTagSelfSimilarity(1000));
        addTest(false, new ArtistTagSimilarityScore(1000));
        addTest(false, new ItemCountConsistencyTest(ItemType.ARTIST));
        addTest(false, new ItemCountConsistencyTest(ItemType.ARTIST_TAG));
        addTest(false, new ItemCountConsistencyTest(ItemType.EVENT));
        addTest(false, new ItemCountConsistencyTest(ItemType.TAG_CLOUD));
        addTest(false, new ItemCountConsistencyTest(ItemType.TRACK));
        addTest(false, new ItemCountConsistencyTest(ItemType.USER));
        addTest(false, new ItemCountConsistencyTest(ItemType.VENUE));
        addTest(false, new RandomAttention(1000));
        addTest(false, new AttentionResolved(1000));
        addTest(false, new AttentionFullTest(1000));


        if (false) { // these take too long
            addTest(false, new AttentionCount(Type.PLAYED));
            addTest(false, new ItemCountConsistencyTest(ItemType.ALBUM));
            addTest(false, new ItemCountConsistencyTest(ItemType.PHOTO));
            addTest(false, new ItemCountConsistencyTest(ItemType.VIDEO));
        }
    }

    public void addTest(boolean shortTest, Test test) {
        if (shortTest) {
            shortTestNames.add(test.getName());
        }
        tests.put(test.getName(), test);
    }

    public List<String> getTestNames(boolean shortTests) {
        if (shortTests) {
            return shortTestNames;
        } else {
            return new ArrayList<String>(tests.keySet());
        }
    }

    public TestStatus runTest(String name, MusicDatabase mdb) {
        Test test = tests.get(name);
        if (test == null) {
            return UNKNOWN_TEST;
        } else {
            return test.runTest(mdb);
        }
    }
}

class PingTest extends Test {

    PingTest() {
        super("Ping Test");
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        ts.setMostRecentQuery("artistGetMostPopular");
        if (mdb.artistGetMostPopularNames(5).size() != 5) {
            ts.fail("Can't find 5 popular artists");
        }
    }
}

class NormalArtistPopularityTest extends Test {

    NormalArtistPopularityTest() {
        super("Artist Popularity");
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        List<String> names = mdb.artistGetMostPopularNames(20);
        Set<String> nameSet = new HashSet<String>(names);

        check(ts, nameSet, "The Beatles");
        check(ts, nameSet, "Radiohead");
        check(ts, nameSet, "Coldplay");
        check(ts, nameSet, "Pink Floyd");
        check(ts, nameSet, "Metallica");
        check(ts, nameSet, "Muse");
    }

    private void check(TestStatus ts, Set<String> nameSet, String name) {
        if (!nameSet.contains(name)) {
            ts.fail("Can't find '" + name + "' in the top 20 artists");
        }
    }
}

class NormalTagPopularityTest extends Test {

    NormalTagPopularityTest() {
        super("Tag Popularity");
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        List<String> names = mdb.artistTagGetMostPopularNames(20);
        Set<String> nameSet = new HashSet<String>(names);

        check(ts, nameSet, "rock");
        check(ts, nameSet, "alternative");
        check(ts, nameSet, "indie");
        check(ts, nameSet, "classic rock");
        check(ts, nameSet, "metal");
        check(ts, nameSet, "punk");
        check(ts, nameSet, "electronic");
    }

    private void check(TestStatus ts, Set<String> nameSet, String name) {
        if (!nameSet.contains(name)) {
            ts.fail("Can't find '" + name + "' in the top 20 tags");
        }
    }
}

class ItemCountConsistencyTest extends Test {

    private ItemType type;

    ItemCountConsistencyTest(ItemType type) {
        super(type.name() + " count check");
        this.type = type;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        ts.setMostRecentQuery("getAllItemKeys " + type.name());
        List<String> keys = mdb.getAllItemKeys(type);
        ts.setMostRecentQuery("getItemCount " + type.name());
        long count = mdb.getDataStore().getItemCount(type);
        if (keys.size() != count) {
            ts.fail("Found " + keys.size() + " expected " + count);
        }
    }
}

class ArtistScoreTest extends Test {

    private int numTests;

    ArtistScoreTest(int numTests) {
        super("Artist Score-" + numTests);
        this.numTests = numTests;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {

        for (int i = 0; i < numTests; i++) {
            Artist queryArtist = selectRandomArtist(mdb);
            ts.setMostRecentQuery(queryArtist.getName());
            List<Scored<Artist>> results = mdb.artistSearch(queryArtist.getName(), 10);
            if (results.size() < 1) {
                ts.fail("No search results for query " + queryArtist.getName());
                return;
            }
            double score = results.get(0).getScore();
            double delta = Math.abs(1.0 - results.get(0).getScore());
            if (delta > TestManager.MAX_SEARCH_ERROR) {
                ts.fail(String.format("Exact match score is %.3f for query %s", score, queryArtist.getName()));
                return;
            }
        }
    }
}

class ArtistSearchTest extends Test {

    private int numTests;

    ArtistSearchTest(int numTests) {
        super("Artist Search-" + numTests);
        this.numTests = numTests;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {

        for (int i = 0; i < numTests; i++) {
            Artist queryArtist = selectRandomArtist(mdb);
            System.out.println("query " + queryArtist.getName() + " " + queryArtist.getKey());
            List<Scored<Artist>> results = mdb.artistSearch(queryArtist.getName(), 10);
            for (int j = 0; j < results.size(); j++) {
                double delta = Math.abs(1.0 - results.get(j).getScore());
                if (delta < TestManager.MAX_SEARCH_ERROR && results.get(j).getItem().getKey().equals(queryArtist.getKey())) {
                    return;
                }
            }
            ts.fail("Search Fail for " + queryArtist.getName());
        }
    }
}

class DistinctiveTagsTest extends Test {

    DistinctiveTagsTest() {
        super("Distinctive tags");
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        Artist weezer = lookupByNameOrKey(mdb, "Weezer");
        if (weezer != null) {
            List<Scored<ArtistTag>> tags = mdb.artistGetDistinctiveTags(weezer.getKey(), 10);
            if (!hasTag(tags, "geek rock")) {
                ts.fail("weezer missing geek rock");
                return;
            }
            if (!hasTag(tags, "nerd rock")) {
                ts.fail("weezer missing nerd rock");
                return;
            }
            if (!hasTag(tags, "emo")) {
                ts.fail("weezer missing emo");
                return;
            }
        } else {
            ts.fail("Can't find weezer");
        }
    }
}

class FindSimilarQuickCheck extends Test {

    FindSimilarQuickCheck() {
        super("Find Similar Quick");
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        Artist beatles = lookupByNameOrKey(mdb, "The Beatles");
        if (beatles != null) {
            ts.setMostRecentQuery("artistFindSimilar " + beatles.getKey());
            List<Scored<Artist>> artists = mdb.artistFindSimilar(beatles.getKey(), 100, MusicDatabase.Popularity.HEAD);
            if (!hasArtist(artists, "The Rolling Stones")) {
                ts.fail("beatles sim missing The Rolling Stones");
                return;
            }
            if (!hasArtist(artists, "The Doors")) {
                ts.fail("beatles sim missing The Doors");
                return;
            }
            if (!hasArtist(artists, "Bob Dylan")) {
                ts.fail("beatles sim missing Bob Dylan");
                return;
            }
            if (!hasArtist(artists, "John Lennon")) {
                ts.fail("beatles sim missing John Lennon ");
                return;
            }
        } else {
            ts.fail("Can't find the beatles");
        }
    }
}

class FindSimilarExecTime extends Test {

    private int maxTries = 100;

    FindSimilarExecTime() {
        super("Find Similar Exec Time");
    }

    FindSimilarExecTime(int tries) {
        super("Find Similar Exec Time-" + tries);
        maxTries = tries;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < maxTries; i++) {
            Artist artist = selectRandomArtist(mdb);
            ts.setMostRecentQuery("artistFindSimilar " + artist.getKey());
            List<Scored<Artist>> artists = mdb.artistFindSimilar(artist.getKey(), 20);
        }
        long delta = System.currentTimeMillis() - start;
        long average = delta / maxTries;
        if (average > 500) {
            ts.fail("Avg. FindSimilar time is too big. (" + average + " ms)");
        }
    }
}

class FindSimilarSymmetric extends Test {

    private int maxTries = 10;

    FindSimilarSymmetric() {
        super("Find Similar Symmetry");
    }

    FindSimilarSymmetric(int tries) {
        super("Find Similar Symmetry-" + tries);
        maxTries = tries;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        for (int i = 0; i < maxTries; i++) {
            Artist artist = selectRandomArtist(mdb);
            ts.setMostRecentQuery("artistFindSimilar " + artist.getKey());
            List<Scored<Artist>> artists = mdb.artistFindSimilar(artist.getKey(), 5);

            if (artists.size() < 1) {
                ts.fail("no similarity results for " + artist.getName());
                return;
            }

            for (Scored<Artist> simArtist : artists) {
                double seedScore = simArtist.getScore();
                ts.setMostRecentQuery("artistFindSimilar " + simArtist.getItem().getKey());
                List<Scored<Artist>> simArtists = mdb.artistFindSimilar(simArtist.getItem().getKey(), 100);
                boolean foundMatch = false;
                double lastScore = 0;
                for (Scored<Artist> match : simArtists) {
                    if (match.getItem().getKey().equals(artist.getKey())) {
                        if (Math.abs(seedScore - match.getScore()) > TestManager.MAX_SIMILARITY_ERROR) {
                            String s = String.format("asymmetric similarity %.2f <> %.2f %s/%s",
                                    seedScore, match.getScore(), artist.getName(), simArtist.getItem().getName());
                            ts.fail(s);
                            return;
                        } else {
                            foundMatch = true;
                            break;
                        }
                    }
                    lastScore = match.getScore();
                }
                // it is possible for the good match to be beyond item 100. We fail if our last matching score is
                // less than the seedScore
                if (!foundMatch && seedScore > lastScore) {
                    ts.fail("no symmetric match found for " + artist.getName() + " and " + simArtist.getItem().getName());
                }
            }
        }
    }
}

class FreshArtists extends Test {

    private int maxTries = 100;

    FreshArtists() {
        super("Fresh Artists");
    }

    FreshArtists(int tries) {
        super("Fresh Artists -" + tries);
        maxTries = tries;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        long staleTime = System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L; // 10 days is too old
        int staleCount = 0;
        for (int i = 0; i < maxTries; i++) {
            Artist artist = selectRandomArtist(mdb);
            if (artist.getUpdateCount() == 0 || artist.getLastCrawl() < staleTime) {
                staleCount++;
            }
        }
        if (staleCount > 0) {
            ts.fail(staleCount + " artists are stale");
        }
    }
}

class FreshListeners extends Test {

    private int maxTries = 100;

    FreshListeners() {
        super("Fresh Listeners");
    }

    FreshListeners(int tries) {
        super("Fresh Listeners -" + tries);
        maxTries = tries;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        long staleTime = System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L; // 10 days is too old
        int staleCount = 0;
        for (int i = 0; i < maxTries; i++) {
            Listener listener = selectRandomListener(mdb);
            if (listener.getUpdateCount() == 0 || listener.getLastCrawl() < staleTime) {
                staleCount++;
            }
        }
        if (staleCount > 0) {
            ts.fail(staleCount + " listeners are stale");
        }
    }
}

class FreshArtistTags extends Test {

    private int maxTries = 100;

    FreshArtistTags() {
        super("Fresh ArtistTags");
    }

    FreshArtistTags(int tries) {
        super("Fresh ArtistTags -" + tries);
        maxTries = tries;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        long staleTime = System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L; // 10 days is too old
        int staleCount = 0;
        for (int i = 0; i < maxTries; i++) {
            ArtistTag artistTag = selectRandomArtistTag(mdb);
            if (artistTag.getLastCrawl() < staleTime) {
                staleCount++;
            }
        }
        if (staleCount > 0) {
            ts.fail(staleCount + " artistTags are stale");
        }
    }
}

class ArtistAddedSince extends Test {

    private int maxTries = 100;

    ArtistAddedSince() {
        super("Artist Added Since");
    }

    ArtistAddedSince(int tries) {
        super("Artist Added Since " + tries);
        maxTries = tries;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        for (int i = 0; i < maxTries; i++) {
            Artist artist = selectRandomArtist(mdb);
            long timeAdded = artist.getTimeAdded();
            if (timeAdded < 1000000L) {
                ts.fail("bad time added for " + artist.getName());
            } else {
                ts.setMostRecentQuery("getItemsAddedSince (artist)" + new Date(timeAdded - 1000000L));
                DBIterator<Item> iter = mdb.getDataStore().getItemsAddedSince(ItemType.ARTIST, new Date(timeAdded - 1000000L));

                try {
                    while (iter.hasNext()) {
                        Item item = iter.next();
                        Artist lartist = new Artist(item);
                        if (lartist.getKey().equals(artist.getKey())) {
                            return;
                        }
                    }

                } finally {
                    iter.close();
                }
                ts.fail("Failed to find artist " + artist.getName());
            }
        }
    }
}

class ArtistSelfSimilarity extends Test {

    private int maxTries = 100;

    ArtistSelfSimilarity() {
        super("Artist Self Similarity");
    }

    ArtistSelfSimilarity(int tries) {
        super("Artist Self Similarity " + tries);
        maxTries = tries;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        for (int i = 0; i < maxTries; i++) {
            boolean foundMatch = false;
            Artist artist = selectRandomArtist(mdb);
            ts.setMostRecentQuery("artistFindSimilar " + artist.getKey());
            List<Scored<Artist>> simArtists = mdb.artistFindSimilar(artist.getKey(), 20);

            if (simArtists.size() == 0) {
                if (artist.getSocialTags().size() > 0) {
                    ts.fail("Can't get similar artists for " + artist.getName());
                    return;
                }
            } else {
                for (Scored<Artist> match : simArtists) {
                    if (match.getItem().getKey().equals(artist.getKey())) {
                        if (Math.abs(1.0 - match.getScore()) > TestManager.MAX_SIMILARITY_ERROR) {
                            String s = String.format("self similarity score for %s is %.2f",
                                    artist.getName(), match.getScore());
                            ts.fail(s);
                            return;
                        } else {
                            foundMatch = true;
                            break;
                        }
                    }
                }
                if (!foundMatch) {
                    ts.fail("no self similarity match found for " + artist.getName());
                }
            }
        }
    }
}

class ArtistTagSelfSimilarity extends Test {

    private int maxTries = 100;

    ArtistTagSelfSimilarity() {
        super("Artist Tag Self Similarity");
    }

    ArtistTagSelfSimilarity(int tries) {
        super("Artist Tag Self Similarity " + tries);
        maxTries = tries;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        for (int i = 0; i < maxTries; i++) {
            boolean foundMatch = false;
            ArtistTag artistTag = selectRandomArtistTag(mdb);
            ts.setMostRecentQuery("artistTagFindSimilar " + artistTag.getKey());
            List<Scored<ArtistTag>> artistTags = mdb.artistTagFindSimilar(artistTag.getKey(), 20);

            if (artistTags.size() == 0) {
                ts.fail("Can't get similar artist tags for " + artistTag.getName());
                return;
            } else {
                for (Scored<ArtistTag> match : artistTags) {
                    if (match.getItem().getKey().equals(artistTag.getKey())) {
                        if (Math.abs(1.0 - match.getScore()) > TestManager.MAX_SIMILARITY_ERROR) {
                            String s = String.format("self similarity score for %s is %.2f",
                                    artistTag.getName(), match.getScore());
                            ts.fail(s);
                            return;
                        } else {
                            foundMatch = true;
                            break;
                        }
                    }
                }
                if (!foundMatch && artistTag.getTaggedArtist().size() > 0) {
                    ts.fail("no self similarity match found for " + artistTag.getName());
                }
            }
        }
    }
}

class ArtistTagSimilarityScore extends Test {

    private int maxTries = 100;

    ArtistTagSimilarityScore() {
        super("Artist Tag Similarity Score");
    }

    ArtistTagSimilarityScore(int tries) {
        super("Artist Tag Similarity Score " + tries);
        maxTries = tries;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        for (int i = 0; i < maxTries; i++) {
            ArtistTag artistTag = selectRandomArtistTag(mdb);
            ts.setMostRecentQuery("artistTagFindSimilar " + artistTag.getKey());
            List<Scored<ArtistTag>> artistTags = mdb.artistTagFindSimilar(artistTag.getKey(), 20);

            if (artistTags.size() == 0 && artistTag.getTaggedArtist().size() > 0) {
                ts.fail("Can't get similar artistTags for " + artistTag.getName());
                return;
            } else {
                double score = artistTags.get(0).getScore();
                double delta = Math.abs(1.0 - score);
                if (delta > TestManager.MAX_SIMILARITY_ERROR) {
                    ts.fail(String.format("Find similarity score is %.3f, should be 1.0 for %s", score, artistTag.getName()));
                    return;
                }
            }
        }
    }
}

class AttentionCount extends Test {

    private Type type;

    AttentionCount(Type type) {
        super("Attention Count " + type.name());
        this.type = type;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        AttentionConfig ac = new AttentionConfig();
        ac.setType(type);
        long count = mdb.getDataStore().getAttentionCount(ac);
        long actualCount = 0;

        DBIterator<Attention> iter = mdb.getDataStore().getAttentionIterator(ac);

        try {
            while (iter.hasNext()) {
                Attention attn = iter.next();
                if (attn.getType() != type) {
                    ts.fail("Unexpected type retrieved found: " + attn.getType() + " expected " + type);
                    return;
                }
                actualCount++;
            }
        } finally {
            iter.close();
        }
        if (actualCount != count) {
            ts.fail("mismatch in attention, found " + actualCount + " expected " + count);
        }
    }
}

class RandomAttention extends Test {

    private int tries;

    RandomAttention(int count) {
        super("Random Attention " + count);
        this.tries = count;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        int found = 0;

        for (int i = 0; i < tries; i++) {
            Listener src = selectRandomListener(mdb);
            Artist tgt = selectRandomArtist(mdb);
            AttentionConfig ac = new AttentionConfig();
            ac.setSourceKey(src.getKey());
            ac.setTargetKey(tgt.getKey());
            List<Attention> attns = mdb.getDataStore().getAttention(ac);
            for (Attention attn : attns) {
                found++;
                if (!attn.getSourceKey().equals(src.getKey())) {
                    ts.fail("source mismatch expected " + src.getKey() + " found " + attn.getSourceKey());
                    return;
                }
                if (!attn.getTargetKey().equals(tgt.getKey())) {
                    ts.fail("target mismatch expected " + tgt.getKey() + " found " + attn.getTargetKey());
                    return;
                }
                found++;
            }
        }
    }
}

class AttentionResolved extends Test {

    private int tries;
    private int depth = 30;

    AttentionResolved(int count) {
        super("Attention Resolved " + count);
        this.tries = count;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        int found = 0;

        for (int i = 0; i < tries; i++) {
            Listener src = selectRandomListener(mdb);
            AttentionConfig ac = new AttentionConfig();
            ac.setSourceKey(src.getKey());
            List<Attention> attns = mdb.getDataStore().getLastAttention(ac, depth);
            for (Attention attn : attns) {
                found++;
                if (!attn.getSourceKey().equals(src.getKey())) {
                    ts.fail("source mismatch expected " + src.getKey() + " found " + attn.getSourceKey());
                    return;
                }
                // see if the target item is really an item
                Item item = mdb.getDataStore().getItem(attn.getTargetKey());
                if (item == null) {
                    ts.fail("Can't find target " + attn.getTargetKey() + " for attn " + attn);
                    return;
                }
            }
        }
    }
}

