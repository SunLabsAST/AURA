/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

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
