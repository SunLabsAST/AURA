package com.sun.labs.aura.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A comparator for sorting scored things in decreasing order.
 */
public class DecreasingScoredComparator implements Comparator<Scored>, Serializable {

    public int compare(Scored o1, Scored o2) {
        return -o1.compareTo(o2);
    }
}
