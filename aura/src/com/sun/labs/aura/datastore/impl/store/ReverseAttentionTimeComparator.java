/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.datastore.Attention;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares attention objects to sort in reverse chronological order
 */
public class ReverseAttentionTimeComparator implements Comparator<Attention>, Serializable {
    public ReverseAttentionTimeComparator() {}
    
    public int compare(Attention o1, Attention o2) {
        if(o1.getTimeStamp() - o2.getTimeStamp() < 0) {
            return 1;
        } else if(o1.getTimeStamp() == o2.getTimeStamp()) {
            return 0;
        } else {
            return -1;
        }

    }
}
