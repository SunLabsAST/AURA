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

package com.sun.labs.aura.service.persist;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.datastore.impl.Util;
import com.sun.labs.aura.util.Times;
import java.io.Serializable;
import java.util.Date;
import java.util.Random;

/**
 *
 */
@Entity(version = 1)
public class SessionKey implements Serializable {
    private static final long serialVersionUID = 1;

    private static Random random = new Random();

    @PrimaryKey
    protected String sessionKey;

    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected String userKey;

    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected String appKey;

    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected long expDate;

    /**
     * Default constructor for reflection
     */
    public SessionKey() {
        
    }

    /**
     * Create a new session key, generating the key value
     *
     * @param userKey the key of the User involved
     * @param appKey the key of the App involved
     */
    public SessionKey(String userKey, String appKey) {
        this.userKey = userKey;
        this.appKey = appKey;
        Date now = new Date();
        expDate = now.getTime() + Times.ONE_MONTH;
        sessionKey = Util.toHexString(random.nextInt());
    }

    /**
     * @return the sessionKey
     */
    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * @return the userKey
     */
    public String getUserKey() {
        return userKey;
    }

    /**
     * @return the appKey
     */
    public String getAppKey() {
        return appKey;
    }

    public boolean isExpired() {
        Date now = new Date();
        if (now.getTime() > expDate) {
            return true;
        }
        return false;
    }

    public String toString() {
        return userKey + " : " + appKey + " (" + sessionKey + ")";
    }
}
