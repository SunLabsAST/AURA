package com.sun.labs.aura.aardvark.store;

/**
 * Some stats about what is in the item store
 */
public class ItemStoreStats {
    protected long numUsers;
    protected long numEntries;
    protected long numAttentions;
    protected long numFeeds;
    
    public ItemStoreStats(long numUsers,
                          long numEntries,
                          long numAttentions,
                          long numFeeds) {
        this.numUsers = numUsers;
        this.numEntries = numEntries;
        this.numAttentions = numAttentions;
        this.numFeeds = numFeeds;
    }
    
    public long getNumUsers() {
        return numUsers;
    }
    
    public long getNumEntries() {
        return numEntries;
    }
    
    public long getNumAttentions() {
        return numAttentions;
    }
    
    public long getNumFeeds() {
        return numFeeds;
    }
    
    public String toString() {
        return String.format("users: %d entries: %d attentions: %d feeds: %d", 
                numUsers, numEntries, numAttentions, numFeeds);
    }
}
