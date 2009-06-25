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

package com.sun.labs.aura.dbbrowser.client.query;

import java.io.Serializable;

/**
 * Attributes of an attention object
 */
public class AttnDesc implements Serializable {
    protected String srcKey;
    protected String targetKey;
    protected String type;
    protected String time;
    protected String strVal;
    protected Long longVal;

    protected long queryTime;
    protected int numTotal;
    
    public AttnDesc() {
        
    }

    public AttnDesc(String src, String targ, String type, String time, String strVal, long longVal) {
        this(src, targ, type, time);
        this.strVal = strVal;
        this.longVal = longVal;
    }

    public AttnDesc(String src, String targ, String type, String time) {
        this.srcKey = src;
        this.targetKey = targ;
        this.type = type;
        this.time = time;
    }
    
    public AttnDesc(long queryTime, int numTotal) {
        this.queryTime = queryTime;
        this.numTotal = numTotal;
    }
    
    public String getType() {
        return type;
    }

    public String getSrcKey() {
        return srcKey;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public String getTime() {
        return time;
    }
    
    public long getQueryTime() {
        return queryTime;
    }
    
    public int getNumTotal() {
        return numTotal;
    }

    public String getStrVal() {
        return strVal;
    }

    public Long getLongVal() {
        return longVal;
    }
}
