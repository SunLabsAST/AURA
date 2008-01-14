/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark;

import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.Component;
import com.sun.syndication.feed.synd.SyndFeed;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author stgreen
 */
public interface Aardvark extends Component, Remote {

    /**
     * Adds a new fed to the system
     * @param feedURL the feed to add
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    void addFeed(URL feedURL) throws AuraException, RemoteException;

    /**
     * Adds a new feed to the system
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
    void addUserFeed(User user, URL feedURL, Attention.Type type) throws AuraException, RemoteException;

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
