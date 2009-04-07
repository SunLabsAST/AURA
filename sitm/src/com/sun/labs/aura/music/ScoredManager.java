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
import com.sun.labs.aura.util.ScoredComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class ScoredManager<T> {
    Map<T, Double> map = new HashMap();

    public void accum(T t, double score) {
        Double d = map.get(t);
        if (d == null) {
            d = Double.valueOf(0);
        }
        map.put(t, d + score);
    }


    public List<Scored<T>> getAll() {
        return getAll(true);
    }

    public List<Scored<T>> getAll(boolean prune) {
        List<Scored<T>> results = new ArrayList();
        for (Map.Entry<T, Double> entry : map.entrySet()) {
            Scored<T> s = new Scored<T>(entry.getKey(), entry.getValue());
            if (!prune || s.getScore() > 0) {
                results.add(s);
            }
        }
        Collections.sort(results, ScoredComparator.COMPARATOR);
        Collections.reverse(results);
        return results;
    }

    public List<Scored<T>> getTopN(int n) {
        return getTopN(n, true);
    }

    public List<Scored<T>> getTopN(int n, boolean prune) {
        List<Scored<T>> results = getAll(prune);
        results = results.size() > n ? results.subList(0, n) : results;
        return results;
    }
}
