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
import com.sun.labs.aura.datastore.Item;
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
     * Adds a new feed to the system. The feed will be crawled periodically
     * @param feedURL the feed to add
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public void addFeed(String feedURL) throws AuraException, RemoteException;

    /**
     * Add an OPML file from an input stream.  This will enroll each of the
     * feeds in the opml file.
     * 
     * @param opmlBytes a byte array that contains opml
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void addOPML(byte[] opmlBytes) throws AuraException, RemoteException;
    
    /**
     * Adds a feed of a particular type for a user
     * @param user the user
     * @param feedURL the url of the feed to add
     * @param type the type of attention the user pays to the URL
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public void addUserFeed(User user, String feedURL, Attention.Type type) throws AuraException, RemoteException;

    /**
     * Remove a feed so that it is no longer crawled.  Future entries will not
     * be used for recommendation (and current ones will eventually fall out
     * of the time filter).
     * 
     * @param user the user to delete the attention for
     * @param feedURL the url of the feed
     * @param type the type of the feed
     * 
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void removeUserFeed(User user, String feedURL, Attention.Type type)
            throws AuraException, RemoteException;
    /**
     * Enrolls a user in the recommender
     * @param openID the openID of the user
     * @return the user
     * @throws AuraException if the user is already enrolled or a problem occurs while enrolling the user
     */
    public User enrollUser(String openID) throws AuraException, RemoteException;

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
     * @return the list of attention data (sorted by timestamp)
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Attention> getLastAttentionData(User user, Attention.Type type, 
                int count) throws AuraException, RemoteException;
    
    /**
     * Gets all the stored attention data for a user
     * @param user the user of interest
     * @return the list of all attention data
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<Attention> getAttention(User user)
            throws AuraException, RemoteException;
    
    /**
     * Get the feeds of a particular type associated with a user.
     * @param user the user whose feeds we want
     * @param type the type of attention that we want the feeds to have
     * @return a list of the feeds of the given type for the given user.
     */
    public Set<BlogFeed> getFeeds(User user, Attention.Type type) throws AuraException, RemoteException;

    /**
     * Gets the feed for the particular user
     * @param user the user
     * @return the feed
     */
    public SyndFeed getRecommendedFeed(User user) throws AuraException, RemoteException;

    /**
     * Gets the feed for the particular user
     * @param user the user
     * @param num the number of entries
     * @return the feed
     */
    public SyndFeed getRecommendedFeed(User user, int num) throws AuraException, RemoteException;

    /**
     * Returns interesting stats about aardvark
     * @return the stats
     */
    public Stats getStats() throws AuraException, RemoteException;

    /**
     * Gets the user from the openID
     * @param openID the openID for the user
     * @return the user or null if the user doesn't exist
     */
    public User getUser(String openID) throws AuraException, RemoteException;

    /**
     * Gets the item from the datastore with the matching key.
     * 
     * @param itemKey the key of the item
     * @return the item, or null if there is no item with the given key
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public Item getItem(String itemKey) throws AuraException, RemoteException;
    
    /**
     * Get a user based on the previously-generated random string for that
     * user.
     * 
     * @param randStr the complete random string for a user
     * @return the matching user
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public User getUserByRandomString(String randStr) throws AuraException, RemoteException;
}
