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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a partition cluster in the UI
 */
public class PCPanel extends HorizontalPanel {
    protected PCInfo pc;
    protected FlowPanel myself;
    protected VerticalPanel replicants;
    
    protected StyleLabel cpuLoad = null;

    protected RepPanel repPanel;

    protected static NumberFormat cpuFormat = NumberFormat.getFormat("###0.#");

    public PCPanel(PCInfo pc) {
        super();
        this.pc = pc;
        setStylePrimaryName("viz-pcPanel");
        myself = new FlowPanel();
        add(myself);
        myself.add(new Label("Partition Cluster " + pc.getPrefix()));
        myself.add(new StyleLabel("Items: " + pc.getNumItems(),
                                  "viz-statLabel"));
        myself.add(new StyleLabel("Attention: " + pc.getNumAttention(),
                                  "viz-statLabel"));
        cpuLoad = new StyleLabel("", "viz-statLabel");
        setCPULoad(0);
        myself.add(cpuLoad);

        Map typeToCount = pc.getTypeToCountMap();
        myself.add(Util.getTypeStatsPanel(typeToCount));
        
        //
        // Add buttons
        StyleLabel halt = new StyleLabel("Halt", "viz-actionLabel");
        VizUI.addConfDialog(halt, new ClickListener() {
            public void onClick(Widget arg0) {
                doHalt();
            }
        }, "Really halt Partition Cluster " + pc.getPrefix() + "?");
        myself.add(halt);
        
        final StyleLabel split = new StyleLabel("Split", "viz-actionLabel");
        VizUI.addConfDialog(split, new ClickListener() {
            public void onClick(Widget arg0) {
                doSplit(split);
            }
        }, "Really start split of Partition Cluster " + pc.getPrefix() + "?");
        myself.add(split);
        
        replicants = new VerticalPanel();
        add(replicants);
        List reps = pc.getRepInfos();
        for (Iterator it = reps.iterator(); it.hasNext();) {
            RepInfo rep = (RepInfo)it.next();
            repPanel = new RepPanel(rep);
            replicants.add(repPanel);
        }
    }
    
    public PCInfo getPCInfo() {
        return pc;
    }

    public void setCPULoad(double load) {
        String str = cpuFormat.format(load);
        cpuLoad.setText("% CPU: " + str);
    }

    public void setRepCPULoad(double load) {
        repPanel.setCPULoad(load);
    }
    
    protected void doHalt() {
        AsyncCallback asyncCallback = new AsyncCallback() {
            public void onFailure(Throwable arg0) {
                Window.alert("Communication disruption: " + arg0);
            }

            public void onSuccess(Object arg0) {
                // not sure yet
                Window.alert("done!");
            }
            
        };
        GWTMainEntryPoint.getVizService().haltPC(pc, asyncCallback);
    }
    
    protected void doSplit(final StyleLabel splitButton) {
        AsyncCallback asyncCallback = new AsyncCallback() {
            public void onFailure(Throwable arg0) {
                Window.alert("Communication disruption: " + arg0);
            }

            public void onSuccess(Object arg0) {
                splitButton.setText("Splitting...");
                splitButton.setStylePrimaryName("viz-actionLabelInProgress");
            }
            
        };
        GWTMainEntryPoint.getVizService().splitPC(pc, asyncCallback);
    }
}
