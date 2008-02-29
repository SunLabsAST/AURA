
package com.sun.labs.aura.dbbrowser.client;

import java.io.Serializable;

/**
 * A description of a type
 */
public class ItemDesc implements Serializable {
    protected String name;
    protected String key;
    protected String type;
    
    public ItemDesc() {
        
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
}
