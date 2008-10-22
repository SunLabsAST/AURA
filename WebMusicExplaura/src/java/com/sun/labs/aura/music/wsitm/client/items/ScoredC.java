/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

import java.io.Serializable;

/**
 *
 * @author mailletf
 */
public class ScoredC<T> implements Serializable {
    
    private T item;
    private double score;

    public ScoredC() {}

    public ScoredC(T item, double score) {
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
