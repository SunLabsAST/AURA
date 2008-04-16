/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.kt.search.ResultAccessor;
import com.sun.kt.search.ResultsFilter;
import com.sun.labs.aura.datastore.Item;
import java.io.Serializable;

/**
 * A filter that will only pass results of a particular type.
 */
public class TypeFilter implements ResultsFilter, Serializable {
    
    private String type;
    
    private int nt;
    
    private int np;
    
    public TypeFilter(Item.ItemType type) {
        this.type = type.toString();
    }

    public boolean filter(ResultAccessor ra) {
        nt++;
        String rt = (String) ra.getSingleFieldValue("aura-type");
        boolean ret =  rt != null && rt.equals(type);
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