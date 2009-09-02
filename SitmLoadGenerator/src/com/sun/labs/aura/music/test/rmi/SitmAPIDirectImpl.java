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

package com.sun.labs.aura.music.test.rmi;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.music.webservices.api.Item;
import com.sun.labs.aura.music.webservices.api.Monitor;
import com.sun.labs.aura.music.webservices.api.Scored;
import com.sun.labs.aura.music.webservices.api.SitmAPI;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ScoredComparator;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

/**
 * An implementation of the SITM API that talks directly to the DataStore
 * via RMI.  (Can be run on-grid)
 */
public class SitmAPIDirectImpl extends SitmAPI {
    protected static Logger logger = Logger.getLogger("");
    protected MusicDatabase mdb = null;
    protected Monitor monitor; // API monitor, not a test monitor even though they're similar

    public SitmAPIDirectImpl(URL configFile, boolean periodicDump) {
        //
        // Get the datastore interface
        try {
            ConfigurationManager cm = new ConfigurationManager();
            cm.addProperties(configFile);

            //
            // Create a Music Database wrapper
            mdb = new MusicDatabase(cm);
            logger.info("Found all components");
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Failed to get service handle", ioe);
        } catch (AuraException e) {
            logger.log(Level.SEVERE, "Failed to create MDB", e);
        }
        monitor = new Monitor(false, periodicDump);
    }

    @Override
    public List<Scored<Item>> artistSearch(String searchString) throws IOException {
        try {
            long start = monitor.opStart();
            List<com.sun.labs.aura.util.Scored<Artist>> scoredArtists = mdb.artistSearch(searchString, 250);
            List<Scored<Item>> result = new ArrayList<Scored<Item>>();
            for (com.sun.labs.aura.util.Scored<Artist> sa : scoredArtists) {
                result.add(new Scored<Item>(new Item(sa.getItem().getName(),
                                                     sa.getItem().getKey()),
                                            sa.getScore()));
            }
            monitor.opFinish("artistSearch", start, 0);
            return result;
        } catch (AuraException e) {
            monitor.opError("artistSearch");
            throw new IOException(e);
        }
    }

    @Override
    public List<Scored<Item>> artistSocialTags(String key, int count) throws IOException {
        try {
            long start = monitor.opStart();
            Artist artist = mdb.artistLookup(key);

            WordCloud tags = mdb.artistGetDistinctiveTagNames(key, count);
            Map<String, com.sun.labs.aura.util.Scored<String>> words = tags.getWords();
            List<com.sun.labs.aura.util.Scored<String>> vals =
                    new ArrayList<com.sun.labs.aura.util.Scored<String>>(words.values());
            Collections.sort(vals, ScoredComparator.COMPARATOR);
            Collections.reverse(vals);

            List<Scored<Item>> result = new ArrayList<Scored<Item>>();
            for (com.sun.labs.aura.util.Scored<String> scoredTag : vals) {
                String tagName = scoredTag.getItem();
                ArtistTag artistTag = mdb.artistTagLookup(ArtistTag.nameToKey(tagName));
                if(artistTag == null) {
                    continue;
                }
                result.add(new Scored<Item>(new Item(artistTag.getKey(), tagName), scoredTag.getScore()));
            }
            monitor.opFinish("artistSocialTags", start, 0);
            return result;
        } catch (AuraException e) {
            monitor.opError("artistSocialTags");
            throw new IOException(e);
        }
    }

    @Override
    public long checkStatus(String msg, Document doc) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Scored<Item>> findSimilarArtistFromWordCloud(String cloudStr, int count) throws IOException {
        try {
            long start = monitor.opStart();
            WordCloud cloud = WordCloud.convertStringToWordCloud(cloudStr);
            if (cloud == null) {
                throw new IOException("Bad wordcloud format");
            }
            List<com.sun.labs.aura.util.Scored<Artist>> scoredArtists = mdb.wordCloudFindSimilarArtists(cloud, count, Popularity.ALL);

            List<Scored<Item>> result = new ArrayList<Scored<Item>>();
            for (com.sun.labs.aura.util.Scored<Artist> sa : scoredArtists) {
                result.add(new Scored<Item>(new Item(sa.getItem().getKey(),
                                                     sa.getItem().getName()),
                                            sa.getScore()));
            }
            monitor.opFinish("findSimilarArtistFromWordCloud", start, 0);
            return result;
        } catch (AuraException e) {
            monitor.opError("findSimilarArtistFromWordCloud");
            throw new IOException(e);
        }
    }

    @Override
    public List<Scored<Item>> findSimilarArtistTags(String key, int count) throws IOException {
        try {
            long start = monitor.opStart();
            ArtistTag artistTag = mdb.artistTagLookup(key);
            List<com.sun.labs.aura.util.Scored<ArtistTag>> scoredTags =
                    mdb.artistTagFindSimilar(key, count);
            
            List<Scored<Item>> result = new ArrayList<Scored<Item>>();
            for (com.sun.labs.aura.util.Scored<ArtistTag> sa : scoredTags) {
                result.add(new Scored<Item>(new Item(sa.getItem().getKey(),
                                                     sa.getItem().getName()),
                                            sa.getScore()));
            }
            monitor.opFinish("findSimilarArtistTags", start, 0);
            return result;
        } catch (AuraException e) {
            monitor.opError("findSimilarArtistTags");
            throw new IOException(e);
        }
    }

