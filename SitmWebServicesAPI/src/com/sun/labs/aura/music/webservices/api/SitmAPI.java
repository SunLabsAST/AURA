/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.webservices.api;

import java.io.IOException;
import java.util.List;
import org.w3c.dom.Document;

/**
 *
 * @author ja151348
 */
public abstract class SitmAPI {

    public abstract List<Scored<Item>> artistSearch(String searchString) throws IOException;

    public abstract List<Scored<Item>> artistSocialTags(String key, int count) throws IOException;

    public abstract long checkStatus(String msg, Document doc) throws IOException;

    public abstract List<Scored<Item>> findSimilarArtistFromWordCloud(String cloud, int count) throws IOException;

    public abstract List<Scored<Item>> findSimilarArtistTags(String key, int count) throws IOException;

    public abstract List<Scored<Item>> findSimilarArtistsByKey(String key, int count) throws IOException;

    public abstract List<Scored<Item>> findSimilarArtistsByName(String name, int count) throws IOException;

    public abstract List<Item> getArtistTags(int count) throws IOException;

    public abstract List<Item> getArtists(int count) throws IOException;

    public abstract Item getItem(String key, boolean compact) throws IOException;

    public abstract List<Item> getItems(List<String> keys, boolean compact) throws IOException;

    public abstract void getStats() throws IOException;

    public abstract void resetStats();

    public abstract void showStats();

    public abstract List<Scored<Item>> tagSearch(String searchString, int count) throws IOException;

    public static SitmAPI getSitmAPI(String host, boolean trace, boolean debug, boolean periodicDump) throws IOException {
        return new SitmAPIImpl(host, trace, debug, periodicDump);
    }
}
