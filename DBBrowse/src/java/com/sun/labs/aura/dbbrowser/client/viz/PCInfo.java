/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
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
     */
    protected List<RepInfo> repInfos;
    
    protected long numItems;
    protected long numAttention;
    
    /**
     * A map of item type names to counts of each type
     */
    protected Map<String,Long> typeToCountMap;
    
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
