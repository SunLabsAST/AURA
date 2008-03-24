/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.kt.search.ResultAccessor;
import com.sun.kt.search.ResultsFilter;
import com.sun.labs.aura.datastore.Item;

/**
 * A filter that will only pass results of a particular type.
 */
public class TypeFilter implements ResultsFilter {
    
    private String type;
    
    public TypeFilter(Item.ItemType type) {
        this.type = type.toString();
    }

    public boolean filter(ResultAccessor ra) {
        String rt = (String) ra.getSingleFieldValue("aura-type");
        return rt != null && rt.equals(type);
    }

}
