package com.sun.labs.aura.aardvark.impl.bdb.store.item;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.aardvark.impl.bdb.store.BerkeleyItemStore;
import com.sun.labs.aura.aardvark.impl.bdb.store.PersistentAttention;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.SimpleAttention;
import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 * Implementation of a persistent User through the Berkeley DB Java Edition.
 */
@Persistent
public class UserImpl extends ItemImpl implements User {

    /**
     * The isUser field exists only so that we can do a DB query to 
     * fetch only those elements that are users.  This will be faster
     * than having to fetch all Items and check each for User.
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected boolean isUser = true;


    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    protected long userAddedTime;
    
    /**
     * The recommendation feed key unqiue to this user.
     * This field is persistent.
     */
    protected String rFeedKey;
        
    /**
     * The last time data was fetched for this user.  This should probably
     * be moved to another table.
     */
    protected Long lastFetchTime;
    
    /**
     * All persisted objects must have a default constructor
     */
    public UserImpl() {
    }
    
    /**
     * Construct a user with a particular key (probably an Open ID username)
     * 
     * @param key the unqiue name of this user
     */
    public UserImpl(String key) {
        super(key);
        this.userAddedTime = System.currentTimeMillis();
    }
    
    public String getRecommenderFeedKey() {
        return rFeedKey;
    }

    public void setRecommenderFeedKey(String newKey) {
        this.rFeedKey = newKey;
    }

    /**
     * Adds a feed for this user.  This is a convenience method that is
     * equivalent to creating an Attention object and passing it to the
     * Item Store's attend method.
     * 
     * @param f the feed to be added
     * @param type the type of attention to be associated with the feed
     */
    public void addFeed(Feed f, Attention.Type type) throws AuraException {
        if (f.getID() <= 0) {
            throw new AuraException("Feed must be made persistent before it "+
                    "can be added to a user");
        }
        try {
            SimpleAttention attn = new SimpleAttention(this, f, type);
            BerkeleyItemStore store = BerkeleyItemStore.getItemStore();
            store.attend(attn);
        } catch(RemoteException rx) {
            throw new AuraException("Error adding feed " + f, rx);
        }
    }
    
    public long getLastFetchTime() {
        if (lastFetchTime != null) {
            return lastFetchTime;
        }
        return 0;
    }

    public void setLastFetchTime(long lastFetchTime) {
        this.lastFetchTime = lastFetchTime;
    }

    
    public String getTypeString() {
        return User.ITEM_TYPE;
    }
    
    public long getTimeStamp() {
        return userAddedTime;
    }

    public void setTimeStamp(long timeStamp) {
        this.userAddedTime = timeStamp;
    }

}
