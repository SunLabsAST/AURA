/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;

import com.google.gwt.user.client.Timer;
import com.sun.labs.aura.dbbrowser.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The main interface to the visualization tool
 */
public class VizUI extends DockPanel {
    protected FlowPanel controls;
    protected HorizontalPanel leftRight;
    protected VerticalPanel dshColumn;
    protected VerticalPanel pcColumn;
    protected VerticalPanel detailsColumn;
    protected VerticalPanel webColumn;

    
    protected List dshInfos;
    protected List pcInfos;

    protected Map<String,DSHPanel> ipToDSHPanel;
    protected Map<String,PCPanel> prefixToPCPanel;
    protected Map<String,WebPanel> procToWebPanel;
    
    protected VizServiceAsync service;
    
    protected static VizUI theUI = null;

    protected Timer statUpdateTimer;
    protected Timer webStatUpdateTimer;
    
    public VizUI() {
        theUI = this;
        //
        // Set up the controls
        controls = new FlowPanel();
        controls.setStylePrimaryName("viz-conrolPanel");
        final AsyncCallback refresher = new AsyncCallback() {
            public void onFailure(Throwable t) {
                alert("Failed to refresh, see the server log for details");
            }

            public void onSuccess(Object result) {
                refresh();
            }
        };

        Button refreshBtn = new Button("Refresh", new ClickListener() {
            public void onClick(Widget arg0) {
                service.refreshSvcs(refresher);
            }
        });
        refreshBtn.setStylePrimaryName("main-selectorButton");
        controls.add(refreshBtn);
        
        Button settingsBtn = new Button("Settings", new ClickListener() {
            public void onClick(Widget w) {
                final AsyncCallback getCodes = new AsyncCallback() {
                    public void onFailure(Throwable t) {
                        alert("Failed to get log codes, see the server log for details");
                    }

                    public void onSuccess(Object result) {
                        new SettingsDialog((List<String>)result).show();
                    }
                };
                service.getRepLogNames(getCodes);
            }
        });
        settingsBtn.setStylePrimaryName("main-selectorButton");
        controls.add(settingsBtn);
        add(controls, NORTH);
        
        //
        // Set up the content
        leftRight = new HorizontalPanel();
        leftRight.setStylePrimaryName("viz-mainPanel");
        dshColumn = new VerticalPanel();
        dshColumn.setStylePrimaryName("viz-dshColumn");
        pcColumn = new VerticalPanel();
        pcColumn.setStylePrimaryName("viz-pcColumn");
        detailsColumn = new VerticalPanel();
        detailsColumn.setStylePrimaryName("viz-detailsColumn");
        leftRight.add(dshColumn);
        leftRight.add(pcColumn);
        leftRight.add(detailsColumn);
        
        add(leftRight, CENTER);

        //
        // And a place to list web servers
        webColumn = new VerticalPanel();
        webColumn.setStylePrimaryName("viz-webColumn");
        add(webColumn, EAST);
        
        service = GWTMainEntryPoint.getVizService();
        service.refreshSvcs(refresher);
    }
    
    public VerticalPanel getDetailsColumn() {
        return detailsColumn;
    }
    
    public static VizUI getVizUI() {
        return theUI;
    }
    
    protected void refresh() {
        //
        // Load info on datastore heads:
        final AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {
                dshInfos = (List) result;
                fillDSH();
            }
            
            public void onFailure(Throwable caught) {
                alert("Communication failed: " + caught.getMessage());
            }
        };
        service.getDSHInfo(callback);
        
        //
        // Load info on partitions (includes replicant info):
        final AsyncCallback pccallback = new AsyncCallback() {
            public void onSuccess(Object result) {
                pcInfos = (List) result;
                fillPC();
            }
            
            public void onFailure(Throwable caught) {
                alert("Communication failed: " + caught.getMessage());
            }
        };
        service.getPCInfo(pccallback);

        if (webStatUpdateTimer != null) {
            webStatUpdateTimer.cancel();
        }
        //
        // Clear the web column.  It'll get filled back in by the timer
        webColumn.clear();
        procToWebPanel = new HashMap<String,WebPanel>();

