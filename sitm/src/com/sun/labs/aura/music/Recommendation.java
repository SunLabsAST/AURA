/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music;

import com.sun.labs.aura.util.Scored;
import java.io.Serializable;
import java.util.ArrayList;
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

    public Recommendation(String id, double score) {
        this(id, score, new ArrayList<Scored<String>>());
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

    void addReason(Scored<String> reason) {
        explanation.add(reason);
    }

    void addReason(String id, double score) {
        explanation.add(new Scored<String>(id, score));
    }
}