    @Override
    public List<Scored<Item>> findSimilarArtistsByKey(String key, int count) throws IOException {
        try {
            long start = monitor.opStart();
            Artist a = mdb.artistLookup(key);
            List<String> keys = new ArrayList<String>();
            keys.add(a.getKey());
            List<com.sun.labs.aura.util.Scored<Artist>> scoredArtists =
                    mdb.artistFindSimilar(keys, null, count + 1, Popularity.ALL);
            
            List<Scored<Item>> result = new ArrayList<Scored<Item>>();
            for (com.sun.labs.aura.util.Scored<Artist> sa : scoredArtists) {
                result.add(new Scored<Item>(new Item(sa.getItem().getKey(),
                                                     sa.getItem().getName()),
                                            sa.getScore()));
            }
            monitor.opFinish("findSimilarArtistsByKey", start, 0);
            return result;
        } catch (AuraException e) {
            monitor.opError("findSimilarArtistsByKey");
            logger.log(Level.INFO, "error", e);
            throw new IOException(e);
        }
    }

    @Override
    public List<Scored<Item>> findSimilarArtistsByName(String name, int count) throws IOException {
        try {
            long start = monitor.opStart();
            Artist a = mdb.artistFindBestMatch(name);
            List<String> keys = new ArrayList<String>();
            keys.add(a.getKey());
            List<com.sun.labs.aura.util.Scored<Artist>> scoredArtists =
                    mdb.artistFindSimilar(keys, null, count + 1, null);

            List<Scored<Item>> result = new ArrayList<Scored<Item>>();
            for (com.sun.labs.aura.util.Scored<Artist> sa : scoredArtists) {
                result.add(new Scored<Item>(new Item(sa.getItem().getKey(),
                                                     sa.getItem().getName()),
                                            sa.getScore()));
            }
            monitor.opFinish("findSimilarArtistsByName", start, 0);
            return result;
        } catch (AuraException e) {
            monitor.opError("findSimilarArtistsByName");
            throw new IOException(e);
        }
    }

    @Override
    public List<Item> getArtistTags(int count) throws IOException {
        try {
            long start = monitor.opStart();
            List<ArtistTag> tags = mdb.artistTagGetMostPopular(count);
            List<Item> result = new ArrayList<Item>();
            for (ArtistTag tag : tags) {
                result.add(new Item(tag.getKey(), tag.getName()));
            }
            monitor.opFinish("getArtistTags", start, 0);
            return result;
        } catch (AuraException e) {
            monitor.opError("getArtistTags");
            throw new IOException(e);
        }
    }

    @Override
    public List<Item> getArtists(int count) throws IOException {
        try {
            long start = monitor.opStart();
            List<Artist> artists = mdb.artistGetMostPopular(count);

            List<Item> result = new ArrayList<Item>();
            for (Artist artist : artists) {
                result.add(new Item(artist.getKey(), artist.getName()));
            }
            monitor.opFinish("getArtists", start, 0);
            return result;
        } catch (AuraException e) {
            monitor.opError("getArtists");
            throw new IOException(e);
        }
    }

    @Override
    public Item getItem(String key, boolean compact) throws IOException {
        try {
            long start = monitor.opStart();
            com.sun.labs.aura.datastore.Item item = mdb.getDataStore().getItem(key);
            
            monitor.opFinish("getItem", start, 0);
            return new Item(item.getKey(), item.getName());
        } catch (AuraException e) {
            monitor.opError("getItem");
            throw new IOException(e);
        }
    }

    @Override
    public List<Item> getItems(List<String> keys, boolean compact) throws IOException {
        try {
            long start = monitor.opStart();

            List<Item> result = new ArrayList<Item>();
            for (String key : keys) {
                com.sun.labs.aura.datastore.Item item = mdb.getDataStore().getItem(key);
                result.add(new Item(item.getKey(), item.getName()));
            }

            monitor.opFinish("getItems", start, 0);
            return result;
        } catch (AuraException e) {
            monitor.opError("getItems");
            throw new IOException(e);
        }
    }

    @Override
    public void getStats() throws IOException {
        try {
            long start = monitor.opStart();
            DataStore ds = mdb.getDataStore();
            // show the number of items of each type
            ds.ready();
            ds.getPrefixes().size();
            for (com.sun.labs.aura.datastore.Item.ItemType t :
                com.sun.labs.aura.datastore.Item.ItemType.values()) {

                    long count = ds.getItemCount(t);
            }
            for (com.sun.labs.aura.datastore.Attention.Type t :
                com.sun.labs.aura.datastore.Attention.Type.values()) {
                
                com.sun.labs.aura.datastore.AttentionConfig ac =
                        new com.sun.labs.aura.datastore.AttentionConfig();
                ac.setType(t);
                long count = ds.getAttentionCount(ac);
            }
            monitor.opFinish("getItems", start, 0);
        } catch (AuraException e) {
            monitor.opError("getItems");
            throw new IOException(e);
        }
    }

    @Override
    public void resetStats() {
        monitor.reset();
    }

    @Override
    public void showStats() {
        monitor.dumpAllStats();
    }

    @Override
    public List<Scored<Item>> tagSearch(String searchString, int count) throws IOException {
        try {
            long start = monitor.opStart();
            List<com.sun.labs.aura.util.Scored<ArtistTag>> scoredTags =
                    mdb.artistTagSearch(searchString, count);

            List<Scored<Item>> result = new ArrayList<Scored<Item>>();
            for (com.sun.labs.aura.util.Scored<ArtistTag> sa : scoredTags) {
                result.add(new Scored<Item>(new Item(sa.getItem().getKey(),
                                                     sa.getItem().getName()),
                                            sa.getScore()));
            }
            monitor.opFinish("tagSearch", start, 0);
            return result;
        } catch (AuraException e) {
            monitor.opError("tagSearch");
            throw new IOException(e);
        }
    }

}
