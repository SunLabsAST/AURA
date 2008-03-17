/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES.
 */
package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.recommender.RecommenderManager;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A recommender manager that returns the starred items for a user.
 */
public class SimpleRecommenderManager implements RecommenderManager, Configurable, AuraService {

    private DataStore dataStore;
    private Logger log;
    private final static int RECENT = 20;
    private final static int SEED_SIZE = 2;
    private final static int NUM_RECS = 20;

    public List<BlogEntry> getRecommendations(User user) throws RemoteException {
        List<BlogEntry> ret = new ArrayList<BlogEntry>();
        try {
            Set<Item> items = dataStore.getItems(user, Attention.Type.STARRED, Item.ItemType.BLOGENTRY);
            System.out.println("Items " + items.size());
            SortedSet<Attention> starredAttention = dataStore.getLastAttentionForSource(user.getKey(), Attention.Type.STARRED, RECENT);

            String[] itemKeys = selectRandomItemKeys(starredAttention, SEED_SIZE);
            SortedSet<Scored<Item>> results = new TreeSet<Scored<Item>>();

            for (String key : itemKeys) {
                results.addAll(dataStore.findSimilar(key, NUM_RECS / itemKeys.length));
            }

            for (Scored<Item> scoredItem : results) {
                if (scoredItem.getItem().getType() == ItemType.BLOGENTRY) {
                    ret.add(new BlogEntry(scoredItem.getItem()));
                }
            }
        } catch (AuraException ex) {
            System.out.println("Aura exception " +ex);
            log.log(Level.SEVERE, "Error getting recommendations", ex);
            ex.printStackTrace();
        } catch (Throwable t) {
            System.out.println("catch throwable exception " +t );
        }
        finally {
            System.out.println("Finally " + ret.size());
            return ret;
        }
    }

    /**
     * Given a set of attention data, return an array of keys to items chosen
     * at random from the set
     * @param attentionSet the set of attention data
     * @param num the number of item IDs to return
     * @return an array of num item ids
     */
    private String[] selectRandomItemKeys(SortedSet<Attention> attentionSet, int num) {
        List<Attention> list = new ArrayList<Attention>(attentionSet);
        Collections.shuffle(list);
        if (num > list.size()) {
            num = list.size();
        }

        String[] results = new String[num];

        for (int i = 0; i < num; i++) {
            results[i] = list.get(i).getTargetKey();
        }
        return results;
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        System.out.println("newProperties recommenderManager");
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        log = ps.getLogger();
    }

    public void start() {
        System.out.println("Starting recommenderManager");
    }

    public void stop() {
    }
    @ConfigComponent(type = DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";
}
