/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music;

import com.sun.labs.aura.util.Scored;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author plamere
 */
public class Recommendation implements Serializable {
    private double score = 0;
    private String id;
    private List<Scored<String>> explanation;

    public Recommendation() {
    }

    public Recommendation(String id, double score, List<Scored<String>> explanation) {
        this.score = score;
        this.id = id;
        this.explanation = explanation;
    }

    public String getId() {
        return id;
    }

    public List<Scored<String>> getExplanation() {
        return explanation;
    }


    public double getScore() {
        return score;
    }
}
