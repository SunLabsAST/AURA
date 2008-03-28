/*
 */

package com.sun.labs.aura.recommender;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.Scored;
import java.util.Comparator;

/**
 * Represents a single recommendatin
 */
public class Recommendation extends Scored<Item> {
    /**
     * A Comparator that will sort recommendations from highest to lowest scores
     */
    public static Comparator<Recommendation> REVERSE = new Comparator<Recommendation>() {
        public int compare(Recommendation o1, Recommendation o2) {
            return o2.compareTo(o1);
        }
    };

    private String explanation;

    /**
     * Creates a new recommendation
     * @param item the item being recommended
     * @param score the recommendation score (1.0 is best, 0.0 is worst)
     * @param explanation text that explains the recommendatin
     */
    public Recommendation(Item item, double score, String explanation) {
        super(item, score);
        this.explanation = explanation;
    }

    /**
     * Returns an explanation about the recommendation
     * @return the explanation
     */
    public String getExplanation() {
        return explanation;
    }
}