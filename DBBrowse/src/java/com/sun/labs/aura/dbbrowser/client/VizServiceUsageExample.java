/*
 * VizServiceUsageExample.java
 *
 * Created on July 18, 2008, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Example class using the VizService service. 
 *
 * @author ja151348
 */
public class VizServiceUsageExample extends VerticalPanel {
    private Label lblServerReply = new Label();
    private TextBox txtUserInput = new TextBox();
    private Button btnSend = new Button("Send to server");
    
    public VizServiceUsageExample() {
        add(new Label("Input your text: "));
        add(txtUserInput);
        add(btnSend);
        add(lblServerReply);
        
        // Create an asynchronous callback to handle the result.
        final AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {
                lblServerReply.setText((String)result);
            }
            
            public void onFailure(Throwable caught) {
                lblServerReply.setText("Communication failed");
            }
        };
        
        // Listen for the button clicks
        btnSend.addClickListener(new ClickListener(){
            public void onClick(Widget w) {
                // Make remote call. Control flow will continue immediately and later
                // 'callback' will be invoked when the RPC completes.
                //getService().myMethod(txtUserInput.getText(), callback);
            }
        });
    }
    
}
