
package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
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
 */
public class ResultsPanel extends DockPanel {
    private ItemDesc[] items;
    private FlexTable results;
    private ScrollPanel center = new ScrollPanel();
    private TabbedGUI parent;

    private DBServiceAsync service;
    
    private static final int TYPE_COL = 0;
    private static final int NAME_COL = 1;
    private static final int KEY_COL = 2;
    private static final int SRC_ATTN_COL = 3;
    private static final int TRG_ATTN_COL = 4;
    
    public ResultsPanel(ItemDesc[] items, final TabbedGUI parent) {
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
        
        service = GWTMainEntryPoint.getService();
    }
    
    protected void fillItems() {
        int row = 1;
        boolean lightRow = true;
        RowFormatter rf = results.getRowFormatter();
        for (int i = 1; i < items.length; i++) {
            results.setText(row, TYPE_COL, items[i].getType());
            results.setText(row, NAME_COL, items[i].getName());
            results.setHTML(row, KEY_COL, TabbedGUI.getLinkText(items[i].getKey()));
            AttnButton srcBtn = new AttnButton(items[i].getKey());
            srcBtn.addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    service.getAttentionForSource(
                            ((AttnButton)arg0).getKey(),
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
            AttnButton trgBtn = new AttnButton(items[i].getKey());
            trgBtn.addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    service.getAttentionForTarget(
                            ((AttnButton)arg0).getKey(),
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
   
    public class AttnButton extends Button {
        protected String key;
        public AttnButton(String key) {
            super("Attention");
            this.key = key;
        }
        
        public String getKey() {
            return key;
        }
    }
}
