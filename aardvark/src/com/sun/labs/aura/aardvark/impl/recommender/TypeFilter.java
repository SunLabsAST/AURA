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
    
    public TypeFilter(Item.ItemType type) {
        this.type = type.toString();
        System.out.println("type filter " + type);
    }

    public boolean filter(ResultAccessor ra) {
        String rt = (String) ra.getSingleFieldValue("aura-type");
        boolean result =  rt != null && rt.equals(type);
        if (true) {
            System.out.println(result + " tf " + rt + " type " + type);
        } 
        return result;
    }

}
