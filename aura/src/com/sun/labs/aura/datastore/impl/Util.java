
package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.datastore.AttentionConfig;
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
    
    /**
     * hashCode may return a negative number, but Integer.toHexString treats
     * the value as unsigned.  If you feed the negative (greater than MAX_VAL)
     * value back into parseInt, it throws a number format exception.  So we'll
     * do the encode and decode with a little custom stuff.
     * 
     * @param hashCode
     * @return
     */
    public static String toHexString(int hashCode) {
        String val = Integer.toString(hashCode, 16);
        val = val.replace('-', 'n');
        return val;
    }
    
    /**
     * Convert a string that was converted by toHexString back to an int
     * @param hexVal
     * @return
     */
    public static int hexToInt(String hexVal) {
        String convert = hexVal.replace('n', '-');
        int val = Integer.parseInt(convert, 16);
        return val;
    }
    

    public static boolean isEmpty(AttentionConfig ac) {
        if (ac.getSourceKey() == null &&
                ac.getTargetKey() == null &&
                ac.getType() == null &&
                ac.getStringVal() == null &&
                ac.getNumberVal() == null) {
            return true;
        }
        return false;
    }

    public static void main(String args[]) {
        for(int i = 0; i < args.length; i++ ) {
            int x = parseBits(args[i]);
            System.out.println("bs: " + args[i] + " " + x);
        }
    }
}
