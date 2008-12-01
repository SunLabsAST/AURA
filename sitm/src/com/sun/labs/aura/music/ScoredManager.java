/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music;

import com.sun.labs.aura.util.Scored;
import java.util.ArrayList;
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
