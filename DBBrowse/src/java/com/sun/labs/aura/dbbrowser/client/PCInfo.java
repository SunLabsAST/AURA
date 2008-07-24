/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * PartitionCluster info
 */
public class PCInfo implements Serializable {
    /** The prefix this pc is responsible for */
    protected String prefix;
    
    /** A list of the replicants that make up this partition */
    protected List repInfos;
    
    public PCInfo() {
        repInfos = new ArrayList();
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public void addRepInfo(RepInfo rep) {
        repInfos.add(rep);
    }
    
    public List getRepInfos() {
        return repInfos;
    }
}
