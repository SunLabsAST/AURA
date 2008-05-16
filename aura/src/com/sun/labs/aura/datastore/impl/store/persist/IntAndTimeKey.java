
package com.sun.labs.aura.datastore.impl.store.persist;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import java.io.Serializable;

/**
 * A persistent composite key class incorporating an int and a timestamp
 */
@Persistent
public class IntAndTimeKey implements Serializable {
    @KeyField(1) private int value;
    @KeyField(2) private long timeStamp;
    
    public IntAndTimeKey() {
    }
    
    public IntAndTimeKey(int value, long timeStamp) {
        this.value = value;
        this.timeStamp = timeStamp;
    }
    
    public int getInt() {
        return value;
    }
    
    public long getTimeStamp() {
        return timeStamp;
    }    
}
