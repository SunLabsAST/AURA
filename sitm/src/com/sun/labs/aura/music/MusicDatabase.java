/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author plamere
 */
public class MusicDatabase {

    private DataStore dataStore;

    public MusicDatabase(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Scored<Artist>> artistSearch(String artistName, int returnCount) throws AuraException {
        String squery = "(aura-type = artist) <AND> (aura-name <matches> \"*" + artistName + "*\")";
        List<Scored<Item>> scoredItems = query(squery, returnCount);
        return convertToScoredArtistList(scoredItems);
    }

    public Artist artistLookup(String artistID) throws AuraException {
        Item item = getItem(artistID);
        if (item != null) {
            typeCheck(item, ItemType.ARTIST);
            return new Artist(item);
        }
        return null;
    }

    public Artist artistFindBestMatch(String artistName) throws AuraException {
        List<Scored<Artist>> artists = artistSearch(artistName, 1);
        if (artists.size() == 1) {
            return artists.get(0).getItem();
        }
        return null;
    }

    public List<Scored<Artist>> artistFindSimilar(String artistID, int count) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(artistID, Artist.FIELD_SOCIAL_TAGS, count, ItemType.ARTIST);
        return convertToScoredArtistList(simItems);
    }

    public List<Scored<ArtistTag>> artistTagSearch(String artistTagName, int returnCount) throws AuraException {
        String query = "(aura-type = artistTag) <AND> (aura-name <matches> \"*" + artistTagName + "*\")";
        List<Scored<Item>> scoredItems = query(query, returnCount);
        return convertToScoredArtistTagList(scoredItems);
    }

    public List<Scored<String>> artistExplainSimilarity(String artistID1, String artistID2, int count) throws AuraException {
        try {
            return dataStore.explainSimilarity(artistID1, artistID2, Artist.FIELD_SOCIAL_TAGS, count);
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
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

    private List<Scored<ArtistTag>> convertToScoredArtistTagList(List<Scored<Item>> items) {
        List<Scored<ArtistTag>> artistTagList = new ArrayList();
        for (Scored<Item> scoredItem : items) {
            artistTagList.add(new Scored<ArtistTag>(new ArtistTag(scoredItem.getItem()), scoredItem.getScore()));
        }
        return artistTagList;
    }

    private List<Scored<Item>> findSimilar(String id, int count, ItemType type) throws AuraException {
        try {
            List<Scored<Item>> simItems = dataStore.findSimilar(id, count, new TypeFilter(type));
            return simItems;
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    private List<Scored<Item>> findSimilar(String id, String field, int count, ItemType type) throws AuraException {
        try {
            List<Scored<Item>> simItems = dataStore.findSimilar(id, field, count, new TypeFilter(type));
            return simItems;
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }
}
