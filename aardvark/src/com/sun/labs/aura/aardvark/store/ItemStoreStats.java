/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.store;

/**
 *
 * @author ja151348
 */
public class ItemStoreStats {
    protected long numUsers;
    protected long numEntries;
    protected long numAttentions;
    
    public ItemStoreStats(long numUsers,
                          long numEntries,
                          long numAttentions) {
        this.numUsers = numUsers;
        this.numEntries = numEntries;
        this.numAttentions = numAttentions;
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
}
