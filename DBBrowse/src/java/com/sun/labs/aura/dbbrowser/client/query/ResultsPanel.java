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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 */
public class ResultsPanel extends DockPanel {
    private ItemDesc[] items;
    private FlexTable results;
    private ScrollPanel center = new ScrollPanel();
    private TabbedQueryUI parent;

    private DBServiceAsync service;
    
    private DialogBox itemInfo;
    
    private static final int TYPE_COL = 0;
    private static final int NAME_COL = 1;
    private static final int KEY_COL = 2;
    private static final int SRC_ATTN_COL = 3;
    private static final int TRG_ATTN_COL = 4;
    private static final int DELETE_COL = 5;
    
    public ResultsPanel(ItemDesc[] items, final TabbedQueryUI parent) {
        this.items = items;
        this.parent = parent;
        results = new FlexTable();
        setStylePrimaryName("db-ResultsPanel");
        setSpacing(5);
        //
        // Put in the headers
        results.setText(0, NAME_COL, "Item Name");
        results.setText(0, KEY_COL, "Item Key");
        results.setText(0, TYPE_COL, "Type");
        results.setText(0, SRC_ATTN_COL, "Attn For Source");
        results.setText(0, TRG_ATTN_COL, "Attn for Target");
        results.setText(0, DELETE_COL, "Delete?");
        RowFormatter rf = results.getRowFormatter();
        rf.setStylePrimaryName(0, "db-TableHeader");
        fillItems();
        center.add(results);
        add(center, CENTER);
        ItemDesc time = items[0];
        add(new Label("Query took: " + time.getQueryTime() + "ms"), SOUTH);
        Button close = new Button("Close");
        close.addClickListener(new ClickListener() {
            public void onClick(Widget arg0) {
                parent.removeTab(arg0.getParent());
            }
        });
        add(close, SOUTH);
        
        itemInfo = new DialogBox(true);
        
        service = GWTMainEntryPoint.getDBService();
    }
    
    protected void fillItems() {
        int row = 1;
        boolean lightRow = true;
        RowFormatter rf = results.getRowFormatter();
        for (int i = 1; i < items.length; i++) {
            results.setText(row, TYPE_COL, items[i].getType());
            results.setText(row, NAME_COL, items[i].getName());
            
            //
            // Make a clickable link for the key to get item info
            final String key = items[i].getKey();
            final Label l = new Label(key);
            l.setStylePrimaryName(".db-actionLabel");
            l.addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    getItemInfo(l, key);
                }
            });
            results.setWidget(row, KEY_COL, l);
            
            KeyButton srcBtn = new KeyButton("Attention", items[i].getKey());
            srcBtn.addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    service.getAttentionForSource(
                            ((KeyButton)arg0).getKey(),
                            new AsyncCallback() {

                        public void onFailure(Throwable ex) {
                            parent.showError(ex.getMessage());
                        }

                        public void onSuccess(Object result) {
                            //
                            // insert code to show attn dialog or panel here
                            // and also made that dialog or panel
                            if (result != null) {
                                parent.showAttention((AttnDesc[])result);
                            } else {
                                parent.showError("Remote error!");
                            }
                        }
                        
                    });
                }
            });
            KeyButton trgBtn = new KeyButton("Attention", items[i].getKey());
            trgBtn.addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    service.getAttentionForTarget(
                            ((KeyButton)arg0).getKey(),
                            new AsyncCallback() {

                        public void onFailure(Throwable ex) {
                            parent.showError(ex.getMessage());
                        }

                        public void onSuccess(Object result) {
                            //
                            // insert code to show attn dialog or panel here
                            // and also made that dialog or panel
                            if (result != null) {
                                parent.showAttention((AttnDesc[])result);
                            } else {
                                parent.showError("Remote error!");
                            }
                        }
                        
                    });
                }
            });
            results.setWidget(row, SRC_ATTN_COL, srcBtn);
            results.setWidget(row, TRG_ATTN_COL, trgBtn);

            KeyButton delBtn = new KeyButton("Delete", items[i].getKey());
            delBtn.addClickListener(new ClickListener() {
                public void onClick(Widget sender) {
                    service.deleteItem(
                            ((KeyButton)sender).getKey(),
                            new AsyncCallback() {

                        public void onFailure(Throwable ex) {
                            parent.showError(ex.getMessage());
                        }

                        public void onSuccess(Object result) {
                            parent.showError("Done!");
                        }

                    });
                }
            });
            results.setWidget(row, DELETE_COL, delBtn);
            
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
   
    protected void getItemInfo(Label l, String key) {
        //
        // make the dialog
        itemInfo.setWidget(new Label("Loading..."));
        itemInfo.setPopupPosition(l.getAbsoluteLeft(),
                                  l.getAbsoluteTop());
        itemInfo.show();
        
        //
        // make the remote call
        AsyncCallback callback = new AsyncCallback() {
            public void onFailure(Throwable arg0) {
                itemInfo.setWidget(new Label("Failed to load: "
                        + arg0.getMessage()));
            }

            public void onSuccess(Object arg0) {
                displayItemInfo((HashMap)arg0);
            }
        };
        service.getItemInfo(key, callback);
    }
    
    protected void displayItemInfo(HashMap results) {
        if (results == null) {
            itemInfo.setWidget(new Label("No info"));
            return;
        }
        FlexTable contents = new FlexTable();
        RowFormatter rf = contents.getRowFormatter();
        Set names = results.keySet();
        Iterator nit = names.iterator();
        int row = 0;
        boolean lightRow = true;
        while (nit.hasNext()) {
            String name = (String)nit.next();
            contents.setText(row, 0, name);
            String val = (String)results.get(name);
            Widget valWidget = null;
            if (val.startsWith("http")) {
                valWidget = new Hyperlink(val, val);
                if (val.endsWith(".jpg") || val.endsWith(".gif") || val.endsWith(".png")) {
                    FlowPanel both = new FlowPanel();
                    both.add(valWidget);
                    both.add(new Image(val));
                    valWidget = both;
                }
            } else {
                valWidget = new Label(val);
            }
            if (lightRow) {
                rf.addStyleName(row, "db-lightRow");
            } else {
                rf.addStyleName(row, "db-darkRow");
            }
            lightRow = !lightRow;
            contents.setWidget(row++, 1, valWidget);
        }
        itemInfo.setWidget(contents);
    }
    
    public class KeyButton extends Button {
        protected String key;
        public KeyButton(String name, String key) {
            super(name);
            this.key = key;
        }
        
        public String getKey() {
            return key;
        }
    }

}
