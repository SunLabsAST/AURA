
package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
    private TextBox itemKey = new TextBox();
    private TextBox itemName = new TextBox();
    private Button itemKeyBtn = new Button("Search");
    private Button itemNameBtn = new Button("Search");
    
    private TabbedGUI parent;
    
    private DBServiceAsync service;
    
    public SearchPanel(final TabbedGUI parent) {
        this.parent = parent;
        service = GWTMainEntryPoint.getService();
        setHeight("400px");
        setWidth("100%");
        setStylePrimaryName("db-SearchPanel");
        setSpacing(5);
        keySrchPanel.setSpacing(3);
        keySrchPanel.add(new Label("Item by key: "));
        keySrchPanel.add(itemKey);
        keySrchPanel.add(itemKeyBtn);
                
        // Listen for the button clicks
        itemKeyBtn.addClickListener(new ClickListener(){
            public void onClick(Widget w) {
                // Make remote call. Control flow will continue immediately and later
                // 'callback' will be invoked when the RPC completes.

                // Create an asynchronous callback to handle the result.
                final String srchStr = itemKey.getText();
                final AsyncCallback callback = new AsyncCallback() {
                    public void onSuccess(Object result) {
                        parent.addResults(srchStr, (ItemDesc[]) result);
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
                
        // Listen for the button clicks
        itemNameBtn.addClickListener(new ClickListener(){
            public void onClick(Widget w) {
                // Make remote call. Control flow will continue immediately and later
                // 'callback' will be invoked when the RPC completes.

                // Create an asynchronous callback to handle the result.
                final String srchStr = itemName.getText();
                final AsyncCallback callback = new AsyncCallback() {
                    public void onSuccess(Object result) {
                        parent.addResults(srchStr, (ItemDesc[]) result);
                    }

                    public void onFailure(Throwable caught) {
                        parent.showError(caught.getMessage());
                    }
                };
                service.searchItemByName(srchStr, callback);
            }
        });
        add(nameSrchPanel);

    }
}
