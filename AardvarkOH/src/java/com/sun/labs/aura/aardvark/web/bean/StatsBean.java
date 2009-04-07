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

package com.sun.labs.aura.aardvark.web.bean;

import com.sun.labs.aura.aardvark.Stats;
import java.text.DecimalFormat;

/**
 * Aardvark stats container
 */
public class StatsBean {
    public static DecimalFormat longForm = new DecimalFormat("###,###,###,###");
    public static DecimalFormat doubForm = new DecimalFormat("###,###,###,###.#");
    
    
    protected long numFeeds;
    protected long numEntries;
    protected long numUsers;
    protected long numTaste;
    protected double entriesPerMin;
    
    public StatsBean() {
        
    }
    
    public StatsBean(Stats stats) {
        numFeeds = stats.getNumFeeds();
        numEntries = stats.getNumEntries();
        numUsers = stats.getNumUsers();
        numTaste = stats.getNumAttentionData();
        entriesPerMin = stats.getEntriesPerMin();
    }

    public String getNumFeeds() {
        return longForm.format(numFeeds);
    }

    public void setNumFeeds(long numFeeds) {
        this.numFeeds = numFeeds;
    }

    public String getNumEntries() {
        return longForm.format(numEntries);
    }

    public long getNumUsers() {
        return numUsers;
    }

    public String getNumTaste() {
        return longForm.format(numTaste);
    }

    public String getEntriesPerMin() {
        return doubForm.format(entriesPerMin);
    }

}
