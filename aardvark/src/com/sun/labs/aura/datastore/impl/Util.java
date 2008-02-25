
package com.sun.labs.aura.datastore.impl;

import java.util.BitSet;

/**
 * Utility methods for use in the Data Store
 */
public class Util {
    
    /**
     * Parses a string containing ones and zeros into an integer.  The bits in the
     * string are assumed to be in most significant to least significant order, and
     * will be put into the integer in that order.
     * @param bs the string to transform into a set of bits.
     * @return
     */
    public static int parseBits(String bs) {
        return Integer.valueOf(bs, 2);
    }

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
        for(int i = 0; i < args.length; i++ ) {
            int x = parseBits(args[i]);
            System.out.println("bs: " + args[i] + " " + x);
        }
    }
}
