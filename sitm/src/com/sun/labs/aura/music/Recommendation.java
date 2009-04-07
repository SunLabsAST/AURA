/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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
