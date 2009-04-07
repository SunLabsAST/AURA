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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class SearchPanel extends VerticalPanel {
    private HorizontalPanel keySrchPanel = new HorizontalPanel();
    private HorizontalPanel nameSrchPanel = new HorizontalPanel();
    private HorizontalPanel genSrchPanel = new HorizontalPanel();
    private HorizontalPanel fsSrchPanel = new HorizontalPanel();
    private TextBox itemKey = new TextBox();
    private TextBox itemName = new TextBox();
    private TextBox itemGen = new TextBox();
    private TextBox itemFS = new TextBox();
    private Button itemKeyBtn = new Button("Search");
    private Button itemNameBtn = new Button("Search");
    private Button itemGenBtn = new Button("Search");
    private Button itemFSBtn = new Button("Search");
    private Button testBtn = new Button("TestMe");
    
    private TabbedQueryUI parent;
    
    private DBServiceAsync service;
    
    public SearchPanel(final TabbedQueryUI parent) {
        this.parent = parent;
        service = GWTMainEntryPoint.getDBService();
        setHeight("400px");
        setWidth("100%");
        setStylePrimaryName("db-SearchPanel");
        setSpacing(5);
        keySrchPanel.setSpacing(3);
        keySrchPanel.add(new Label("Item by key: "));
        keySrchPanel.add(itemKey);
        keySrchPanel.add(itemKeyBtn);
        
        itemKey.addKeyboardListener(new KeyboardListener() {
            public void onKeyDown(Widget arg0, char arg1, int arg2) {
            }

            public void onKeyPress(Widget w, char c, int mod) {
                if (c == KEY_ENTER)  {
                    itemKeyBtn.click();
                }
            }

            public void onKeyUp(Widget arg0, char arg1, int arg2) {
            }
           
        });

        // Listen for the button clicks
        itemKeyBtn.addClickListener(new ClickListener(){
            public void onClick(Widget w) {
                // Make remote call. Control flow will continue immediately and later
                // 'callback' will be invoked when the RPC completes.

                // Create an asynchronous callback to handle the result.
                final String srchStr = itemKey.getText();
                final AsyncCallback callback = new AsyncCallback() {
                    public void onSuccess(Object result) {
                        if (result != null) {
                            parent.addResults("Key:" + srchStr, (ItemDesc[]) result);
                        } else {
                            parent.showError("Remote error!");
                        }
                    }

                    public void onFailure(Throwable caught) {
                        parent.showError(caught.getMessage());
                    }
                };
                service.searchItemByKey(srchStr, callback);
            }
        });
        add(keySrchPanel);

        nameSrchPanel.setSpacing(3);
        nameSrchPanel.add(new Label("Item by name: "));
        nameSrchPanel.add(itemName);
        nameSrchPanel.add(itemNameBtn);
        
        itemName.addKeyboardListener(new KeyboardListener() {
            public void onKeyDown(Widget arg0, char arg1, int arg2) {
            }

            public void onKeyPress(Widget w, char c, int mod) {
                if (c == KEY_ENTER)  {
                    itemNameBtn.click();
                }
            }

            public void onKeyUp(Widget arg0, char arg1, int arg2) {
            }
           
        });

        // Listen for the button clicks
        itemNameBtn.addClickListener(new ClickListener(){
            public void onClick(Widget w) {
                // Make remote call. Control flow will continue immediately and later
                // 'callback' will be invoked when the RPC completes.

                // Create an asynchronous callback to handle the result.
                final String srchStr = itemName.getText();
                final AsyncCallback callback = new AsyncCallback() {
                    public void onSuccess(Object result) {
                        if (result != null) {
                            parent.addResults("Name:" + srchStr, (ItemDesc[]) result);
                        } else {
                            parent.showError("Remote error!");
                        }
                    }

                    public void onFailure(Throwable caught) {
                        parent.showError(caught.getMessage());
                    }
                };
                service.searchItemByName(srchStr, callback);
            }
        });
        add(nameSrchPanel);

        genSrchPanel.setSpacing(3);
        genSrchPanel.add(new Label("Freeform query: "));
        genSrchPanel.add(itemGen);
        genSrchPanel.add(itemGenBtn);

        itemGen.addKeyboardListener(new KeyboardListener() {

            public void onKeyDown(Widget arg0, char arg1, int arg2) {
            }

            public void onKeyPress(Widget w, char c, int mod) {
                if(c == KEY_ENTER) {
                    itemGenBtn.click();
                }
            }

            public void onKeyUp(Widget arg0, char arg1, int arg2) {
            }
        });

        // Listen for the button clicks
        itemGenBtn.addClickListener(new ClickListener() {

            public void onClick(Widget w) {
                // Make remote call. Control flow will continue immediately and later
                // 'callback' will be invoked when the RPC completes.

                // Create an asynchronous callback to handle the result.
                final String srchStr = itemGen.getText();
                final AsyncCallback callback = new AsyncCallback() {

                    public void onSuccess(Object result) {
                        if(result != null) {
                            parent.addResults(srchStr, (ItemDesc[]) result);
                        } else {
                            parent.showError("Remote error!");
                        }
                    }

                    public void onFailure(Throwable caught) {
                        parent.showError(caught.getMessage());
                    }
                };
                service.searchItemByGen(srchStr, callback);
            }
        });
        add(genSrchPanel);
    
        fsSrchPanel.setSpacing(3);
        fsSrchPanel.add(new Label("Find similar: "));
        fsSrchPanel.add(itemFS);
        fsSrchPanel.add(itemFSBtn);

        itemFS.addKeyboardListener(new KeyboardListener() {

            public void onKeyDown(Widget arg0, char arg1, int arg2) {
            }

            public void onKeyPress(Widget w, char c, int mod) {
                if(c == KEY_ENTER) {
                    itemFSBtn.click();
                }
            }

            public void onKeyUp(Widget arg0, char arg1, int arg2) {
            }
        });

        // Listen for the button clicks
        itemFSBtn.addClickListener(new ClickListener() {

            public void onClick(Widget w) {
                // Make remote call. Control flow will continue immediately and later
                // 'callback' will be invoked when the RPC completes.

                // Create an asynchronous callback to handle the result.
                final String key = itemFS.getText();
                final AsyncCallback callback = new AsyncCallback() {

                    public void onSuccess(Object result) {
                        if(result != null) {
                            parent.addResults(key, (ItemDesc[]) result);
                        } else {
                            parent.showError("Remote error!");
                        }
                    }

                    public void onFailure(Throwable caught) {
                        parent.showError(caught.getMessage());
                    }
                };
                service.findSimilar(key, callback);
            }
        });
        add(fsSrchPanel);
    
        testBtn.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                final AsyncCallback callback = new AsyncCallback() {

                    public void onSuccess(Object result) {
                        if(result != null) {
                            parent.showAttention((AttnDesc[]) result);
                        } else {
                            parent.showError("Remote error!");
                        }
                    }

                    public void onFailure(Throwable caught) {
                        parent.showError(caught.getMessage());
                    }
                };
                service.doTest(callback);
            }
        });
        add(testBtn);
    }
}
