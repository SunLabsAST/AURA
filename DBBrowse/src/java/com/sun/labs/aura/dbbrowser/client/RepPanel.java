/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * A UI widget that represents a replicant
 */
public class RepPanel extends FlowPanel {
    protected RepInfo rep;
    
    public RepPanel(RepInfo rep) {
        super();
        this.rep = rep;
        setStylePrimaryName("viz-repPanel");
        add(new Label("Replicant"));
        long dbSize = rep.getDBSize();
        dbSize /= 1024 * 1024;
        add(new StyleLabel("DB Size:  " + dbSize + "MB",
                           "viz-statLabel"));
        long indexSize = rep.getIndexSize();
        indexSize /= 1024 * 1024;
        add(new StyleLabel("Index Size:  " + indexSize + "MB",
                           "viz-statLabel"));
        add(new StyleLabel("Halt", "viz-actionLabel"));
    }
    
    public RepInfo getRepInfo() {
        return rep;
    }
}
