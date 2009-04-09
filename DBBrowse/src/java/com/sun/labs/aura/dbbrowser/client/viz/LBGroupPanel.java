/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;

/**
 * A load-balancer group of hosts
 */
public class LBGroupPanel extends FlowPanel {
    protected String groupName;
    protected StyleLabel sessionsLabel;
    protected List<WebPanel> webPanels;

    public LBGroupPanel(String groupName) {
        this.groupName = groupName;
        setStylePrimaryName("viz-lbGroupPanel");
        webPanels = new ArrayList<WebPanel>();
        add(new Label(groupName));
        sessionsLabel = new StyleLabel("", "viz-statLabel");
        add(sessionsLabel);
    }

    public void updateActiveSessions() {
        int numActive = 0;
        for (WebPanel panel : webPanels) {
            numActive += panel.getActiveSessions();
        }
        sessionsLabel.setText("Active Sessions: " + numActive);
    }

    public void add(Widget w) {
        if (w instanceof WebPanel) {
            webPanels.add((WebPanel)w);
        }
        super.add(w);
    }
}
