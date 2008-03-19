/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES..
 */

package com.sun.labs.aura.util;

import com.sun.kt.search.Posting;
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
