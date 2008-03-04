/*
 * GWTMainEntryPoint.java
 *
 * Created on February 27, 2008, 1:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author ja151348
 */
public class GWTMainEntryPoint implements EntryPoint {
    
    /** Creates a new instance of GWTMainEntryPoint */
    public GWTMainEntryPoint() {
    }
    
    /**
     * The entry point method, called automatically by loading a module
     * that declares an implementing class as an entry-point
     */
    public void onModuleLoad() {
        final Label label = new Label("Hello, GWT!!!");
        final Button button = new Button("Click me!");
        
        button.addClickListener(new ClickListener(){
            public void onClick(Widget w) {
                label.setVisible(!label.isVisible());
            }
        });
        RootPanel.get().add(new TabbedGUI());
        //RootPanel.get().add(button);
        //RootPanel.get().add(label);
    }
    
    public static DBServiceAsync getService(){
        // Create the client proxy. Note that although you are creating the
        // service interface proper, you cast the result to the asynchronous
        // version of
        // the interface. The cast is always safe because the generated proxy
        // implements the asynchronous interface automatically.
        DBServiceAsync service = (DBServiceAsync) GWT.create(DBService.class);
        // Specify the URL at which our service implementation is running.
        // Note that the target URL must reside on the same domain and port from
        // which the host page was served.
        //
        ServiceDefTarget endpoint = (ServiceDefTarget) service;
        String moduleRelativeURL = GWT.getModuleBaseURL() + "dbservice";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
        return service;
    }

}
