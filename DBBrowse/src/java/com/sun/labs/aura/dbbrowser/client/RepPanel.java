/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import java.util.Iterator;

/**
 * A UI widget that represents a replicant
 */
public class RepPanel extends FlowPanel {
    protected RepInfo rep;
    
    protected NumberFormat statForm = NumberFormat.getFormat("#########0.###");
    
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
        StyleLabel stats = new StyleLabel("Stats", "viz-actionLabel");
        stats.addClickListener(new ClickListener() {
            public void onClick(Widget arg0) {
                showStats();
            }
        });
        add(stats);
    }
    
    public RepInfo getRepInfo() {
        return rep;
    }
    
    public void showStats() {
        //
        // Before blowing away the current details display, check to see if
        // it is a timer panel.  If so, stop the timer.
        VizUI ui = VizUI.getVizUI();
        FlowPanel details = ui.getDetailsPanel();
        Iterator kids = details.iterator();
        if (kids.hasNext()) {
            Object kid = kids.next();
            if (kid instanceof TimerPanel) {
                TimerPanel tp = (TimerPanel)kid;
                tp.stop();
            }
        }
        details.setVisible(true);
        
        //
        // Clear current details and make a new timer panel to put in
        details.clear();
        
        TimerPanel repStatsPanel = new TimerPanel(15) {
            public void redraw() {
                VizServiceAsync service = GWTMainEntryPoint.getVizService();
                final AsyncCallback callback = new AsyncCallback() {
                    public void onSuccess(Object result) {
                        RepStats stats = (RepStats)result;
                        displayStats(stats);
                    }

                    public void onFailure(Throwable caught) {
                        stop();
                        VizUI.alert("Communication failed: " + caught.getMessage());
                    }
                };
                service.getRepStats(rep.getPrefix(), callback);
            }
            
            public void displayStats(RepStats stats) {
                clear();
                add(new Label("Stats for Replicant " + rep.getPrefix()));
                add(new StyleLabel("Refreshes every 15 seconds", "viz-subText"));
                add(new StyleLabel("Attentions per sec: " +
                                      statForm.format(stats.getAttentionsPerSec()),
                                   "viz-statLabel"));
                add(new StyleLabel("New Items per sec: " +
                                      statForm.format(stats.getNewItemsPerSec()),
                                  "viz-statLabel"));
                add(new StyleLabel("Updated Items per sec: " +
                                      statForm.format(stats.getUpdatedItemsPerSec()),
                                  "viz-statLabel"));
                add(new StyleLabel("Items from getItem(s) per sec: " +
                                      statForm.format(stats.getGetItemsPerSec()),
                                   "viz-statLabel"));
                add(new StyleLabel("Find Similars per sec: " +
                                      statForm.format(stats.getFindSimsPerSec()),
                                   "viz-statLabel"));
            }
            
        };
        details.add(repStatsPanel);
        repStatsPanel.start();
        
        
    }
}
