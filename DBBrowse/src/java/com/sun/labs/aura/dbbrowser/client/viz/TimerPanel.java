/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
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
