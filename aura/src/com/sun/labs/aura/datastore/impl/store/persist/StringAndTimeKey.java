
package com.sun.labs.aura.datastore.impl.store.persist;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import java.io.Serializable;

/**
 * A persistent composite key class incorporating an Key and a timestamp
 */
@Persistent
public class StringAndTimeKey implements Serializable {
    @KeyField(1) private String key;
    @KeyField(2) private long timeStamp;
    
    public StringAndTimeKey() {
    }
    
    public StringAndTimeKey(String key, long timeStamp) {
        this.key = key;
        this.timeStamp = timeStamp;
    }
    
    public String getKey() {
        return key;
    }
    
    public long getTimeStamp() {
        return timeStamp;
    }    
}
