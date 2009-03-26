/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;

import com.google.gwt.i18n.client.NumberFormat;
import com.sun.labs.aura.dbbrowser.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel that displays the info for a datastore head
 */
public class DSHPanel extends FlowPanel {
    protected DSHInfo dsh;

    protected StyleLabel cpuLoad = null;

    protected static NumberFormat cpuFormat = NumberFormat.getFormat("###0.#");

    public DSHPanel(DSHInfo dsh) {
        super();
        this.dsh = dsh;
        setStylePrimaryName("viz-dshPanel");
        add(new Label(dsh.getName()));
        add(new StyleLabel("Status: " + (dsh.isReady() ? "ready" : "not ready"),
                           "viz-statLabel"));
        add(new StyleLabel("IP: " + dsh.getIP(), "viz-statLabel"));
        cpuLoad = new StyleLabel("", "viz-statLabel");
        setCPULoad(0);
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
        String str = cpuFormat.format(load);
        cpuLoad.setText("% CPU: " + str);
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
