/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util;

import com.sun.labs.aura.datastore.impl.DSBitSet;

/**
 *
 * @author stgreen
 */
public class KeyToPrefix {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        int prefixLen = Integer.parseInt(args[0]);
        String key = args[1];
        DSBitSet bs = DSBitSet.parse(key.hashCode());
        bs.setPrefixLength(prefixLen);
        System.out.println("Prefix: " + bs.toString());
    }

}
