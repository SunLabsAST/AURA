/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util;

import java.util.Comparator;

/**
 * A Comparator for <Scored<T>>s
 */

public class ReverseScoredComparator<T> implements Comparator<Scored<T>> {

    public final static ReverseScoredComparator COMPARATOR = new ReverseScoredComparator();

    public int compare(Scored<T> o1, Scored<T> o2) {
        return -o1.compareTo(o2);
    }
}
