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
import java.util.Collections;
import java.util.List;

/**
 *
 * @author plamere
 */
public class MusicDatabase {

    private DataStore dataStore;
    private List<SimType> simTypes;

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

        initSimTypes();

    }

    private void initSimTypes() {
        List<SimType> stypes = new ArrayList();
        stypes.add(new FieldSimType("Social Tags", "Similarity based upon Social Tags", Artist.FIELD_SOCIAL_TAGS));
        stypes.add(new FieldSimType("Bio Tags", "Similarity based upon BIO tags", Artist.FIELD_BIO_TAGS));
        stypes.add(new FieldSimType("Auto Tags", "Similarity based upon Auto tags", Artist.FIELD_AUTO_TAGS));
        stypes.add(new FieldSimType("Biography", "Similarity based upon the biography", Artist.FIELD_BIOGRAPHY_SUMMARY));
        stypes.add(new FieldSimType("Related", "Similarity based upon related artists", Artist.FIELD_RELATED_ARTISTS));
        stypes.add(new AllSimType());
        simTypes = Collections.unmodifiableList(stypes);
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

    public List<SimType> getSimTypes() {
        return simTypes;
    }

    public Artist artistFindBestMatch(String artistName) throws AuraException {
        List<Scored<Artist>> artists = artistSearch(artistName, 1);
        if (artists.size() == 1) {
            return artists.get(0).getItem();
        }
        return null;
    }

    public ArtistTag artistTagFindBestMatch(String artistTagName) throws AuraException {
        List<Scored<ArtistTag>> artistTags = artistTagSearch(artistTagName, 1);
        if (artistTags.size() == 1) {
            return artistTags.get(0).getItem();
        }
        return null;
    }

    public List<Scored<Artist>> artistFindSimilar(String artistID, int count) throws AuraException {
        List<Scored<Item>> simItems = findSimilar(artistID, Artist.FIELD_SOCIAL_TAGS, count, ItemType.ARTIST);
        return convertToScoredArtistList(simItems);
    }

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
            return dataStore.explainSimilarity(artistID1, artistID2, Artist.FIELD_SOCIAL_TAGS, count);
        } catch (RemoteException ex) {
            throw new AuraException("Can't talk to the datastore " + ex, ex);
        }
    }

    public List<Scored<String>> artistExplainSimilarity(String artistID1, String artistID2, String field, int count) throws AuraException {
        try {
            return dataStore.explainSimilarity(artistID1, artistID2, field, count);
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

    private List<Scored<ArtistTag>> artistGetDistinctiveTags(String id, String field, int count) throws AuraException {
        try {
            List<Scored<ArtistTag>> artistTags = new ArrayList();

            List<Scored<String>> tagNames = dataStore.getTopTerms(id, field, count);
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
                return dataStore.explainSimilarity(id1, id1, field, count);
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
}
