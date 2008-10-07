/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PartitionCluster info
 */
public class PCInfo implements Serializable {
    /** The prefix this pc is responsible for */
    protected String prefix;
    
    /** A list of the replicants that make up this partition
     * @gwt.typeArgs <com.sun.labs.aura.dbbrowser.client.viz.RepInfo>
     */
    protected List repInfos;
    
    protected long numItems;
    protected long numAttention;
    
    /**
     * A map of item type names to counts of each type
     * @gwt.typeArgs <java.lang.String,java.lang.Long>
     */
    protected Map typeToCountMap;
    
    public PCInfo() {
        repInfos = new ArrayList();
        typeToCountMap = new HashMap();
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
    
    public Map getTypeToCountMap() {
        return typeToCountMap;
    }
    
    public void setTypeToCountMap(Map m) {
        typeToCountMap = m;
    }
    
    public void addRepInfo(RepInfo rep) {
        repInfos.add(rep);
    }
    
    public List getRepInfos() {
        return repInfos;
    }
}
