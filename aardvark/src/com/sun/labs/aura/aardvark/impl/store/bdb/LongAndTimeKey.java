
package com.sun.labs.aura.aardvark.impl.store.bdb;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

/**
 * A persistent composite key class incorporating an ID and a timestamp
 */
@Persistent
public class LongAndTimeKey {
    @KeyField(1) private long ID;
    @KeyField(2) private long timeStamp;
    
    public LongAndTimeKey() {
    }
    
    public LongAndTimeKey(long ID, long timeStamp) {
        this.ID = ID;
        this.timeStamp = timeStamp;
    }
    
    public long getID() {
        return ID;
    }
    
    public long getTimeStamp() {
        return timeStamp;
    }    
}
