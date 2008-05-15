package com.sun.labs.aura.datastore.impl.store.persist;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.datastore.User;

/**
 * Provides a user implementation that is persistent in the BDB.
 */
@Persistent(version = 2)
public class UserImpl extends ItemImpl implements User {

    /** Signal variable to indicate that this is a user, not a regular item */
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected boolean isUser = true;
    
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    protected String randStr = "";
    
    public UserImpl() {
        //
        // Default constructor for BDB
    }
    
    public UserImpl(String key, String name) {
        this.itemType = ItemType.USER.ordinal();
        this.key = key;
        this.name = name;
        this.typeAndTimeAdded = new IntAndTimeKey(this.itemType,
                                                  System.currentTimeMillis());
    }

    public String getUserRandString() {
        return randStr;
    }

    public void setUserRandString(String random) {
        this.randStr = random;
    }
}
