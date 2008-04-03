/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES.
 */
package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.kt.search.CompositeResultsFilter;
import com.sun.kt.search.ResultsFilter;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.util.SimpleTimer;
import com.sun.labs.aura.aardvark.util.Times;
import com.sun.labs.aura.recommender.RecommenderManager;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DBIterator;
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
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A recommender manager that returns the starred items for a user.
 */
public class SimpleRecommenderManager implements RecommenderManager, Configurable, AuraService {

    private final static int RECENT_STARRED = 20;
    private final static int SEED_SIZE = 2;
    private final static int MAX_SKIP = 1000;
    private final static int VALID_RECOMMENDATION_DAYS = 7;
    private DataStore dataStore;
    private Logger log;

    public List<Recommendation> getRecommendations(User user, int num) throws RemoteException {
        SimpleTimer t = new SimpleTimer(true);
        List<Recommendation> resultList = new ArrayList<Recommendation>();
        Set<String> titles = new HashSet<String>();

        try {
            // get a set of items that we've recently starred
            t.mark("init");
            List<Attention> starredAttention = dataStore.getLastAttentionForSource(
                    user.getKey(), Attention.Type.STARRED, RECENT_STARRED);
            t.mark("getLastAttention starred");

            // 
            // A filter that will only pass documents that have not been seen
            // recently and that are of the blog entry type.  This saves us 
            // having to post-filter things.

            CompositeResultsFilter rf = new CompositeResultsFilter(
                    getRecentItemFilter(user), new TypeFilter(ItemType.BLOGENTRY));
            rf.addFilter(getDateFilter(VALID_RECOMMENDATION_DAYS));

            t.mark("created filter");

            List<Scored<Item>> results = new ArrayList<Scored<Item>>();
            if (!starredAttention.isEmpty()) {
                if (num > 1) {
                    // select a few documents from the starred set of items to serve
                    // as the similarity seeds
                    List<String> itemKeys = selectRandomItemKeys(starredAttention, SEED_SIZE);
                    t.mark("select random item keys");

                    results = dataStore.findSimilar(itemKeys, "content", num * 2, rf);
                    t.mark("findSimilar");
                } else {
                    Random r = new Random();
                    int n = r.nextInt(starredAttention.size());
                    t.mark("select random item key");
                    results = dataStore.findSimilar(
                            starredAttention.get(n).getTargetKey(),
                            num,
                            rf);
                    t.mark("findSimilar");
                }
            }
            
            //
            // Get the blog entries and return the set.  This is a change.
            for (Scored<Item> scoredItem : results) {
                BlogEntry blogEntry = new BlogEntry(scoredItem.getItem());
                if (!titles.contains(blogEntry.getTitle())) {
                    String explanation = "Similar to items you like";
                    resultList.add(new Recommendation(scoredItem.getItem(),
                            scoredItem.getScore(), explanation));
                    titles.add(blogEntry.getTitle());
                    if (resultList.size() >= num) {
                        break;
                    }
                }
            }
            t.mark("results built - size: " + resultList.size());

            // we didn't find anything, so lets at least give some recent
            // items.
            if (resultList.size() == 0) {
                for (Item item : getSomeRecentItems(num * 2)) {
                    if (!titles.contains(item.getName())) {
                        resultList.add(new Recommendation(item, 1.0f, "latest entries"));
                        titles.add(item.getName());
                    }
                    if (resultList.size() >= num) {
                        break;
                    }
                }
                t.mark("fallback - size: " + resultList.size());
            }

            // add attention data for the user

            for (Recommendation recommendation : resultList) {
                Attention attention = StoreFactory.newAttention(user,
                        recommendation.getItem(), Attention.Type.VIEWED);
                dataStore.attend(attention);
            }
            t.mark("added attention");

        } catch (AuraException ex) {
            log.log(Level.SEVERE, "Error getting recommendations", ex);
            ex.printStackTrace();
        } catch (Throwable thrown) {
            System.out.println("catch throwable exception " + thrown);
            thrown.printStackTrace();
        } finally {
            t.mark("done");
            Collections.sort(resultList);
            Collections.reverse(resultList);
            return resultList;
        }
    }

    /**
     * Gets some blog entries from the database. Don't really care which ones
     * @param num the number to return
     * @return a list of items
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    private List<Item> getSomeRecentItems(int num) throws AuraException, RemoteException {
        List<Item> items = new ArrayList<Item>();
        DBIterator<Item> iter = dataStore.getItemsAddedSince(ItemType.BLOGENTRY, Times.getDaysAgo(VALID_RECOMMENDATION_DAYS));
        try {
            while (items.size() < num && iter.hasNext()) {
                items.add(iter.next());
            }
        } finally {
            iter.close();
        }
        return items;
    }

    /**
     * Gets a result filter that will skip items that should not
     * be recommended
     * @param user the user of interest
     * @return the result filter
     */
    private ResultsFilter getRecentItemFilter(User user) throws AuraException, RemoteException {
        List<Attention> attentions = dataStore.getLastAttentionForSource(user.getKey(), null, MAX_SKIP);

        Set<String> keySkipSet = new HashSet<String>();
        Set<String> titleSkipSet = new HashSet<String>();

        for (Attention att : attentions) {
            keySkipSet.add(att.getTargetKey());
            Item item = dataStore.getItem(att.getTargetKey());
            if (item != null) {
                titleSkipSet.add(item.getName());
            }
        }

        ResultsFilter keyFilter = new FieldExclusionFilter("aura-key", keySkipSet);
        ResultsFilter titleFilter = new FieldExclusionFilter("aura-name", titleSkipSet);
        return new CompositeResultsFilter(keyFilter, titleFilter);
    }

    /**
     * Return a result filter that includes only recent entries
     * @param days the number of days to include in the filter
     * @return the filter
     */
    private ResultsFilter getDateFilter(int days) {
        return new DateExclusionFilter(Times.getDaysAgo(days));
    }

    /**
     * Given a set of attention data, return an array of keys to items chosen
     * at random from the set
     * @param attentionSet the set of attention data
     * @param num the number of item IDs to return
     * @return an array of num item ids
     */
    private List<String> selectRandomItemKeys(List<Attention> attentionSet, int num) {
        List<Attention> list = new ArrayList<Attention>(attentionSet);
        Collections.shuffle(list);
        if (num > list.size()) {
            num = list.size();
        }

        List<String> results = new ArrayList<String>();

        for (int i = 0; i < num; i++) {
            results.add(list.get(i).getTargetKey());
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

