/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util;

import java.util.Comparator;

/**
 *
 * @author plamere
 */

public class ScoredComparator<T> implements Comparator<Scored<T>> {

    public static ScoredComparator COMPARATOR = new ScoredComparator();

    public int compare(Scored<T> o1, Scored<T> o2) {
        return o1.compareTo(o2);
    }
}
