/*
 * Project Aura,
 * 
 * Copyright (c) 2008,  Sun Microsystems Inc
 * See license.txt for licensing info.
 */
package com.sun.labs.aura.aardvark.impl.crawler;

import com.sun.kt.search.SearchEngine;
import com.sun.kt.search.SearchEngineException;
import com.sun.kt.search.SearchEngineFactory;
import com.sun.kt.search.SimpleIndexer;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class PersistentStringSet {

    private SearchEngine searchEngine;
    private Set<String> inMemoryCache;
    private final static int MAX_MEMORY_CACHE_SIZE = 10000;
    private final static String CONFIG_FILE = "persistentStringSetEngineConfig.xml";

    PersistentStringSet(String index) throws IOException {
        inMemoryCache = new HashSet();
        try {
            URL engineConfig = PersistentStringSet.class.getResource(CONFIG_FILE);
            if (engineConfig == null) {
                throw new IOException("Can't find configuration file " + CONFIG_FILE);
            }
            searchEngine = SearchEngineFactory.getSearchEngine(index, engineConfig);

            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                        public void run() {
                            close();
                        }
                    });
        } catch (SearchEngineException ex) {
            throw new IOException("Can't create search engine " + ex);
        }
    }

    /**
     * Close the set
     */
    public void close() {
        try {
            if (searchEngine != null) {
                searchEngine.close();
                searchEngine = null;
            }
        } catch (SearchEngineException ex) {
        }
    }

    public synchronized void add(String s) {
        if (!contains(s)) {
            inMemoryCache.add(s);
            if (inMemoryCache.size() > MAX_MEMORY_CACHE_SIZE) {
                SimpleIndexer indexer = searchEngine.getSimpleIndexer();
                for (String cachedString : inMemoryCache) {
                    indexer.startDocument(cachedString);
                    indexer.endDocument();
                }
                indexer.finish();
                inMemoryCache.clear();
            }
        }
    }


    public synchronized void remove(String s) {
        if (contains(s)) {
            if (!inMemoryCache.remove(s)) {
                if (searchEngine.isIndexed(s)) {
                    searchEngine.delete(s);
                }
            }
        }
    }

    public synchronized boolean contains(String s) {
        if (inMemoryCache.contains(s)) {
            return true;
        } else {
            return searchEngine.isIndexed(s);
        }
    }

    public static void main(String[] args) {
        int MAX_SET = 1000000;
        int MAX_TRIES = 1000000;
        int failures = 0;

        try {
            Random rng = new Random();
            PersistentStringSet pss = new PersistentStringSet("./index");
            long indexingStart = System.currentTimeMillis();
            if (false) {
            for (int i = 0; i < MAX_SET; i++) {
                pss.add("cached value " + i);
                if (i % 1000 == 0) {
                    System.out.println("added " + i);
                }
            }
            }

            long indexingTime = System.currentTimeMillis() - indexingStart;

            long queryStart = System.currentTimeMillis();
            for (int i = 0; i < MAX_TRIES; i++) {
                int val = rng.nextInt(MAX_SET * 2);
                boolean in = val < MAX_SET;
                if (in != pss.contains("cached value " + val)) {
                    failures++;
                }
                if (i % 10000 == 0) {
                    System.out.println("try " + i);
                }
            }

            long queryTime = System.currentTimeMillis() - queryStart;

            System.out.println("Tries: " + MAX_TRIES + " fails " 
                    + failures + " index: " + indexingTime + " query " + queryTime);
        } catch (IOException ex) {
            Logger.getLogger(PersistentStringSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
