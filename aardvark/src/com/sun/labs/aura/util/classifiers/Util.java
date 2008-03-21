/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.classifiers;

import com.sun.kt.search.FieldFrequency;
import com.sun.kt.search.SearchEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Util {
    
    public static List<String> getTopClasses(
            SearchEngine engine,
            String fieldName,
            int top) {
        List<String> classes = new ArrayList<String>();
        List<FieldFrequency> topVals = engine.getTopFieldValues(fieldName, -1);
        //
        // Normalize to lower case.
        Map<String, Integer> freqs = new HashMap<String, Integer>();
        for(FieldFrequency tv : topVals) {
            String tag = tv.getVal().toString().toLowerCase().trim();
            if(tag.equals("")) {
                continue;
            }
            Integer count = freqs.get(tag);
            if(count == null) {
                count = new Integer(0);
            }
            freqs.put(tag, count + tv.getFreq());
        }

        List<FieldFrequency> l = new ArrayList<FieldFrequency>(freqs.size());
        for(Map.Entry<String, Integer> e : freqs.entrySet()) {
            l.add(new FieldFrequency(e.getKey(), e.getValue()));
        }

        Collections.sort(l);
        Collections.reverse(l);

        for(FieldFrequency ff : l.subList(0, top)) {
            classes.add(ff.getVal().toString());
        }
        return classes;
    }

}
