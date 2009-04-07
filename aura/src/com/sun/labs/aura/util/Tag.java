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

package com.sun.labs.aura.util;

import com.sun.labs.minion.Posting;
import java.io.Serializable;

/**
 * Represents a tag - some free text with a frequency count.  This class extends
 * the <code>Posting</code> interface because we want to be able to treat tags
 * as words with associated frequencies while indexing.
 */
public class Tag extends Posting implements Serializable, Comparable {

    public Tag() {
    }
    
    /**
     * Create a tag with an initial count
     * @param name the tag text
     * @param count the frequency of the tag occurence
     */
    public Tag(String name, int count) {
        super(name, count);
    }

    /**
     * Gets the tag text
     * @return the tag text
     */
    public String getName() {
        return term;
    }
    
    /**
     * Gets the tag frequency count
     * @return the tag frequency count
     */
    public int getCount() {
        return freq;
    }
    
    /**
     * Accumulate more tag counts
     * @param count the count to accumulate
     */
    public void accum(int count) {
        this.freq += count;
    }

    public int compareTo(Object o) {
        Tag other = (Tag) o;
        return getCount() - other.getCount();
    }
}
