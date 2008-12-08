
package com.sun.labs.aura.service;

import com.sun.labs.aura.service.persist.SessionKey;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface for a service that handles login-related activity such as
 * user cookies, app keys, and sessions.  Sessions should be persistent.
 */
public interface LoginService extends Remote, Component {

    /**
     * Creates a new session key for a given user/app pair.  This should be
     * used if a session does not exist or has expired.
     * 
     * @param userKey the key of the user involved
     * @param appKey the key of the application involved
     * @return a new session key
     */
    public SessionKey newUserSessionKey(String userKey, String appKey)
            throws RemoteException, AuraException;

    /**
     * Gets a session key for a user using a particular app if one exists.
     * 
     * @param userKey the key of the user involved
     * @param appKey the key of the application involved
     * @return a session key if one exists, otherwise returns null
     */
    public SessionKey getUserSessionKey(String userKey, String appKey)
            throws RemoteException;


    /**
     * Gets a session key object for a user given an existing key string
     *
     * @param sessionKey the session key string
     * @return the session key object
     */
    public SessionKey getUserSessionKey(String sessionKey)
            throws RemoteException;
}
