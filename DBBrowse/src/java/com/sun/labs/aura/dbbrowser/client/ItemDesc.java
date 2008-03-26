
package com.sun.labs.aura.dbbrowser.client;

import java.io.Serializable;

/**
 * A description of a type
 */
public class ItemDesc implements Serializable {
    protected String name;
    protected String key;
    protected String type;
    
    protected long timeInMS;
    
    public ItemDesc() {
        
    }
    
    public ItemDesc(long time) {
        timeInMS = time;
    }
    
    public ItemDesc(String name, String key, String type) {
        this.name = name;
        this.key = key;
        this.type = type;
    }
    
    
    public String getName() {
        return name;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getType() {
        return type;
    }
    
    public long getQueryTime() {
        return timeInMS;
    }
}
