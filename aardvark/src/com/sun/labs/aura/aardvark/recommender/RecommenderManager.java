package com.sun.labs.aura.aardvark.recommender;

import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * An interface for managing the recommenders used by Aardvark.
 */
public interface RecommenderManager extends Component, Remote {

    /**
     * Gets a list of recommendations for a user.
     * @param user the user that we want recommendations for
     * @return a list of the entries that we want to recommend to the user.
     */
    public List<Entry> getRecommendations(User user) throws RemoteException;
    
}
