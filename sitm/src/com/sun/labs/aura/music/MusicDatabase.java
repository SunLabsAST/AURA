/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class MusicDatabase {

    private DataStore dataStore;
    private List<SimType> simTypes;
    private Map<String, RecommendationType> recTypeMap;
    private Random rng = new Random();
    private ArtistTag rockTag = null;
    private final String BEATLES_ID = "b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d";
    public final static String DEFAULT_RECOMMENDER = "SimToRecent(2)";

    public MusicDatabase(DataStore dataStore) throws AuraException {
        this.dataStore = dataStore;
        new Album().defineFields(dataStore);
        new Artist().defineFields(dataStore);
        new ArtistTag().defineFields(dataStore);
        new Event().defineFields(dataStore);
        new Photo().defineFields(dataStore);
        new Track().defineFields(dataStore);
        new Venue().defineFields(dataStore);
        new Video().defineFields(dataStore);
        new Listener().defineFields(dataStore);

        initSimTypes();
        initArtistRecommendationTypes();
    }

    /**
     * Gets the datastore
     * @return the datastore
     */
    public DataStore getDataStore() {
        return dataStore;
    }

    public void flush(ItemAdapter itemAdapter) throws AuraException {
        try {
            itemAdapter.flush(dataStore);
        } catch (RemoteException rx) {
            throw new AuraException("Error communicating with item store", rx);
        }
    }

    private void initSimTypes() {
        List<SimType> stypes = new ArrayList();
        stypes.add(new FieldSimType("Social Tags", "Similarity based upon Social Tags", Artist.FIELD_SOCIAL_TAGS));
        stypes.add(new FieldSimType("Bio Tags", "Similarity based upon BIO tags", Artist.FIELD_BIO_TAGS));
        stypes.add(new FieldSimType("Auto Tags", "Similarity based upon Auto tags", Artist.FIELD_AUTO_TAGS));
        stypes.add(new FieldSimType("Related", "Similarity based upon related artists", Artist.FIELD_RELATED_ARTISTS));
        stypes.add(new AllSimType());
        simTypes = Collections.unmodifiableList(stypes);
    }

    private void initArtistRecommendationTypes() {
        List<RecommendationType> rtypes = new ArrayList();
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
     * Enrolls a listener in the recommender
     * @param openID the openID of the listener
     * @return the listener
     * @throws AuraException if the listener is already enrolled or a problem occurs while enrolling the listener
     */
    public Listener enrollListener(String openID) throws AuraException, RemoteException {
        if (getListener(openID) == null) {
            try {
                User theUser = StoreFactory.newUser(openID, openID);
                return updateListener(new Listener(theUser));
            } catch (RemoteException rx) {
                throw new AuraException("Error communicating with item store", rx);
            }
        } else {
            throw new AuraException("attempting to enroll duplicate listener " +
                    openID);
        }
    }

    /**
     * Update the version of the listener stored in the datastore
     * 
     * @param listener the listener to update
     * @return the listener
     * @throws AuraException if there was an error
     */
    public Listener updateListener(Listener listener) throws AuraException, RemoteException {
        try {
            return new Listener(dataStore.putUser(listener.getUser()));
        } catch (RemoteException rx) {
            throw new AuraException("Error communicating with item store", rx);
        }
    }

    /**
     * Adds play info for a listener
     * @param listener the listener
     * @param artistID the artist ID
     * @param playCount the playcount
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void addPlayAttention(Listener listener, String artistID, int playCount) throws AuraException, RemoteException {
        for (int i = 0; i < playCount; i++) {
            Attention attention = StoreFactory.newAttention(listener.getKey(), artistID, Attention.Type.PLAYED);
            dataStore.attend(attention);
        }
    }

    /**
     * Adds fav info for a listener
     * @param listener the listener
     * @param artistID the artist ID
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void addFavoriteAttention(Listener listener, String artistID) throws AuraException, RemoteException {
        Attention attention = StoreFactory.newAttention(listener.getKey(), artistID, Attention.Type.LOVED);
        dataStore.attend(attention);
    }

    public void addViewedAttention(Listener listener, String artistID) throws AuraException, RemoteException {
        Attention attention = StoreFactory.newAttention(listener.getKey(), artistID, Attention.Type.VIEWED);
        dataStore.attend(attention);
    }

    /**
     * Adds ratings
     * @param listener the listener
     * @param artistID the artist ID
     * @param rating the  rating (0 to 5)
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void addRating(Listener listener, String artistID, int numStars) throws AuraException, RemoteException {
        if (numStars < 0 || numStars > 5) {
            throw new IllegalArgumentException("numStars must be between 0 and 5");
        }
        Attention attention = StoreFactory.newAttention(listener.getKey(), artistID, Attention.Type.RATING, Long.valueOf(numStars));
        dataStore.attend(attention);
    }

    public int getLatestRating(Listener listener, String artistID) throws AuraException, RemoteException {
        // BUG: fix this when the new attention methods arrive
        List<Attention> attns = dataStore.getLastAttentionForSource(listener.getKey(), Attention.Type.RATING, 10000);
        for (Attention attn : attns) {
            if (attn.getTargetKey().equals(artistID)) {
                return attn.getNumber().intValue();
            }
        }
        return 0;
    }

    /**
     * Adds a tag for a listener for an item
     * @param listener the listener doing the tagging
     * @param item the item being tagged
     * @param tag the tag
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void addTag(Listener listener, String itemID, String tag) throws AuraException, RemoteException {
        if (dataStore.getItem(itemID) != null) {
            Attention attention = StoreFactory.newAttention(listener.getKey(), itemID,
                    Attention.Type.TAG, tag);
            dataStore.attend(attention);
        }
    }

    /**
     * Gets the list of tags applied to the item by the user
     * @param listener the listener 
     * @param item the item 
     * @return
     */
    public List<String> getTags(Listener listener, String itemID) throws AuraException, RemoteException {
        List<String> results = new ArrayList();
        List<Attention> attns = dataStore.getLastAttentionForSource(listener.getKey(), Attention.Type.TAG, Integer.MAX_VALUE);
        for (Attention attn : attns) {
            if (attn.getTargetKey().equals(itemID)) {
                results.add(attn.getString());
            }
        }
        return results;
    }

    /**
     * Gets the list of all tags applied by the user
     * @param listener the listener 
     * @param item the item 
     * @return
     */
    public List<String> getAllTags(Listener listener) throws AuraException, RemoteException {
        List<String> results = new ArrayList();
        List<Attention> attns = dataStore.getLastAttentionForSource(listener.getKey(), Attention.Type.TAG, Integer.MAX_VALUE);
        for (Attention attn : attns) {
            results.add(attn.getString());
        }
        return results;
    }

    /**
     * Gets the Favorite artists IDs for a listener
     * @param listener the listener of interest
     * @param max the maximum number to return
     * @return the set of artist IDs
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Artist> getFavoriteArtists(Listener listener, int max) throws AuraException, RemoteException {
        List<Attention> attns = dataStore.getLastAttentionForSource(listener.getKey(), Attention.Type.LOVED, max);
        List<Artist> results = new ArrayList();
        for (Attention attn : attns) {
            Artist artist = artistLookup(attn.getTargetKey());
            if (artist != null) {
                results.add(artist);
            }
        }
        return results;
    }

    public Set<String> getFavoriteArtistsAsIDSet(Listener listener, int max) throws AuraException, RemoteException {
        List<Attention> attns = dataStore.getLastAttentionForSource(listener.getKey(), Attention.Type.LOVED, max);
        Set<String> results = new HashSet();
        for (Attention attn : attns) {
            results.add(attn.getTargetKey());
        }
        return results;
    }

    public List<Scored<String>> getRecentTopArtistsAsIDs(Listener listener, int max) throws AuraException, RemoteException {
        ScoredManager<String> sm = new ScoredManager();
        List<Attention> attns = dataStore.getLastAttentionForSource(listener.getKey(), max * 100);
        for (Attention attn : attns) {
            if (attn.getType() == Attention.Type.PLAYED) {
                sm.accum(attn.getTargetKey(), 1);
            } else if (attn.getType() == Attention.Type.LOVED) {
                sm.accum(attn.getTargetKey(), 100);
            } else if (attn.getType() == Attention.Type.DISLIKED) {
                sm.accum(attn.getTargetKey(), -100);
            } else if (attn.getType() == Attention.Type.RATING) {
                if (attn.getNumber() == 5) {
                    sm.accum(attn.getTargetKey(), 100);
                } else if (attn.getNumber() == 4) {
                    sm.accum(attn.getTargetKey(), 10);
                } else if (attn.getNumber() == 2) {
                    sm.accum(attn.getTargetKey(), -10);
                } else if (attn.getNumber() == 1) {
                    sm.accum(attn.getTargetKey(), -100);
                }
            }
        }
        return sm.getTopN(max);
    }

    public List<Scored<String>> getRecentFiveStarArtists(Listener listener, int max) throws AuraException, RemoteException {
        ScoredManager<String> sm = new ScoredManager();
        List<Attention> attns = dataStore.getLastAttentionForSource(listener.getKey(), Attention.Type.RATING, 10000);
        for (Attention attn : attns) {
            if (attn.getNumber() == 5) {
                sm.accum(attn.getTargetKey(), 1);
            }
        }
        return sm.getTopN(max);
    }

    public List<Scored<String>> getAllArtistsAsIDs(Listener listener) throws AuraException, RemoteException {
        ScoredManager<String> sm = new ScoredManager();
        List<Attention> attns = dataStore.getAttentionForSource(listener.getKey());
        for (Attention attn : attns) {
            if (attn.getType() == Attention.Type.PLAYED) {
                sm.accum(attn.getTargetKey(), 1);
            } else if (attn.getType() == Attention.Type.LOVED) {
                sm.accum(attn.getTargetKey(), 100);
            }
        }
        return sm.getAll();
    }

    public List<Scored<Artist>> getRecommendations(Listener listener, int max) throws AuraException, RemoteException {
        Set<String> skipIDS = getAttendedToArtists(listener);
        Artist artist = getRandomGoodArtistFromListener(listener);
        List<Scored<Artist>> results = new ArrayList();
        List<Scored<Artist>> simArtists = artistFindSimilar(artist.getKey(), max * 5);
        for (Scored<Artist> sartist : simArtists) {
            if (!skipIDS.contains(sartist.getItem().getKey())) {
                results.add(sartist);
                if (results.size() >= max) {
                    break;
                }
            }
        }
        return results;
    }

    public TagCloud tagCloudCreate(String id, String name) throws AuraException {
        if (getTagCloud(id) == null) {
            Item item = StoreFactory.newItem(ItemType.TAG_CLOUD, id, name);
            return new TagCloud(item);
        } else {
            throw new AuraException("attempting to create duplicate tagcloud " + id);
        }
    }

    public TagCloud getTagCloud(String id) throws AuraException {
        try {
            Item item = dataStore.getItem(id);
            return new TagCloud(item);
        } catch (RemoteException rx) {
            throw new AuraException("Error communicating with item store", rx);
        }
    }

    private Artist getRandomGoodArtistFromListener(Listener listener) throws AuraException, RemoteException {
        List<Attention> attentions = dataStore.getAttentionForSource(listener.getKey());
        attentions = filterOut(attentions, Attention.Type.DISLIKED);
        String id = BEATLES_ID;
        if (attentions.size() > 0) {
            id = selectRandom(attentions).getTargetKey();
        }
        return artistLookup(id);
    }

    private List<Attention> filterOut(List<Attention> attns, Attention.Type type) {
        List<Attention> attentions = new ArrayList();
        for (Attention attn : attns) {
            if (attn.getType() != type) {
                attentions.add(attn);
            }
        }
        return attentions;
    }

    private Set<String> getAttendedToArtists(Listener listener) throws AuraException, RemoteException {
        Set<String> ids = new HashSet();
        List<Attention> attentions = dataStore.getAttentionForSource(listener.getKey());
        for (Attention attn : attentions) {
            ids.add(attn.getTargetKey());
        }
        return ids;
    }

    private <T> T selectRandom(List<T> l) {
        if (l.size() > 0) {
            int index = rng.nextInt(l.size());
            return l.get(index);
        } else {
            return null;
        }
    }

    /**
     * Deletes a listener from the data store
     * 
     * @param listener the listener to delete
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void deleteListener(Listener listener) throws AuraException, RemoteException {
        if (listener != null) {
            dataStore.deleteUser(listener.getKey());
        }
    }

    /**
     * Gets the attention data for a listener
     * @param listener the listener of interest
     * @param type the type of attention data of interest (null indicates all)
     * @return the list of attention data (sorted by timestamp)
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Attention> getLastAttentionData(Listener listener, Attention.Type type,
            int count) throws AuraException, RemoteException {
        return dataStore.getLastAttentionForSource(listener.getKey(), type, count);
    }

    /**
     * Gets all the stored attention data for a listener
     * @param listener the listener of interest
     * @return the list of all attention data
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Attention> getAttention(Listener listener) throws AuraException, RemoteException {
        return dataStore.getAttentionForSource(listener.getKey());
    }

    /**
     * Gets the listener from the openID
     * @param openID the openID for the listener
     * @return the listener or null if the listener doesn't exist
     */
    public Listener getListener(String openID) throws AuraException, RemoteException {
        try {
            User user = dataStore.getUser(openID);
            if (user != null) {
                return new Listener(user);
            } else {
                return null;
            }
        } catch (RemoteException rx) {
            throw new AuraException("Error communicating with item store", rx);
        }
    }

    /**
     * Searches for artists that matc the given name
     * @param artistName the name to search for
     * @param returnCount the number of artists to return
     * @return a list fo artists scored by how well they match the query
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> artistSearch(String artistName, int returnCount) throws AuraException {
        String squery = "(aura-type = artist) <AND> (aura-name <matches> \"*" + artistName + "*\")";
        List<Scored<Item>> scoredItems = query(squery, returnCount);
        return convertToScoredArtistList(scoredItems);
    }

    /**
     * Looks up an Artist by the ID of the artist
     * @param artistID the musicbrainz id of the artist
     * @return the artist or null if the artist could not be found
     * @throws com.sun.labs.aura.util.AuraException
     */
    public Artist artistLookup(String artistID) throws AuraException {
        Item item = getItem(artistID);
        if (item != null) {
            typeCheck(item, ItemType.ARTIST);
            return new Artist(item);
        }
        return null;
    }

    /**
     * Gets the similarity types for the system. The simlarity types control
     * the type of artist similarity used.
     * @return the list of SimTypes
     */
    public List<SimType> getSimTypes() {
        return simTypes;
    }

    /**
     * Gets the recommendatin types for the system.
     * @return
     */
    public List<RecommendationType> getArtistRecommendationTypes() {
        return new ArrayList(recTypeMap.values());
    }

    public RecommendationType getArtistRecommendationType(String recTypeName) {
        return recTypeMap.get(recTypeName);
    }

    /**
     * Given an artist query, find the best matching artist
     * @param artistName an artist query
     * @return the best matching artist or null if no match could be found.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public Artist artistFindBestMatch(String artistName) throws AuraException {
        List<Scored<Artist>> artists = artistSearch(artistName, 1);
        if (artists.size() == 1) {
            return artists.get(0).getItem();
        }
        return null;
    }

    /**
     * Finds the best matching artist tag
     * @param artistTagName the name of the artist tag
     * @return the best matching artist tag or null if none could be found.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public ArtistTag artistTagFindBestMatch(String artistTagName) throws AuraException {
        List<Scored<ArtistTag>> artistTags = artistTagSearch(artistTagName, 1);
        if (artistTags.size() == 1) {
            return artistTags.get(0).getItem();
        }
        return null;
    }

    /**
     * Find the most similar artist to a given artist
     * @param artistID the ID of the seed artist
     * @param count the number of similar artists to return
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> artistFindSimilar(String artistID, int count) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(artistID, Artist.FIELD_SOCIAL_TAGS, count, ItemType.ARTIST);
        return convertToScoredArtistList(simItems);
    }

    /**
     * Find the most similar artist to a given tagcloud
     * @param tagCloudID the ID of the tag cloud
     * @param count the number of similar artists to return
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> tagCloudFindSimilarArtists(TagCloud tagCloud, int count) throws AuraException {
        return wordCloudFindSimilarArtists(tagCloud.getWordCloud(), count);
    }

    /**
     * Find the most similar artist to a given WordCloud
     * @param tagCloudID the ID of the tag cloud
     * @param count the number of similar artists to return
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> wordCloudFindSimilarArtists(WordCloud wc, int count) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(wc, Artist.FIELD_SOCIAL_TAGS, count, ItemType.ARTIST);
        return convertToScoredArtistList(simItems);
    }

    /**
     * Find the most similar tag cloud to a given tagcloud
     * @param tagCloudID the ID of the tag cloud
     * @param count the number of similar artists to return
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<TagCloud>> tagCloudFindSimilarTagClouds(TagCloud tagCloud, int count) throws AuraException {
        // BUG: fix this once the new similarity methods are in place
        List<Scored<Item>> simItems = findSimilar(tagCloud.getKey(), Artist.FIELD_SOCIAL_TAGS, count, ItemType.TAG_CLOUD);
        return convertToScoredTagCloudList(simItems);
    }

    /**
     * Find the most similar listenr to a given listeners
     * @param userID the ID of the user
     * @param count the number of similar listeners to return
     * @return a list of listeners scored by their similarity to the seed listener.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Listener>> listenerFindSimilar(String userID, int count) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(userID, count, ItemType.USER);
        return convertToScoredListenerList(simItems);
    }

    /**
     * Find the most similar artist to a given artist
     * @param artistID the ID of the seed artist
     * @param field the field to use for similarity
     * @param count the number of similar artists to return
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> artistFindSimilar(String artistID, String field, int count) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(artistID, field, count, ItemType.ARTIST);
        return convertToScoredArtistList(simItems);
    }

    public List<Scored<ArtistTag>> artistTagSearch(String artistTagName, int returnCount) throws AuraException {
        String query = "(aura-type = ARTIST_TAG) <AND> (aura-name <matches> \"*" + artistTagName + "*\")";
        List<Scored<Item>> scoredItems = query(query, returnCount);
        return convertToScoredArtistTagList(scoredItems);
    }

    public List<Scored<String>> artistExplainSimilarity(String artistID1, String artistID2, int count) throws AuraException {
        try {
            return dataStore.explainSimilarity(artistID1, artistID2, new SimilarityConfig(Artist.FIELD_SOCIAL_TAGS, count));
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    public List<Scored<String>> artistExplainSimilarity(WordCloud cloud, String artistID1, int count) throws AuraException {
        try {
            return dataStore.explainSimilarity(cloud, artistID1, new SimilarityConfig(Artist.FIELD_SOCIAL_TAGS, count));
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    public List<Scored<String>> artistExplainSimilarity(String artistID1, String artistID2, String field, int count) throws AuraException {
        try {
            return dataStore.explainSimilarity(artistID1, artistID2, new SimilarityConfig(field, count));
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    public List<String> artistGetMostPopularNames(int count) throws AuraException {
        try {
            List<Scored<Item>> items = dataStore.query("aura-type=ARTIST", "-popularity", count, null);
            List<Artist> artists = new ArrayList<Artist>();
            for (Scored<Item> i : items) {
                artists.add(new Artist(i.getItem()));
            }

            List<String> artistNames = new ArrayList();
            for (Artist artist : artists) {
                artistNames.add(artist.getName());
            }
            return artistNames;

        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    public List<ArtistTag> artistTagGetMostPopular(int count) throws AuraException {
        try {
            List<Scored<Item>> items = dataStore.query("aura-type=ARTIST_TAG", "-popularity", count, null);
            List<ArtistTag> artistTags = new ArrayList();
            for (Scored<Item> i : items) {
                artistTags.add(new ArtistTag(i.getItem()));
            }

            return artistTags;
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    public List<String> artistTagGetMostPopularNames(int count) throws AuraException {
        List<ArtistTag> artistTags = artistTagGetMostPopular(count);
        List<String> artistTagNames = new ArrayList();
        for (ArtistTag artistTag : artistTags) {
            artistTagNames.add(artistTag.getName());
        }
        return artistTagNames;
    }
    
    public float artistTagGetNormalizedPopularity(ArtistTag aTag) throws AuraException {
        if (rockTag == null) {
            rockTag = artistTagLookup(ArtistTag.nameToKey("rock"));
        }
        return aTag.getPopularity() / rockTag.getPopularity();
    }

    public ArtistTag artistTagLookup(String artistTagID) throws AuraException {
        Item item = getItem(artistTagID);
        if (item != null) {
            typeCheck(item, ItemType.ARTIST_TAG);
            return new ArtistTag(item);
        }
        return null;
    }

    public List<Scored<ArtistTag>> artistTagFindSimilar(String id, int count) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(id, ArtistTag.FIELD_TAGGED_ARTISTS, count, ItemType.ARTIST_TAG);
        return convertToScoredArtistTagList(simItems);
    }

    public List<Scored<ArtistTag>> artistGetDistinctiveTags(String id, int count) throws AuraException {
        return artistGetDistinctiveTags(id, Artist.FIELD_SOCIAL_TAGS, count);
    }

    public WordCloud artistGetDistinctiveTagNames(String id, int count) throws AuraException {
        try {
            return dataStore.getTopTerms(id, Artist.FIELD_SOCIAL_TAGS, count);
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    private List<Scored<ArtistTag>> artistGetDistinctiveTags(String id, String field, int count) throws AuraException {
        try {
            List<Scored<ArtistTag>> artistTags = new ArrayList();

            WordCloud tagNames = dataStore.getTopTerms(id, field, count);
            for (Scored<String> scoredTagName : tagNames) {
                ArtistTag artistTag = artistTagLookup(ArtistTag.nameToKey(scoredTagName.getItem()));
                // not all tags may be in the database yet
                if (artistTag != null) {
                    artistTags.add(new Scored<ArtistTag>(artistTag, scoredTagName.getScore()));
                }
            }
            return artistTags;
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    public Album albumLookup(String albumID) throws AuraException {
        Item item = getItem(albumID);
        if (item != null) {
            typeCheck(item, ItemType.ALBUM);
            return new Album(item);
        }
        return null;
    }

    public Event eventLookup(String eventID) throws AuraException {
        Item item = getItem(eventID);
        if (item != null) {
            typeCheck(item, ItemType.EVENT);
            return new Event(item);
        }
        return null;
    }

    public Photo photoLookup(String photoID) throws AuraException {
        Item item = getItem(photoID);
        if (item != null) {
            typeCheck(item, ItemType.PHOTO);
            return new Photo(item);
        }
        return null;
    }

    public Track trackLookup(String trackID) throws AuraException {
        Item item = getItem(trackID);
        if (item != null) {
            typeCheck(item, ItemType.TRACK);
            return new Track(item);
        }
        return null;
    }

    public Video videoLookup(String videoID) throws AuraException {
        Item item = getItem(videoID);
        if (item != null) {
            typeCheck(item, ItemType.VIDEO);
            return new Video(item);
        }
        return null;
    }

    private Item getItem(String id) throws AuraException {
        try {
            return dataStore.getItem(id);
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    private List<Scored<Item>> query(String query, int count) throws AuraException {
        try {
            return dataStore.query(query, "-score", count, null);
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    private void typeCheck(Item item, ItemType expected) throws AuraException {
        if (item.getType() != expected) {
            throw new AuraException("Mismatched item type, expected " + expected + ", found " + item.getType());
        }
    }

    private List<Scored<Artist>> convertToScoredArtistList(List<Scored<Item>> items) {
        List<Scored<Artist>> artistList = new ArrayList();
        for (Scored<Item> scoredItem : items) {
            artistList.add(new Scored<Artist>(new Artist(scoredItem.getItem()), scoredItem.getScore()));
        }
        return artistList;
    }

    private List<Scored<TagCloud>> convertToScoredTagCloudList(List<Scored<Item>> items) {
        List<Scored<TagCloud>> tagCloudList = new ArrayList();
        for (Scored<Item> scoredItem : items) {
            tagCloudList.add(new Scored<TagCloud>(new TagCloud(scoredItem.getItem()), scoredItem.getScore()));
        }
        return tagCloudList;
    }

    private List<Scored<Listener>> convertToScoredListenerList(List<Scored<Item>> items) {
        List<Scored<Listener>> listenerList = new ArrayList();
        for (Scored<Item> scoredItem : items) {
            listenerList.add(new Scored<Listener>(new Listener(scoredItem.getItem()), scoredItem.getScore()));
        }
        return listenerList;
    }

    private List<Scored<ArtistTag>> convertToScoredArtistTagList(List<Scored<Item>> items) {
        List<Scored<ArtistTag>> artistTagList = new ArrayList();
        for (Scored<Item> scoredItem : items) {
            artistTagList.add(new Scored<ArtistTag>(new ArtistTag(scoredItem.getItem()), scoredItem.getScore()));
        }
        return artistTagList;
    }

    private List<Scored<Item>> findSimilar(String id, int count, ItemType type) throws AuraException {
        try {
            List<Scored<Item>> simItems = dataStore.findSimilar(id, getFindSimilarConfig(count, new TypeFilter(type)));
            return simItems;
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    private List<Scored<Item>> findSimilar(String id, String field, int count, ItemType type) throws AuraException {
        try {
            List<Scored<Item>> simItems = dataStore.findSimilar(id, getFindSimilarConfig(field, count, new TypeFilter(type)));
            return simItems;
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    private List<Scored<Item>> findSimilar(WordCloud wc, String field, int count, ItemType type) throws AuraException {
        try {
            System.out.println("FindSimilarWC " + wc);
            List<Scored<Item>> simItems = dataStore.findSimilar(wc, getFindSimilarConfig(field, count, new TypeFilter(type)));
            return simItems;
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    private class FieldSimType implements SimType {

        private String name;
        private String description;
        private String field;

        FieldSimType(String name, String description, String fieldName) {
            this.name = name;
            this.description = description;
            this.field = fieldName;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<Scored<Artist>> findSimilarArtists(String artistID, int count) throws AuraException {
            return artistFindSimilar(artistID, field, count);
        }

        public List<Scored<String>> explainSimilarity(String id1, String id2, int count) throws AuraException {
            try {
                return dataStore.explainSimilarity(id1, id1, new SimilarityConfig(field, count));
            } catch (RemoteException ex) {
                throw new AuraException("Can't talk to the datastore " + ex, ex);
            }
        }

        private List<Scored<ArtistTag>> getDistinctiveTags(String id, int count) throws AuraException {
            return artistGetDistinctiveTags(id, field, count);
        }
    }

    private class AllSimType implements SimType {

        private String name;
        private String description;

        AllSimType() {
            this.name = "All";
            this.description = "Artist similarity based upon all fields";
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<Scored<Artist>> findSimilarArtists(String artistID, int count) throws AuraException {
            List<Scored<Item>> simItems = findSimilar(artistID, count, ItemType.ARTIST);
            return convertToScoredArtistList(simItems);
        }

        public List<Scored<String>> explainSimilarity(String artistID1, String artistID2, int count) throws AuraException {
            return artistExplainSimilarity(artistID1, artistID2, count);
        }
    }

    private class SimpleArtistRecommendationType implements RecommendationType {

        public String getName() {
            return "SimpleArtist";
        }

        public String getDescription() {
            return "a simple recommender that just returns artists that are similar to " +
                    " a single artist, selected at random, that the listener likes";
        }

        public RecommendationSummary getRecommendations(Listener listener, int count, RecommendationProfile rp)
                throws AuraException, RemoteException {
            Set<String> skipIDS = getAttendedToArtists(listener);
            Artist artist = getRandomGoodArtistFromListener(listener);
            List<Recommendation> results = new ArrayList();
            List<Scored<Artist>> simArtists = artistFindSimilar(artist.getKey(), count * 5);
            for (Scored<Artist> sartist : simArtists) {
                if (!skipIDS.contains(sartist.getItem().getKey())) {
                    skipIDS.add(sartist.getItem().getKey());
                    List<Scored<String>> reason = artistExplainSimilarity(artist.getKey(), sartist.getItem().getKey(), 20);
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

        public RecommendationSummary getRecommendations(Listener listener, int count, RecommendationProfile rp)
                throws AuraException, RemoteException {
            ArtistScoreManager sm = new ArtistScoreManager(true);
            List<Scored<String>> artistIDs = getRecentTopArtistsAsIDs(listener, 20);
            Set<String> skipIDS = getAttendedToArtists(listener);
            StringBuilder sb = new StringBuilder();

            sb.append("Similar to recently played artists like ");

            for (Scored<String> seedArtist : artistIDs) {
                Artist artist = artistLookup(seedArtist.getItem());
                if (artist != null) {
                    sb.append(artist.getName());
                    sb.append(",");
                    sm.addSeedArtist(artist);
                    List<Scored<Artist>> simArtists = artistFindSimilar(seedArtist.getItem(), count * 3);
                    for (Scored<Artist> simArtist : simArtists) {
                        if (!skipIDS.contains(simArtist.getItem().getKey())) {
                            sm.accum(simArtist.getItem(), seedArtist.getItem(), seedArtist.getScore() * simArtist.getScore());
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

    private SimilarityConfig getFindSimilarConfig(String field, int count, TypeFilter filter) {
        SimilarityConfig fsc = new SimilarityConfig(field, count, filter);
        fsc.setSkimPercent(1.);
        fsc.setReportPercent(1.);
        return fsc;
    }

    private SimilarityConfig getFindSimilarConfig(int count, TypeFilter filter) {
        SimilarityConfig fsc = new SimilarityConfig(count, filter);
        fsc.setSkimPercent(1.);
        fsc.setReportPercent(1.);
        return fsc;
    }

    private List<Recommendation> getSimilarArtists(List<Scored<String>> seedArtists, Set<String> skipArtists, int count)
            throws AuraException {
        List<Recommendation> recommendations = new ArrayList();
        for (Scored<String> seedArtist : seedArtists) {
            List<Scored<Artist>> simArtists = artistFindSimilar(seedArtist.getItem(), count);
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

    private class SimToRecentArtistRecommender2 implements RecommendationType {

        public String getName() {
            return "SimToRecent(2)";
        }

        public String getDescription() {
            return "Finds artists that are similar to the recently played artists (version 2)";
        }

        public RecommendationSummary getRecommendations(Listener listener, int count, RecommendationProfile rp)
                throws AuraException, RemoteException {
            List<Scored<String>> artistIDs = getRecentTopArtistsAsIDs(listener, count / 2);
            Set<String> skipIDS = getAttendedToArtists(listener);
            List<Recommendation> recommendations = getSimilarArtists(artistIDs, skipIDS, count);
            return new RecommendationSummary("similarity to recent artists", recommendations);
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

        public RecommendationSummary getRecommendations(Listener listener, int count, RecommendationProfile rp)
                throws AuraException, RemoteException {
            List<Scored<Item>> items = dataStore.findSimilar(listener.getKey(),
                    getFindSimilarConfig(Listener.FIELD_SOCIAL_TAGS,
                    count * 5, new TypeFilter(ItemType.ARTIST)));
            List<Recommendation> results = new ArrayList();
            Set<String> skipIDS = getAttendedToArtists(listener);

            for (Scored<Item> item : items) {
                if (!skipIDS.contains(item.getItem().getKey())) {
                    List<Scored<String>> reason = dataStore.explainSimilarity(listener.getKey(),
                            item.getItem().getKey(), new SimilarityConfig(Listener.FIELD_SOCIAL_TAGS, count));
                    results.add(new Recommendation(item.getItem().getKey(), item.getScore(), reason));
                    if (results.size() >= count) {
                        break;
                    }
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

        public RecommendationSummary getRecommendations(Listener listener, int count, RecommendationProfile rp)
                throws AuraException, RemoteException {

            Set<String> skipIDs = getAttendedToArtists(listener);
            List<Scored<Listener>> simListeners = listenerFindSimilar(listener.getKey(), MAX_LISTENERS);
            ArtistScoreManager sm = new ArtistScoreManager(true);
            for (Scored<Listener> sl : simListeners) {
                if (sl.getItem().equals(listener)) {
                    continue;
                }
                List<Scored<String>> artistIDs = getRecentTopArtistsAsIDs(sl.getItem(), count * 5);
                for (Scored<String> sartistID : artistIDs) {
                    if (!skipIDs.contains(sartistID.getItem())) {
                        // System.out.println("id " + sartistID.getItem());
                        Artist artist = artistLookup(sartistID.getItem());
                        if (artist != null) {
                            sm.accum(artist, sartistID.getItem(), sl.getScore() * sartistID.getScore());
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
}
