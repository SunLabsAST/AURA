/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;

import java.io.Serializable;

/**
 * Class that holds onto info about a Data Store Head.. maybe some stats?
 */
public class DSHInfo implements Serializable {
    protected boolean isReady = true;
    
    protected String name;
    
    public DSHInfo() {
    }
    
    public String getName() {
        return "DataStoreHead";
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isReady() {
        return isReady;
    }
    
    public void setIsReady(boolean isReady) {
        this.isReady = isReady;
    }
}
