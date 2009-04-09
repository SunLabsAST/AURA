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

import com.sun.labs.aura.dbbrowser.client.*;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
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
        cpuLoad = Util.getHisto("CPU", 0, 100, 50, "00.0%");
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
        String str = VizUI.cpuFormat.format(load);
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
                add(reset);
                final DialogBox resetConfirm = new DialogBox(true, true);
                FlowPanel contents = new FlowPanel();
                resetConfirm.setWidget(contents);
                contents.add(new Label("Really reset all stats for replicant " +
                        rep.getPrefix() + "?"));
                Button resetButton = new Button("Reset " + rep.getPrefix());
                resetButton.addClickListener(new ClickListener() {
                    public void onClick(Widget w) {
                        resetConfirm.hide();
                        resetStats(container);
                    }
                });
                contents.add(resetButton);

                Button resetAllButton = new Button("Reset All Replicants");
                resetAllButton.addClickListener(new ClickListener() {
                    public void onClick(Widget arg0) {
                        resetConfirm.hide();
                        resetStats(null);
                    }
                });
                contents.add(resetAllButton);

                resetConfirm.setPopupPosition(reset.getAbsoluteLeft(),
                                              reset.getAbsoluteTop());
                reset.addClickListener(new ClickListener() {
                    @Override
                    public void onClick(Widget sender) {
                        resetConfirm.show();
                    }
                });
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
                if (toUpdate != null) {
                    toUpdate.redraw();
                }
            }

            public void onFailure(Throwable caught) {
                VizUI.alert("Communication failed: " + caught.getMessage());
            }
        };
        if (toUpdate != null) {
            service.resetRepStats(rep.getPrefix(), callback);
        } else {
            service.resetRepStats(null, callback);
        }
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
