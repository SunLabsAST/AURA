
package com.sun.labs.aura.dbbrowser.client;

import java.io.Serializable;

/**
 * Attributes of an attention object
 */
public class AttnDesc implements Serializable {
    protected String srcKey;
    protected String targetKey;
    protected String type;
    protected String time;
    
    protected long queryTime;
    protected int numTotal;
    
    public AttnDesc() {
        
    }
    
    public AttnDesc(String src, String targ, String type, String time) {
        this.srcKey = src;
        this.targetKey = targ;
        this.type = type;
        this.time = time;
    }
    
    public AttnDesc(long queryTime, int numTotal) {
        this.queryTime = queryTime;
        this.numTotal = numTotal;
    }
    
    public String getType() {
        return type;
    }

    public String getSrcKey() {
        return srcKey;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public String getTime() {
        return time;
    }
    
    public long getQueryTime() {
        return queryTime;
    }
    
    public int getNumTotal() {
        return numTotal;
    }
}
