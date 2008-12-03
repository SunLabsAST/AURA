/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.minion.CompositeResultsFilter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class RecommendationManager {

    private MusicDatabase mdb;
    private Map<String, RecommendationType> recTypeMap;
    private final static String DEFAULT_RECOMMENDER = "Quickomendation";

    RecommendationManager(MusicDatabase mdb) {
        this.mdb = mdb;
        recTypeMap = new HashMap<String, RecommendationType>();

        List<RecommendationType> rtypes = new ArrayList();
        rtypes.add(new QuickRecommendation());
        rtypes.add(new SimpleArtistRecommendationType());
        rtypes.add(new SimToRecentArtistRecommender());
        rtypes.add(new SimToRecentArtistRecommender2());
        rtypes.add(new SimToUserTagCloud());
        rtypes.add(new CollaborativeFilterer());

        recTypeMap = new HashMap();
        for (RecommendationType rtype : rtypes) {
            recTypeMap.put(rtype.getName(), rtype);
        }
    }

    /**
     * Gets the recommendation types for the system.
     * @return
     */
    public List<RecommendationType> getArtistRecommendationTypes() {
        return new ArrayList(recTypeMap.values());
    }

    /**
     * Gets a RecommendationType by name
     * @param recTypeName the name of the recommendation type
     * @return the recommendaton type
     */
    public RecommendationType getArtistRecommendationType(
            String recTypeName) {
        return recTypeMap.get(recTypeName);
    }

    public RecommendationType getDefaultArtistRecommendationType() {
        return getArtistRecommendationType(DEFAULT_RECOMMENDER);
    }

    /**
     * Determines if the recommendaion name is a valid name
     * @param name the name to test
     * @return true if the name is a valid name
     */
    public boolean isValidRecommendationType(String name) {
        for (String rname : recTypeMap.keySet()) {
            if (rname.equalsIgnoreCase(name)) {
                return true;
            }

        }
        return false;
    }

    private Set<String> getAttendedToArtists(String listenerID) throws AuraException, RemoteException {
        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(listenerID);

        Set<String> ids = new HashSet();
        DBIterator<Attention> attentionIterator = mdb.getDataStore().getAttentionIterator(ac);
        try {
            while (attentionIterator.hasNext()) {
                Attention attn = attentionIterator.next();
                if (mdb.isArtist(attn.getTargetKey())) {
                    ids.add(attn.getTargetKey());
                }

            }
        } finally {
            attentionIterator.close();
        }
        return ids;
    }

    private List<String> getKeyListFromScoredKeyList(List<Scored<String>> scoredIds) {
        List<String> results = new ArrayList<String>();
        for (Scored<String> ss : scoredIds) {
            results.add(ss.getItem());
        }

        return results;
    }

    private Artist getRandomGoodArtistFromListener(String listenerID) throws AuraException, RemoteException {
        Collection<Artist> artists = mdb.getFavoriteArtists(listenerID, 20);
        Artist artist = null;
        if (artists.size() > 0) {
            artist = mdb.selectRandom(artists);
        } else {
            artist = mdb.getMostPopularArtist();
        }

        return artist;
    }

    private List<Recommendation> getSimilarArtists(List<Scored<String>> seedArtists, Set<String> skipArtists, int count)
            throws AuraException {
        List<Recommendation> recommendations = new ArrayList();
        for (Scored<String> seedArtist : seedArtists) {
            List<Scored<Artist>> simArtists = mdb.artistFindSimilar(seedArtist.getItem(), count);
            for (Scored<Artist> sa : simArtists) {
                if (!skipArtists.contains(sa.getItem().getKey())) {
                    skipArtists.add(sa.getItem().getKey());
                    List<Scored<String>> reason = new ArrayList();
                    reason.add(new Scored<String>(seedArtist.getItem(), sa.getScore()));
                    recommendations.add(new Recommendation(sa.getItem().getKey(), sa.getScore(), reason));
                    if (recommendations.size() >= count) {
                        break;
                    }

                }
            }
        }
        return recommendations;
    }

    private class SimpleArtistRecommendationType implements RecommendationType {

        public String getName() {
            return "SimpleArtist";
        }

        public String getDescription() {
            return "a simple recommender that just returns artists that are similar to " +
                    " a single artist, selected at random, that the listener likes";
        }

        public RecommendationSummary getRecommendations(String listenerID, int count, RecommendationProfile rp)
                throws AuraException, RemoteException {
            Set<String> skipIDS = getAttendedToArtists(listenerID);
            Artist artist = getRandomGoodArtistFromListener(listenerID);
            List<Recommendation> results = new ArrayList();
            List<Scored<Artist>> simArtists = mdb.artistFindSimilar(artist.getKey(), count * 5);
            for (Scored<Artist> sartist : simArtists) {
                if (!skipIDS.contains(sartist.getItem().getKey())) {
                    skipIDS.add(sartist.getItem().getKey());
                    List<Scored<String>> reason = mdb.artistExplainSimilarity(artist.getKey(), sartist.getItem().getKey(), 20);
                    results.add(new Recommendation(sartist.getItem().getKey(),
                            sartist.getScore(), reason));
                    if (results.size() >= count) {
                        break;
                    }
                }
            }
            String reason = "Artists similarity to " + artist.getName();
            return new RecommendationSummary(reason, results);
        }

        public ItemType getType() {
            return ItemType.ARTIST;
        }
    }

    private class SimToRecentArtistRecommender implements RecommendationType {

        public String getName() {
            return "SimToRecent";
        }

        public String getDescription() {
            return "Finds artists that are similar to the recently played artists";
        }

        public RecommendationSummary getRecommendations(String listenerID, int count, RecommendationProfile rp)
                throws AuraException, RemoteException {
            ArtistScoreManager sm = new ArtistScoreManager(true);
            Set<String> keys = mdb.getFavoriteArtistKeys(listenerID, 20);
            Set<String> skipIDS = getAttendedToArtists(listenerID);
            StringBuilder sb = new StringBuilder();

            sb.append("Similar to recently played artists like ");

            for (Artist artist : mdb.artistLookup(keys)) {
                if (artist != null) {
                    sb.append(artist.getName());
                    sb.append(",");
                    sm.addSeedArtist(artist);
                    List<Scored<Artist>> simArtists = mdb.artistFindSimilar(artist.getKey(), count * 3);
                    for (Scored<Artist> simArtist : simArtists) {
                        if (!skipIDS.contains(simArtist.getItem().getKey())) {
                            // BUG: this should  include the score of the seed artist.
                            sm.accum(simArtist.getItem(), artist.getKey(), 1.0 * simArtist.getScore());
                        }
                    }
                }
            }

            List<Recommendation> results = new ArrayList();
            for (Scored<Artist> sartist : sm.getTop(count)) {
                List<Scored<String>> thisReason = sm.getReason(sartist.getItem());
                results.add(new Recommendation(sartist.getItem().getKey(), sartist.getScore(), thisReason));
            }
            return new RecommendationSummary(sb.toString(), results);
        }

        public ItemType getType() {
            return ItemType.ARTIST;
        }
    }

    private class SimToRecentArtistRecommender2 implements RecommendationType {

        public String getName() {
            return "SimToRecent(2)";
        }

        public String getDescription() {
            return "Finds artists that are similar to the recently played artists (version 2)";
        }

        public RecommendationSummary getRecommendations(String listenerID, int count, RecommendationProfile rp)
                throws AuraException, RemoteException {
            Set<String> keys = mdb.getFavoriteArtistKeys(listenerID, count / 2);
            Set<String> skipIDS = getAttendedToArtists(listenerID);
            List<String> keyList = new ArrayList<String>();
            keyList.addAll(keys);
            List<Scored<Artist>> simArtists = mdb.artistFindSimilar(keyList, count, skipIDS, Popularity.ALL);

            List<Recommendation> results = new ArrayList();
            List<Scored<String>> emptyReason = new ArrayList<Scored<String>>();
            for (Scored<Artist> sartist : simArtists) {
                results.add(new Recommendation(sartist.getItem().getKey(), sartist.getScore(), emptyReason));
            }
            return new RecommendationSummary("similarity to recent artists", results);
        }

        public ItemType getType() {
            return ItemType.ARTIST;
        }
    }

    private class SimToUserTagCloud implements RecommendationType {

        public String getName() {
            return "SimToUserTagCloud";
        }

        public String getDescription() {
            return "Finds artists that are similar to users tag cloud";
        }

        public RecommendationSummary getRecommendations(String listenerID, int count, RecommendationProfile rp)
                throws AuraException, RemoteException {
            Set<String> skipIDS = getAttendedToArtists(listenerID);
            List<Scored<Item>> items = mdb.getDataStore().findSimilar(listenerID,
                    mdb.getFindSimilarConfig(Listener.FIELD_SOCIAL_TAGS,
                    count, new CompositeResultsFilter(new TypeFilter(ItemType.ARTIST), new KeyResultsFilter(skipIDS))));
            List<Recommendation> results = new ArrayList();

            for (Scored<Item> item : items) {
                List<Scored<String>> reason = mdb.getDataStore().explainSimilarity(listenerID,
                        item.getItem().getKey(), mdb.getFindSimilarConfig(Listener.FIELD_SOCIAL_TAGS, count, null));
                results.add(new Recommendation(item.getItem().getKey(), item.getScore(), reason));
                if (results.size() >= count) {
                    break;
                }
            }
            return new RecommendationSummary("Similarity to your personal tag cloud", results);
        }

        public ItemType getType() {
            return ItemType.ARTIST;
        }
    }

    private class CollaborativeFilterer implements RecommendationType {

        private final static int MAX_LISTENERS = 10;

        public String getName() {
            return "CollaborativeFilterer";
        }

        public String getDescription() {
            return "Finds favorites artists from similar users";
        }

        public RecommendationSummary getRecommendations(String listenerID, int count, RecommendationProfile rp)
                throws AuraException, RemoteException {

            Set<String> skipIDs = getAttendedToArtists(listenerID);
            List<Scored<Listener>> simListeners = mdb.listenerFindSimilar(listenerID, MAX_LISTENERS);
            ArtistScoreManager sm = new ArtistScoreManager(true);
            for (Scored<Listener> sl : simListeners) {
                if (sl.getItem().getKey().equals(listenerID)) {
                    continue;
                }
                List<Scored<String>> scoredKeys = mdb.getAllArtistsAsIDs(listenerID, count);
                for (Scored<String> scoredKey : scoredKeys) {
                    if (!skipIDs.contains(scoredKey.getItem())) {
                        Artist artist = mdb.artistLookup(scoredKey.getItem());
                        if (artist != null) {
                            sm.accum(artist, listenerID, sl.getScore() * scoredKey.getScore());
                        }
                    }
                }
            }

            List<Recommendation> results = new ArrayList();
            for (Scored<Artist> sartist : sm.getTop(count)) {
                List<Scored<String>> thisReason = sm.getReason(sartist.getItem());
                results.add(new Recommendation(sartist.getItem().getKey(), sartist.getScore(), thisReason));
            }
            return new RecommendationSummary("Favorite Artists from similar listeners", results);
        }

        public ItemType getType() {
            return ItemType.ARTIST;
        }
    }

    private class QuickRecommendation implements RecommendationType {

        public String getName() {
            return "Quickomendation";
        }

        public String getDescription() {
            return "Find music similar to your favorites";
        }

        public RecommendationSummary getRecommendations(String listenerID, int count, RecommendationProfile rp)
                throws AuraException, RemoteException {
            Set<String> skipIDs = getAttendedToArtists(listenerID);
            String artistKey = mdb.selectRandom(mdb.getFavoriteArtistKeys(listenerID, count / 2));
            List<Scored<Artist>> simArtists = mdb.artistFindSimilar(artistKey, count, skipIDs, Popularity.ALL);
            List<Recommendation> results = new ArrayList();
            for (Scored<Artist> sartist : simArtists) {
                Recommendation recommendation = new Recommendation(sartist.getItem().getKey(), sartist.getScore());
                recommendation.addReason(artistKey, sartist.getScore());
                results.add(recommendation);
            }
            return new RecommendationSummary("Favorite Artists from similar listeners", results);
        }

        public ItemType getType() {
            return ItemType.ARTIST;
        }
    }
}
