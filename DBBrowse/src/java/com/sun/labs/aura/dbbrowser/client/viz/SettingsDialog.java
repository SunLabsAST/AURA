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

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configure UI settings for the Viz UI
 */
public class SettingsDialog extends DialogBox {

    protected List<String> logCodes;
    
    protected List<CheckBox> checks;
        
    public SettingsDialog(List<String> logCodes) {
        super(false);
        this.logCodes = logCodes;
        String[] selectedCodes = Util.getStatDisplayCodes();
        List<String> selected = Arrays.asList(selectedCodes);
        
        setText("Edit Settings");
        
        FlowPanel mainPanel = new FlowPanel();
        FlowPanel repStatPanel = new FlowPanel();
        repStatPanel.setStylePrimaryName("viz-settingsGroup");
        repStatPanel.add(new Label("Show these Replicant Stats:"));
        checks = new ArrayList<CheckBox>();
        int numColumns = logCodes.size() / 15;
        if (logCodes.size() % 15 > 0) {
            numColumns++;
        }
        Grid grid = new Grid(15, numColumns);
        int currCol = 0;
        int currRow = 0;
        HTMLTable.CellFormatter fmt = grid.getCellFormatter();
        for (String currName : logCodes) {
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
        repStatPanel.add(grid);
        mainPanel.add(repStatPanel);
        
        Button cancel = new Button("Cancel");
        cancel.addClickListener(new ClickListener() {
            public void onClick(Widget arg0) {
                hide();
            }
        });
        mainPanel.add(cancel);
        

        Button save = new Button("Save");
        save.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                doSave();
                hide();
            }
        });
        mainPanel.add(save);
        
        setWidget(mainPanel);
        setPopupPosition(30,30);
    }
    
    protected void doSave() {
        ArrayList<String> list = new ArrayList<String>();
        for (CheckBox check : checks) {
            if (check.isChecked()) {
                list.add(check.getName());
            }
        }
        Util.setStatDisplayCodes(list);
    }
    
}
