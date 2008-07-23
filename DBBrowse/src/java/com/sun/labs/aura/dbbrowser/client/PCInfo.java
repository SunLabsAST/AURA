/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import java.io.Serializable;

/**
 * PartitionCluster info
 */
public class PCInfo implements Serializable {
    
    protected String prefix;
    
    public PCInfo() {
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
