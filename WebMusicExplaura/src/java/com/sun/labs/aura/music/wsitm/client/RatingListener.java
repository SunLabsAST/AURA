/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

/**
 *
 * @author mailletf
 */
public interface RatingListener extends WebListener {

    /**
     * Called when the item the listener is representing has received a new rating
     * @param itemId
     * @param rating
     */
    public void onRate(String itemId, int rating);

}
