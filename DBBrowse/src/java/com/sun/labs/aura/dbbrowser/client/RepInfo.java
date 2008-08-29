
package com.sun.labs.aura.dbbrowser.client;

import java.io.Serializable;

/**
 * Info that rerpesents a Replicant
 */
public class RepInfo implements Serializable {
    protected long dbSize = 0;
    protected long indexSize = 0;
    protected String prefix;
    
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
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
