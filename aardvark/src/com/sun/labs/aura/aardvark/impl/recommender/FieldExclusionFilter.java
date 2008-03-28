/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.kt.search.ResultAccessor;
import com.sun.kt.search.ResultsFilter;
import java.io.Serializable;
import java.util.Set;

/**
 * A results filter that will exclude items that have a field containing
 * belonging to a set of excluded values
 */
public class FieldExclusionFilter implements ResultsFilter, Serializable {
    private String fieldName;
    private Set<String> excludedValues;
    
    public FieldExclusionFilter(String fieldName, Set<String> excludedValues) { 
        this.fieldName = fieldName;
        this.excludedValues = excludedValues;
    }
    public boolean filter(ResultAccessor ra) {
        String v = (String) ra.getSingleFieldValue(fieldName);
        return v != null && !excludedValues.contains(v);
    }

}
