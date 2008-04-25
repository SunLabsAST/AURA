package com.sun.labs.aura.util.io;

/**
 * A combiner for integer values.
 */
public class LongCombiner implements Combiner<Long> {

    public Long combine(Long v1, Long v2) {
        if(v1 == null) {
            return v2;
        }
        if(v2 == null) {
            return v1;
        }
        return v1 + v2;
    }

}
