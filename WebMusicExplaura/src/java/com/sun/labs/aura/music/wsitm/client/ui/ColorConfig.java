/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

/**
 *
 * @author mailletf
 */
public class ColorConfig {

    private String positive;
    private String negative;

    public ColorConfig(String positive, String negative) {
        this.positive = positive;
        this.negative = negative;
    }

    /**
     * Return the right color based on the current size of the item.
     * @param size
     * @return
     */
    public final String getColor(double size) {
        if (size < 0) {
            return negative;
        } else {
            return positive;
        }
    }
}
