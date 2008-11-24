
package com.sun.labs.aura.dbbrowser.client.viz;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A holder for the stats from a particular replicant.
 */
public class RepStats implements Serializable {

    protected HashMap<String,Double> callsPerSec = new HashMap<String,Double>();
    protected HashMap<String,Double> avgCallTime = new HashMap<String,Double>();
    protected HashSet<String> names = new HashSet<String>();
    
    public boolean contains(String name) {
        return names.contains(name);
    }
    
    public void putRate(String name, Double cps) {
        names.add(name);
        callsPerSec.put(name, cps);
    }
    
    public Double getRate(String name) {
        return callsPerSec.get(name);
    }
    
    public void putTime(String name, Double time) {
        avgCallTime.put(name, time);
    }
    
    public Double getTime(String name) {
        return avgCallTime.get(name);
    }

}
