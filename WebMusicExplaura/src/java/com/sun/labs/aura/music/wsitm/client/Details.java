/*
 * Details.java
 *
 * Created on April 7, 2007, 8:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

/**
 *
 * @author plamere
 */
public interface Details {
    /**
     * Ensures proper conditions (ie no null arrays)
     */
    void fixup();

    String getEncodedName();

    String getId();

    String getName();

    String getStatus();

    boolean isOK();
    
}
