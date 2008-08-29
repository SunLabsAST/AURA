/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A panel with a timer that calls a method to update the contents
 * of the panel periodically
 */
public abstract class TimerPanel extends FlowPanel {
    protected int refreshRate = 0;
    
    protected Timer timer;
    
    /**
     * Create a panel with a refresh rate
     * 
     * @param refreshRate the number of seconds between refreshes
     */
    public TimerPanel(int refreshRate) {
        this.refreshRate = refreshRate;
        timer = new Timer() {
            public void run() {
                redraw();
            }
        };
    }
    
    public abstract void redraw();

    public void start() {
        redraw();
        timer.scheduleRepeating(refreshRate * 1000);
    }
    
    public void stop() {
        timer.cancel();
    }
}
