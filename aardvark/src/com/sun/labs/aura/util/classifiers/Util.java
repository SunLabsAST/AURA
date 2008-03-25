/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.classifiers;

import com.sun.kt.search.FieldFrequency;
import com.sun.kt.search.SearchEngine;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Util {
    
    public static List<String> getTopClasses(
            SearchEngine engine,
            String fieldName,
            int top) {
        List<String> classes = new ArrayList<String>();
        for(FieldFrequency ff : engine.getTopFieldValues(fieldName, top, true)) {
            classes.add(ff.getVal().toString());
        }
        return classes;
    }

}
