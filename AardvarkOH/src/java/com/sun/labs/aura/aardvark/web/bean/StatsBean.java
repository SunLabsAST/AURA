package com.sun.labs.aura.aardvark.web.bean;

import com.sun.labs.aura.aardvark.Stats;
import java.text.DecimalFormat;

/**
 * Aardvark stats container
 */
public class StatsBean {
    public static DecimalFormat df = new DecimalFormat("###,###,###,###");
    
    protected long numFeeds;
    protected long numEntries;
    protected long numUsers;
    protected long numTaste;
    protected long entriesPerMin;
    
    public StatsBean() {
        
    }
    
    public StatsBean(Stats stats) {
        numFeeds = stats.getNumFeeds();
        numEntries = stats.getNumEntries();
        numUsers = stats.getNumUsers();
        numTaste = stats.getNumAttentionData();
    }

    public String getNumFeeds() {
        return df.format(numFeeds);
    }

    public void setNumFeeds(long numFeeds) {
        this.numFeeds = numFeeds;
    }

    public String getNumEntries() {
        return df.format(numEntries);
    }

    public void setNumEntries(long numEntries) {
        this.numEntries = numEntries;
    }

    public long getNumUsers() {
        return numUsers;
    }

    public void setNumUsers(long numUsers) {
        this.numUsers = numUsers;
    }

    public String getNumTaste() {
        return df.format(numTaste);
    }

    public void setNumTaste(long numTaste) {
        this.numTaste = numTaste;
    }

    public long getEntriesPerMin() {
        return entriesPerMin;
    }

    public void setEntriesPerMin(long entriesPerMin) {
        this.entriesPerMin = entriesPerMin;
    }
}
