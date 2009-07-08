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

package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Counted;
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.aura.util.RemoteComponentManager;
import com.sun.labs.aura.util.RemoteMultiComponentManager;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.CompositeResultsFilter;
import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.query.And;
import com.sun.labs.minion.query.Element;
import com.sun.labs.minion.query.Equals;
import com.sun.labs.minion.query.Not;
import com.sun.labs.minion.query.Substring;
import com.sun.labs.minion.query.Undefined;
import com.sun.labs.util.props.ConfigurationManager;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.python.core.PyDictionary;
import org.python.core.PyList;

/**
 *
 */
public class MusicDatabase {

    private final static int MAX_ATTENTION_GET = 100000;

    /**
     * Used to determine from which portion of the long tail recommendations or
     * similar artists are to be pulled
     */
    public enum Popularity {
        ALL, HEAD, MID, TAIL, HEAD_MID, MID_TAIL
    };

    public enum DBOperation {
        ReadOnly, AddAttention, AddItem
    };

    /**
     * Used to determine which metric is to be used when fetching
     * popular artists
     */
    public enum PopularityMetric {
        LastFM(Artist.FIELD_POPULARITY),
        Hotttnesss(Artist.FIELD_HOTTTNESSS),
        Familiarity(Artist.FIELD_FAMILIARITY);
        
        private String fieldName;

        PopularityMetric(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getField() {
            return fieldName;
        }

    };

    private Pattern mbidRegex;
    private List<SimType> simTypes;
    private Random rng = new Random();
    private ArtistTag rockTag = null;
    private Artist mostPopularArtist = null;
    private double skimPercent = 1;
    private RemoteComponentManager rcm;
    private final static long DEFAULT_FIND_SIMILAR_TIMEOUT = 10000L;
    private RecommendationManager recommendationManager;

    private Logger logger = Logger.getLogger(getClass().getName());
    
    public MusicDatabase(ConfigurationManager cm) throws AuraException {
        this.rcm = new RemoteMultiComponentManager(cm, DataStore.class);
        new Album().defineFields(getDataStore());
        new Artist().defineFields(getDataStore());
        new ArtistTag().defineFields(getDataStore());
        new ArtistTagRaw().defineFields(getDataStore());
        new Event().defineFields(getDataStore());
        new Photo().defineFields(getDataStore());
        new Track().defineFields(getDataStore());
        new Venue().defineFields(getDataStore());
        new Video().defineFields(getDataStore());
        new Listener().defineFields(getDataStore());

        initSimTypes();
        mbidRegex = Pattern.compile("^(\\{{0,1}([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}\\}{0,1})$");
        recommendationManager = new RecommendationManager(this);
    }

    public void shutdown() {
        rcm.shutdown();
    }

    /**
     * Gets the datastore
     * @return the datastore
     */
    public DataStore getDataStore() throws AuraException {
        return (DataStore) rcm.getComponent();
    }

    public void flush(ItemAdapter itemAdapter) throws AuraException {
        try {
            itemAdapter.flush(getDataStore());
        } catch (RemoteException rx) {
            throw new AuraException("Error communicating with item store", rx);
        }
    }

