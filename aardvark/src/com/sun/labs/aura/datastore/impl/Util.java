
package com.sun.labs.aura.datastore.impl;

import java.util.BitSet;

/**
 * Utility methods for use in the Data Store
 */
public class Util {

    public static int bitSetToInt(BitSet bs) {
        int res = 0;
        //
        // Look at bits from 0 to 30 (31 total bits).  Don't use the sign bit.
        for (int i = 30; i >= 0 ; i--) {
            if (bs.get(i) == true) {
                res |= (1<<i);
            }
        }
        return res;
    }
    
    public static BitSet intToBitSet(int bits) {
        BitSet res = new BitSet();
        for (int i = 0; i < 31; i++) {
            if (((1<<i) & bits) != 0) {
                res.set(i);
            }
        }
        return res;
    }
    
    public static void main(String args[]) {
        int i = 8;
        BitSet bs = intToBitSet(i);
        System.out.println(bs);
    }
}
