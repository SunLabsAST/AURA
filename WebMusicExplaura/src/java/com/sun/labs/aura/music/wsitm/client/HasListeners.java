/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

/**
 *
 * @author mailletf
 */
public interface HasListeners {

    /**
     * Called by the parent widget when the object is about to be deleted and all isteners
     * must be removed to avoid memory leaks
     */
    public void doRemoveListeners();

}
