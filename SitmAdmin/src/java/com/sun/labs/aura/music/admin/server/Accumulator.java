/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.admin.server;

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
public class Accumulator {
    private Map<String, Long> map = new HashMap<String, Long>();

    void add(String item, long c) {
        Long count = map.get(item);
        if (count == null) {
            map.put(item, c);
        } else {
            map.put(item, count + c);
        }
    }

    List<Scored<String>> getAll() {
        List<Scored<String>> list = new ArrayList<Scored<String>>();

        for (String item : map.keySet()) {
            list.add(new Scored<String>(item, map.get(item)));
        }

        Collections.sort(list, ScoredComparator.COMPARATOR);
        Collections.reverse(list);
        return list;
    }

}
