/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music;

import java.io.Serializable;

/**
 *
 * @author plamere
 */
public class Recommendation implements Serializable {
    private double score = 0;
    private String id;
    private String explanation;

    public Recommendation() {
    }

    public Recommendation(String id, double score, String explanation) {
        this.score = score;
        this.id = id;
        this.explanation = explanation;
    }

    public String getId() {
        return id;
    }

    public String getExplanation() {
        return explanation;
    }


    public double getScore() {
        return score;
    }
}
