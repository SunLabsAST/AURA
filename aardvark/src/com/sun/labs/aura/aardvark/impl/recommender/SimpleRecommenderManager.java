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
import com.sun.labs.aura.recommender.RecommenderManager;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.recommender.Recommendation;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
    private final static int RECENT_STARRED = 20;
    private final static int SEED_SIZE = 2;
    private final static int NUM_RECS = 20;
    private final static int MAX_SKIP = 1000;

    public SortedSet<Recommendation>  getRecommendations(User user) throws RemoteException {
        SortedSet<Recommendation> resultSet = new TreeSet<Recommendation>(Recommendation.REVERSE);
        Set<String> titles = new HashSet<String>();
        try {
            // get a set of items that we've recently starred
            SortedSet<Attention> starredAttention = dataStore.getLastAttentionForSource(user.getKey(), Attention.Type.STARRED, RECENT_STARRED);

            // gets the set of entry ids that we should skip because they've
            // been used recently

            Set<String> skipSet = getSkipSet(user);

            // select a few documents from the starred set of items to serve
            // as the similarity seeds
            String[] itemKeys = selectRandomItemKeys(starredAttention, SEED_SIZE);
            SortedSet<Scored<Item>> results = new TreeSet<Scored<Item>>();

            // Get documents that are similar to the seeds
            for (String key : itemKeys) {
                results.addAll(dataStore.findSimilar(key, NUM_RECS));
            }

            // filter the list to eliminate docs that have already been attended
            // to, also don't include docs with the same title in the result set
            for (Scored<Item> scoredItem : results) {
                if (scoredItem.getItem().getType() == ItemType.BLOGENTRY) {
                    BlogEntry blogEntry = new BlogEntry(scoredItem.getItem());
                    if (!skipSet.contains(blogEntry.getKey())) {
                        String explanation = "";
                        resultSet.add(new Recommendation(scoredItem.getItem(), scoredItem.getScore(), explanation));
                        titles.add(blogEntry.getTitle());
                        Attention attention = StoreFactory.newAttention(user, 
                                    scoredItem.getItem(), Attention.Type.VIEWED);
                        dataStore.attend(attention);
                        if (resultSet.size() >= NUM_RECS) {
                            break;
                        }
                    }
                }
            }
        } catch (AuraException ex) {
            log.log(Level.SEVERE, "Error getting recommendations", ex);
            ex.printStackTrace();
        } catch (Throwable t) {
            System.out.println("catch throwable exception " +t );
        }
        finally {
            return resultSet;
        }
    }

    /**
     * Gets the set of keys for things we've paid attention to
     * @param attentions the set of recent attentions
     * @return set of item ids to skip
     */
    private Set<String> getSkipSet(User user) throws AuraException, RemoteException {
        SortedSet<Attention> attentions = dataStore.getLastAttentionForSource(user.getKey(), null, MAX_SKIP);
        Set<String> retSet = new HashSet<String>();
        for (Attention att : attentions) {
            retSet.add(att.getTargetKey());
        }
        return retSet;
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
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        log = ps.getLogger();
    }

    public void start() {
    }

    public void stop() {
    }
    @ConfigComponent(type = DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";
}
