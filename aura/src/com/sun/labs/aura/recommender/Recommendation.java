/*
 */

package com.sun.labs.aura.recommender;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.Scored;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Represents a single recommendatin
 */
public class Recommendation extends Scored<Item> implements Serializable {
    static class ReverseComparator<T extends Comparable> implements Comparator<T>, Serializable {
        public int compare(T o1, T o2) {
            return o2.compareTo(o1);
        }        
    }

    /**
     * A Comparator that will sort recommendations from highest to lowest scores
     */
    public static Comparator<Recommendation> REVERSE = new ReverseComparator<Recommendation>();
    
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
