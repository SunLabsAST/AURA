/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

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
    private TabbedGUI parent;
    private Label timeStamp;

    private DBServiceAsync service;
    
    private static final int TYPE_COL = 0;
    private static final int SRC_COL = 1;
    private static final int TRG_COL = 2;
    private static final int TIME_COL = 3;

    public AttnPanel(AttnDesc[] attns, final TabbedGUI parent) {
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
        
        service = GWTMainEntryPoint.getService();
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
            results.setHTML(row, TRG_COL, TabbedGUI.getLinkText(attns[i].getTargetKey()));
            results.setText(row, TIME_COL, attns[i].getTime());
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
