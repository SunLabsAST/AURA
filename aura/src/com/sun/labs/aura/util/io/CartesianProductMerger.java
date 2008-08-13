/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author wh224365
 */
public abstract class CartesianProductMerger<K, V> implements RecordMerger<K, V> {
    public void merge(List<RecordSet<K, V>> inputs,
                      KeyedOutputStream output) throws IOException {
        // There's a recursive n-dimensional solution, but stick to two for now
        if(inputs.size() > 2) {
            throw new IllegalArgumentException("Only two element joins currently");
        }
        for(Record<K, V> outer : inputs.get(0)) {
            for(Record<K, V> inner : inputs.get(0)) {
                merge(outer, inner, output);
            }
        }
    }

    public abstract void merge(Record<K, V> recordOne, Record<K, V> recordTwo,
                               KeyedOutputStream output) throws IOException;
}
