/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a partition cluster in the UI
 */
public class PCPanel extends HorizontalPanel {
    protected PCInfo pc;
    protected FlowPanel myself;
    protected VerticalPanel replicants;
    
    public PCPanel(PCInfo pc) {
        super();
        this.pc = pc;
        setStylePrimaryName("viz-pcPanel");
        myself = new FlowPanel();
        add(myself);
        myself.add(new Label("Partition Cluster " + pc.getPrefix()));
        replicants = new VerticalPanel();
        add(replicants);
        List reps = pc.getRepInfos();
        for (Iterator it = reps.iterator(); it.hasNext();) {
            RepInfo rep = (RepInfo)it.next();
            replicants.add(new RepPanel(rep));
        }
    }
    
    public PCInfo getPCInfo() {
        return pc;
    }
}