    private void initSimTypes() {
        List<SimType> stypes = new ArrayList();
        stypes.add(new FieldSimType("Social Tags", "Similarity based upon Social Tags", Artist.FIELD_SOCIAL_TAGS));
        stypes.add(new FieldSimType("Raw Social Tags", "Similarity based upon Raw Social Tags", Artist.FIELD_SOCIAL_TAGS_RAW));
        stypes.add(new FieldSimType("Listeners", "Similarity based upon listeners' profile", Artist.FIELD_LISTENER_PLAY_COUNTS));
        //stypes.add(new FieldSimType("Blurb Tags", "Similarity based upon tags extracted from reviews", Artist.FIELD_BLURB_TAGS));
        //stypes.add(new FieldSimType("Auto Tags", "Similarity based upon Auto tags", Artist.FIELD_AUTO_TAGS));
        stypes.add(new FieldSimType("Related", "Similarity based upon related artists", Artist.FIELD_RELATED_ARTISTS));
        stypes.add(new AllSimType());
        simTypes = Collections.unmodifiableList(stypes);
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
            return new Listener(getDataStore().putUser(listener.getUser()));
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
    public void addPlayAttention(String listenerID, String artistID, int playCount) throws AuraException, RemoteException {
        if (playCount > 0) {
            Attention attention = StoreFactory.newAttention(listenerID, artistID,
                    Attention.Type.PLAYED, Long.valueOf(playCount));
            getDataStore().attend(attention);
        } else {
            throw new IllegalArgumentException("Play count for attention must be >0. '"+playCount+"' given");
        }
    }

    public void addPlayAttentionsWithDetails(String listenerID, String artistID,
            String details, int playCount, long timestamp) throws AuraException, RemoteException {
        if (playCount > 0) {
            Attention attention = StoreFactory.newAttention(listenerID, artistID,
                    Attention.Type.PLAYED, new Date(timestamp), Long.valueOf(playCount), details);
            getDataStore().attend(attention);
        } else {
            throw new IllegalArgumentException("Play count for attention must be >0. '"+playCount+"' given");
        }
    }

    public void addViewedAttention(String listenerID, String artistID) throws AuraException, RemoteException {
        Attention attention = StoreFactory.newAttention(listenerID, artistID, Attention.Type.VIEWED);
        getDataStore().attend(attention);
    }

    /**
     * Adds ratings
     * @param listener the listener
     * @param artistID the artist ID
     * @param rating the  rating (0 to 5)
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void addRating(String listenerID, String artistID, int numStars) throws AuraException, RemoteException {
        if (numStars < 0 || numStars > 5) {
            throw new IllegalArgumentException("numStars must be between 0 and 5");
        }
        Attention attention = StoreFactory.newAttention(listenerID, artistID,
                Attention.Type.RATING, Long.valueOf(numStars));
        getDataStore().attend(attention);
    }

    public int getLatestRating(String listenerID, String artistID) throws AuraException, RemoteException {
        int rating = 0;
        AttentionConfig ac = new AttentionConfig();
        ac.setType(Attention.Type.RATING);
        ac.setSourceKey(listenerID);
        ac.setTargetKey(artistID);
        List<Attention> attns = getDataStore().getLastAttention(ac, 1);
        if (attns.size() > 0) {
            rating = (int) getNumber(attns.get(0));
        }
        return rating;
    }

    public long getNumber(Attention attn) {
        Long val = attn.getNumber();
        return val == null ? 0L : Long.valueOf(val);
    }

    /**
     * Adds an artist with the given musicbrainz ID to the database
     * @param mbaid the musicbrainz ID
     */
    public void addArtist(String mbaid) throws AuraException, RemoteException {
        if (artistLookup(mbaid) == null) {
            Item item = StoreFactory.newItem(ItemType.ARTIST, mbaid, "(unknown)");
            Artist artist = new Artist(item);
            artist.flush(getDataStore());
        }
    }

    /**
     * Determines if the application has authorization to perform the operation
     * @param appID the application ID
     * @param operation the operation of interest
     * @return true if the application has permission to perform the requested operation.
     */
    public boolean hasAuthorization(String appID, DBOperation operation) {
        return true;        // TBD - write me
    }

    /**
     * Determines if the appID represents a valid application
     * @param appID the application ID
     * @return true if the application is a valid application
     */
    public boolean isValidApplication(String appID) {
        return true;        // TBD - write me
    }

    /**
     * Adds a tag for a listener for an item
     * @param listener the listener doing the tagging
     * @param item the item being tagged
     * @param tag the tag
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void addTag(String listenerID, String itemID, String tag) throws AuraException, RemoteException {
        if (getDataStore().getItem(itemID) != null) {
            Attention attention = StoreFactory.newAttention(listenerID, itemID,
                    Attention.Type.TAG, tag);
            getDataStore().attend(attention);
        }
    }

    /**
     * Adds an attention
     * @param srcKey the source key for the attention
     * @param targetKey the target key for the attention
     * @param type the type of attention
     * @param value string value to be associated with the attention
     * @throws AuraException
     * @throws RemoteException
     */
    public void addAttention(String srcKey, String targetKey, 
                            Attention.Type type, String sval, Long lval)
            throws AuraException, RemoteException {

        if (getDataStore().getItem(srcKey) == null) {
            throw new AuraException("attention src does not exist");
        }

        if (getDataStore().getItem(targetKey) == null) {
            throw new AuraException("attention target does not exist");
        }

        Attention attention = null;
        switch (type) {
            case PLAYED:
                if (lval == null) {
                    lval = new Long(1);
                }
                if (lval < 1 || lval > 1000) {
                    throw new AuraException("Playcount out of valid range");
                }
                attention = StoreFactory.newAttention(srcKey, targetKey, type,
                        new Date(), lval, sval);
                break;
            case RATING:
                if (lval == null || lval < 1 || lval > 5) {
                    throw new AuraException("rating out of valid range (1-5)");
                }
                attention = StoreFactory.newAttention(srcKey, targetKey, type,
                        new Date(), lval, sval);
                break;
            case TAG:
                if (sval == null) {
                    throw new AuraException("tag attention must have a string value");
                }
                attention = StoreFactory.newAttention(srcKey, targetKey, type,
                        new Date(), lval, sval);
                break;
            default:
                attention = StoreFactory.newAttention(srcKey, targetKey, type,
                        new Date(), lval, sval);
        }
        getDataStore().attend(attention);
    }

    /**
     * Gets the list of tags applied to the item by the user
     * @param listener the listener
     * @param item the item
     * @return
     */
    public List<String> getTags(String listenerID, String itemID) throws AuraException, RemoteException {
        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(listenerID);
        ac.setType(Attention.Type.TAG);
        ac.setTargetKey(itemID);

        List<Attention> attns = getDataStore().getAttention(ac);
        List<String> results = new ArrayList(attns.size());
        for (Attention attn : sortReverseChronologically(attns)) {
            results.add(attn.getString());
        }
        return results;
    }

    /**
     * Gets the list of all tags applied by the user
     * @param listener the listener
     * @return a list of all tags, scored by there frequency of application
     */
    public List<Scored<String>> getAllTags(String listenerID) throws AuraException, RemoteException {
        ScoredManager<String> sm = new ScoredManager();
        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(listenerID);
        ac.setType(Attention.Type.TAG);

        List<Attention> attns = getDataStore().getAttention(ac);
        for (Attention attn : attns) {
            sm.accum(attn.getString(), 1);
        }
        return sm.getAll();
    }

    /**
     * Gets the Favorite artists IDs for a listener
     * @param listener the listener of interest
     * @param max the maximum number of return. no particular order is garanteed
     * @return the set of artist objects
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public Collection<Artist> getFavoriteArtists(String listenerID, int max) throws AuraException, RemoteException {
        Set<String> keys = getFavoriteArtistKeys(listenerID, max);
        return artistLookup(keys);
    }

    /**
     * Gets the favorite artists' keys for a listener
     * @param listenerID the listener of interest
     * @param max the maximum number of return. no particular order is garanteed
     * @return the set of artist IDs
     * @throws AuraException
     * @throws RemoteException
     */
    public Set<String> getFavoriteArtistKeys(String listenerID, int max) throws AuraException, RemoteException {
        Listener listener = getListener(listenerID);
        List<Tag> tags = listener.getAggregatedPlayCount();
        Set<String> results = new HashSet<String>();
        for (Tag tag : tags) {
            results.add(tag.getName());
            if (results.size() >= max) {
                break;
            }
        }
        return results;
    }

    /**
     * Gets the set of artistIds that have the RATED, VIEWED or PLAYED attention associated
     * with them and a particular listener
     * @param listenerID the listener of interest
     * @param max the maximum number of return. no particular order is garanteed
     * @return the set of artist ids
     * @throws AuraException
     * @throws RemoteException
     */
    public Set<String> getAttendedToArtists(String listenerID, int max) throws AuraException, RemoteException {
        Set<String> results = new HashSet<String>();
        results.addAll(getAttendedToArtists(listenerID, Attention.Type.RATING, max - results.size()));
        results.addAll(getAttendedToArtists(listenerID, Attention.Type.VIEWED, max - results.size()));
        results.addAll(getAttendedToArtists(listenerID, Attention.Type.PLAYED, max - results.size()));
        return results;
    }

    /**
     * Gets the set of artistIds that have a particular attention associated with them
     * @param listenerID look only for attentions for a particular user. can be null to search for all users
     * @param type attention type
     * @param max max results to return. no particular order is guaranteed. Use Integer.MAX_VALUE for no max value
     * @return a set of artist ids
     * @throws AuraException
     * @throws RemoteException
     */
    public Set<String> getAttendedToArtists(String listenerID, Attention.Type type, int max) throws AuraException, RemoteException {
        Set<String> results = new HashSet<String>();
        if (max > 0) {

            AttentionConfig aC = new AttentionConfig();
            if (listenerID != null) {
                aC.setSourceKey(listenerID);
            }
            aC.setType(type);

            // Only add the check for max value in the python script if we're actually
            // asking for one
            String maxStr="";
            if (max<Integer.MAX_VALUE) {
                maxStr = "       if len(s)>=" + max + ": break \n";
            }

            // TODO. Figure out why set() isn't working and use instead of dict
            String script = "def process(attns): \n" +
                            "   #s = set() \n" +
                            "   s = {} \n" +
                            "   for a in attns.toArray(): \n" +
                            "       #s.add(a.getTargetKey()) \n" +
                            "       s[a.getTargetKey()] = 0 \n" +
                            maxStr +
                            "   return s \n" +
                            "\n"+
                            "def collect(results): \n" +
                            "   #s = set() \n" +
                            "   s = {} \n" +
                            "   for tS in results.toArray(): \n" +
                            "       #s = s.union(tS) \n" +
                            "       s.update(tS) \n" +
                            "   return s";

            PyDictionary pyDict = (PyDictionary) this.getDataStore().processAttention(aC, script, "python");
            PyList items = pyDict.keys();
            for (int i=0; i<items.size(); i++) {
                String artistId =  (String) items.get(i);
                if (isMbid(artistId)) {
                    results.add(artistId);
                }
                if (results.size()>=max) {
                    break;
                }
            }

        }
        return results;
    }

    public Set<String> getArtistKeysRatedAs(String listenerID, int max, int rating) throws AuraException, RemoteException {
        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(listenerID);
        ac.setType(Attention.Type.RATING);
        ac.setNumberVal(Long.valueOf(rating));

        List<Attention> attns = sortReverseChronologically(getDataStore().getLastAttention(ac, max));
        Set<String> results = new HashSet();
        for (Attention attn : attns) {
            results.add(attn.getTargetKey());
        }
        return results;
    }

    private String getRandomRecentGoodArtistKey(String listenerID) throws AuraException, RemoteException {
        Set<String> keys = getFavoriteArtistKeys(listenerID, 50);
        return selectRandom(keys);
    }

    private int getAttentionScore(Attention attn) {
        long attentionValue = getNumber(attn);
        int score = 0;
        if (attn.getType() == Attention.Type.PLAYED) {
            score = (int) (attentionValue == 0L ? 1 : attentionValue);
        } else if (attn.getType() == Attention.Type.LOVED) {
            score = 100;
        } else if (attn.getType() == Attention.Type.DISLIKED) {
            score = -100;
        } else if (attn.getType() == Attention.Type.RATING) {
            if (attentionValue == 5) {
                score = 100;
            } else if (attentionValue == 4) {
                score = 10;
            } else if (attentionValue == 2) {
                score = -10;
            } else if (attentionValue == 1) {
                score = -100;
            }

        }
        return score;
    }

    public List<Scored<String>> getAllArtistsAsIDsWithIterator(String listenerID) throws AuraException, RemoteException {
        ScoredManager<String> sm = new ScoredManager();
        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(listenerID);
        DBIterator<Attention> attentionIterator = getDataStore().getAttentionIterator(ac);
        try {
            while (attentionIterator.hasNext()) {
                Attention attn = attentionIterator.next();
                if (isMbid(attn.getTargetKey())) {
                    sm.accum(attn.getTargetKey(), getAttentionScore(attn));
                }

            }
        } finally {
            attentionIterator.close();
        }
        return sm.getAll();
    }

    public List<Scored<String>> getWeightedAttendedArtistsAsIDs(String listenerID) throws AuraException, RemoteException {
        return getWeightedAttendedArtistsAsIDs(listenerID, MAX_ATTENTION_GET);
    }

    public List<Scored<String>> getWeightedAttendedArtistsAsIDs(String listenerID, int max) throws AuraException, RemoteException {
        ScoredManager<String> sm = new ScoredManager();
        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(listenerID);
        List<Attention> attns = getDataStore().getLastAttention(ac, max);
        for (Attention attn : attns) {
            if (isMbid(attn.getTargetKey())) {
                sm.accum(attn.getTargetKey(), getAttentionScore(attn));
            }

        }
        return sm.getAll();
    }

    /**
     * Gets all of the item keys for items of a particular type
     * @param type the type of interest
     * @return a list containing all of the IDs of that type
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<String> getAllItemKeys(ItemType type) throws AuraException, RemoteException {
        List<String> keys = new ArrayList<String>();
        DBIterator<Item> itemIterator = getDataStore().getAllIterator(type);
        try {
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();
                keys.add(item.getKey());
            }

        } finally {
            itemIterator.close();
        }

        return keys;
    }

    /**
     * Determines if the given string is a valid MusicBrainz ID.
     * @param id MusicBrainz to validate
     * @return weather the given id is a valid MusicBrainz id
     */
    public boolean isMbid(String id) {
        if (id != null) {
            return mbidRegex.matcher(id).matches();
        }
        return false;
    }

    /**
     * Gets the top listeners for an artist
     * @param artistId artist key
     * @param count number of listeners to return
     * @return a sorted list of counted listener ids
     */
    public List<Counted<String>> getListenersIdsForArtist(String artistId, int count) 
            throws AuraException, RemoteException {
        return getDataStore().getTermCounts(artistId, 
                Listener.FIELD_AGGREGATED_PLAY_HISTORY, count, new TypeFilter(ItemType.USER));
    }

    public List<Scored<Artist>> getRecommendations(String listenerID, int max) throws AuraException, RemoteException {
        RecommendationType rtype = recommendationManager.getDefaultArtistRecommendationType();
        RecommendationSummary rs = rtype.getRecommendations(listenerID, max, null);
        List<Scored<Artist>> results = new ArrayList();
        for (Recommendation r : rs.getRecommendations()) {
            Artist artist = artistLookup(r.getId());
            results.add(new Scored<Artist>(artist, r.getScore()));
        }

        return results;
    }

    /**
     * Gets the most recent attention that matches the given data
     * @param srcID the desired src ID (typically a listener ID) (or null for any)
     * @param targetID the desired target id (or null for any)
     * @param type the desired attention typ (or null for all types)
     * @param count the return count
     * @return a list of the most recent attentions that match the give set of parameters
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Attention> getRecentAttention(String srcID, String targetID, Attention.Type type, int count)
            throws AuraException, RemoteException {
        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(srcID);
        ac.setTargetKey(targetID);
        ac.setType(type);
        return getDataStore().getLastAttention(ac, count);
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
            Item item = getDataStore().getItem(id);
            return new TagCloud(item);
        } catch (RemoteException rx) {
            throw new AuraException("Error communicating with item store", rx);
        }

    }

    /**
     * Gets the most popular artist based on the default popularity metric
     * @return most popular artist
     * @throws AuraException
     */
    public Artist getMostPopularArtist() throws AuraException {
        if (mostPopularArtist == null) {
            List<Artist> popularList = artistGetMostPopular(PopularityMetric.LastFM, 1);
            if (popularList.size() > 0) {
                mostPopularArtist = popularList.get(0);
            } else {
                throw new AuraException("No artists in database");
            }

        }
        return mostPopularArtist;
    }

    <T> T selectRandom(Collection<T> l) {
        if (l.size() > 0) {
            ArrayList<T> list = new ArrayList<T>(l);
            int index = rng.nextInt(list.size());
            return list.get(index);
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
    public void deleteListener(String listenerID) throws AuraException, RemoteException {
        getDataStore().deleteUser(listenerID);
    }

    /**
     * Gets the attention data for a listener
     * @param listener the listener of interest
     * @param type the type of attention data of interest (null indicates all)
     * @return the list of attention data (sorted by timestamp)
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Attention> getLastAttentionData(String listenerID, Attention.Type type,
            int count) throws AuraException, RemoteException {
        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(listenerID);
        ac.setType(type);
        return sortReverseChronologically(getDataStore().getLastAttention(ac, count));
    }

    List<Attention> sortReverseChronologically(List<Attention> attns) {
        Collections.sort(attns, AttentionComparator.COMPARATOR);
        Collections.reverse(attns);
        return attns;
    }

    /**
     * Gets the listener from the openID
     * @param openID the openID for the listener
     * @return the listener or null if the listener doesn't exist
     */
    public Listener getListener(
            String openID) throws AuraException, RemoteException {
        try {
            User user = getDataStore().getUser(openID);
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
     * Searches for artists that match the given name
     * @param artistName the name to search for
     * @param returnCount the number of artists to return
     * @return a list fo artists scored by how well they match the query
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> artistSearch(String artistName, int returnCount) throws AuraException {
        Element query = new And(new Equals("aura-type", "artist"),
                new Substring("aura-name", artistName));
        List<Scored<Item>> scoredItems = query(query, returnCount);
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
     * Looks up a collection of artists by id
     * @param ids the collection of ids for the artist
     * @return the collection of artists
     * @throws com.sun.labs.aura.util.AuraException
     */
    public Collection<Artist> artistLookup(Collection<String> ids) throws AuraException {
        Collection<Item> items = getItems(ids);
        Collection<Artist> artists = new ArrayList<Artist>();
        for (Item item : items) {
            typeCheck(item, ItemType.ARTIST);
            artists.add(new Artist(item));
        }

        return artists;
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
     * Gets the recommendation types for the system.
     * @return
     */
    public List<RecommendationType> getArtistRecommendationTypes() {
        return recommendationManager.getArtistRecommendationTypes();
    }

    public RecommendationType getArtistRecommendationType(String recTypeName) {
        return recommendationManager.getArtistRecommendationType(recTypeName);
    }

    public boolean isValidRecommendationType(String name) {
        return recommendationManager.isValidRecommendationType(name);
    }

    public RecommendationType getDefaultArtistRecommendationType() {
        return recommendationManager.getDefaultArtistRecommendationType();
    }

    /**
     * Given an artist query, find the best matching artist
     * @param artistName an artist query
     * @return the best matching artist or null if no match could be found.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public Artist artistFindBestMatch(
            String artistName) throws AuraException {
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
    public ArtistTag artistTagFindBestMatch(
            String artistTagName) throws AuraException {
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
        List<Scored<Item>> simItems = findSimilar(artistID, Artist.FIELD_SOCIAL_TAGS, count, null, ItemType.ARTIST, Popularity.ALL, 0);
        return convertToScoredArtistList(simItems);
    }

    /**
     * Find the most similar artist to a given artist
     * @param artistID the ID of the seed artist
     * @param count the number of similar artists to return
     * @param popularity the popularity of the resulting artists
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> artistFindSimilar(String artistID, int count, Popularity popularity) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(artistID, Artist.FIELD_SOCIAL_TAGS, count, null, ItemType.ARTIST, popularity,
                getMostPopularArtist().getPopularity());
        return convertToScoredArtistList(simItems);
    }

    /**
     * Find the most similar artist to a given artist
     * @param artistID the ID of the seed artist
     * @param count the number of similar artists to return
     * @param skipSet the keys of the items that should be excluded from the results
     * @param popularity the popularity of the resulting artists
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> artistFindSimilar(String artistID, int count, Set<String> skipSet, Popularity popularity) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(artistID, Artist.FIELD_SOCIAL_TAGS, count, skipSet, ItemType.ARTIST, popularity,
                getMostPopularArtist().getPopularity());
        return convertToScoredArtistList(simItems);
    }

    /**
     * Find the most similar artist to a given list of artists
     * @param artistID the ID of the seed artist
     * @param count the number of similar artists to return
     * @param popularity the popularity of the resulting artists
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> artistFindSimilar(List<String> keys, int count, Popularity popularity) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(keys, Artist.FIELD_SOCIAL_TAGS, count, null, ItemType.ARTIST, popularity,
                getMostPopularArtist().getPopularity());
        return convertToScoredArtistList(simItems);
    }

    /**
     * Find the most similar artist to a given list of artists
     * @param artistID the ID of the seed artist
     * @param count the number of similar artists to return
     * @param skipSet the keys of the items that should be excluded from the results
     * @param popularity the popularity of the resulting artists
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> artistFindSimilar(List<String> keys, int count, Set<String> skipSet, Popularity popularity) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(keys, Artist.FIELD_SOCIAL_TAGS, count, skipSet, ItemType.ARTIST, popularity,
                getMostPopularArtist().getPopularity());
        return convertToScoredArtistList(simItems);
    }

    /**
     * Find the most similar artist to a given tagcloud
     * @param tagCloudID the ID of the tag cloud
     * @param count the number of similar artists to return
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     * @deprecated
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
        return wordCloudFindSimilarArtists(wc, count, Popularity.ALL);
    }

    /**
     * Find the most similar artist to a given WordCloud
     * @param tagCloudID the ID of the tag cloud
     * @param count the number of similar artists to return
     * @return a list of artists scored by their similarity to the seed artist.
     * @param popularity the popularity of the resulting artists
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> wordCloudFindSimilarArtists(WordCloud wc, int count, Popularity pop) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(wc, Artist.FIELD_SOCIAL_TAGS, count, ItemType.ARTIST, pop,
                getMostPopularArtist().getPopularity());
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
     * Find the most similar listenr to a given listeners based on their listener habbits
     * @param userID the ID of the user
     * @param count the number of similar listeners to return
     * @return a list of listeners scored by their similarity to the seed listener.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Listener>> listenerFindSimilar(String userID, int count) throws AuraException {
        return listenerFindSimilar(userID, Listener.FIELD_AGGREGATED_PLAY_HISTORY, count);
    }

    /**
     * Find the most similar listenr to a given listeners
     * @param userID the ID of the user
     * @param field field on which to base the similarity computation
     * @param count the number of similar listeners to return
     * @return a list of listeners scored by their similarity to the seed listener.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Listener>> listenerFindSimilar(String userID, String field, int count) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(userID, field, count, ItemType.USER);
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
        return artistFindSimilar(artistID, field, count, Popularity.ALL);
    }

    /**
     * Find the most similar artist to a given artist
     * @param artistID the ID of the seed artist
     * @param field the field to use for similarity
     * @param count the number of similar artists to return
     * @param popularity the popularity of the resulting artists
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> artistFindSimilar(String artistID, String field, int count, Popularity popularity) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(artistID, field, count, null, ItemType.ARTIST, popularity,
                getMostPopularArtist().getPopularity());
        return convertToScoredArtistList(simItems);
    }

    /**
     * Find the most similar artist to a set of artists
     * @param keys the list of artist keys
     * @param field the field to use for similarity
     * @param count the number of similar artists to return
     * @param popularity the popularity of the resulting artists
     * @return a list of artists scored by their similarity to the seed artist.
     * @throws com.sun.labs.aura.util.AuraException
     */
    public List<Scored<Artist>> artistFindSimilar(List<String> keys, String field, int count, Popularity popularity) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(keys, field, count, null, ItemType.ARTIST, popularity,
                getMostPopularArtist().getPopularity());
        return convertToScoredArtistList(simItems);
    }

    public List<Scored<ArtistTag>> artistTagSearch(String artistTagName, int returnCount) throws AuraException {
        Element query = new And(new Equals("aura-type", "ARTIST_TAG"),
                new Substring("aura-name", artistTagName));
        List<Scored<Item>> scoredItems = query(query, returnCount);
        return convertToScoredArtistTagList(scoredItems);
    }

    public List<Scored<String>> artistExplainSimilarity(String artistID1, String artistID2, int count) throws AuraException {
        try {
            return getDataStore().explainSimilarity(artistID1, artistID2, getFindSimilarConfig(Artist.FIELD_SOCIAL_TAGS, count, null));
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }

    }

    public List<Scored<String>> artistExplainSimilarity(WordCloud cloud, String artistID1, int count) throws AuraException {
        try {
            return getDataStore().explainSimilarity(cloud, artistID1, getFindSimilarConfig(Artist.FIELD_SOCIAL_TAGS, count, null));
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }

    }

    public List<Scored<String>> artistExplainSimilarity(String artistID1, String artistID2, String field, int count) throws AuraException {
        try {
            return getDataStore().explainSimilarity(artistID1, artistID2, getFindSimilarConfig(field, count, null));
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }

    }

    public float artistGetNormalizedPopularity(Artist artist) throws AuraException {
        return artist.getPopularity() / getMostPopularArtist().getPopularity();
    }

    public List<String> artistGetMostPopularNames(int count) throws AuraException {
        return artistGetMostPopularNames(PopularityMetric.LastFM, count);
    }

    public List<String> artistGetMostPopularNames(PopularityMetric popMetric, int count) throws AuraException {
        List<String> artistNames = new ArrayList();
        for (Artist artist : artistGetMostPopular(popMetric, count)) {
            artistNames.add(artist.getName());
        }

        return artistNames;

    }

    public List<Artist> artistGetMostPopular(int count) throws AuraException {
        return artistGetMostPopular(PopularityMetric.LastFM, count);
    }

    public List<Artist> artistGetMostPopular(PopularityMetric popMetric, int count) throws AuraException {
        try {
            List<Scored<Item>> items = getDataStore().query(
                    new Equals("aura-type", "artist"),
                    String.format("-%s", popMetric.getField()),
                    count, null);
            List<Artist> artists = new ArrayList<Artist>();
            for (Scored<Item> i : items) {
                artists.add(new Artist(i.getItem()));
            }

            return artists;

        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }

    }

    public List<Listener> listenerGetMostActive(int count) throws AuraException {
        try {
            // TBD - activity field has not been added to the listner yet.
            List<Scored<Item>> items = getDataStore().query(
                    new Equals("aura-type", "USER"),
                    "-score", count, null);
            List<Listener> listeners = new ArrayList<Listener>();
            for (Scored<Item> i : items) {
                if (i.getItem() != null) {
                    listeners.add(new Listener(i.getItem()));
                }
            }
            return listeners;
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }

    }

    /**
     * Gets a list of listeners for which their neighbours have never been crawled
     * @param count number of listeners to return
     * @return list of listeners
     * @throws AuraException
     */
    public List<Listener> listenerGetNeverCrawledNeighbours(int count) throws AuraException {
        try {

            Equals p1 = new Equals("aura-type", Item.ItemType.USER.toString());
            Undefined p2 = new Undefined(Listener.FIELD_LAST_NEIGHBOURS_CRAWL);
            Not p3 = new Not(new Undefined(Listener.FIELD_LAST_FM_NAME));
            List<Scored<Item>> items = getDataStore().query(new And(p1, p2, p3), count, null);

            List<Listener> listeners = new ArrayList<Listener>();
            for (Scored<Item> i : items) {
                if (i.getItem() != null) {
                    listeners.add(new Listener(i.getItem()));
                }
            }
            return listeners;

        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    public List<ArtistTag> artistTagGetMostPopular(int count) throws AuraException {
        try {
            List<Scored<Item>> items = getDataStore().query(
                    new Equals("aura-type", "ARTIST_TAG"),
                    "-popularity", count, null);
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

    public ArtistTag artistTagLookup(
            String artistTagID) throws AuraException {
        Item item = getItem(artistTagID);
        if (item != null) {
            typeCheck(item, ItemType.ARTIST_TAG);
            return new ArtistTag(item);
        }

        return null;
    }

    /**
     * Looks up a collection of artistTags by id
     * @param ids the collection of ids for the artistTags
     * @return the collection of artists
     * @throws com.sun.labs.aura.util.AuraException
     */
    public Collection<ArtistTag> artistTagLookup(Collection<String> ids) throws AuraException {
        Collection<Item> items = getItems(ids);
        Collection<ArtistTag> results = new ArrayList<ArtistTag>();
        for (Item item : items) {
            typeCheck(item, ItemType.ARTIST_TAG);
            results.add(new ArtistTag(item));
        }

        return results;
    }

    public List<Scored<ArtistTag>> artistTagFindSimilar(String id, int count) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(id, ArtistTag.FIELD_TAGGED_ARTISTS, count, ItemType.ARTIST_TAG);
        return convertToScoredArtistTagList(simItems);
    }

    public List<Scored<ArtistTag>> artistGetDistinctiveTags(String id, int count) throws AuraException {
        return artistGetDistinctiveTags(id, Artist.FIELD_SOCIAL_TAGS, count);
    }

    public List<Scored<ArtistTag>> listenerGetDistinctiveTags(String id, int count) throws AuraException {
        return listenerGetDistinctiveTags(id, Listener.FIELD_SOCIAL_TAGS, count);
    }

    public WordCloud artistGetDistinctiveTagNames( String id, int count) throws AuraException {
        try {
            return getDataStore().getTopTerms(id, Artist.FIELD_SOCIAL_TAGS, count);
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }

    }

    private List<Scored<ArtistTag>> artistGetDistinctiveTags(String id, String field, int count) throws AuraException {
        try {
            List<Scored<ArtistTag>> artistTags = new ArrayList();

            WordCloud tagNames = getDataStore().getTopTerms(id, field, count);
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

    private List<Scored<ArtistTag>> listenerGetDistinctiveTags(String id, String field, int count) throws AuraException {
        return artistGetDistinctiveTags(id, field, count);
    }

    public Album albumLookup(
            String albumID) throws AuraException {
        Item item = getItem(albumID);
        if (item != null) {
            typeCheck(item, ItemType.ALBUM);
            return new Album(item);
        }

        return null;
    }

    /**
     * Looks up a collection of albums by id
     * @param ids the collection of ids for the albums
     * @return the collection of artists
     * @throws com.sun.labs.aura.util.AuraException
     */
    public Collection<Album> albumLookup(Collection<String> ids) throws AuraException {
        Collection<Item> items = getItems(ids);
        Collection<Album> results = new ArrayList<Album>();
        for (Item item : items) {
            typeCheck(item, ItemType.ALBUM);
            results.add(new Album(item));
        }

        return results;
    }

    public Event eventLookup(
            String eventID) throws AuraException {
        Item item = getItem(eventID);
        if (item != null) {
            typeCheck(item, ItemType.EVENT);
            return new Event(item);
        }

        return null;
    }

    /**
     * Looks up a collection of events by id
     * @param ids the collection of ids for the events
     * @return the collection of events
     * @throws com.sun.labs.aura.util.AuraException
     */
    public Collection<Event> eventLookup(Collection<String> ids) throws AuraException {
        Collection<Item> items = getItems(ids);
        Collection<Event> results = new ArrayList<Event>();
        for (Item item : items) {
            typeCheck(item, ItemType.EVENT);
            results.add(new Event(item));
        }

        return results;
    }

    public Photo photoLookup(
            String photoID) throws AuraException {
        Item item = getItem(photoID);
        if (item != null) {
            typeCheck(item, ItemType.PHOTO);
            return new Photo(item);
        }

        return null;
    }

    /**
     * Looks up a collection of photos by id
     * @param ids the collection of ids for the photos
     * @return the collection of photos
     * @throws com.sun.labs.aura.util.AuraException
     */
    public Collection<Photo> photoLookup(Collection<String> ids) throws AuraException {
        Collection<Item> items = getItems(ids);
        Collection<Photo> results = new ArrayList<Photo>();
        for (Item item : items) {
            typeCheck(item, ItemType.PHOTO);
            results.add(new Photo(item));
        }

        return results;
    }

    public Track trackLookup(
            String trackID) throws AuraException {
        Item item = getItem(trackID);
        if (item != null) {
            typeCheck(item, ItemType.TRACK);
            return new Track(item);
        }

        return null;
    }

    /**
     * Looks up a collection of tracks by id
     * @param ids the collection of ids for the tracks
     * @return the collection of tracks
     * @throws com.sun.labs.aura.util.AuraException
     */
    public Collection<Track> trackLookup(Collection<String> ids) throws AuraException {
        Collection<Item> items = getItems(ids);
        Collection<Track> results = new ArrayList<Track>();
        for (Item item : items) {
            typeCheck(item, ItemType.TRACK);
            results.add(new Track(item));
        }

        return results;
    }

    public Video videoLookup(
            String videoID) throws AuraException {
        Item item = getItem(videoID);
        if (item != null) {
            typeCheck(item, ItemType.VIDEO);
            return new Video(item);
        }

        return null;
    }

    /**
     * Looks up a collection of Video by id
     * @param ids the collection of ids for the Video
     * @return the collection of Video
     * @throws com.sun.labs.aura.util.AuraException
     */
    public Collection<Video> videoLookup(Collection<String> ids) throws AuraException {
        Collection<Item> items = getItems(ids);
        Collection<Video> results = new ArrayList<Video>();
        for (Item item : items) {
            typeCheck(item, ItemType.VIDEO);
            results.add(new Video(item));
        }

        return results;
    }

    private Item getItem(String id) throws AuraException {
        try {
            return getDataStore().getItem(id);
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }

    }

    private Collection<Item> getItems(Collection<String> ids) throws AuraException {
        try {
            return getDataStore().getItems(ids);
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }

    }

    private List<Scored<Item>> query(Element query, int count) throws AuraException {
        try {
            return getDataStore().query(query, "-score", count, null);
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

    private List<Scored<Item>> findSimilar(String key, int count, Set<String> skipSet, ItemType type, Popularity pop, float maxPopularity) throws AuraException {
        return findSimilar(key, null, count, skipSet, type, pop, maxPopularity);
    }

    private List<Scored<Item>> findSimilar(String key, String field, int count, ItemType type) throws AuraException {
        return findSimilar(key, field, count, null, type, Popularity.ALL, 0);
    }

    private List<Scored<Item>> findSimilar(String key, int count, ItemType type) throws AuraException {
        return findSimilar(key, null, count, null, type, Popularity.ALL, 0);
    }

    private List<Scored<Item>> findSimilar(String key, String field, int count, Set<String> skipSet,
            ItemType type, Popularity pop, float maxPopularity) throws AuraException {
        List<String> keys = new ArrayList<String>();
        keys.add(key);
        return findSimilar(keys, field, count, skipSet, type, pop, maxPopularity);
    }

    private List<Scored<Item>> findSimilar(List<String> keys, String field, int count, Set<String> skipSet, ItemType type, Popularity pop, float maxPopularity) throws AuraException {
        try {
            CompositeResultsFilter filter = new CompositeResultsFilter(
                    new TypeFilter(type),
                    new PopularityResultsFilter(pop, maxPopularity));
            if (skipSet != null && skipSet.size() > 0) {
                filter.addFilter(new KeyResultsFilter(skipSet));
            }

            List<Scored<Item>> simItems;
            if (keys.size() == 1) {
                simItems = getDataStore().findSimilar(keys.get(0), getFindSimilarConfig(field, count, filter));
            } else {
                simItems = getDataStore().findSimilar(keys, getFindSimilarConfig(field, count, filter));
            }
            return simItems;
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }


    private List<Scored<Item>> findSimilar(WordCloud wc, String field, int count, ItemType type, Popularity pop, float maxPopularity) throws AuraException {
        try {
            CompositeResultsFilter filter = new CompositeResultsFilter(new TypeFilter(type),
                    new PopularityResultsFilter(pop, maxPopularity));
            List<Scored<Item>> simItems = getDataStore().findSimilar(wc, getFindSimilarConfig(field, count, filter));
            return simItems;
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    public void setSkimPercent(double skimPercent) {
        this.skimPercent = skimPercent;
    }

    SimilarityConfig getFindSimilarConfig(String field, int count, ResultsFilter filter) {
        SimilarityConfig fsc;
        if (field == null) {
            fsc = new SimilarityConfig(count, filter);
        } else {
            fsc = new SimilarityConfig(field, count, filter);
        }

        fsc.setSkimPercent(skimPercent);
        fsc.setReportPercent(1.);
        fsc.setTimeout(DEFAULT_FIND_SIMILAR_TIMEOUT);
        return fsc;
    }

    /**
     * Converts the string to a Popularity
     * @param s the string
     * @return the popularity or none if none can be found
     */
    public Popularity toPopularity(
            String s) {
        for (Popularity p : Popularity.values()) {
            if (p.name().equalsIgnoreCase(s)) {
                return p;
            }

        }
        return null;
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

        @Override
        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public List<Scored<Artist>> findSimilarArtists(String artistID, int count) throws AuraException {
            return artistFindSimilar(artistID, field, count);
        }

        @Override
        public List<Scored<Artist>> findSimilarArtists(String artistID, int count,
                MusicDatabase.Popularity pop) throws AuraException {
            return artistFindSimilar(artistID, field, count, pop);
        }

        @Override
        public List<Scored<String>> explainSimilarity(String id1, String id2, int count) throws AuraException {
            try {
                return getDataStore().explainSimilarity(id1, id2, getFindSimilarConfig(field, count, null));
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

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public List<Scored<Artist>> findSimilarArtists(String artistID, int count) throws AuraException {
            List<Scored<Item>> simItems = findSimilar(artistID, count, ItemType.ARTIST);
            return convertToScoredArtistList(simItems);
        }

        @Override
        public List<Scored<Artist>> findSimilarArtists(String artistID, int count,
                MusicDatabase.Popularity pop) throws AuraException {
            List<Scored<Item>> simItems = findSimilar(artistID, count, null, ItemType.ARTIST, pop, getMostPopularArtist().getPopularity());
            return convertToScoredArtistList(simItems);
        }

        @Override
        public List<Scored<String>> explainSimilarity(String artistID1, String artistID2, int count) throws AuraException {
            return artistExplainSimilarity(artistID1, artistID2, count);
        }
    }
}

class AttentionComparator implements Comparator<Attention> {

    public final static AttentionComparator COMPARATOR = new AttentionComparator();

    @Override
    public int compare(Attention o1, Attention o2) {
        if (o1.getTimeStamp() > o2.getTimeStamp()) {
            return 1;
        } else if (o1.getTimeStamp() < o2.getTimeStamp()) {
            return -1;
        } else {
            return 0;
        }
    }
}
