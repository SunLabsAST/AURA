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
    
    /** A list of the replicants that make up this partition
     * @gwt.typeArgs <com.sun.labs.aura.dbbrowser.client.RepInfo>
     */
    protected List repInfos;
    
    protected long numItems;
    protected long numAttention;
    
    public PCInfo() {
        repInfos = new ArrayList();
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public long getNumItems() {
        return numItems;
    }
    
    public void setNumItems(long numItems) {
        this.numItems = numItems;
    }
    
    public long getNumAttention() {
        return numAttention;
    }
    
    public void setNumAttention(long numAttention) {
        this.numAttention = numAttention;
    }
    
    public void addRepInfo(RepInfo rep) {
        repInfos.add(rep);
    }
    
    public List getRepInfos() {
        return repInfos;
    }
}
