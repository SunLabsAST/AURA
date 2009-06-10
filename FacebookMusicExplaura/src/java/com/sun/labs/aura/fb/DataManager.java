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

package com.sun.labs.aura.fb;

import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.fb.util.ExpiringLRACache;
import com.sun.labs.aura.fb.util.Util;
import com.sun.labs.aura.music.Album;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.music.Photo;
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

    public List<Scored<Artist>> getSimilarArtists(List<String> artistKeys,
                                          int count, Popularity popularity) {
        List<Scored<Artist>> ret = null;
        try {
            List<Scored<Artist>> results = mdb.artistFindSimilar(artistKeys, count + artistKeys.size(), popularity);
            //
            // We need to manually filter out the original artists
            ret = new ArrayList<Scored<Artist>>();
            for (Scored<Artist> sa : results) {
                Artist a = sa.getItem();
                if (!artistKeys.contains(a.getKey())) {
                    ret.add(sa);
                }
            }
            if (ret.size() > count) {
                ret = ret.subList(0, count);
            }
        } catch (AuraException e) {
            logger.log(Level.WARNING, "Failed to get similar artists", e);
            return new ArrayList<Scored<Artist>>();
        }
        sortByArtistPopularity(ret);
        return ret;
    }

    public Artist guessArtist(String artistName) {
        Artist bestMatch = null;
        bestMatch = nameToArtist.get(artistName);
        if (bestMatch == null) {
            try {
                List<Scored<Artist>> matches = mdb.artistSearch(artistName, 20);
                if (!matches.isEmpty()) {
                    //
                    // Find the best matched name
                    Artist firstMatch = matches.get(0).getItem();
                    if (matches.size() == 1) {
                        bestMatch = firstMatch;
                    } else {
                        if (artistName.equalsIgnoreCase(firstMatch.getName())) {
                            //
                            // First match is an exact hit, so take it
                            bestMatch = firstMatch;
                        } else {
                            //
                            // Sort the top 20 by popularity and use the most
                            // popular option
                            Collections.sort(matches,
                                    new Comparator<Scored<Artist>>() {
                                @Override
                                public int compare(Scored<Artist> o1,
                                        Scored<Artist> o2) {
                                    Float p1 = o1.getItem().getPopularity();
                                    Float p2 = o2.getItem().getPopularity();
                                    //
                                    // Reverse order, most popular first
                                    return p2.compareTo(p1);
                                }

                            });
                            bestMatch = matches.get(0).getItem();
                        }
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
     * @param artists artists to include
     * @param size
     * @return a normalized tag cloud
     */
    public ItemInfo[] getMergedCloud(List<Artist> artists, int size) {
        String[] artistIDs = new String[artists.size()];
        for (int i = 0; i < artists.size(); i++) {
            artistIDs[i] = artists.get(i).getKey();
        }
        return getMergedCloud(artistIDs, size);
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
            result = mdb.getDataStore().explainSimilarity(one, two, new SimilarityConfig(CLOUD_SIZE));
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

    /**
     * Get the URL for a thumbnail of the provided artist.
     * 
     * @param a the artist
     * @return a thumbnail URL or null if we couldn't get one
     * @throws com.sun.labs.aura.util.AuraException
     */
    public String getThumbnailImageURL(Artist a) throws AuraException {
        String url = null;

        if (url == null) {
            Set<String> photoIDs = a.getPhotos();
            if (photoIDs.size() > 0) {
                String[] ids = photoIDs.toArray(new String[photoIDs.size()]);
                url = Photo.idToThumbnail(ids[0]);
            }
        }

        if (url == null) {
            Set<String> albumIDs = a.getAlbums();
            if (albumIDs.size() > 0) {
                String[] ids = albumIDs.toArray(new String[albumIDs.size()]);
                Album album = mdb.albumLookup(ids[0]);
                url = album.getAlbumArt();
            }
        }
        return url;
    }

    private Set<String> getNameSet(ItemInfo[] itemInfo) {
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < itemInfo.length; i++) {
            set.add(itemInfo[i].getItemName());
        }
        return set;
    }

    /**
     * Returns the Levenshtein edit distance between the two strings
     * @param str1
     * @param str2
     * @return
     */
    private int editDistance(String str1, String str2) {
        return com.sun.labs.minion.util.Util.levenshteinDistance(str1, str2);
    }

    private void sortByArtistPopularity(List<Scored<Artist>> scoredArtists) {
        Collections.sort(scoredArtists, new ArtistPopularitySorter());
        Collections.reverse(scoredArtists);
    }

    class ArtistPopularitySorter implements Comparator<Scored<Artist>> {

        public int compare(Scored<Artist> o1, Scored<Artist> o2) {
            double s1 = o1.getItem().getPopularity();
            double s2 = o2.getItem().getPopularity();
            if (s1 > s2) {
                return 1;
            } else if (s1 < s2) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
