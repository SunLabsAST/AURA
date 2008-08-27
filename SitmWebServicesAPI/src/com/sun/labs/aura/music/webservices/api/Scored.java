/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.webservices.api;

/**
 *
 * @author plamere
 */
public class Scored<T> {
    private T item;
    private double score;

    public Scored(T item, double score) {
        this.item = item;
        this.score = score;
    }

    public T getItem() {
        return item;
    }

    public double getScore() {
        return score;
    }

}
