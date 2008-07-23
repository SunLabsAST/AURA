/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Represents a partition cluster in the UI
 */
public class PCPanel extends FlowPanel {
    protected PCInfo pc;
    
    public PCPanel(PCInfo pc) {
        super();
        this.pc = pc;
        setStylePrimaryName("viz-pcPanel");
        add(new Label("Partition Cluster " + pc.getPrefix()));
    }
    
    public PCInfo getPCInfo() {
        return pc;
    }
}
