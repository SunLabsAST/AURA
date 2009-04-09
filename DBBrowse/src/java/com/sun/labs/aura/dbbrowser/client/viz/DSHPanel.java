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

import com.google.gwt.i18n.client.NumberFormat;
import com.sun.labs.aura.dbbrowser.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel that displays the info for a datastore head
 */
public class DSHPanel extends FlowPanel {
    protected DSHInfo dsh;

    protected Panel cpuLoad = null;

    public DSHPanel(DSHInfo dsh) {
        super();
        this.dsh = dsh;
        setStylePrimaryName("viz-dshPanel");
        add(new Label(dsh.getName()));
        add(new StyleLabel("Status: " + (dsh.isReady() ? "ready" : "not ready"),
                           "viz-statLabel"));
        add(new StyleLabel("IP: " + dsh.getIP(), "viz-statLabel"));
        cpuLoad = Util.getHisto("CPU", 0, 100, 50, "00.0%");
        add(cpuLoad);

        StyleLabel shutDown = new StyleLabel("Shutdown", "viz-actionLabel");
        VizUI.addConfDialog(shutDown, new ClickListener() {
            public void onClick(Widget arg0) {
                doShutDown();
            }
        }, "Really shut down the whole Data Store?");
        add(shutDown);

    }
    
    public DSHInfo getDSHInfo() {
        return dsh;
    }
    
    public void setCPULoad(double load) {
        String str = VizUI.cpuFormat.format(load);
        Panel newLoad = Util.getHisto("CPU", Double.valueOf(load).intValue(), 100, 50, str + "%");
        int index = getWidgetIndex(cpuLoad);
        remove(index);
        cpuLoad = newLoad;
        insert(cpuLoad, index);
    }

    protected void doShutDown() {
        AsyncCallback asyncCallback = new AsyncCallback() {
            public void onFailure(Throwable arg0) {
                Window.alert("Communication disruption: " + arg0);
            }

            public void onSuccess(Object arg0) {
                // not sure yet
            }
            
        };
        GWTMainEntryPoint.getVizService().shutDown(asyncCallback);
    }

}
