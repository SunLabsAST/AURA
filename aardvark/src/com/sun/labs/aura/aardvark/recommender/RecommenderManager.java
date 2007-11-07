package com.sun.labs.aura.aardvark.recommender;

import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.util.props.Configurable;
import java.util.List;

/**
 * An interface for managing the recommenders used by Aardvark.
 */
public interface RecommenderManager extends Configurable {

    /**
     * Gets a list of recommendations for a user.
     * @param user the user that we want recommendations for
     * @return a list of the entries that we want to recommend to the user.
     */
    public List<Entry> getRecommendations(User user);
    
    /**
     * Shuts down any recommenders that have open resources.
     */
    public void shutdown();
}
