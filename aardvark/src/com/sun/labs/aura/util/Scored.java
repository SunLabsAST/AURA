/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util;

import java.io.Serializable;

/**
 * Item that have a score associated with them
 * @param T the type of the scored item.
 */
public class Scored<T> implements Comparable<Scored<T>>, Serializable {

    private T item;

    private double score;

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
     * Gets the item
     * @return the item
     */
    public T getItem() {
        return item;
    }

    /**
     * Gets the score of the item
     * @return  the score for the scored item
     */
    public double getScore() {
        return score;
    }

    public int compareTo(Scored<T> o) {
        double v = getScore() - o.getScore();
        if(v < 0) {
            return -1;
        } else if(v > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
