/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
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
import java.util.Set;

/**
 *
 * @author plamere
 */
public class TestManager {

    private final static TestStatus UNKNOWN_TEST = new TestStatus(false, 0L, "unknown test");
    private Map<String, Test> tests = new LinkedHashMap<String, Test>();
    private List<String> shortTestNames = new ArrayList<String>();

    public TestManager() {
        addTest(true, new PingTest());
        addTest(true, new NormalArtistPopularityTest());
        addTest(true, new NormalTagPopularityTest());
        addTest(true, new ArtistScoreTest(100));
        addTest(true, new ArtistSearchTest(100));
        addTest(true, new DistinctiveTagsTest());
        addTest(true, new FindSimilarExecTime());
        addTest(true, new FindSimilarQuickCheck());
        addTest(true, new FindSimilarSymmetric());
        addTest(true, new FreshArtists());
        addTest(true, new FreshListeners());
        addTest(true, new FreshArtistTags());
        addTest(true, new ArtistAddedSince());
        addTest(true, new ArtistSelfSimilarity());
        addTest(true, new ArtistSimilarityScore());

        addTest(false, new ArtistScoreTest(1000));
        addTest(false, new ArtistSearchTest(1000));
        addTest(false, new FreshArtists(1000));
        addTest(false, new FreshListeners(1000));
        addTest(false, new FreshArtistTags(1000));
        addTest(false, new ArtistAddedSince(1000));
        addTest(false, new ArtistSelfSimilarity(1000));
        addTest(false, new ArtistSimilarityScore(1000));
        addTest(false, new ItemCountConsistencyTest(ItemType.ARTIST));
        addTest(false, new ItemCountConsistencyTest(ItemType.ARTIST_TAG));
        addTest(false, new ItemCountConsistencyTest(ItemType.EVENT));
        addTest(false, new ItemCountConsistencyTest(ItemType.TAG_CLOUD));
        addTest(false, new ItemCountConsistencyTest(ItemType.TRACK));
        addTest(false, new ItemCountConsistencyTest(ItemType.USER));
        addTest(false, new ItemCountConsistencyTest(ItemType.VENUE));

        if (false) { // these take too long
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
        List<String> keys = mdb.getAllItemKeys(type);
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
            List<Scored<Artist>> results = mdb.artistSearch(queryArtist.getName(), 10);
            if (results.size() < 1) {
                ts.fail("No search results for query " + queryArtist.getName());
                return;
            }
            double score = results.get(0).getScore();
            double delta = Math.abs(1.0 - results.get(0).getScore());
            if (delta > .01) {
                ts.fail(String.format("Exact match score is %.3f", score));
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
            if (results.size() < 1) {
                ts.fail("No search results for query " + queryArtist.getName());
                return;
            }

            if (!results.get(0).getItem().getKey().equals(queryArtist.getKey())) {
                ts.fail("Search Fail for " + queryArtist.getName());
                return;
            }
        }
    }
}

class DistinctiveTagsTest extends Test {

    DistinctiveTagsTest() {
        super("Distinctive tags");
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        Artist weezer = lookupByName(mdb, "Weezer");
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
        Artist beatles = lookupByName(mdb, "The Beatles");
        if (beatles != null) {
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
    private double MAX_ERROR = 0.01;

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
            List<Scored<Artist>> artists = mdb.artistFindSimilar(artist.getKey(), 5);

            if (artists.size() < 1) {
                ts.fail("no similarity results for " + artist.getName());
                return;
            }

            for (Scored<Artist> simArtist : artists) {
                double seedScore = simArtist.getScore();
                List<Scored<Artist>> simArtists = mdb.artistFindSimilar(simArtist.getItem().getKey(), 100);
                boolean foundMatch = false;
                for (Scored<Artist> match : simArtists) {
                    if (match.getItem().getKey().equals(artist.getKey())) {
                        if (Math.abs(seedScore - match.getScore()) > MAX_ERROR) {
                            String s = String.format("asymmetric similarity %.2f <> %.2f %s/%s",
                                    seedScore, match.getScore(), artist.getName(), simArtist.getItem().getName());
                            ts.fail(s);
                            return;
                        } else {
                            foundMatch = true;
                            break;
                        }
                    }
                }
                if (!foundMatch) {
                    ts.fail("no symmetric match found for " + artist.getName());
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
            if (timeAdded <  1000000L) {
                ts.fail("bad time added");
            } else {
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
            Artist artist = selectRandomArtist(mdb);
            List<Scored<Artist>> artists = mdb.artistFindSimilar(artist.getKey(), 20);

            if (artists.size() == 0) {
                ts.fail("Can't get similar artists for " + artist.getName());
                return;
            } else {
                Artist sartist = artists.get(0).getItem();
                if (!artist.getKey().equals(sartist.getKey())) {
                    ts.fail("No self similarity " +  artist.getName() + " <> " + sartist.getName());
                    return;
                }
            }
        }
    }
}

class ArtistSimilarityScore extends Test {

    private int maxTries = 100;

    ArtistSimilarityScore() {
        super("Artist Similarity Score");
    }

    ArtistSimilarityScore(int tries) {
        super("Artist Similarity Score" + tries);
        maxTries = tries;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {
        for (int i = 0; i < maxTries; i++) {
            Artist artist = selectRandomArtist(mdb);
            List<Scored<Artist>> artists = mdb.artistFindSimilar(artist.getKey(), 20);

            if (artists.size() == 0) {
                ts.fail("Can't get similar artists for " + artist.getName());
                return;
            } else {
                double score = artists.get(0).getScore();
                double delta = Math.abs(1.0 - score);
                if (delta > .01) {
                    ts.fail(String.format("Find similarity score is %.3f, should be 1.0", score));
                    return;
                }
            }
        }
    }
}