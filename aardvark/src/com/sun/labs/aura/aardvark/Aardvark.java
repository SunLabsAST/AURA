/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES..
 */

package com.sun.labs.aura.aardvark;

import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.util.props.Component;
import com.sun.syndication.feed.synd.SyndFeed;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

/**
 *
 */
public interface Aardvark extends Component, Remote {

    /**
     * Adds a new fed to the system
     * @param feedURL the feed to add
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    void addFeed(String feedURL) throws AuraException, RemoteException;


    /**
     * Adds a feed of a particular type for a user
     * @param user the user
     * @param feedURL the url of the feed to add
     * @param type the type of attention the user pays to the URL
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    void addUserFeed(User user, String feedURL, Attention.Type type) throws AuraException, RemoteException;

    /**
     * Enrolls a user in the recommender
     * @param openID the openID of the user
     * @return the user
     * @throws AuraException if the user is already enrolled or a problem occurs while enrolling the user
     */
    User enrollUser(String openID) throws AuraException, RemoteException;

    /**
     * Enrolls a user in the recommender
     * @param openID the openID of the user
     * @param feed the starred item feed of the user
     * @return the user
     * @throws AuraException
     */
    User enrollUser(String openID, String feed) throws AuraException, RemoteException;

    List<Attention> getAttentionData(User user) throws AuraException, RemoteException;
    
    /**
     * Get the feeds of a particular type associated with a user.
     * @param user the user whose feeds we want
     * @param type the type of attention that we want the feeds to have
     * @return a list of the feeds of the given type for the given user.
     */
    Set<BlogFeed> getFeeds(User user, Attention.Type type) throws AuraException, RemoteException;

    /**
     * Gets the feed for the particular user
     * @param user the user
     * @return the feed
     */
    SyndFeed getRecommendedFeed(User user) throws AuraException, RemoteException;

    /**
     * Returns interesting stats about aardvark
     * @return the stats
     */
    Stats getStats() throws AuraException, RemoteException;

    /**
     * Gets the user from the openID
     * @param openID the openID for the user
     * @return the user or null if the user doesn't exist
     */
    User getUser(String openID) throws AuraException, RemoteException;

}
