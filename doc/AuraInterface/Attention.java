/*
 * Attention.java
 * 
 * Created on Oct 24, 2007, 3:18:19 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark;

/**
 *
 * @author plamere
 */
public interface Attention {
    enum Type { STARRED, VIEWED };
    long getUserID();
    long getEntryID();
    long getTimeStamp();
    Type getType();
}
