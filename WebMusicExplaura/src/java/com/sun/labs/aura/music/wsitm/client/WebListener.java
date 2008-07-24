/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

/**
 *
 * @author mailletf
 */
public interface WebListener {

    /**
     * Called when the item is about to be deleted and it must call the listener manager
     * to remove its reference
     */
    public void onDelete();

}
