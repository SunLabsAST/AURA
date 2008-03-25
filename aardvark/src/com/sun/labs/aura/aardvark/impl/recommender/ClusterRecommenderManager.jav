/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.kt.search.CompositeResultsFilter;
import com.sun.kt.search.DocumentVector;
import com.sun.kt.search.ResultsFilter;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.recommender.Recommendation;
import com.sun.labs.aura.recommender.RecommenderManager;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.KMeans;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ClusterRecommenderManager implements RecommenderManager, Configurable, AuraService {

    @ConfigComponent(type = DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";
    
    private DataStore dataStore;
    private Logger log;
    private final static int RECENT_STARRED = 20;
    private final static int SEED_SIZE = 2;
    private final static int NUM_RECS = 20;
    private final static int MAX_SKIP = 1000;

    public SortedSet<Recommendation> getRecommendations(User user) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
        SortedSet<Recommendation> resultSet =
                new TreeSet<Recommendation>(Recommendation.REVERSE);
        Set<String> titles = new HashSet<String>();
        try {
            // get a set of items that we've recently starred
            SortedSet<Attention> starredAttention =
                    dataStore.getLastAttentionForSource(user.getKey(),
                    Attention.Type.STARRED, RECENT_STARRED);
            
            //
            // Get the document vectors for each of the attention data.
            List<DocumentVector> dvs = new ArrayList<DocumentVector>();
            for(Attention a : starredAttention) {
                dvs.add(dataStore.getDocumentVector(a.getSourceKey(), "content"));
            }
            KMeans km = new KMeans(dvs, 4, 200);
            km.cluster();
            dvs = km.getClusters();

            // 
            // A filter that will only pass documents that have not been seen
            // recently and that are of the blog entry type.  This saves us 
            // having to post-filter things.
            ResultsFilter rf = new CompositeResultsFilter(getSkipSet(user),
                    new TypeFilter(ItemType.BLOGENTRY));
            
            for(DocumentVector dv : dvs) {
                dataStore.fin
            }


            // select a few documents from the starred set of items to serve
            // as the similarity seeds
            List<String> itemKeys = selectRandomItemKeys(starredAttention,
                    SEED_SIZE);
            t.mark("select random item keys");

            List<Scored<Item>> results = dataStore.findSimilar(itemKeys,
                    "content", NUM_RECS, rf);
            t.mark("findSimilar");

            //
            // Get the blog entries and return the set.  This is a change.
            for(Scored<Item> scoredItem : results) {
                BlogEntry blogEntry = new BlogEntry(scoredItem.getItem());
                String explanation = "Similar to items you like";
                resultSet.add(new Recommendation(scoredItem.getItem(),
                        scoredItem.getScore(), explanation));
                titles.add(blogEntry.getTitle());
                Attention attention = StoreFactory.newAttention(user,
                        scoredItem.getItem(), Attention.Type.VIEWED);
                dataStore.attend(attention);
                if(resultSet.size() >= NUM_RECS) {
                    break;
                }
            }
        } catch(AuraException ex) {
            log.log(Level.SEVERE, "Error getting recommendations", ex);
            ex.printStackTrace();
        } catch(Throwable thrown) {
            System.out.println("catch throwable exception " + thrown);
            thrown.printStackTrace();
        } finally {
            t.mark("done");
            return resultSet;
        }
    }

    /**
     * Gets the set of keys for things we've paid attention to
     * @param attentions the set of recent attentions
     * @return set of item ids to skip
     */
    private ResultsFilter getSkipSet(User user) throws AuraException, RemoteException {
        SortedSet<Attention> attentions =
                dataStore.getLastAttentionForSource(user.getKey(), null,
                MAX_SKIP);

        Set<String> retSet = new HashSet<String>();
        for(Attention att : attentions) {
            retSet.add(att.getTargetKey());
        }
        return new KeyExclusionFilter(retSet);
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        log = ps.getLogger();
    }

    public void start() {
    }

    public void stop() {
    }
}