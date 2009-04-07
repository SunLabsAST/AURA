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

package com.sun.labs.aura.music.web.lastfm;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author plamere
 */
public class LastItem implements Serializable {
    private String name;
    private String mbid;
    private int frequency;
    
    public final static Comparator<LastItem> FREQ_ORDER = new Comparator<LastItem>() {
        public int compare(LastItem o1, LastItem o2) {
            return o1.getFreq() - o2.getFreq();
        }
    };

    public final static Comparator<LastItem> ALPHA_ORDER = new Comparator<LastItem>() {
        public int compare(LastItem o1, LastItem o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };

    public LastItem(String name, String mbid, int frequency) {
        this.name = name;
        this.frequency = frequency;
        this.mbid = mbid;
    }

    public LastItem(String name, int frequency) {
        this.name = name;
        this.frequency = frequency;
    }

    public int getFreq() {
        return frequency;
    }

    public String getName() {
        return name;
    }

    public String getMBID() {
        return mbid;
    }

    public String toString() {
        return name + " " + getFreq();
    }
}
