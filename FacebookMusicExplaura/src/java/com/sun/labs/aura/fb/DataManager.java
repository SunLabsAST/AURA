package com.sun.labs.aura.fb;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Holds the code that generates the data necessary for the various UI elements
 */
public class DataManager {
    protected Logger logger = Logger.getLogger("");
    protected MusicDatabase mdb;

    public DataManager(MusicDatabase mdb) {
        this.mdb = mdb;
    }

    public Artist guessArtist(String artistName) {
        Artist bestMatch = null;
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
        } catch (AuraException e) {
            logger.info("Search failed for " + artistName + e.getMessage());
        }
        return bestMatch;
    }

    /**
     * Gets an array of tag ItemInfos that represents a merged cloud made from
     * the distinctive tags of all the provided artists.
     * 
     * @param artistIDs keys of artists to include
     * @param size
     * @return
     */
    public ItemInfo[] getMergedCloud(String[] artistKeys, int size) {
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

        return items;
    }
}
