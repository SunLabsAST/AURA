/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.kt.search.ResultAccessor;
import com.sun.kt.search.ResultsFilter;
import java.io.Serializable;

/**
 *
 */
public class LengthFilter implements ResultsFilter, Serializable {

    private String field;
    
    private int len;
    
    public LengthFilter(String field, int len) {
        this.field = field;
        this.len = len;
    }
    
    public boolean filter(ResultAccessor ra) {
        String v = (String) ra.getSingleFieldValue(field);
        return v != null && v.toString().length() >= len;
    }
}
