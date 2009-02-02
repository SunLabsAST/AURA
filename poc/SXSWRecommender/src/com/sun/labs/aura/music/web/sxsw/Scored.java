/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.sxsw;


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
    
    @Override
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
}
