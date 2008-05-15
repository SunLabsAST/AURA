/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.recommender;

import com.sun.labs.minion.ResultAccessor;
import com.sun.labs.minion.ResultsFilter;
import java.io.Serializable;

/**
 *
 */
public class LengthFilter implements ResultsFilter, Serializable {

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
