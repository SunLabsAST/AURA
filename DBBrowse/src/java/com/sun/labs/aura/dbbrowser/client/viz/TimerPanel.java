/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A panel with a timer that calls a method to update the contents
 * of the panel periodically
 */
public abstract class TimerPanel extends FlowPanel {
    protected int refreshRate = 0;
    
    protected Timer timer;
    
    protected String name;
    
    /**
     * Create a panel with a refresh rate
     * 
     * @param refreshRate the number of seconds between refreshes
     */
    public TimerPanel(String name, int refreshRate) {
        this.refreshRate = refreshRate;
        this.name = name;
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
    
    public String getName() {
        return name;
    }
}
