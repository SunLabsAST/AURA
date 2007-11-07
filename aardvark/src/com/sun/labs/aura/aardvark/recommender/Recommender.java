package com.sun.labs.aura.aardvark.recommender;

import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.User;
import java.util.List;

/**
 * An interface for recommenders.
 */
public interface Recommender {
    
    /**
     * Gets a list of recommendations for a user.
     * @param user the user that we want recommendations for
     * @return a list of the entries that we want to recommend to the user.
     */
    List<Entry> getRecommendations(User user);

}
