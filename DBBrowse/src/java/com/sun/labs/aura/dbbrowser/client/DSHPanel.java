/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * A panel that displays the info for a datastore head
 */
public class DSHPanel extends FlowPanel {
    protected DSHInfo dsh;
    
    public DSHPanel(DSHInfo dsh) {
        super();
        this.dsh = dsh;
        setStylePrimaryName("viz-dshPanel");
        add(new Label(dsh.getName()));
        add(new Label("Status: " + (dsh.isReady() ? "ready" : "not ready")));
    }
    
    public DSHInfo getDSHInfo() {
        return dsh;
    }
}
