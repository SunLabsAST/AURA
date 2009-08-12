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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.dbbrowser.client.GWTMainEntryPoint;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that shows a dialog for customizing the log settings
 */
public class RepLogDialog extends DialogBox {
    protected List<String> logNames;
    
    protected List<String> selected;
    
    protected String prefix;
    
    protected List<CheckBox> checks;

    protected ListBox levelMenu;
    
    protected String initialLogLevel;
    
    public RepLogDialog(String prefix, List<String> logNames, List<String> selected, String logLevel) {
        super(false);
        this.prefix = prefix;
        this.logNames = logNames;
        this.selected = selected;
        initialLogLevel = logLevel;
        setText("Change Log Settings");
        
        FlowPanel mainPanel = new FlowPanel();

        //
        // Put in a grid of check boxes for setting what methods are logged
        checks = new ArrayList<CheckBox>();
        int numColumns = logNames.size() / 15;
        if (logNames.size() % 15 > 0) {
            numColumns++;
        }
        Grid grid = new Grid(15, numColumns);
        int currCol = 0;
        int currRow = 0;
        HTMLTable.CellFormatter fmt = grid.getCellFormatter();
        for (String currName : logNames) {
            CheckBox cb = new CheckBox(currName);
            cb.setName(currName);
            String dispName = Util.logCodeToDisplay(currName);
            if (dispName != null) {
                cb.setText(dispName);
            }
            if (selected.contains(currName)) {
                cb.setChecked(true);
            }
            checks.add(cb);
            fmt.setStylePrimaryName(currRow, currCol, "viz-logGridCell");
            grid.setWidget(currRow++, currCol, cb);
            if (currRow >= 15) {
                currRow = 0;
                currCol++;
            }
        }
        mainPanel.add(grid);

        //
        // Add in the pop-up for setting the log level
        levelMenu = new ListBox();
        String[] levels = new String[] {"SEVERE",
                                        "WARNING",
                                        "INFO",
                                        "CONFIG",
                                        "FINE",
                                        "FINER",
                                        "FINEST"};
        for (int i = 0; i < levels.length; i++) {
            levelMenu.addItem(levels[i]);
            if (levels[i].equals(initialLogLevel)) {
                levelMenu.setSelectedIndex(i);
            }
        }
        levelMenu.setVisibleItemCount(1);

        HorizontalPanel selectLevelPanel = new HorizontalPanel();
        selectLevelPanel.add(new Label("Set log level: "));
        selectLevelPanel.add(levelMenu);
        selectLevelPanel.setStylePrimaryName("viz-logLevelSelect");
        mainPanel.add(selectLevelPanel);

        Button cancel = new Button("Cancel");
        cancel.addClickListener(new ClickListener() {
            public void onClick(Widget arg0) {
                hide();
            }
        });
        mainPanel.add(cancel);
        
        Button changeMe = new Button("Change " + prefix);
        changeMe.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                doChange(false);
            }
        });
        mainPanel.add(changeMe);

        Button changeAll = new Button("Change All");
        changeAll.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                doChange(true);
            }
        });
        mainPanel.add(changeAll);
        setWidget(mainPanel);
        setPopupPosition(30,30);
    }
    
    public void doChange(final boolean all) {
        selected = new ArrayList<String>();
        for (CheckBox cb : checks) {
            if (cb.isChecked()) {
                selected.add(cb.getName());
            }
        }
        
        VizServiceAsync service = GWTMainEntryPoint.getVizService();
        final AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {
                doChange2(all);
            }

            public void onFailure(Throwable caught) {
                VizUI.alert("Communication failed: " + caught.getMessage());
            }
        };
        
        service.setRepSelectedLogNames(all ? null : prefix, selected, callback);
    }

    public void doChange2(boolean all) {
        String logLevel = levelMenu.getItemText(levelMenu.getSelectedIndex());
        if (logLevel.equals(initialLogLevel)) {
            hide();
        } else {
            VizServiceAsync service = GWTMainEntryPoint.getVizService();
            final AsyncCallback callback = new AsyncCallback() {
                public void onSuccess(Object result) {
                    hide();
                }

                public void onFailure(Throwable caught) {
                    VizUI.alert("Communication failed: " + caught.getMessage());
                }
            };

            service.setLogLevel(all ? null : prefix, logLevel, callback);

        }
    }
}
