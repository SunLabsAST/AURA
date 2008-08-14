/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

import java.io.IOException;
import java.util.List;

/**
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public abstract class CartesianProductMerger<K, V> extends GroupedMerger {
    public void mergeList(List inputs,
                          KeyedOutputStream output) throws IOException {
        
        // The Cartesian product of an element with the empty set is the empty set
        if(inputs.size() < 2) {
            return;
        }
        // There's a n-dimensional solution, but stick to two for now
        if(inputs.size() > 2) {
            throw new IllegalArgumentException("Only two element joins currently: " + inputs.size());
        }
        
        // If I specify this as the type in the signature, the compiler doesn't
        // think the method overrides the abstract method in GroupedMerger even
        // though the type there is specified as being this.
        List<List<Record<K, V>>> castInputs = (List<List<Record<K, V>>>)inputs;
        for(Record<K, V> outer : castInputs.get(0)) {
            for(Record<K, V> inner : castInputs.get(1)) {
                merge(output, outer, inner);
            }
        }
    }
    
    public abstract void merge(KeyedOutputStream output, Record<K, V>... inputs)
            throws IOException;
}
