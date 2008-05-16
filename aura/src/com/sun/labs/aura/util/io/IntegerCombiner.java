package com.sun.labs.aura.util.io;

/**
 * A combiner for integer values.
 */
public class IntegerCombiner implements Combiner<Integer> {

    public Integer combine(Integer v1, Integer v2) {
        if(v1 == null) {
            return v2;
        }
        if(v2 == null) {
            return v1;
        }
        return v1 + v2;
    }

}
