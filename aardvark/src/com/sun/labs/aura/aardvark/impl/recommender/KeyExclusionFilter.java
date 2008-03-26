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
 * A results filter that will exclude keys in a set of keys.
 */
public class KeyExclusionFilter implements ResultsFilter, Serializable {

    private Set<String> keys;
    
    public KeyExclusionFilter(Set<String> keys) { 
        this.keys = keys;
    }
    public boolean filter(ResultAccessor ra) {
        String v = (String) ra.getSingleFieldValue("aura-key");
        return v != null && !keys.contains(v);
    }

}
