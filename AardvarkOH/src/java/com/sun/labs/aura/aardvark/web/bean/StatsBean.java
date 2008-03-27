package com.sun.labs.aura.aardvark.web.bean;

import com.sun.labs.aura.aardvark.Stats;
import java.text.DecimalFormat;

/**
 * Aardvark stats container
 */
public class StatsBean {
    public static DecimalFormat longForm = new DecimalFormat("###,###,###,###");
    public static DecimalFormat doubForm = new DecimalFormat("###,###,###,###.#");
    
    
    protected long numFeeds;
    protected long numEntries;
    protected long numUsers;
    protected long numTaste;
    protected double entriesPerMin;
    
    public StatsBean() {
        
    }
    
    public StatsBean(Stats stats) {
        numFeeds = stats.getNumFeeds();
        numEntries = stats.getNumEntries();
        numUsers = stats.getNumUsers();
        numTaste = stats.getNumAttentionData();
        entriesPerMin = stats.getEntriesPerMin();
    }

    public String getNumFeeds() {
        return longForm.format(numFeeds);
    }

    public void setNumFeeds(long numFeeds) {
        this.numFeeds = numFeeds;
    }

    public String getNumEntries() {
        return longForm.format(numEntries);
    }

    public long getNumUsers() {
        return numUsers;
    }

    public String getNumTaste() {
        return longForm.format(numTaste);
    }

    public String getEntriesPerMin() {
        return doubForm.format(entriesPerMin);
    }

}
