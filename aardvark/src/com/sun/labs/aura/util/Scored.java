/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.util;

import com.sun.labs.aura.datastore.Item;
import java.io.Serializable;

import java.io.Serializable;
import java.util.logging.Logger;
import ngnova.util.Util;

/**
 * Items that have a score associated with them, as well as additional sorting
 * criteria.
 * 
 * @param T the type of the scored item.
 */
public class Scored<T> implements Comparable<Scored<T>>, Serializable {

    private T item;

    private double score;

    private Object[] sortVals;

    private boolean[] directions;

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
        this.sortVals = sortVals;
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
        return String.format("%.3f %s %s", score, item.toString(),
                Util.arrayToString(sortVals));
    }

    /**
     * Gets the score of the item
     * @return  the score for the scored item
     */
    public double getScore() {
        return score;
    }
    
    public Object[] getSortVals() {
        return sortVals;
    }
    
    public boolean[] getDirections() {
        return directions;
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
                int pre = cmp;
                cmp =  directions[i] ? -cmp : cmp;
                return cmp;
            }
            return 0;
        }
    }
}
