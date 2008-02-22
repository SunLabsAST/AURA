/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES..
 */

package com.sun.labs.aura.aardvark;

import java.io.Serializable;

/**
 * Represents a tag - some free text with a frequency count
 */
public class Tag implements Serializable, Comparable {
    private String name;
    private int count;

    /**
     * Create a tag with an initial count
     * @param name the tag text
     * @param count the frequency of the tag occurence
     */
    public Tag(String name, int count) {
        this.name = name;
        this.count = count;
    }

    /**
     * Gets the tag text
     * @return the tag text
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the tag frequency count
     * @return the tag frequency count
     */
    public int getCount() {
        return count;
    }

    /**
     * Accumulate more tag counts
     * @param count the count to accumulate
     */
    public void accum(int count) {
        this.count += count;
    }

    public int compareTo(Object o) {
        Tag other = (Tag) o;
        return getCount() - other.getCount();
    }
}
