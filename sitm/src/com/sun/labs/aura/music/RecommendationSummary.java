/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music;

import java.util.List;

/**
 *
 * @author plamere
 */
public class RecommendationSummary {
    private String explanation;
    private List<Recommendation> recommendations;

    public RecommendationSummary(String explanation, List<Recommendation> recommendations) {
        this.explanation = explanation;
        this.recommendations = recommendations;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }
}
