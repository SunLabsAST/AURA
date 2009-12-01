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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
    
    protected Panel cpuLoad = null;

    protected Map<String,RepPanel> repPanelMap;

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
        cpuLoad = Util.getHisto("CPU", 0, 100, 50, "00.0%");
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
        //
        // Sort the replicants in order of their IdStrings
        List reps = pc.getRepInfos();
        Comparator<RepInfo> cmp = new Comparator<RepInfo>() {
            @Override
            public int compare(RepInfo o1, RepInfo o2) {
                return o1.getIdString().compareTo(o2.getIdString());
            }
        };
        repPanelMap = new HashMap<String,RepPanel>();
        Collections.sort(reps, cmp);
        for (Iterator it = reps.iterator(); it.hasNext();) {
            RepInfo rep = (RepInfo)it.next();
            RepPanel curr = new RepPanel(rep);
            repPanelMap.put(rep.getIdString(), curr);
            replicants.add(curr);
        }
    }
    
    public PCInfo getPCInfo() {
        return pc;
    }

    public void setCPULoad(double load) {
        String str = VizUI.cpuFormat.format(load);
        Panel newLoad = Util.getHisto("CPU", Double.valueOf(load).intValue(), 100, 50, str + "%");
        int index = myself.getWidgetIndex(cpuLoad);
        myself.remove(index);
        cpuLoad = newLoad;
        myself.insert(cpuLoad, index);
    }

    public void setRepCPULoads(Map<String,Double> loads) {
        for (String idStr : repPanelMap.keySet()) {
            RepPanel panel = repPanelMap.get(idStr);
            Double val = loads.get("replicant-" + idStr);
            panel.setCPULoad(val);
        }
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
