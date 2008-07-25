/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import java.io.Serializable;

/**
 * Info that rerpesents a Replicant
 */
public class RepInfo implements Serializable {
    protected long dbSize;
    protected long indexSize;
    
    public RepInfo() {
        
    }
    
    public long getDBSize() {
        return dbSize;
    }

    public void setDBSize(long dbSize) {
        this.dbSize = dbSize;
    }
    
    public long getIndexSize() {
        return indexSize;
    }
    
    public void setIndexSize(long indexSize) {
        this.indexSize = indexSize;
    }
}
