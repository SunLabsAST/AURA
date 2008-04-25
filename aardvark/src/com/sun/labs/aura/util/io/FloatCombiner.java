package com.sun.labs.aura.util.io;

/**
 * A combiner for integer values.
 */
public class FloatCombiner implements Combiner<Float> {

    public Float combine(Float v1, Float v2) {
        if(v1 == null) {
            return v2;
        }
        if(v2 == null) {
            return v1;
        }
        return v1 + v2;
    }

}
