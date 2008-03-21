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
import java.util.Set;
import java.util.SortedSet;

/**
 *
 */
public interface Aardvark extends Component, Remote {

    /**
     * Adds a new feed to the system. The feed will be crawled periodically
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
     * Update the version of the user stored in the datastore
     * 
     * @param user the user to update
     * @return
     * @throws AuraException if there was an error
     */
    public User updateUser(User user) throws AuraException, RemoteException;
    
    /**
     * Deletes a user from the data store
     * 
     * @param user the user to delete
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void deleteUser(User user) throws AuraException, RemoteException;
    
    /**
     * Gets the attention data for a user
     * @param user the user of interest
     * @param type the type of attention data of interest (null indicates all)
     * @return the set of attention data (sorted by timestamp)
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    SortedSet<Attention> getLastAttentionData(User user, Attention.Type type, 
                int count) throws AuraException, RemoteException;
    
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
