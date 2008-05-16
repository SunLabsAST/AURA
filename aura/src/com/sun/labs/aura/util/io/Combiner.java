/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

/**
 * An interface to be implemented by those who wish to merge the values of a
 * number of keyed input streams when merging those streams into a single 
 * output stream.
 * @param V the type of the values to combine
 */
public interface Combiner<V> {
    /**
     * Combines two input values into the output value. Classes implementing this
     * method <em>must<em> be able to handle a <code>null</code> value for either of the 
     * input values.
     * @param v1 the first value
     * @param v2 the second value
     * @return the combined value
     */
    public V combine(V v1, V v2);
}
