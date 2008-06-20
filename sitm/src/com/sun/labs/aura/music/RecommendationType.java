/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author plamere
 */
public interface RecommendationType {
    /**
     * Gets the name of the recommendation type
     * @return the name of the recommendation type
     */
    String getName();

    /**
     * Gets a detailed description of the recommendation type
     * @return the description of the recommendation type
     */
    String getDescription();

    /**
     * Gets recommendations
     * @param listener the listener of interest
     * @param count the number of recommendations to generate
     * @param the recommendation profile
     * @return  the ordered list of recommendations
     */
    List<Recommendation> getRecommendations(Listener listener, int count, RecommendationProfile rp) 
                throws AuraException, RemoteException;


    /**
     *  Returns the type of item that is recommended
     * @return the type of item that is recommendec
     */
    ItemType getType();
}
