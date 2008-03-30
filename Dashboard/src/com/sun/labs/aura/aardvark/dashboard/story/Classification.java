/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.dashboard.story;

/**
 *
 * @author plamere
 */
public class Classification {
    public String name;
    public float  score;

    public Classification(String name, float score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public float getScore() {
        return score;
    }
    

}
