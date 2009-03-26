/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;

import com.sun.labs.aura.dbbrowser.client.*;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;

/**
 * A UI widget that represents a replicant
 */
public class RepPanel extends FlowPanel {
    protected RepInfo rep;
    
    protected NumberFormat statForm = NumberFormat.getFormat("#########0.###");
    protected static NumberFormat cpuFormat = NumberFormat.getFormat("###0.#");
    protected Panel cpuLoad = null;
    
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
        cpuLoad = Util.getHisto("CPU", 0, 100, 50, "0%");
        add(cpuLoad);

        add(new StyleLabel("Halt", "viz-actionLabel"));

        StyleLabel stats = new StyleLabel("Stats", "viz-actionLabel");
        stats.addClickListener(new ClickListener() {
            public void onClick(Widget arg0) {
                showStats();
            }
        });
        add(stats);
        
        StyleLabel logConfig = new StyleLabel("LogMod", "viz-actionLabel");
        logConfig.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                doLogDialog();
            }
        });
        add(logConfig);
    }
    
    public RepInfo getRepInfo() {
        return rep;
    }
    
    public void setCPULoad(double load) {
        String str = cpuFormat.format(load);
        Panel newLoad = Util.getHisto("CPU", Double.valueOf(load).intValue(), 100, 50, str + "%");
        int index = getWidgetIndex(cpuLoad);
        remove(index);
        cpuLoad = newLoad;
        insert(cpuLoad, index);
    }

    public void showStats() {
        //
        // If there isn't already a stats display for us, add one.
        VizUI ui = VizUI.getVizUI();
        final VerticalPanel details = ui.getDetailsColumn();
        int numDet = details.getWidgetCount();
        for (int i = 0; i < numDet; i++) {
            Widget w = details.getWidget(i);
            if (w instanceof TimerPanel) {
                TimerPanel t = (TimerPanel)w;
                if (t.getName().equals("rep" + rep.getPrefix())) {
                    return;
                }
            }
        }
        
        TimerPanel repStatsPanel = new TimerPanel("rep" + rep.getPrefix(), 15) {
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
                
                //
                // Only display the stats selected by the user
                String[] toDisplay = Util.getStatDisplayCodes();
                for (String code : toDisplay) {
                    if (stats.contains(code)) {
                        add(new StyleLabel(Util.logCodeToDisplay(code) + " per sec: " +
                                statForm.format(stats.getRate(code)) + " (Avg: " +
                                statForm.format(stats.getTime(code)) + "ms)",
                                "viz-statLabel"));
                    }
                }

                StyleLabel close = new StyleLabel("Close", "viz-actionLabel");
                close.addStyleName("viz-closeLabel");

                final TimerPanel container = this;
                close.addClickListener(new ClickListener() {
                    public void onClick(Widget arg0) {
                        container.stop();
                        container.removeFromParent();
                    }
                });
                add(close);

                StyleLabel reset = new StyleLabel("Reset", "viz-actionLabel");
                VizUI.addConfDialog(reset,
                        new ClickListener() {
                            public void onClick(Widget arg0) {
                                resetStats(container);
                            }
                        },
                        "Really reset the stats for this replicant?");
                add(reset); 
            }
            
        };
        repStatsPanel.setStylePrimaryName("viz-detailsPanel");
        details.add(repStatsPanel);
        repStatsPanel.start();
    }
    
    public void resetStats(final TimerPanel toUpdate) {
        VizServiceAsync service = GWTMainEntryPoint.getVizService();
        final AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {
                toUpdate.redraw();
            }

            public void onFailure(Throwable caught) {
                VizUI.alert("Communication failed: " + caught.getMessage());
            }
        };
        service.resetRepStats(rep.getPrefix(), callback);
    }

    public void doLogDialog() {
        VizServiceAsync service = GWTMainEntryPoint.getVizService();
        final AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {
                doLogDialog2((List<String>)result);
            }

            public void onFailure(Throwable caught) {
                VizUI.alert("Communication failed: " + caught.getMessage());
            }
        };
        service.getRepLogNames(callback);
    }
    
    public void doLogDialog2(final List<String> allNames) {
        VizServiceAsync service = GWTMainEntryPoint.getVizService();
        final AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {
                new RepLogDialog(rep.getPrefix(), allNames, (List<String>)result).show();
            }

            public void onFailure(Throwable caught) {
                VizUI.alert("Communication failed: " + caught.getMessage());
            }
        };
        service.getRepSelectedLogNames(rep.getPrefix(), callback);
    }
}
