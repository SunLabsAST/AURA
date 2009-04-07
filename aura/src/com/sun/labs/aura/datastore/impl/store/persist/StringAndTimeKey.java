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
