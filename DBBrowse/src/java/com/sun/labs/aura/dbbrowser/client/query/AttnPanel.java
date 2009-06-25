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

package com.sun.labs.aura.dbbrowser.client.query;

import com.sun.labs.aura.dbbrowser.client.*;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author ja151348
 */
public class AttnPanel extends DockPanel {
    private AttnDesc[] attns;
    private FlexTable results;
    private ScrollPanel center = new ScrollPanel();
    private TabbedQueryUI parent;
    private Label timeStamp;

    private DBServiceAsync service;
    
    private static final int TYPE_COL = 0;
    private static final int SRC_COL = 1;
    private static final int TRG_COL = 2;
    private static final int TIME_COL = 3;
    private static final int STRVAL_COL = 4;
    private static final int LONGVAL_COL = 5;

    public AttnPanel(AttnDesc[] attns, final TabbedQueryUI parent) {
        this.attns = attns;
        this.parent = parent;
        results = new FlexTable();
        setStylePrimaryName("db-ResultsPanel");
        setSpacing(5);
        //
        // Put in the headers
        results.setText(0, TYPE_COL, "Type");
        results.setText(0, SRC_COL, "Source Key");
        results.setText(0, TRG_COL, "Target Key");
        results.setText(0, TIME_COL, "Timestamp");
        results.setText(0, STRVAL_COL, "String Val");
        results.setText(0, LONGVAL_COL, "Long Val");
        RowFormatter rf = results.getRowFormatter();
        rf.setStylePrimaryName(0, "db-TableHeader");
        fillAttns();
        center.add(results);
        add(center, CENTER);
        Button close = new Button("Close");
        close.addClickListener(new ClickListener() {
            public void onClick(Widget arg0) {
                parent.removeAttnTab(arg0.getParent());
            }
        });
        timeStamp = new Label();
        add(timeStamp, SOUTH);
        AttnDesc info = attns[0];
        setQueryInfo(info.getQueryTime(), attns.length - 1, info.getNumTotal());
        add(close, SOUTH);
        
        service = GWTMainEntryPoint.getDBService();
    }
    
    protected void setQueryInfo(long time, int resultsShown, int totalResults) {
        timeStamp.setText("Query took: " + time + "ms" + "   Showing " 
                + resultsShown + " of " + totalResults);
    }
    
    protected void fillAttns() {
        int row = 1;
        boolean lightRow = true;
        RowFormatter rf = results.getRowFormatter();
        for (int i = 1; i < attns.length; i++) {
            results.setText(row, TYPE_COL, attns[i].getType());
            results.setText(row, SRC_COL, attns[i].getSrcKey());
            results.setHTML(row, TRG_COL, TabbedQueryUI.getLinkText(attns[i].getTargetKey()));
            results.setText(row, TIME_COL, attns[i].getTime());
            results.setText(row, STRVAL_COL, ((attns[i].getStrVal() != null) ? attns[i].getStrVal() : ""));
            results.setText(row, LONGVAL_COL, ((attns[i].getLongVal() != null) ? attns[i].getLongVal().toString() : ""));
            //
            // Stylize the row
            if (lightRow) {
                rf.setStylePrimaryName(row, "db-LightRow");
            } else {
                rf.setStylePrimaryName(row, "db-DarkRow");
            }
            lightRow = !lightRow;
            row++;
        }
    }
    
    public void replaceAttns(AttnDesc[] attns) {
        this.attns = attns;
        empty();
        fillAttns();
    }
    
    public void empty() {
        int cnt = results.getRowCount() - 1;
        for (int i = cnt; i >= 1; i--) {
            results.removeRow(i);
        }
    }
    
}
