/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The main interface to the visualization tool
 */
public class VizUI extends DockPanel {
    protected FlowPanel controls;
    protected HorizontalPanel leftRight;
    protected VerticalPanel dshColumn;
    protected VerticalPanel pcColumn;
    protected VerticalPanel detailsColumn;

    
    protected List dshInfos;
    protected List pcInfos;
    
    protected VizServiceAsync service;
    
    protected static VizUI theUI = null;
    
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
        controls.add(refreshBtn);
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
        
    }
    
    protected void fillDSH() {
        dshColumn.clear();
        for (Iterator dit = dshInfos.iterator(); dit.hasNext();) {
            DSHInfo dsh = (DSHInfo)dit.next();
            dshColumn.add(new DSHPanel(dsh));
        }
    }

    protected void fillPC() {
        pcColumn.clear();
        Map typeToTotals = new HashMap();
        for (Iterator pit = pcInfos.iterator(); pit.hasNext();) {
            PCInfo pc = (PCInfo)pit.next();
            pcColumn.add(new PCPanel(pc));
            
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
        insertTypeTotals(typeToTotals);
    }
    
    protected void insertTypeTotals(Map typeTotals) {
        FlowPanel container = new FlowPanel();
        container.setStylePrimaryName("viz-clearPanel");
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
