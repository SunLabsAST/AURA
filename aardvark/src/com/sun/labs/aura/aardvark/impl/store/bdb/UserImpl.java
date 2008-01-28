package com.sun.labs.aura.aardvark.impl.store.bdb;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.store.item.SimpleUser;
import java.util.HashSet;

/**
 * Provides a user implementation that is persistent in the BDB.
 */
@Persistent(version = 1)
public class UserImpl extends ItemImpl implements SimpleUser {

    /** Signal variable to indicate that this is a user, not a regular item */
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected boolean isUser = true;
    
    public UserImpl() {
        //
        // Default constructor for BDB
    }
    
    public UserImpl(String key, String name) {
        this.itemType = -1;
        this.key = key;
        this.name = name;
        this.attentionIDs = new HashSet<Long>();
        this.typeAndTimeAdded = new IntAndTimeKey(this.itemType,
                                                  System.currentTimeMillis());
    }
}
