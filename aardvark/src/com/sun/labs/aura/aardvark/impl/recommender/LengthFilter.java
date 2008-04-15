/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.kt.search.ResultAccessor;
import com.sun.kt.search.ResultsFilter;
import java.util.logging.Logger;

/**
 *
 */
public class LengthFilter implements ResultsFilter {

    private String field;
    
    private int len;
    
    private int nt;
    
    private int np;
    
    public LengthFilter(String field, int len) {
        this.field = field;
        this.len = len;
    }
    
    public boolean filter(ResultAccessor ra) {
        nt++;
        String v = (String) ra.getSingleFieldValue(field);
        
        boolean ret = v != null && v.toString().length() >= len;
        if(ret) {
            np++;
        }
        return ret;
    }

    public int getTested() {
        return nt;
    }

    public int getPassed() {
        return np;
    }
}
