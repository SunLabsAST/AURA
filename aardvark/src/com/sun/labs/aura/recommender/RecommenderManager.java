/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES.
 */
package com.sun.labs.aura.recommender;

import com.sun.labs.aura.datastore.User;
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
     * @param num the number of recommendations to generate
     * @return a list of the entries that we want to recommend to the user.
     */
    public List<Recommendation> getRecommendations(User user, int num) throws RemoteException;

    /**
     * Gets a list of recommendations for a user.
     * @param user the user that we want recommendations for
     * @param recommenderProfile controls how recommendations are generated
     * @param num the number of recommendations to generate
     * @return a list of the entries that we want to recommend to the user.
     */
    public List<Recommendation> getRecommendations(User user, 
                RecommenderProfile recommenderProfile, int m) throws RemoteException;
    
}
