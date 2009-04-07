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


import java.io.Serializable;
import java.util.Arrays;

/**
 * Items that have a score associated with them, as well as additional sorting
 * criteria.
 * 
 * @param T the type of the scored item.
 */
//public class Scored<T> implements Comparable<Scored<T>>, Serializable {
public class Scored<T> implements Serializable {

    private T item;

    private double score;

    private Serializable[] sortVals;

    private boolean[] directions;
    
    public double time;

    /**
     * Creates a scored item
     * @param item the item
     * @param score the score
     */
    public Scored(T item, double score) {
        this.item = item;
        this.score = score;
    }

    /**
     * Creates a scored item with associated field values for extended sorting
     * fun.
     */
    public Scored(T item, double score, Object[] sortVals, boolean[] directions) {
        this.item = item;
        this.score = score;
        this.sortVals = new Serializable[sortVals.length];
        for(int i = 0; i < sortVals.length; i++) {
            this.sortVals[i] = (Serializable) sortVals[i];
        }
        this.directions = directions;
    }
    
    /**
     * A copy constructor to copy from one type of scored item to another.
     * @param item
     * @param other
     */
    public Scored(T item, Scored<?> other) {
        this.item = item;
        this.score = other.score;
        this.sortVals = other.sortVals;
        this.directions = other.directions;
    }

    /**
     * Gets the item
     * @return the item
     */
    public T getItem() {
        return item;
    }
    
    public String toString() {
        return String.format("<%.3f, %s>", score, item.toString());
    }

    /**
     * Gets the score of the item
     * @return  the score for the scored item
     */
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
    
    public Object[] getSortVals() {
        if (sortVals != null) {
            return Arrays.copyOf(sortVals, sortVals.length);
        } else {
            return null;
        }
    }
    
    public boolean[] getDirections() {
        if (directions != null) {
            return Arrays.copyOf(directions, directions.length);
        } else {
            return null;
        }
    }

    public boolean equals(Object o) {
        if (o instanceof Scored) {
            Scored os = (Scored)o;
            if (score == os.score
                    && item.equals(os.item)
                    && time == os.time) {
                if (((directions != null) &&
                        (os.directions != null) &&
                        Arrays.equals(directions, os.directions))
                        ||
                        ((directions == null) && os.directions == null)) {
                        
                    if (((sortVals != null) && (os.sortVals != null) &&
                            Arrays.equals(sortVals, os.sortVals))
                            ||
                            ((sortVals == null) && os.sortVals == null)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public int hashCode() {
        return item.hashCode();
    }
    
    public int compareTo(Scored<T> o) {
        if(sortVals == null) {
            //
            // Sort just based on score.
            double v = getScore() - o.getScore();
            if(v < 0) {
                return -1;
            } else if(v > 0) {
                return 1;
            } else {
                return 0;
            }
        } else {
            //
            // Do the sort based on the fields.
            for(int i = 0; i < sortVals.length; i++) {

                //
                // Compare the field values.
                int cmp = ((Comparable) sortVals[i]).compareTo(o.sortVals[i]);

                //
                // No decision...
                if(cmp == 0) {
                    continue;
                }

                //
                // If this field is increasing, we can just use the comparison
                // that we just got.
                cmp =  directions[i] ? -cmp : cmp;
                return cmp;
            }
            return 0;
        }
    }
}
