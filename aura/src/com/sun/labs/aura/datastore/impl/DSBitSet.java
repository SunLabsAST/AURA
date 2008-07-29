
package com.sun.labs.aura.datastore.impl;

import java.util.BitSet;

/**
 * A BitSet customized for use within the Data Store.  This BitSet adds a
 * prefix length that identifies the number of significant bits in this
 * set (including leading zeros).  The prefix length does not grow implicitly
 * when a bit beyond the prefix is set.  The prefix count starts with the least
 * significant (0th) bit.
 */
public class DSBitSet extends BitSet {
    protected int prefixLen = 0;
    
    /**
     * Constructs a DSBitSet with a certain number of significant digits,
     * starting with the least significant bit.
     * 
     * @param prefixLen the number of significant bits
     */
    public DSBitSet(int prefixLen) {
        this.prefixLen = prefixLen;
    }
    
    @Override
    public Object clone() {
        DSBitSet ret = (DSBitSet)super.clone();
        ret.setPrefixLength(prefixLen);
        return ret;
    }
    
    /**
     * Sets the length of the prefix represented by this bit set.  This is the
     * number of significant bits in the prefix.  Any other values set will
     * be ignored.
     * 
     * @param prefixLen
     */
    public void setPrefixLength(int prefixLen) {
        this.prefixLen = prefixLen;
    }
    
    /**
     * Get the number of significant bits in this prefix.
     * @return
     */
    public int prefixLength() {
        return prefixLen;
    }
    
    /**
     * Adds a new most-significant bit to the bit set with the given zero or
     * one value.
     * 
     * @param isOne true if this should be a one bit
     */
    public void addBit(boolean isOne) {
        set(prefixLen, isOne);
        prefixLen++;
    }
    
    /**
     * Determines if two DSBitSet objects are equal.  Only compares bits up
     * to the prefix length.
     * 
     * @param o the DSBitSet to compare to
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof DSBitSet) {
            DSBitSet bs = (DSBitSet) o;
            if (bs.prefixLength() == prefixLength()) {
                for (int i = 0; i < prefixLength(); i++) {
                    if (get(i) != bs.get(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Integer.valueOf(toInt()).hashCode();
    }

    /**
     * Create a DSBitSet from an int, ignoring the sign bit
     * @param value the int value of the bit set
     * @return
     */
    public static DSBitSet parse(int value) {
        DSBitSet res = new DSBitSet(31);
        for (int i = 0; i < 31; i++) {
            if (((1<<i) & value) != 0) {
                res.set(i);
            }
        }
        return res;
    }
    
    /**
     * Parses a string representation of a bit set, including leading zeros.
     * The right-most bit is considered to be the least significant.  To
     * lengthen a prefix string, a digit should be added to the left of the
     * string.
     * 
     * @param prefix the string representation of the prefix
     * @return the bit set representation of the string
     */
    public static DSBitSet parse(String prefix) throws NumberFormatException {
        int strLen = prefix.length();
        DSBitSet res = new DSBitSet(strLen);
        for (int i = 1; i <= strLen; i++) {
            switch (prefix.charAt(strLen - i)) {
                case '1':
                    res.set(i - 1);
                    break;
                case '0':
                    break;
                default:
                    throw new NumberFormatException("Illegal bit: " +
                            prefix.charAt(strLen - i));
            }
        }
        return res;
    }
    
    /**
     * Get the integer represented by this DSBitSet, looking only at the bits
     * included in the prefixLength, counting from the least signficant bit.
     * @return the integer represented by this DSBitSet
     */
    public int toInt() {
        int res = 0;
        //
        // Look at bits from 0 to prefixLength.
        for (int i = prefixLength(); i >= 0 ; i--) {
            if (get(i) == true) {
                res |= (1<<i);
            }
        }
        return res;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = prefixLength() - 1; i >= 0 ; i--) {
            if (get(i) == true) {
                sb.append('1');
            } else {
                sb.append('0');
            }
        }
        return sb.toString();
    }
}
