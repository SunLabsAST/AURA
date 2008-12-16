/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