        webStatUpdateTimer = new Timer() {
            @Override
            public void run() {
                fetchWebStats();
            }
        };
        webStatUpdateTimer.scheduleRepeating(10 * 1000);
    }
    
    protected void fillDSH() {
        dshColumn.clear();
        ipToDSHPanel = new HashMap<String,DSHPanel>();
        for (Iterator dit = dshInfos.iterator(); dit.hasNext();) {
            DSHInfo dsh = (DSHInfo)dit.next();
            DSHPanel panel = new DSHPanel(dsh);
            ipToDSHPanel.put(dsh.getIP(), panel);
            dshColumn.add(panel);
        }
    }

    protected void fillPC() {
        pcColumn.clear();
        prefixToPCPanel = new HashMap<String,PCPanel>();
        Map typeToTotals = new HashMap();
        long totalItems = 0;
        long totalAttn = 0;
        for (Iterator pit = pcInfos.iterator(); pit.hasNext();) {
            PCInfo pc = (PCInfo)pit.next();
            PCPanel panel = new PCPanel(pc);
            prefixToPCPanel.put(pc.getPrefix(), panel);
            pcColumn.add(panel);
            
            totalItems += pc.getNumItems();
            totalAttn += pc.getNumAttention();
            
            //
            // Update the total type counts
            Map typeCount = pc.getTypeToCountMap();
            Iterator entIt = typeCount.entrySet().iterator();
            while (entIt.hasNext()) {
                Entry e = (Entry)entIt.next();
                String type = (String)e.getKey();
                Long count = (Long)e.getValue();
                Long currVal = (Long)typeToTotals.get(type);
                if (currVal != null) {
                    currVal = new Long(currVal.longValue() + count.longValue());
                    typeToTotals.put(type, currVal);
                } else {
                    typeToTotals.put(type, count);
                }
            }
        }
        insertTotals(totalItems, totalAttn, typeToTotals);

        //
        // Start (or restart) collecting CPU loads
        if (statUpdateTimer != null) {
            statUpdateTimer.cancel();
        }
        statUpdateTimer = new Timer() {
            public void run() {
                fetchCPULoads();
            }
        };
        statUpdateTimer.scheduleRepeating(10 * 1000);
    }

    protected void fetchCPULoads() {
        //
        // Load info on partitions (includes replicant info):
        final AsyncCallback cpucallback = new AsyncCallback() {
            public void onSuccess(Object result) {
                updateCPULoads((Map<String,Double>)result);
            }

            public void onFailure(Throwable caught) {
                if (statUpdateTimer != null) {
                    statUpdateTimer.cancel();
                }
            }
        };
        service.getCPULoads(cpucallback);

    }

    protected void updateCPULoads(Map<String,Double> loads) {
        //
        // Update the DataStoreHeads
        for (Map.Entry<String,DSHPanel> dsh : ipToDSHPanel.entrySet()) {
            Double load = loads.get("dshead-" + dsh.getKey());
            dsh.getValue().setCPULoad(load);
        }

        //
        // Update the PartitionClusters and replicants
        for (Map.Entry<String,PCPanel> pc : prefixToPCPanel.entrySet()) {
            Double load = loads.get("part-" + pc.getKey());
            pc.getValue().setCPULoad(load);
            load = loads.get("replicant-" + pc.getKey());
            pc.getValue().setRepCPULoad(load);
        }
    }

    protected void fetchWebStats() {
        //
        // Load info on partitions (includes replicant info):
        final AsyncCallback cpucallback = new AsyncCallback() {
            public void onSuccess(Object result) {
                updateWebStats((Map<String,Double>)result);
            }

            public void onFailure(Throwable caught) {
                if (webStatUpdateTimer != null) {
                    webStatUpdateTimer.cancel();
                }
            }
        };
        service.getWebStats(cpucallback);
    }

    protected void updateWebStats(Map<String,Double> stats) {
        //
        // Figure out all the stat names
        Set<String> procNames = new HashSet<String>();
        for (String name : stats.keySet()) {
            //
            // name is procName:statName
            String procName = name.substring(0, name.indexOf(':'));
            procNames.add(procName);
        }

        //
        // Get or add a panel for each proc
        for (String procName : procNames) {
            WebPanel panel = procToWebPanel.get(procName);
            if (panel == null) {
                panel = new WebPanel(procName);
                webColumn.add(panel);
                procToWebPanel.put(procName, panel);
            }
            panel.setCPULoad(stats.get(procName + ":" + "PERCENT_CPU"));
            panel.setActiveSessions(stats.get(procName + ":" + "ACTIVE_SESSIONS").intValue());
        }
    }

    protected void insertTotals(long totalItems, long totalAttn, Map typeTotals) {
        FlowPanel container = new FlowPanel();
        container.setStylePrimaryName("viz-clearPanel");
        container.add(new StyleLabel("Total Items: " + totalItems, "viz-statLabel"));
        container.add(new StyleLabel("Total Attention: " + totalAttn, "viz-statLabel"));
        container.add(Util.getTypeStatsPanel(typeTotals));
        dshColumn.insert(container, 0);
    }
    
    protected static void alert(String msg) {
        Window.alert(msg);
    }
    
    protected static void addConfDialog(final Label clickable,
                                        final ClickListener listener,
                                        final String msg) {
        //
        // create the logic to show a dialog when the widget is clicked
        clickable.addClickListener(new ClickListener() {
            public void onClick(Widget arg0) {
                //
                // make the dialog
                final DialogBox dbox = new DialogBox(true, true);
                FlowPanel contents = new FlowPanel();
                dbox.setWidget(contents);
                contents.add(new Label(msg));
                Button b = new Button("Yes");
                b.addClickListener(listener);
                b.addClickListener(new ClickListener() {
                    public void onClick(Widget arg0) {
                        dbox.hide();
                    }
                });
                contents.add(b);
                dbox.setPopupPosition(clickable.getAbsoluteLeft(),
                                      clickable.getAbsoluteTop());
                dbox.show();
            }
        });
    }
}
