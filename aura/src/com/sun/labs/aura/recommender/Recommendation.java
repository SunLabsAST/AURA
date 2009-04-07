/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.recommender;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.Scored;
import java.io.Serializable;

/**
 * Represents a single recommendatin
 */
public class Recommendation extends Scored<Item> implements Serializable {
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
    
    public int hashCode() {
        return explanation.hashCode();
    }
    
    public boolean equals(Object o) {
        if (o instanceof Recommendation) {
            Recommendation or = (Recommendation)o;
            if (explanation.equals(or.explanation)) {
                return super.equals(o);
            }
        }
        return false;
    }
}
