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
import java.util.HashMap;
import java.util.HashSet;

/**
 * A holder for the stats from a particular replicant.
 */
public class RepStats implements Serializable {

    protected HashMap<String,Double> callsPerSec = new HashMap<String,Double>();
    protected HashMap<String,Double> avgCallTime = new HashMap<String,Double>();
    protected HashSet<String> names = new HashSet<String>();
    
    public boolean contains(String name) {
        return names.contains(name);
    }
    
    public void putRate(String name, Double cps) {
        names.add(name);
        callsPerSec.put(name, cps);
    }
    
    public Double getRate(String name) {
        return callsPerSec.get(name);
    }
    
    public void putTime(String name, Double time) {
        avgCallTime.put(name, time);
    }
    
    public Double getTime(String name) {
        return avgCallTime.get(name);
    }

}
