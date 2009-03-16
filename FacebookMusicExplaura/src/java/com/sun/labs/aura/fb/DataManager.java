package com.sun.labs.aura.fb;

import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.fb.util.ExpiringLRACache;
import com.sun.labs.aura.fb.util.Util;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * Holds the code that generates the data necessary for the various UI elements
 */
public class DataManager {
    protected Logger logger = Logger.getLogger("");
    protected MusicDatabase mdb;

    protected static final int CLOUD_SIZE = 18;

    protected ExpiringLRACache<String,Artist> nameToArtist;
    protected ExpiringLRACache<String,ItemInfo[]> artistsToCloud;

    public DataManager(MusicDatabase mdb) {
        this.mdb = mdb;
        nameToArtist = new ExpiringLRACache<String,Artist>(1000, 6 * 60 * 60 * 1000);
        artistsToCloud = new ExpiringLRACache<String,ItemInfo[]>(1000,  6 * 60 * 60 * 1000);
    }

    public Artist guessArtist(String artistName) {
        Artist bestMatch = null;
        bestMatch = nameToArtist.get(artistName);
        if (bestMatch == null) {
            try {
                List<Scored<Artist>> matches = mdb.artistSearch(artistName, 10);
                if (!matches.isEmpty()) {
                    //
                    // Find the best matched name
                    if (matches.size() == 1) {
                        bestMatch = matches.get(0).getItem();
                    } else {
                        //
                        // Use the edit distance to pick the closest match....
                        // but for now, just pick the first one anyways
                        bestMatch = matches.get(0).getItem();
                    }
                }
                if (bestMatch != null) {
                    nameToArtist.put(artistName, bestMatch);
                }
            } catch (AuraException e) {
                logger.info("Search failed for " + artistName + e.getMessage());
            }
        }
        return bestMatch;
    }

    /**
     * Gets an array of tag ItemInfos that represents a merged cloud made from
     * the distinctive tags of all the provided artists.
     * 
     * @param artistIDs keys of artists to include
     * @param size
     * @return a normalized tag cloud
     */
    public ItemInfo[] getMergedCloud(String[] artistKeys, int size) {
        String cacheKey = StringUtils.join(artistKeys);
        ItemInfo[] cached = artistsToCloud.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        //
        // For each artist, get its distinctive tags and throw them
        // into a merged set
        HashMap<String,Scored<ArtistTag>> merged = new HashMap<String,Scored<ArtistTag>>();
        for (String artist : artistKeys) {
            try {
                List<Scored<ArtistTag>> tags = mdb.artistGetDistinctiveTags(artist, size);
                //
                // Do I want to normalize these values or not?
                // Not is easiest for now
                for (Scored<ArtistTag> scored : tags) {
                    ArtistTag tag = scored.getItem();
                    Scored<ArtistTag> existing = merged.get(tag.getName());
                    if (existing != null) {
                        existing.setScore(existing.getScore() + scored.getScore());
                    } else {
                        merged.put(tag.getName(), scored);
                    }
                }
            } catch (AuraException e) {
                logger.info("Failed to get tags for artist " + artist);
            }
        }

        //
        // Sort the merged values by descending score
        Collection<Scored<ArtistTag>> values = merged.values();
        List<Scored<ArtistTag>> l = new ArrayList<Scored<ArtistTag>>(values.size());
        l.addAll(values);
        Collections.sort(l, new Comparator<Scored<ArtistTag>>() {
            public int compare(Scored<ArtistTag> o1, Scored<ArtistTag> o2) {
                return -1 * (new Double(o1.getScore()).compareTo(new Double(o2.getScore())));
            }
        });

        //
        // Get the top N, normalizing as we go
        int numTags = Math.min(size, values.size());
        double maxScore = 1;
        if (numTags > 0) {
            maxScore = l.get(0).getScore();
        }

        ItemInfo[] items = new ItemInfo[numTags];
        for (int i = 0; i < numTags; i++) {
            Scored<ArtistTag> scored = l.get(i);
            ArtistTag tag = scored.getItem();
            items[i] = new ItemInfo(tag.getKey(), tag.getName(),
                                    scored.getScore() / maxScore, tag.getPopularity(),
                                    ItemInfo.CONTENT_TYPE.TAG);
        }
        artistsToCloud.put(cacheKey, items);
        return items;
    }

    public ItemInfo[] getComparisonCloud(ItemInfo[] tagCloud1,
                                         ItemInfo[] tagCloud2) {
        //
        // Make two WordClouds, then ask Aura to explain how they overlap
        WordCloud one  = new WordCloud();
        for (ItemInfo i : tagCloud1) {
            one.add(i.getItemName(), i.getScore());
        }

        WordCloud two = new WordCloud();
        for (ItemInfo i : tagCloud2) {
            two.add(i.getItemName(), i.getScore());
        }

        //
        // Get the overlap set
        List<Scored<String>> result = new ArrayList<Scored<String>>();
        try {
            result = mdb.getDataStore().explainSimilarity(one, two, new SimilarityConfig(CLOUD_SIZE / 2));
        } catch (AuraException e) {
            logger.log(Level.WARNING, "Failed while talking to Aura", e);
        } catch (RemoteException e) {
            logger.log(Level.WARNING, "Communication disruption", e);
        }

        //
        // Normalize the overlaps cloud
        ItemInfo[] overlap = Util.normalize(result);

        //
        // Now assemble the results for display as a cloud
        Set<String> commonNames = getNameSet(overlap);
        List<ItemInfo> infos = new ArrayList<ItemInfo>();

        List<ItemInfo> head = Util.negative(Util.getTopUniqueInfo(tagCloud1, commonNames, CLOUD_SIZE / 2));
        infos.addAll(head);

        overlap = ItemInfo.shuffle(overlap);
        for (ItemInfo ii : overlap) {
            infos.add(ii);
        }

        List<ItemInfo> tail = Util.negative(Util.getTopUniqueInfo(tagCloud2, commonNames, CLOUD_SIZE /2));
        Collections.reverse(tail);
        infos.addAll(tail);

        return infos.toArray(new ItemInfo[0]);
    }

    private Set<String> getNameSet(ItemInfo[] itemInfo) {
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < itemInfo.length; i++) {
            set.add(itemInfo[i].getItemName());
        }
        return set;
    }

}
