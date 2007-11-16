/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.labs.aura.aardvark.recommender.RecommenderManager;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.List;

/**
 * A recommender manager for recommenders that are based on the search engine.
 */
public class SearchRecommenderManager implements RecommenderManager {
    
    private EntryContentEngine contentEngine;
    
    public List<Entry> getRecommendations(User user) {
        return contentEngine.getRecommendations(user);
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        contentEngine = (EntryContentEngine) ps.getComponent(PROP_ENTRY_ENGINE);
    }
    
    public void shutdown() {
        contentEngine.shutdown();
    }

    @ConfigComponent(type=com.sun.labs.aura.aardvark.impl.recommender.EntryContentEngine.class)
    public static final String PROP_ENTRY_ENGINE = "entryEngine";

}